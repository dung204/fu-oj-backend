package com.example.modules.users.repositories;

import com.example.modules.auth.entities.Account;
import com.example.modules.users.entities.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository
  extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
  Optional<User> findByAccount(Account account);

  @Query("SELECT u FROM User u JOIN u.account a WHERE a.email = :email")
  Optional<User> findByAccountEmail(String email);

  User findUserById(String id);
}
