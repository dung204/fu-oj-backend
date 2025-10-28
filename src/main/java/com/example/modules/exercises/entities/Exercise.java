package com.example.modules.exercises.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.exercises.enums.Difficulty;
import com.example.modules.groups.entities.Group;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.topics.entities.Topic;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = { "testCases", "topics", "groups" })
@Table(name = "exercises")
public class Exercise extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String code;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private Integer maxSubmissions;

  @Column(columnDefinition = "double precision default 0.2 check (time_limit > 0)")
  private Double timeLimit; // in seconds

  @Column(columnDefinition = "double precision default 65.536 check (memory > 0)")
  private Double memory; // kilobytes

  @Column(
    nullable = false,
    columnDefinition = "varchar(255) default 'EASY' check (difficulty in ('EASY','MEDIUM','HARD'))"
  )
  @Enumerated(EnumType.STRING)
  private Difficulty difficulty = Difficulty.EASY;

  @ManyToMany
  @JoinTable(
    name = "exercise_topics",
    joinColumns = @JoinColumn(name = "exercise_id"),
    inverseJoinColumns = @JoinColumn(name = "topic_id")
  )
  private List<Topic> topics;

  @OneToMany(mappedBy = "exercise")
  private List<TestCase> testCases;

  @ManyToMany(mappedBy = "exercises")
  private List<Group> groups;
}
