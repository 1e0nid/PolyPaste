package com.atta.PolyPaste.services;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private static final Logger log = LoggerFactory.getLogger(MinioService.class);

    @Value("${app.minio.bucket}")
    private String bucket;

    public void saveContent(String content, String minioKey) throws MinioException {
        log.info("call saveContent");
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(contentBytes);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(minioKey)
                            .stream(bais, (long) contentBytes.length, (long) -1) // -1 для автоматического определения размера чанка
                            .contentType("text/plain")
                            .build()
            );
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    public String getPasteContent(String minioKey) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(minioKey)
                        .build())) {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось прочитать содержимое");
        }
    }

    public void deleteContent(String shortId) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(shortId) // Файл обычно называется так же, как shortId
                            .build()
            );
        } catch (Exception e) {
            log.error("Ошибка при удалении файла из MinIO: {}", e.getMessage());
            // Можно не кидать исключение, если БД важнее, но лучше знать об этом
        }
    }
}
