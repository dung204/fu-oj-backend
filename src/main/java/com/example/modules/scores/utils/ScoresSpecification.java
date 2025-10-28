package com.example.modules.scores.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.scores.entities.Score;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScoresSpecification extends SpecificationBuilder<Score> {

  public static ScoresSpecification builder() {
    return new ScoresSpecification();
  }

  public ScoresSpecification withUserId(String userId) {
    if (userId != null && !userId.trim().isEmpty()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("user").get("id"), userId)
      );
    }
    return this;
  }

  public ScoresSpecification withUserEmail(String userEmail) {
    if (userEmail != null && !userEmail.trim().isEmpty()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.like(
          criteriaBuilder.lower(root.get("user").get("email")),
          "%" + userEmail.toLowerCase() + "%"
        )
      );
    }
    return this;
  }

  public ScoresSpecification withMinScore(Double minScore) {
    if (minScore != null) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.greaterThanOrEqualTo(root.get("totalScore"), minScore)
      );
    }
    return this;
  }

  public ScoresSpecification withMaxScore(Double maxScore) {
    if (maxScore != null) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.lessThanOrEqualTo(root.get("totalScore"), maxScore)
      );
    }
    return this;
  }
}
