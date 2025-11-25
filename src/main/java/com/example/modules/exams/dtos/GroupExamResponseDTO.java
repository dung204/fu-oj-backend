package com.example.modules.exams.dtos;

import com.example.base.dtos.EntityDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GroupExamResponseDTO extends EntityDTO {

  private String groupExamId;
  private ExamBasicInfoDTO exam;
  private GroupBasicInfoDTO group;
  private String status;

  @Data
  @SuperBuilder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExamBasicInfoDTO {

    private String id;
    private String code;
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private Double timeLimit;
  }

  @Data
  @SuperBuilder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GroupBasicInfoDTO {

    private String id;
    private String code;
    private String name;
    private String ownerId;
    private String ownerName;
  }
}
