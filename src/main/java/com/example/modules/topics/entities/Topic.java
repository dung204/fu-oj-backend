package com.example.modules.topics.entities;

import com.example.base.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@ToString(exclude = "topics")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "topics")
public class Topic extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column
  private String description;
}
