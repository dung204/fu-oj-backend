package com.example.modules.users.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.auth.entities.Account;
import com.example.modules.certifications.entities.Certification;
import com.example.modules.groups.entities.Group;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "account", "joinedGroups", "certifications" })
@ToString(exclude = { "account", "joinedGroups", "certifications" })
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

  @Column(unique = true)
  private String rollNumber;

  @Column
  private String firstName;

  @Column
  private String lastName;

  @Column
  private String phone;

  @Column
  private String address;

  @Column
  private String avatar;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @ManyToMany(mappedBy = "students")
  private List<Group> joinedGroups;

  @ManyToMany
  @JoinTable(
    name = "user_certifications",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "certification_id")
  )
  private List<Certification> certifications;
}
