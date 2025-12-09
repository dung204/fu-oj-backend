package com.example.modules.file.excel.service;

import com.example.modules.auth.dtos.RegisterRequestDTO;
import com.example.modules.auth.entities.Account;
import com.example.modules.auth.enums.Role;
import com.example.modules.auth.repositories.AccountsRepository;
import com.example.modules.auth.services.AuthService;
import com.example.modules.email.service.EmailService;
import com.example.modules.file.excel.exceptions.AccountImportLimitExceeded;
import com.example.modules.file.excel.exceptions.FileNotValidException;
import com.example.modules.file.excel.utils.ExcelExporter;
import com.example.modules.file.excel.utils.ExcelHelper;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService implements IExcelService {

  private final AccountsRepository accountsRepository;
  private final EmailService emailService;
  private final PasswordEncoder passwordEncoder;
  private final UsersRepository usersRepository;

  @Override
  public List<RegisterRequestDTO> importAccountFromExcel(MultipartFile file, User user) {
    List<RegisterRequestDTO> accountRegisterSuccessfully = new ArrayList<>();
    try {
      if (!validateFile(file)) {
        throw new FileNotValidException();
      }
      ExcelHelper.processExcelWithChunk(file, accounts -> {
        List<RegisterRequestDTO> dtoList = new ArrayList<>(500);
        List<RegisterRequestDTO> validAccounts = new ArrayList<>();
        List<String> emails = new ArrayList<>();

        for (int i = 0; i < accounts.size(); i++) {
          RegisterRequestDTO register = accounts.get(i);
          int rowNumber = i + 2;
          if (isValidRow(register, rowNumber)) {
            validAccounts.add(register);
            emails.add(register.getEmail());
          }
        }

        if (validAccounts.isEmpty()) {
          return;
        }
        List<Account> existingAccounts = accountsRepository.findByEmailIn(emails);
        Set<String> existingEmails = existingAccounts
          .stream()
          .map(Account::getEmail)
          .collect(Collectors.toSet());

        for (RegisterRequestDTO register : validAccounts) {
          if (!existingEmails.contains(register.getEmail())) {
            dtoList.add(register);
            if (dtoList.size() == 500) {
              registerBatchAccounts(dtoList, user);
              accountRegisterSuccessfully.addAll(dtoList);
              dtoList.clear();
            }
          }
        }
        if (!dtoList.isEmpty()) {
          registerBatchAccounts(dtoList, user);
          accountRegisterSuccessfully.addAll(dtoList);
        }
      });
    } catch (IOException e) {
      log.info("Unable to read Excel file: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.info(e.getMessage());
    } catch (AccountImportLimitExceeded e) {
      log.info("account limit exceeded: {}", e.getMessage());
    }
    return accountRegisterSuccessfully;
  }

  public byte[] exportAccountsToExcel() throws IOException {
    List<Account> accounts = accountsRepository
      .findAll()
      .stream()
      .filter(a -> a.getRole() != Role.ADMIN)
      .toList();
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

  private void registerBatchAccounts(List<RegisterRequestDTO> dtoList, User user) {
    List<Account> accounts = new ArrayList<>(500);
    Map<String, String> emailToPlainPassword = new LinkedHashMap<>();
    List<User> users = new ArrayList<>(500);

    for (RegisterRequestDTO account : dtoList) {
      emailToPlainPassword.put(account.getEmail(), account.getPassword());
      Account savedAccount = Account.builder()
        .email(account.getEmail())
        .password(passwordEncoder.encode(account.getPassword()))
        .createdBy(user.getAccount().getEmail())
        .deletedTimestamp(Instant.now())
        .build();
      accounts.add(savedAccount);
    }
    // batch accounts
    List<Account> savedAccounts = accountsRepository.saveAll(accounts);
    for (Account account : savedAccounts) {
      User savedUser = User.builder().account(account).build();
      users.add(savedUser);
    }
    // batch users
    usersRepository.saveAll(users);
    // for gui emai
    for (Account account : savedAccounts) {
      try {
        String password = emailToPlainPassword.get(account.getEmail());
        emailService.sendEmailWithTemplate(
          account.getEmail(),
          "ACTIVE ACCOUNT",
          "active-account",
          Map.of(
            "email",
            account.getEmail(),
            "password",
            password,
            "activationLink",
            "https://fu-oj.vercel.app/auth/active-account?email=" + account.getEmail()
          )
        );
      } catch (Exception emailException) {
        // Log email error but don't fail the import
        log.error(
          "Failed to send activation email to '{}': {}",
          account.getEmail(),
          emailException.getMessage(),
          emailException
        );
      }
    }
  }
}
