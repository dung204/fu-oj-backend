package com.example.modules.file.excel.utils;

import com.example.modules.auth.dtos.RegisterRequestDTO;
import com.example.modules.file.excel.exceptions.AccountImportLimitExceeded;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

public class ExcelHelper {

  public static List<RegisterRequestDTO> parseExcel(MultipartFile file) throws IOException {
    List<RegisterRequestDTO> accounts = new ArrayList<>();

    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);
      Row header = sheet.getRow(0);

      if (
        header == null ||
        !"studentId".equalsIgnoreCase(header.getCell(0).getStringCellValue().trim()) ||
        !"name".equalsIgnoreCase(header.getCell(1).getStringCellValue().trim()) ||
        !"mail".equalsIgnoreCase(header.getCell(2).getStringCellValue().trim())
      ) {
        throw new IllegalArgumentException("File Excel not correct format");
      }

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;
        String user = getString(row.getCell(2));
        if (user.isEmpty()) continue;
        String password = PasswordUtils.generateRandomPassword(8);

        accounts.add(new RegisterRequestDTO(user, password));
        if (accounts.size() > 100) throw new AccountImportLimitExceeded();
      }
    }
    return accounts;
  }

  private static String getString(Cell cell) {
    if (cell == null) return "";
    cell.setCellType(CellType.STRING);
    return cell.getStringCellValue().trim();
  }
}
