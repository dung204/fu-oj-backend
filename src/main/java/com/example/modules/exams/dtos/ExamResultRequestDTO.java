package com.example.modules.exams.dtos;

import lombok.Data;

@Data
public class ExamResultRequestDTO {

  private String examId; // Optional: xem kết quả theo exam (all groups)
  private String groupExamId; // Optional: xem kết quả theo groupExam cụ thể
  private String userId;
}
