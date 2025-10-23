package com.example.modules.groups.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.modules.users.dtos.UserProfileDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
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
  private UserProfileDTO owner;
  private Integer studentsCount;

  // This field should be null for ADMIN & INSTRUCTOR, true/false for STUDENT
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean joined;
}
