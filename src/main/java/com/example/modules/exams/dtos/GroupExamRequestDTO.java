package com.example.modules.exams.dtos;

import lombok.Data;

@Data
public class GroupExamRequestDTO {

  private String groupExamId; // ID của chính GroupExam
  private String groupId;
  private String examId;
  private String ownerId; // ID của instructor owner của group
  private String status;
}
