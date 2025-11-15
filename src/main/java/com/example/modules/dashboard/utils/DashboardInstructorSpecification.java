package com.example.modules.dashboard.utils;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardInstructorSpecification {

  private String instructorId;
  private String groupId;

  public static DashboardInstructorSpecificationBuilder builder() {
    return new DashboardInstructorSpecificationBuilder();
  }

  public DashboardInstructorSpecificationBuilder withInstructorId(String instructorId) {
    return DashboardInstructorSpecification.builder().instructorId(instructorId);
  }

  public DashboardInstructorSpecificationBuilder withGroupId(String groupId) {
    this.groupId = groupId;
    return DashboardInstructorSpecification.builder()
      .instructorId(this.instructorId)
      .groupId(groupId);
  }

  public boolean hasGroupFilter() {
    return groupId != null && !groupId.trim().isEmpty();
  }
}
