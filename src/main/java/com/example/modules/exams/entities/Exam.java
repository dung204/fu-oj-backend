package com.example.modules.exams.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.exams.enums.Status;
import com.example.modules.groups.entities.Group;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(
  callSuper = true,
  exclude = { "group", "examExercises", "examSubmissions", "rankings" }
)
@ToString(exclude = { "group", "examExercises", "examSubmissions", "rankings" })
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id")
  private Group group; // exam thuộc group nào (nếu có)

  private Instant startTime;
  private Instant endTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "varchar(255) default 'UPCOMING'")
  @Builder.Default
  private Status status = Status.UPCOMING;

  @OneToMany(
    mappedBy = "exam", // exam: liên kết với tên thuộc tính trong ExamExercise
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.LAZY
  )
  private List<ExamExercise> examExercises;

  @OneToMany(mappedBy = "exam", fetch = FetchType.LAZY)
  private List<ExamSubmission> examSubmissions;

  @OneToMany(mappedBy = "exam", fetch = FetchType.LAZY)
  private List<ExamRanking> rankings;
}
