package com.example.modules.file.excel.controller;

import static com.example.base.utils.AppRoutes.EXCEL_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.dtos.RegisterRequestDTO;
import com.example.modules.file.excel.service.ExcelService;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = EXCEL_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "excel", description = "Operations related to comments")
@RequiredArgsConstructor
public class ExcelController {

  private final ExcelService excelService;

  @PostMapping("/import")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<List<RegisterRequestDTO>> importExcel(
    @RequestParam("file") MultipartFile file,
    @CurrentUser User user
  ) {
    List<RegisterRequestDTO> registerRequestDTOS = excelService.importAccountFromExcel(file, user);

    return SuccessResponseDTO.<List<RegisterRequestDTO>>builder()
      .status(201)
      .message("import successful")
      .data(registerRequestDTOS)
      .build();
  }

  //export => data, nameSheet, header, field
  @GetMapping("/export/accounts")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<byte[]> exportAccountToExcel() throws IOException {
    byte[] excelFile = excelService.exportAccountsToExcel();
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=accounts.xlsx")
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(excelFile);
  }
}
