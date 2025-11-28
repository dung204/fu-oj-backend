package com.example.modules.courses.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.users.entities.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Set;
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
@Table(name = "courses")
public class Course extends BaseEntity {

  @Column(nullable = false, columnDefinition = "TEXT")
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column
  private String image;

  @ManyToMany
  @JoinTable(
    name = "course_exercises",
    joinColumns = @JoinColumn(name = "course_id"),
    inverseJoinColumns = @JoinColumn(name = "exercise_id")
  )
  private Set<Exercise> exercises;

  @ManyToMany
  @JoinTable(
    name = "course_users",
    joinColumns = @JoinColumn(name = "course_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private Set<User> enrolledStudents;
}
