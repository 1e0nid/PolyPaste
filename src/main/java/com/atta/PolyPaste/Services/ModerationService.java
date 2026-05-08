package com.atta.PolyPaste.Services;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@Service
public class ModerationService {

    @Value("${app.ml.model-path}")
    private String modelPath;

    @Value("${app.ml.threshold}")
    private double threshold;

    private OrtEnvironment env;
    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;

    @PostConstruct
    public void init() {
        try {
            env = OrtEnvironment.getEnvironment();
            session = env.createSession(modelPath, new OrtSession.SessionOptions());
            log.info("ONNX model loaded successfully from {}", modelPath);

            Path modelDirPath = Paths.get(modelPath).getParent();
            Path tokenizerPath = modelDirPath.resolve("tokenizer.json");

            if (!tokenizerPath.toFile().exists()) {
                throw new RuntimeException("Tokenizer file not found at: " + tokenizerPath.toAbsolutePath());
            }

            tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);
            log.info("Tokenizer loaded locally from {}", tokenizerPath);

        } catch (Exception e) {
            log.error("Initialization failed", e);
            throw new RuntimeException("Failed to start ModerationService", e);
        }
    }

    public String moderate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "accepted";
        }
        try {
            Encoding encoding = tokenizer.encode(text);

            long[][] inputIds = { encoding.getIds() };
            long[][] attentionMask = { encoding.getAttentionMask() };
            long[][] tokenTypeIds = { encoding.getTypeIds() };

            try (OnnxTensor inputIdsTensor = OnnxTensor.createTensor(env, inputIds);
                 OnnxTensor maskTensor = OnnxTensor.createTensor(env, attentionMask);
                 OnnxTensor typeIdsTensor = OnnxTensor.createTensor(env, tokenTypeIds)) {

                Map<String, OnnxTensor> inputs = Map.of(
                        "input_ids", inputIdsTensor,
                        "attention_mask", maskTensor,
                        "token_type_ids", typeIdsTensor
                );

                try (OrtSession.Result results = session.run(inputs)) {
                    float[][] output = (float[][]) results.get(0).getValue();

                    float maxToxicProb = 0;
                    for (int i = 1; i < output[0].length; i++) {
                        float prob = sigmoid(output[0][i]);
                        if (prob > maxToxicProb) maxToxicProb = prob;
                    }

                    log.info("Text: '{}' | Max Toxicity Prob: {}", text, maxToxicProb);

                    return maxToxicProb > threshold ? "rejected" : "accepted";
                }
            }
        } catch (Exception e) {
            log.error("Moderation error", e);
            return "accepted";
        }
    }

    private float sigmoid(float x) {
        return (float) (1 / (1 + Math.exp(-x)));
    }

    @PreDestroy
    public void close() {
        try {
            if (session != null) session.close();
            if (env != null) env.close();
            if (tokenizer != null) tokenizer.close();
        } catch (Exception e) {
            log.error("Error closing resources", e);
        }
    }
}