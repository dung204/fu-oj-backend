package com.example.modules.auth.services;

import com.example.modules.auth.dtos.account.AccountResponseDTO;
import com.example.modules.auth.entities.Account;
import com.example.modules.auth.repositories.AccountsRepository;
import com.example.modules.auth.utils.AccountMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

  private final AccountsRepository accountsRepository;
  private final AccountMapper accountMapper;

  public List<AccountResponseDTO> getAccounts() {
    List<Account> accounts = Optional.ofNullable(accountsRepository.findAll()).orElseGet(
      ArrayList::new
    );

    return accounts.stream().map(accountMapper::toAccountResponseDTO).toList();
  }
}
