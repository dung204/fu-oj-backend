package com.example.modules.auth.repositories;

import com.example.modules.auth.entities.Account;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountsRepository
  extends JpaRepository<Account, String>, JpaSpecificationExecutor<Account> {
  Optional<Account> findByEmail(String email);

  Account findAccountByEmail(String email);

  Account findAccountById(String id);

  @Query("SELECT a FROM Account a WHERE a.email IN :emails")
  List<Account> findByEmailIn(@Param("emails") List<String> emails);
}
