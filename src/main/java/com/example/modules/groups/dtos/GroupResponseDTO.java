package com.example.modules.groups.dtos;

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
public class GroupResponseDTO extends EntityDTO {

  private String code;
  private String name;
  private String description;
  private boolean isPublic;
  private String ownerId;
}
