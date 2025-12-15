package com.example.modules.file.excel.utils;

import com.example.modules.auth.dtos.RegisterRequestDTO;
import com.example.modules.file.excel.exceptions.AccountImportLimitExceeded;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

public class ExcelHelper {

  public static void processExcelWithChunk(
    MultipartFile file,
    Consumer<List<RegisterRequestDTO>> consumer
  ) throws IOException {
    List<RegisterRequestDTO> chunk = new ArrayList<>(1000);
    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);
      Row header = sheet.getRow(0);

      if (
        header == null ||
        !"RollNumber".equalsIgnoreCase(getString(header.getCell(0))) ||
        !"Email".equalsIgnoreCase(getString(header.getCell(1))) ||
        !"FullName".equalsIgnoreCase(getString(header.getCell(2)))
      ) {
        throw new FileNotFoundException("File Excel not correct format");
      }
      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;

        String email = getString(row.getCell(1));
        if (email.isEmpty()) continue;

        String password = PasswordUtils.generateRandomPassword(8);
        chunk.add(new RegisterRequestDTO(email, password));
        if (chunk.size() >= 1000) {
          consumer.accept(chunk);
          chunk.clear();
        }
      }
      if (!chunk.isEmpty()) {
        consumer.accept(chunk);
      }
    }
  }

  private static String getString(Cell cell) {
    if (cell == null) return "";
    cell.setCellType(CellType.STRING);
    return cell.getStringCellValue().trim();
  }
}
