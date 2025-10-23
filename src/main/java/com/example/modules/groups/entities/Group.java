package com.example.modules.groups.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.users.entities.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
@Table(name = "groups")
public class Group extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String code;

  @ManyToOne
  @JoinColumn(name = "owner_id", nullable = false)
  private User instructor;

  @Column(nullable = false)
  private String name;

  @Column
  private String description;

  @Column
  private Boolean isPublic;

  @ManyToMany
  @JoinTable(
    name = "group_students",
    joinColumns = @JoinColumn(name = "group_id"),
    inverseJoinColumns = @JoinColumn(name = "student_id")
  )
  private List<User> students;

  @ManyToMany
  @JoinTable(
    name = "group_exercises",
    joinColumns = @JoinColumn(name = "group_id"),
    inverseJoinColumns = @JoinColumn(name = "exercise_id")
  )
  private List<Exercise> exercises;
}
