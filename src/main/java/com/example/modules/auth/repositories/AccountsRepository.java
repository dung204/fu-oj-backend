package com.example.modules.auth.repositories;

import com.example.modules.auth.entities.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountsRepository
  extends JpaRepository<Account, String>, JpaSpecificationExecutor<Account> {
  Optional<Account> findByEmail(String email);
}
