package com.example.modules.system_config.entities;

import com.example.base.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "system_config")
public class SystemConfigs extends BaseEntity {

  @Column(columnDefinition = "DOUBLE PRECISION DEFAULT 100")
  private Double easy;

  @Column(columnDefinition = "DOUBLE PRECISION DEFAULT 200")
  private Double medium;

  @Column(columnDefinition = "DOUBLE PRECISION DEFAULT 300")
  private Double difficult;

  @Column(name = "bonus_the_first_submit", columnDefinition = "DOUBLE PRECISION DEFAULT 10")
  private Double bonusTheFirstSubmit;

  @Column(name = "bonus_no_wrong_answer", columnDefinition = "DOUBLE PRECISION DEFAULT 5")
  private Double bonusNoWrongAnswer;

  @Column(name = "bonus_time", columnDefinition = "DOUBLE PRECISION DEFAULT 10")
  private Double bonusTime;

  @Column(name = "count_report", columnDefinition = "DOUBLE PRECISION DEFAULT 10")
  private Double countReport;
}
