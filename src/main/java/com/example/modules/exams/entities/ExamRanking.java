package com.example.modules.exams.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.users.entities.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = { "exam", "user" })
@ToString(exclude = { "exam", "user" })
@Entity
@Table(name = "exam_rankings")
public class ExamRanking extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id", nullable = false)
  private Exam exam;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, name = "total_score", columnDefinition = "double precision default 0")
  private Double totalScore;

  @Column(
    nullable = false,
    name = "number_of_completed_execises",
    columnDefinition = "double precision default 0"
  )
  private Double numberOfCompletedExercises;

  @Column(
    nullable = false,
    name = "number_of_execises",
    columnDefinition = "double precision default 0"
  )
  private Double numberOfExercises;
}
