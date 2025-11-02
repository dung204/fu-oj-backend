package com.example.modules.file.excel.service;

import com.example.modules.auth.dtos.RegisterRequestDTO;
import com.example.modules.users.entities.User;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface IExcelService {
  public List<RegisterRequestDTO> importAccountFromExcel(MultipartFile file, User user);

  public <T> byte[] exportToExcel(
    List<T> data,
    String sheetName,
    String[] headers,
    String[] fieldNames
  ) throws IOException;
}
