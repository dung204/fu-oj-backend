package com.example.modules.topics.dtos;

import com.example.base.dtos.EntityDTO;
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
public class TopicsResponseDTO extends EntityDTO {

  private String name;
  private String description;
}
