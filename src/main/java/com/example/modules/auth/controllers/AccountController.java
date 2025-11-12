package com.example.modules.auth.controllers;

import static com.example.base.utils.AppRoutes.ACCOUNT_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.dtos.account.AccountResponseDTO;
import com.example.modules.auth.services.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = ACCOUNT_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "account", description = "Operations related to account")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @GetMapping
  public SuccessResponseDTO<List<AccountResponseDTO>> getAccount() {
    return SuccessResponseDTO.<List<AccountResponseDTO>>builder()
      .status(200)
      .message("get list account successfully")
      .data(accountService.getAccounts())
      .build();
  }

  @PutMapping("/active")
  public SuccessResponseDTO<String> actionAccount(
    @RequestParam boolean action,
    @RequestParam String id
  ) {
    accountService.actionAccount(action, id);
    return SuccessResponseDTO.<String>builder()
      .status(204)
      .message("in active account successful")
      .data(null)
      .build();
  }

  @PutMapping("/role")
  public SuccessResponseDTO<String> changeRoleAccount(
    @RequestParam String role,
    @RequestParam String id
  ) {
    accountService.changeRoleAccount(role, id);
    return SuccessResponseDTO.<String>builder()
      .status(204)
      .message("change role account successful")
      .data(null)
      .build();
  }
}
