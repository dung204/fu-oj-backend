package com.example.modules.submissions.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.enums.Verdict;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubmissionsSpecification extends SpecificationBuilder<Submission> {

  public static SubmissionsSpecification builder() {
    return new SubmissionsSpecification();
  }

  public SubmissionsSpecification withStudentId(String studentId) {
    if (studentId != null && !studentId.trim().isEmpty()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("user").get("id"), studentId)
      );
    }
    return this;
  }

  public SubmissionsSpecification withExerciseId(String exerciseId) {
    if (exerciseId != null && !exerciseId.trim().isEmpty()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("exercise").get("id"), exerciseId)
      );
    }
    return this;
  }

  public SubmissionsSpecification isOneOfStatuses(Collection<String> statuses) {
    if (statuses == null || statuses.isEmpty()) {
      return this;
    }

    // Define verdict categories based on priority
    final List<String> processingVerdicts = Verdict.getProcessingVerdicts();
    final List<String> acceptedVerdicts = List.of(Verdict.ACCEPTED.getValue());
    // All other verdicts are considered "error" verdicts for this logic
    final List<String> allPrioritizedVerdicts = List.of(
      Verdict.IN_QUEUE.getValue(),
      Verdict.PROCESSING.getValue(),
      Verdict.ACCEPTED.getValue()
    );

    // Separate the requested statuses into categories
    Set<String> requestedProcessing = statuses
      .stream()
      .filter(processingVerdicts::contains)
      .collect(Collectors.toSet());
    Set<String> requestedAccepted = statuses
      .stream()
      .filter(acceptedVerdicts::contains)
      .collect(Collectors.toSet());
    Set<String> requestedErrors = statuses
      .stream()
      .filter(s -> !allPrioritizedVerdicts.contains(s))
      .collect(Collectors.toSet());

    specifications.add((root, query, cb) -> {
      List<Predicate> orPredicates = new ArrayList<>();

      // Rule 3 (Highest Priority): Filter for PROCESSING or IN_QUEUE
      if (!requestedProcessing.isEmpty()) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<SubmissionResult> subRoot = subquery.from(SubmissionResult.class);
        subquery
          .select(subRoot.get("id"))
          .where(
            cb.equal(subRoot.get("submission"), root),
            subRoot.get("verdict").in(requestedProcessing)
          );
        orPredicates.add(cb.exists(subquery));
      }

      // Rule 2: Filter for ERROR verdicts
      if (!requestedErrors.isEmpty()) {
        // Must have at least one of the requested error verdicts
        Subquery<Long> hasErrorSubquery = query.subquery(Long.class);
        Root<SubmissionResult> hasErrorRoot = hasErrorSubquery.from(SubmissionResult.class);
        hasErrorSubquery
          .select(hasErrorRoot.get("id"))
          .where(
            cb.equal(hasErrorRoot.get("submission"), root),
            hasErrorRoot.get("verdict").in(requestedErrors)
          );

        // AND must NOT have any processing verdicts (since they have higher priority)
        Subquery<Long> noProcessingSubquery = query.subquery(Long.class);
        Root<SubmissionResult> noProcessingRoot = noProcessingSubquery.from(SubmissionResult.class);
        noProcessingSubquery
          .select(noProcessingRoot.get("id"))
          .where(
            cb.equal(noProcessingRoot.get("submission"), root),
            noProcessingRoot.get("verdict").in(processingVerdicts)
          );

        orPredicates.add(
          cb.and(cb.exists(hasErrorSubquery), cb.not(cb.exists(noProcessingSubquery)))
        );
      }

      // Rule 1 (Lowest Priority): Filter for ACCEPTED
      if (!requestedAccepted.isEmpty()) {
        // Must NOT have any non-accepted results (i.e., no errors and no processing)
        Subquery<Long> noOtherVerdictSubquery = query.subquery(Long.class);
        Root<SubmissionResult> noOtherVerdictRoot = noOtherVerdictSubquery.from(
          SubmissionResult.class
        );
        noOtherVerdictSubquery
          .select(noOtherVerdictRoot.get("id"))
          .where(
            cb.equal(noOtherVerdictRoot.get("submission"), root),
            cb.not(noOtherVerdictRoot.get("verdict").in(acceptedVerdicts))
          );

        // AND must have at least one result to not match empty submissions
        Subquery<Long> hasResultsSubquery = query.subquery(Long.class);
        Root<SubmissionResult> hasResultsRoot = hasResultsSubquery.from(SubmissionResult.class);
        hasResultsSubquery
          .select(hasResultsRoot.get("id"))
          .where(cb.equal(hasResultsRoot.get("submission"), root));

        orPredicates.add(
          cb.and(cb.not(cb.exists(noOtherVerdictSubquery)), cb.exists(hasResultsSubquery))
        );
      }

      // Combine all rules with an OR condition
      return cb.or(orPredicates.toArray(new Predicate[0]));
    });
    return this;
  }

  public SubmissionsSpecification isOneOfLanguageCodes(Collection<String> languageCodes) {
    if (languageCodes != null) {
      specifications.add((root, query, criteriaBuilder) ->
        root.get("languageCode").in(languageCodes)
      );
    }
    return this;
  }
}
