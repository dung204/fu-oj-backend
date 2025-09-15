package com.example.modules.minio.configs;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinioConfig {

  @Value("${minio.endpoint:http://localhost:9000}")
  private String endpoint;

  @Value("${minio.access_key:minioadmin}")
  private String accessKey;

  @Value("${minio.secret_key:minioadmin}")
  private String secretKey;

  @Value("${minio.bucket:spring-rest-api-bucket}")
  private String bucket;

  @Bean
  MinioClient minioClient()
    throws MinioException, InvalidKeyException, IOException, NoSuchAlgorithmException {
    log.info("Initializing MinIO client with endpoint: {}", endpoint);
    MinioClient client = MinioClient.builder()
      .endpoint(endpoint)
      .credentials(accessKey, secretKey)
      .build();

    if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
      log.info("Bucket '{}' does not exist, creating it", bucket);
      client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
      log.info("Successfully created bucket '{}'", bucket);
    } else {
      log.info("Bucket '{}' already exists", bucket);
    }

    log.info("MinIO client initialized successfully");
    return client;
  }
}
