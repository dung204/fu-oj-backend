package com.example.modules.minio.services;

import com.example.modules.minio.dtos.MinioFileResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioService {

  @Value("${minio.bucket}")
  private String bucket;

  @Value("${minio.public_endpoint}")
  private String publicEndpoint;

  private final MinioClient minioClient;

  /**
   * Uploads a file to MinIO storage with a unique filename.
   *
   * @param file the multipart file to upload
   * @param folder the folder path where the file should be stored (can be null or empty for root)
   * @return a payload containing the saved filename and presigned URL for accessing the file
   * @throws MinioException if there's an error communicating with MinIO server
   * @throws InvalidKeyException if the MinIO credentials are invalid
   * @throws IOException if there's an I/O error reading the file
   * @throws NoSuchAlgorithmException if required cryptographic algorithm is not available
   */
  public MinioFileResponse uploadFile(MultipartFile file, String folder)
    throws MinioException, InvalidKeyException, IOException, NoSuchAlgorithmException {
    String timestamp = Instant.now().toString();
    String uniqueId = UUID.randomUUID().toString();
    String originalName = this.sanitizeFileName(file.getOriginalFilename());

    String savedFileName = folder == null || folder.isEmpty()
      ? "%s-%s-%s".formatted(timestamp, uniqueId, originalName)
      : "%s/%s-%s-%s".formatted(folder, timestamp, uniqueId, originalName);

    minioClient.putObject(
      PutObjectArgs.builder()
        .bucket(bucket)
        .stream(file.getInputStream(), file.getSize(), -1) // Part size is auto detected
        .object(savedFileName)
        .contentType(file.getContentType())
        .build()
    );

    return MinioFileResponse.builder()
      .fileName(savedFileName)
      .url(this.generatePresignedUrl(savedFileName))
      .build();
  }

  /**
   * Generates a presigned URL for accessing a file in the MinIO bucket.
   *
   * A presigned URL allows temporary access to a private object without requiring
   * authentication credentials. The URL has a default expiration time set by MinIO.
   *
   * @param fileName the name of the file/object in the MinIO bucket for which to generate the presigned URL
   * @return a presigned URL string that can be used to access the file, or null if fileName is null or empty
   * @throws MinioException if an error occurs while communicating with the MinIO server
   * @throws InvalidKeyException if the provided credentials or bucket configuration is invalid
   * @throws IOException if an I/O error occurs during the operation
   * @throws NoSuchAlgorithmException if the required cryptographic algorithm is not available
   */
  public String generatePresignedUrl(String fileName)
    throws MinioException, InvalidKeyException, IOException, NoSuchAlgorithmException {
    if (fileName == null || fileName.isEmpty()) return null;

    return this.minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
          .bucket(bucket)
          .object(fileName)
          .method(Method.GET)
          // TODO: add custom expiry duration, if needed
          .build()
      )
      .replaceAll("http.*%s".formatted(bucket), publicEndpoint)
      .replaceAll("\\?.+", "");
  }

  /**
   * Deletes a file from the MinIO storage bucket.
   *
   * @param fileName the name of the file to delete from the bucket. If null or empty, the operation is skipped.
   * @throws MinioException if an error occurs during MinIO operations
   * @throws InvalidKeyException if the provided key is invalid
   * @throws IOException if an I/O error occurs during the operation
   * @throws NoSuchAlgorithmException if the required cryptographic algorithm is not available
   */
  public void deleteFile(String fileName)
    throws MinioException, InvalidKeyException, IOException, NoSuchAlgorithmException {
    if (fileName == null || fileName.isEmpty()) return;

    this.minioClient.removeObject(
      RemoveObjectArgs.builder().bucket(bucket).object(fileName).build()
    );
  }

  /**
   * Checks if a file exists in the configured MinIO bucket.
   *
   * @param fileName the name of the file to check for existence
   * @return {@code true} if the file exists, {@code false} if it does not exist or an error response is received
   * @throws RuntimeException if an unexpected exception occurs during the check
   */
  public boolean checkFileExists(String fileName) {
    try {
      this.minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(fileName).build());
      return true;
    } catch (ErrorResponseException e) {
      log.error(e.getMessage(), e);
      return false;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sanitizes a file name by removing diacritical marks, replacing whitespace with underscores,
   * and removing any characters that are not letters, digits, underscores, hyphens, or periods.
   * If the input is null, empty, or results in an empty string after sanitization,
   * returns {@code "untitled"}.
   *
   * @param fileName the original file name to sanitize
   * @return a sanitized file name safe for use in file systems or URLs
   */
  public String sanitizeFileName(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      return "untitled";
    }

    String sanitized = Normalizer.normalize(fileName, Normalizer.Form.NFD)
      .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
      .replaceAll("\\s+", "_")
      .replaceAll("[^a-zA-Z0-9_\\-.]+", "");

    if (sanitized.isEmpty()) {
      return "untitled";
    }

    return sanitized;
  }
}
