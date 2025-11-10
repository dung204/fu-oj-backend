package com.example.modules.auth.utils;

import com.example.modules.auth.dtos.account.AccountResponseDTO;
import com.example.modules.auth.entities.Account;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class AccountMapper {

  public abstract AccountResponseDTO toAccountResponseDTO(Account account);
}
