package com.atta.PolyPaste.services.rabbit.moderation;

// --- ДОБАВЛЕННЫЕ ИМПОРТЫ ДЛЯ РАБОТЫ С ФАЙЛАМИ И РЕСУРСАМИ ---
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.springframework.core.io.ClassPathResource;

// --- ТВОИ ОСТАЛЬНЫЕ ИМПОРТЫ ---
import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
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
            // 1. Создаем временную директорию в контейнере
            Path tempDir = Files.createTempDirectory("onnx_model_");
            
            // 2. Пути к ресурсам внутри JAR (проверь, что они такие)
            String modelResourcePath = "models/rubert-tiny-toxic.onnx";
            String tokenizerResourcePath = "models/tokenizer.json";

            // 3. Извлекаем файлы
            Path tempModelPath = tempDir.resolve("model.onnx");
            Path tempTokenizerPath = tempDir.resolve("tokenizer.json");

            extractResource(modelResourcePath, tempModelPath);
            extractResource(tokenizerResourcePath, tempTokenizerPath);

            // 4. Инициализируем ONNX среду и сессию
            this.env = OrtEnvironment.getEnvironment();
            this.session = env.createSession(tempModelPath.toString(), new OrtSession.SessionOptions());
            log.info("ONNX model loaded successfully from temp path: {}", tempModelPath);

            // 5. Инициализируем Токенизатор
            this.tokenizer = HuggingFaceTokenizer.newInstance(tempTokenizerPath);
            log.info("Tokenizer loaded successfully from temp path: {}", tempTokenizerPath);

            // Помечаем файлы на удаление при выходе из приложения
            tempModelPath.toFile().deleteOnExit();
            tempTokenizerPath.toFile().deleteOnExit();
            tempDir.toFile().deleteOnExit();

        } catch (Exception e) {
            log.error("Initialization of ModerationService failed", e);
            throw new RuntimeException("Failed to start ModerationService", e);
        }
    }

    // Вспомогательный метод для копирования из JAR в файловую систему
    private void extractResource(String resourcePath, Path targetPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream is = resource.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public String moderate(String text) {
        log.info("paste start moderate");

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