package com.example.modules.file.excel.controller;

import static com.example.base.utils.AppRoutes.EXCEL_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.dtos.RegisterRequestDTO;
import com.example.modules.file.excel.service.ExcelService;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

  @Operation(
    summary = "Import accounts from Excel file",
    description = """
    Import a list of user accounts from an Excel file (.xlsx).
    The file must include required columns such as email and password.
    Each valid record will be converted into a `RegisterRequestDTO`.
    """,
    responses = {
      @ApiResponse(responseCode = "201", description = "Import successful"),
      @ApiResponse(
        responseCode = "400",
        description = """
        - File is missing or invalid format (not .xlsx)
        - Excel data contains invalid or empty fields
        """,
        content = @Content
      ),
      @ApiResponse(
        responseCode = "500",
        description = "Internal Server Error while processing Excel file",
        content = @Content
      ),
    }
  )
  @PostMapping("/import")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<List<RegisterRequestDTO>> importExcel(
    @RequestParam("file") MultipartFile file,
    @CurrentUser User user
  ) {
    List<RegisterRequestDTO> registerRequestDTOS = excelService.importAccountFromExcel(file, user);

    return SuccessResponseDTO.<List<RegisterRequestDTO>>builder()
      .status(201)
      .message("Thêm tài khoản thành công")
      .data(registerRequestDTOS)
      .build();
  }

  @Operation(
    summary = "Export account list to Excel",
    description = """
    Export all existing accounts in the system to an Excel file (.xlsx).
    The generated file will include headers and account details.
    """,
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Export successful. Returns Excel file as attachment.",
        content = @Content(
          mediaType = "application/octet-stream",
          schema = @Schema(type = "string", format = "binary")
        )
      ),
      @ApiResponse(
        responseCode = "500",
        description = "Internal Server Error while generating Excel file",
        content = @Content
      ),
    }
  )
  // export => data, nameSheet, header, field
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
