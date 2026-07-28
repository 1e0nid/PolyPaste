package com.atta.PolyPaste.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    @Value("${app.minio.endpoint}")
    private String endpoint;

    @Value("${app.minio.access-key}")
    private String accessKey;

    @Value("${app.minio.secret-key}")
    private String secretKey;

    @Value("${app.minio.bucket}")
    private String bucket;

    @Bean
    public MinioClient minioClient() throws Exception {

        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        boolean exists = client.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucket)
                        .build()
        );

        if (!exists) {
            client.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucket)
                            .build()
            );

            System.out.println("Bucket created: " + bucket);
        } else {
            System.out.println("Bucket already exists: " + bucket);
        }

        return client;
    }
}