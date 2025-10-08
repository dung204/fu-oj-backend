package com.example.modules.exercises.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.groups.entities.Group;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.topics.entities.Topic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

  @ManyToMany
  @JoinTable(
    name = "exercise_topics",
    joinColumns = @JoinColumn(name = "exercise_id"),
    inverseJoinColumns = @JoinColumn(name = "topic_id")
  )
  private List<Topic> topics;

  @OneToMany(mappedBy = "exercise")
  private List<TestCase> testCases;

  @JsonIgnore
  @ManyToMany(mappedBy = "exercises")
  private List<Group> groups;
}
