package com.example.modules.exams.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.exams.enums.ExamStatus;
import com.example.modules.groups.entities.Group;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "exam", "group" })
@ToString(exclude = { "exam", "group" })
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "group_exams")
public class GroupExam extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id", nullable = false)
  private Exam exam;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;

  @Enumerated(EnumType.STRING)
  @Column(
    nullable = false,
    columnDefinition = "varchar(255) default 'DRAFT' check (status in ('DRAFT','UPCOMING','ONGOING','COMPLETED','CANCELLED','OUTDATED'))"
  )
  @Builder.Default
  private ExamStatus status = ExamStatus.DRAFT;

  @Column(name = "is_examined", columnDefinition = "boolean default false")
  @Builder.Default
  private Boolean isExamined = false; // kiểm tra hay chưa
}
