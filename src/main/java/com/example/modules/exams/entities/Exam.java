package com.example.modules.exams.entities;

import com.example.base.entities.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "groupExams", "examExercises" })
@ToString(exclude = { "groupExams", "examExercises" })
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exams")
public class Exam extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String code;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @OneToMany(
    mappedBy = "exam",
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.LAZY
  )
  private List<GroupExam> groupExams; // exam có thể thuộc nhiều group

  private Instant startTime;
  private Instant endTime;

  @Column(name = "time_limit")
  private Double timeLimit;

  @OneToMany(
    mappedBy = "exam", // exam: liên kết với tên thuộc tính trong ExamExercise
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.LAZY
  )
  private List<ExamExercise> examExercises;
}

/**
 * Nên đưa exam vào redis để check nhanh hơn
 * bởi vì bài exam thường có số lượng lớn học sinh tham gia và cần xử lý nhanh
 * và giảm tải cho database + ít khi update
 */
