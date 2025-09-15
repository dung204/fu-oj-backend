package com.example.modules.minio.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.base.BaseServiceTest;
import com.example.modules.minio.dtos.MinioFileResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

public class MinioServiceTest extends BaseServiceTest {

  @Mock
  private MinioClient minioClient;

  @InjectMocks
  private MinioService minioService;

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(minioService, "bucket", "test-bucket");
  }

  @Test
  void uploadFile_shouldUploadAndReturnResponse() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    String presignedUrl = "http://minio/presigned-url";

    when(file.getOriginalFilename()).thenReturn("test-file.txt");
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));
    when(file.getSize()).thenReturn(7L);
    when(file.getContentType()).thenReturn("text/plain");

    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(
      presignedUrl
    );

    MinioFileResponse response = minioService.uploadFile(file, "folder");

    assertNotNull(response);
    assertTrue(response.getFileName().contains("folder/"));
    assertTrue(response.getFileName().contains("test-file.txt"));
    assertEquals(presignedUrl, response.getUrl());
  }

  @Test
  void uploadFile_shouldHandleNullFolder() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    String presignedUrl = "url";

    when(file.getOriginalFilename()).thenReturn("test-file.txt");
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));
    when(file.getSize()).thenReturn(7L);
    when(file.getContentType()).thenReturn("text/plain");

    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(
      presignedUrl
    );

    MinioFileResponse response = minioService.uploadFile(file, null);

    assertNotNull(response);
    assertFalse(response.getFileName().startsWith("/"));
    assertTrue(response.getFileName().contains("test-file.txt"));
    assertEquals(presignedUrl, response.getUrl());
  }

  @Test
  void uploadFile_shouldSanitizeFileName() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    String presignedUrl = "url";

    when(file.getOriginalFilename()).thenReturn("têst fïlè.txt");
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));
    when(file.getSize()).thenReturn(7L);
    when(file.getContentType()).thenReturn("text/plain");

    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(
      presignedUrl
    );

    MinioFileResponse response = minioService.uploadFile(file, "");

    assertNotNull(response);
    assertTrue(response.getFileName().matches(".*test_file.txt$"));
  }

  @Test
  void uploadFile_shouldThrowExceptionOnPutObjectError() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    MinioException err = mock(ErrorResponseException.class);

    when(file.getOriginalFilename()).thenReturn("file.txt");
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));
    when(file.getSize()).thenReturn(7L);
    when(file.getContentType()).thenReturn("text/plain");

    when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(err);

    assertThrows(MinioException.class, () -> minioService.uploadFile(file, "folder"));
  }

  @Test
  void generatePresignedUrl_shouldReturnUrlForValidFileName() throws Exception {
    String fileName = "file.txt";
    String presignedUrl = "http://minio/presigned-url";
    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(
      presignedUrl
    );

    String result = minioService.generatePresignedUrl(fileName);

    assertEquals(presignedUrl, result);
    verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
  }

  @Test
  void generatePresignedUrl_shouldReturnNullForNullFileName() throws Exception {
    String result = minioService.generatePresignedUrl(null);

    assertEquals(null, result);
    verify(minioClient, times(0)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
  }

  @Test
  void generatePresignedUrl_shouldReturnNullForEmptyFileName() throws Exception {
    String result = minioService.generatePresignedUrl("");

    assertEquals(null, result);
    verify(minioClient, times(0)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
  }

  @Test
  void generatePresignedUrl_shouldThrowExceptionOnMinioError() throws Exception {
    String fileName = "file.txt";

    MinioException err = mock(ErrorResponseException.class);
    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenThrow(err);

    assertThrows(MinioException.class, () -> minioService.generatePresignedUrl(fileName));
  }

  @Test
  void deleteFile_shouldRemoveObjectForValidFileName() throws Exception {
    String fileName = "file.txt";
    doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

    minioService.deleteFile(fileName);

    verify(minioClient).removeObject(any(RemoveObjectArgs.class));
  }

  @Test
  void deleteFile_shouldNotRemoveObjectForNullFileName() throws Exception {
    minioService.deleteFile(null);

    verify(minioClient, times(0)).removeObject(any(RemoveObjectArgs.class));
  }

  @Test
  void deleteFile_shouldNotRemoveObjectForEmptyFileName() throws Exception {
    minioService.deleteFile("");

    verify(minioClient, times(0)).removeObject(any(RemoveObjectArgs.class));
  }

  @Test
  void deleteFile_shouldThrowExceptionOnRemoveObjectError() throws Exception {
    String fileName = "file.txt";
    MinioException err = mock(ErrorResponseException.class);
    doThrow(err).when(minioClient).removeObject(any(RemoveObjectArgs.class));

    assertThrows(MinioException.class, () -> minioService.deleteFile(fileName));
  }
}
