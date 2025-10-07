package com.example.modules.test_cases.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.exercises.entities.Exercise;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@ToString(exclude = "exercise")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "test_cases")
public class TestCase extends BaseEntity {

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "exercise_id", nullable = false)
  private Exercise exercise;

  @Column(columnDefinition = "TEXT")
  private String input;

  @Column(columnDefinition = "TEXT")
  private String output;
}
