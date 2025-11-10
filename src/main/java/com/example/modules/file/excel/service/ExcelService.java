package com.example.modules.file.excel.service;

import com.example.modules.auth.dtos.RegisterRequestDTO;
import com.example.modules.auth.entities.Account;
import com.example.modules.auth.exceptions.EmailHasAlreadyBeenUsedException;
import com.example.modules.auth.repositories.AccountsRepository;
import com.example.modules.auth.services.AuthService;
import com.example.modules.email.service.EmailService;
import com.example.modules.file.excel.exceptions.FileNotValidException;
import com.example.modules.file.excel.utils.ExcelExporter;
import com.example.modules.file.excel.utils.ExcelHelper;
import com.example.modules.users.entities.User;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService implements IExcelService {

  private final AuthService authService;
  private final AccountsRepository accountsRepository;
  private final EmailService emailService;

  @Override
  public List<RegisterRequestDTO> importAccountFromExcel(MultipartFile file, User user) {
    List<RegisterRequestDTO> accountRegisterSuccessfully = new ArrayList<>();
    try {
      if (!validateFile(file)) {
        throw new FileNotValidException();
      }
      List<RegisterRequestDTO> accounts = ExcelHelper.parseExcel(file);
      for (int i = 0; i < accounts.size(); i++) {
        RegisterRequestDTO register = accounts.get(i);
        int rowNumber = i + 2; // skip header
        if (!isValidRow(register, rowNumber)) continue;
        if (registerAccountSafely(register, rowNumber, user)) {
          accountRegisterSuccessfully.add(register);
        }
      }
    } catch (IOException e) {
      log.info("Unable to read Excel file: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.info(e.getMessage());
    }
    return accountRegisterSuccessfully;
  }

  public byte[] exportAccountsToExcel() throws IOException {
    List<Account> accounts = accountsRepository.findAll();
    String[] header = { "Create At", "Email", "Role", "Create By" };
    String[] field = { "createdTimestamp", "email", "role", "createdBy" };
    return exportToExcel(accounts, "account", header, field);
  }

  @Override
  public <T> byte[] exportToExcel(
    List<T> data,
    String sheetName,
    String[] headers,
    String[] fieldNames
  ) throws IOException {
    ExcelExporter<T> exporter = new ExcelExporter<>();
    return exporter.export(data, sheetName, headers, fieldNames);
  }

  public boolean validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      log.info("File must not be empty");
      return false;
    }
    if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx")) {
      log.info("File must be in .xlsx format");
      return false;
    }
    if (file.getSize() > 20L * 1024 * 1024) {
      log.info("File size exceeds 20MB");
      return false;
    }
    return true;
  }

  private boolean isValidRow(RegisterRequestDTO account, int row) {
    if (account.getEmail() == null || account.getEmail().isEmpty()) {
      log.info("Row {}: user cannot be empty", row);
      return false;
    }
    if (account.getPassword() == null || account.getPassword().isEmpty()) {
      log.info("Row {}: password cannot be empty", row);
      return false;
    }
    return true;
  }

  private boolean registerAccountSafely(RegisterRequestDTO account, int row, User user) {
    try {
      authService.register(account);
      Account acc = accountsRepository.findAccountByEmail(account.getEmail());
      // set created by
      acc.setCreatedBy(user.getAccount().getUsername());
      acc.setDeletedTimestamp(Instant.now());
      accountsRepository.save(acc);
      log.info("{}=> import", acc.getId());
      emailService.sendEmailWithTemplate(
        acc.getEmail(),
        "ACTIVE ACCOUNT",
        "active-account",
        Map.of(
          "name",
          acc.getUsername(),
          "activationLink",
          "http://localhost:4000/api/v1/auth/active-account/" + account.getEmail()
        )
      );
      return true;
    } catch (EmailHasAlreadyBeenUsedException e) {
      log.info("Row {}: email '{}' has already been used", row, account.getEmail());
    } catch (Exception e) {
      log.error(
        "Row {}: unexpected error - {}: {}",
        row,
        e.getClass().getSimpleName(),
        e.getMessage()
      );
      if (e.getCause() != null) {
        log.error(
          "Row {}: cause - {}: {}",
          row,
          e.getCause().getClass().getSimpleName(),
          e.getCause().getMessage()
        );
      }
      log.error("Row {}: stack trace", row, e);
    }
    return false;
  }
}
