package com.example.modules.scores.entities;

import com.example.base.entities.BaseEntity;
import com.example.modules.users.entities.User;
import jakarta.persistence.*;
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
@Table(name = "scores")
public class Score extends BaseEntity {

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  private Double totalScore;

  private Integer solvedEasy;

  private Integer solvedMedium;

  private Integer solvedHard;
}
