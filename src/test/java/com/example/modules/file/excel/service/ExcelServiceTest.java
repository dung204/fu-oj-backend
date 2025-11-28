package com.example.modules.file.excel.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.base.BaseServiceTest;
import com.example.modules.auth.entities.Account;
import com.example.modules.auth.enums.Role;
import com.example.modules.auth.repositories.AccountsRepository;
import com.example.modules.email.service.EmailService;
import com.example.modules.users.repositories.UsersRepository;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

class ExcelServiceTest extends BaseServiceTest {

  @Mock
  private AccountsRepository accountsRepository;

  @Mock
  private EmailService emailService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UsersRepository usersRepository;

  @InjectMocks
  private ExcelService excelService;

  private ExcelService excelServiceSpy;

  @BeforeEach
  void setUp() {
    excelServiceSpy = Mockito.spy(
      new ExcelService(accountsRepository, emailService, passwordEncoder, usersRepository)
    );
  }

  @Test
  void exportAccountsToExcel_ShouldFilterOutAdmins() throws IOException {
    Account admin = Account.builder().email("admin@test.com").role(Role.ADMIN).build();
    Account instructor = Account.builder().email("teacher@test.com").role(Role.INSTRUCTOR).build();
    when(accountsRepository.findAll()).thenReturn(List.of(admin, instructor));
    byte[] expectedBytes = "excel".getBytes();
    doReturn(expectedBytes)
      .when(excelServiceSpy)
      .exportToExcel(anyList(), eq("account"), any(String[].class), any(String[].class));

    byte[] actual = excelServiceSpy.exportAccountsToExcel();

    assertArrayEquals(expectedBytes, actual);
    verify(excelServiceSpy).exportToExcel(
      anyList(),
      eq("account"),
      any(String[].class),
      any(String[].class)
    );
  }

  @Test
  void validateFile_WhenFileEmpty_ShouldReturnFalse() {
    MultipartFile file = Mockito.mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(true);

    assertFalse(excelService.validateFile(file));
  }

  @Test
  void validateFile_WhenWrongExtensionOrTooLarge_ShouldReturnFalse() {
    MultipartFile file = Mockito.mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getOriginalFilename()).thenReturn("accounts.xls");

    assertFalse(excelService.validateFile(file));

    MultipartFile largeFile = Mockito.mock(MultipartFile.class);
    when(largeFile.isEmpty()).thenReturn(false);
    when(largeFile.getOriginalFilename()).thenReturn("accounts.xlsx");
    when(largeFile.getSize()).thenReturn(25L * 1024 * 1024);

    assertFalse(excelService.validateFile(largeFile));
  }

  @Test
  void validateFile_WhenValid_ShouldReturnTrue() {
    MultipartFile file = Mockito.mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getOriginalFilename()).thenReturn("accounts.xlsx");
    when(file.getSize()).thenReturn(5L * 1024 * 1024);

    assertTrue(excelService.validateFile(file));
  }
}
