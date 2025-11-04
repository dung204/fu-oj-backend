package com.example.modules.submission_results.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.test_cases.entities.TestCase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "submission", "testCase" })
@ToString(exclude = { "submission", "testCase" })
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "submission_results")
public class SubmissionResult extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "submission_id", nullable = false)
  private Submission submission;

  @ManyToOne
  @JoinColumn(name = "test_case_id", nullable = false)
  private TestCase testCase;

  @Column
  private String token; // token từ Judge0 cho test case này

  @Column(columnDefinition = "TEXT")
  private String actualOutput; // stdout Judge0 trả về

  @Column(columnDefinition = "TEXT")
  private String stderr;

  @Column
  private String verdict; // Accepted, Wrong Answer, CE, RE...

  @Column
  private String time;

  @Column
  private String memory;
}
