package com.example.modules.comments.entities;

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
@Table(name = "comments")
public class Comment extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "exercise_id", nullable = false)
  private Exercise exercise;

  @ManyToOne
  @JoinColumn(name = "parent_id")
  private Comment parent;

  @Column(nullable = false)
  private String content;
}
