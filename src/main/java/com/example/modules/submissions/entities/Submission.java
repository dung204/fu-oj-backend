package com.example.modules.submissions.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.users.entities.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "submissions")
public class Submission extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "exercise_id", nullable = false)
  private Exercise exercise;

  // code ở đây là code của bài tập, kiểu mã code để phân biệt các bài tập với nhau
  @Column
  private String code;

  @Column(columnDefinition = "TEXT") // text: for long source codes
  private String sourceCode;

  @Column
  private String languageCode;

  @Column
  private String time;

  @Column
  private String memory;

  // ghi lại tên bài tập
  @Column
  private String exerciseItem;
}
