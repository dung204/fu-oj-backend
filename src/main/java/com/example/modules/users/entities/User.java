package com.example.modules.users.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.auth.entities.Account;
import com.example.modules.posts.entities.Post;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

  @Column
  private String firstName;

  @Column
  private String lastName;

  @Column
  private String avatar;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private List<Post> posts;
}
