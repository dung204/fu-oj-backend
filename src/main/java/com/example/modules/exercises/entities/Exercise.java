package com.example.modules.exercises.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.exercises.enums.Difficulty;
import com.example.modules.exercises.enums.Visibility;
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

  @Builder.Default
  private Integer maxSubmissions = 10;

  @Column(columnDefinition = "double precision default 0.2 check (time_limit > 0)")
  @Builder.Default
  private Double timeLimit = 0.2; // in seconds

  @Column(columnDefinition = "double precision default 65536 check (memory >= 2048)")
  @Builder.Default
  private Double memory = 65536d; // kilobytes

  @Column(
    columnDefinition = "varchar(255) default 'EASY' check (difficulty in ('EASY','MEDIUM','HARD'))"
  )
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Difficulty difficulty = Difficulty.EASY;

  @Column(
    columnDefinition = "varchar(255) default 'DRAFT' check (visibility in ('PUBLIC','PRIVATE','DRAFT'))"
  )
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Visibility visibility = Visibility.DRAFT;

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
