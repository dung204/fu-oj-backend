package com.example.modules.exams.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.users.entities.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "exam", "user", "exercise" })
@ToString(exclude = { "exam", "user", "exercise" })
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exam_submissions")
public class ExamSubmission extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id", nullable = false)
  private Exam exam;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exercise_id", nullable = false)
  private Exercise exercise;

  @Column(nullable = false)
  private String submissionId; // FK đến submission hệ thống chấm

  private Double score;
}
