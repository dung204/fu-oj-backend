package com.example.modules.minio.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.base.BaseServiceIntegrationTest;
import com.example.modules.minio.dtos.MinioFileResponse;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class MinioServiceIntegrationTest extends BaseServiceIntegrationTest {

  @Autowired
  private MinioService minioService;

  @BeforeEach
  @Override
  protected void setup() {} // No initial setup

  @Test
  void uploadFile_ShouldUploadAndReturnResponse() throws Exception {
    MultipartFile file = new MockMultipartFile(
      "file",
      "test-file.txt",
      "text/plain",
      "content".getBytes()
    );

    MinioFileResponse response = minioService.uploadFile(file, "folder");

    assertNotNull(response);
    assertTrue(response.getFileName().contains("folder/"));
    assertTrue(response.getFileName().contains("test-file.txt"));
    assertDoesNotThrow(() -> URI.create(response.getUrl()));
  }

  @Test
  void uploadFile_WhenFolderIsNull_ShouldUploadAndReturnResponse() throws Exception {
    MultipartFile file = new MockMultipartFile(
      "file",
      "test-file.txt",
      "text/plain",
      "content".getBytes()
    );

    MinioFileResponse response = minioService.uploadFile(file, null);

    assertNotNull(response);
    assertFalse(response.getFileName().contains("/"));
    assertTrue(response.getFileName().contains("test-file.txt"));
    assertDoesNotThrow(() -> URI.create(response.getUrl()));
  }

  @Test
  void uploadFile_WhenFolderIsEmpty_ShouldUploadAndReturnResponse() throws Exception {
    MultipartFile file = new MockMultipartFile(
      "file",
      "test-file.txt",
      "text/plain",
      "content".getBytes()
    );

    MinioFileResponse response = minioService.uploadFile(file, "");

    assertNotNull(response);
    assertFalse(response.getFileName().contains("/"));
    assertTrue(response.getFileName().contains("test-file.txt"));
    assertDoesNotThrow(() -> URI.create(response.getUrl()));
  }

  @Test
  void generatePresignedUrl_WhenFileNameIsValid_ShouldReturnURL() throws Exception {
    MultipartFile file = new MockMultipartFile(
      "file",
      "test-file.txt",
      "text/plain",
      "content".getBytes()
    );

    MinioFileResponse response = minioService.uploadFile(file, "");

    String presignedUrl = minioService.generatePresignedUrl(response.getFileName());

    assertNotNull(presignedUrl);
    assertDoesNotThrow(() -> URI.create(presignedUrl));
  }

  @Test
  void generatePresignedUrl_WhenFileNameIsNullOrEmpty_ShouldReturnNull() throws Exception {
    assertNull(minioService.generatePresignedUrl(null));
    assertNull(minioService.generatePresignedUrl(""));
  }

  @Test
  void deleteFile_ShouldRemoveFileFromBucket() throws Exception {
    MultipartFile file = new MockMultipartFile(
      "file",
      "test-file.txt",
      "text/plain",
      "content".getBytes()
    );
    MinioFileResponse response = minioService.uploadFile(file, "test-delete");
    String filename = response.getFileName();

    assertTrue(minioService.checkFileExists(filename));

    minioService.deleteFile(filename);

    assertFalse(minioService.checkFileExists(filename));
  }

  @Test
  void sanitizeFileName_ShouldReplaceSpacesAndDiacritics() {
    String input = "têst file.png";
    String expected = "test_file.png";
    String actual = minioService.sanitizeFileName(input);
    assertEquals(expected, actual);
  }

  @Test
  void sanitizeFileName_ShouldRemoveSpecialCharacters() {
    String input = "my@file#name!.txt";
    String expected = "myfilename.txt";
    String actual = minioService.sanitizeFileName(input);
    assertEquals(expected, actual);
  }

  @Test
  void sanitizeFileName_ShouldReturnUntitledForNullOrEmpty() {
    assertEquals("untitled", minioService.sanitizeFileName(null));
    assertEquals("untitled", minioService.sanitizeFileName(""));
    assertEquals("untitled", minioService.sanitizeFileName("   "));
  }

  @Test
  void sanitizeFileName_ShouldKeepValidCharacters() {
    String input = "valid-File_Name123.txt";
    String expected = "valid-File_Name123.txt";
    String actual = minioService.sanitizeFileName(input);
    assertEquals(expected, actual);
  }
}
