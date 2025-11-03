package com.example.modules.auth.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.auth.entities.Account;

public class AccountsSpecification extends SpecificationBuilder<Account> {

  public static AccountsSpecification builder() {
    return new AccountsSpecification();
  }

  public AccountsSpecification withEmail(String email) {
    if (email != null && !email.trim().isEmpty()) {
      specifications.add((root, query, criteriaBuilder) -> {
        return criteriaBuilder.equal(root.get("email"), email);
      });
    }
    return this;
  }
}
