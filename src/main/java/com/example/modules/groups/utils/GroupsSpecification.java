package com.example.modules.groups.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.groups.entities.Group;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupsSpecification extends SpecificationBuilder<Group> {

  public static GroupsSpecification builder() {
    return new GroupsSpecification();
  }
}
