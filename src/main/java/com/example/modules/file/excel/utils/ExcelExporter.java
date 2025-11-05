package com.example.modules.file.excel.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExporter<T> {

  public byte[] export(List<T> data, String sheetName, String[] headers, String[] fieldNames)
    throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet(sheetName);

      // Header Style
      CellStyle headerStyle = workbook.createCellStyle();
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerStyle.setFont(headerFont);

      // Header Row
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
      }

      // Data Rows
      int rowIndex = 1;
      for (T item : data) {
        Row dataRow = sheet.createRow(rowIndex++);
        for (int i = 0; i < fieldNames.length; i++) {
          Object value = getFieldValue(item, fieldNames[i]);
          dataRow.createCell(i).setCellValue(value != null ? value.toString() : "");
        }
      }

      // Auto-size columns
      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
      }

      // Convert to byte array
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      workbook.write(outputStream);
      return outputStream.toByteArray();
    }
  }

  private Object getFieldValue(Object obj, String fieldName) {
    try {
      Class<?> clazz = obj.getClass();
      Field field = null;

      // Search through the class hierarchy to find the field
      while (clazz != null && field == null) {
        try {
          field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
          clazz = clazz.getSuperclass();
        }
      }

      if (field != null) {
        field.setAccessible(true);
        Object value = field.get(obj);

        // Handle Instant type for createdTimestamp
        if (value instanceof java.time.Instant) {
          return ((java.time.Instant) value).toString();
        }

        return value;
      }

      return null;
    } catch (Exception e) {
      return null;
    }
  }
}
