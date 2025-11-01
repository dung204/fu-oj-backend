package com.example.modules.exams.utils;

import com.example.modules.exams.dtos.ExamResultResponseDto;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamSubmission;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.users.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public abstract class ExamResultMapper {

  @Named("toBaseExamResult")
  @Mapping(target = "examId", source = "exam.id")
  @Mapping(target = "examCode", source = "exam.code")
  @Mapping(target = "examTitle", source = "exam.title")
  @Mapping(target = "startTime", source = "exam.startTime")
  @Mapping(target = "endTime", source = "exam.endTime")
  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "userName", expression = "java(buildUserName(user))")
  @Mapping(target = "submissions", ignore = true)
  @Mapping(target = "totalScore", ignore = true)
  @Mapping(target = "totalExercises", ignore = true)
  @Mapping(target = "completedExercises", ignore = true)
  public abstract ExamResultResponseDto toBaseExamResult(Exam exam, User user);

  @Named("toSubmissionDetail")
  @Mapping(
    target = "exerciseId",
    expression = "java(es.getExercise() != null ? es.getExercise().getId() : null)"
  )
  @Mapping(
    target = "exerciseTitle",
    expression = "java(es.getExercise() != null ? es.getExercise().getTitle() : null)"
  )
  @Mapping(
    target = "exerciseCode",
    expression = "java(es.getExercise() != null ? es.getExercise().getCode() : null)"
  )
  @Mapping(target = "submissionId", source = "submission.id")
  @Mapping(target = "score", source = "submission.score")
  @Mapping(target = "isAccepted", source = "submission.isAccepted")
  @Mapping(target = "passedTestCases", source = "submission.passedTestCases")
  @Mapping(target = "totalTestCases", source = "submission.totalTestCases")
  @Mapping(target = "submittedAt", source = "es.createdTimestamp")
  public abstract ExamResultResponseDto.ExamSubmissionDetail toSubmissionDetail(
    ExamSubmission es,
    Submission submission
  );

  protected String buildUserName(User user) {
    if (user == null) return null;
    String first = user.getFirstName() != null ? user.getFirstName() : "";
    String last = user.getLastName() != null ? user.getLastName() : "";
    String full = (first + " " + last).trim();
    return full.isEmpty() ? null : full;
  }
}
