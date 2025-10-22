package com.example.modules.groups.dtos;

import com.example.base.dtos.PaginatedQueryDTO;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupsSearchDTO extends PaginatedQueryDTO {

  @Parameter(
    description = "A general search term to find groups by their name (supports partial matching)."
  )
  private String name;

  @Parameter(
    description = "Special filter for the STUDENT role to switch between views. " +
      "Use `joined` to see joined groups. " +
      "Defaults to showing public groups. This parameter is ignored for `ADMIN` and `INSTRUCTOR` roles."
  )
  private String filter;

  @Parameter(
    description = "Filter groups by their public status. " +
      "If not provided, both public and private groups are returned. " +
      "This is primarily for `ADMIN` and `INSTRUCTOR` roles."
  )
  private Boolean isPublic;
}
