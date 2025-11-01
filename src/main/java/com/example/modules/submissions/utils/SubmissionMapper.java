package com.example.modules.submissions.utils;

import com.example.modules.exercises.utils.ExerciseMapper;
import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submissions.dtos.SubmissionResponseDTO;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.enums.Verdict;
import com.example.modules.users.utils.UserMapper;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
  componentModel = "spring",
  uses = { UserMapper.class, ExerciseMapper.class, SubmissionResultMapper.class }
)
public abstract class SubmissionMapper {

  @Mapping(source = "user", target = "user", qualifiedByName = "toUserProfileDTO")
  @Mapping(source = "exercise", target = "exercise", qualifiedByName = "toExerciseResponseDTO")
  @Mapping(source = "submissionResults", target = "submissionResults")
  public abstract SubmissionResponseDTO toSubmissionResponseDTO(Submission submission);

  @AfterMapping
  protected void calculateDerivedFields(
    Submission submission,
    @MappingTarget SubmissionResponseDTO dto
  ) {
    List<SubmissionResult> results = submission.getSubmissionResults();

    dto.setTotalTestCases(submission.getExercise().getTestCases().size());

    // --- Calculate Passed and Total Test Cases ---
    if (results != null) {
      long passedCount = results
        .stream()
        .filter(result -> Verdict.ACCEPTED.getValue().equals(result.getVerdict()))
        .count();
      dto.setPassedTestCases((int) passedCount);
    } else {
      dto.setPassedTestCases(0);
    }

    // --- Calculate Verdict (your existing logic) ---
    if (results == null || results.isEmpty()) {
      dto.setVerdict(Verdict.PROCESSING.getValue()); // Or some other default status
      return;
    }

    // Define verdict priorities
    final List<String> processingVerdicts = Verdict.getProcessingVerdicts();
    final List<String> errorVerdicts = Verdict.getErrorVerdicts();

    String firstError = null;
    boolean allAccepted = true;

    // Rule 3: Highest priority - check for processing verdicts
    for (SubmissionResult result : results) {
      if (processingVerdicts.contains(result.getVerdict())) {
        dto.setVerdict(result.getVerdict());
        return;
      }

      if (errorVerdicts.contains(result.getVerdict()) && firstError == null) {
        firstError = result.getVerdict();
      }

      if (!Verdict.ACCEPTED.getValue().equals(result.getVerdict())) {
        allAccepted = false;
      }
    }

    // Rule 2: If no processing verdicts, check for errors
    if (firstError != null) {
      dto.setVerdict(firstError);
      return;
    }

    // Rule 1: If no processing or errors, check if all are accepted
    if (allAccepted) {
      dto.setVerdict(Verdict.ACCEPTED.getValue());
    } else {
      // This case might happen if there are other statuses not accounted for
      dto.setVerdict(Verdict.UNKNOWN.getValue());
    }
  }
}
