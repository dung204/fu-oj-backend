package com.example.modules.scores.services;

import com.example.modules.exercises.enums.Difficulty;
import com.example.modules.scores.entities.Score;
import com.example.modules.scores.repositories.ScoresRepository;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.repositories.SubmissionsRepository;
import com.example.modules.users.entities.User;
import com.example.modules.users.exceptions.UserNotFoundException;
import com.example.modules.users.repositories.UsersRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoresService {

  private final ScoresRepository scoresRepository;
  private final SubmissionsRepository submissionsRepository;
  private final UsersRepository usersRepository;

  /**
   * Tính điểm cho 1 submission
   * finalScore = baseScore * accuracy + bonus
   *
   * @param submission Submission đã hoàn thành
   * @return Điểm tính được
   */
  public double calculateSubmissionScore(Submission submission) {
    // 1. Lấy baseScore từ độ khó
    Difficulty difficulty = submission.getExercise().getDifficulty();
    int baseScore = difficulty.getValue();

    // 2. Tính accuracy
    double accuracy = submission.getTotalTestCases() > 0
      ? (double) submission.getPassedTestCases() / submission.getTotalTestCases()
      : 0.0;

    // 3. Tính bonus
    double bonus = 0.0;

    // Kiểm tra xem có phải lần submit đầu tiên không
    long submissionCount = submissionsRepository.countByUserIdAndExerciseId(
      submission.getUser().getId(),
      submission.getExercise().getId()
    );

    // Bonus: Pass toàn bộ ngay lần đầu (+10)
    if (submissionCount == 1 && submission.getIsAccepted()) {
      bonus += 10;
      log.info("Bonus +10: Pass ngay lần đầu cho submission {}", submission.getId());
    }

    // Bonus: Không bị Wrong Answer nào (+5)
    if (submission.getIsAccepted() && !hasWrongAnswer(submission)) {
      bonus += 5;
      log.info("Bonus +5: Không có Wrong Answer cho submission {}", submission.getId());
    }

    // 4. Tính finalScore
    double finalScore = baseScore * accuracy + bonus;

    log.info(
      "Tính điểm submission {}: baseScore={}, accuracy={}, bonus={}, finalScore={}",
      submission.getId(),
      baseScore,
      accuracy,
      bonus,
      finalScore
    );

    return finalScore;
  }

  /**
   * Kiểm tra xem submission có Wrong Answer không
   */
  private boolean hasWrongAnswer(Submission submission) {
    return submission
      .getSubmissionResults()
      .stream()
      .anyMatch(sr -> "WRONG_ANSWER".equals(sr.getVerdict()));
  }

  /**
   * Cập nhật tổng điểm cho User
   * Tổng điểm User = Tổng điểm cao nhất mỗi bài
   *
   * @param userId ID của user
   */
  @Transactional
  public void updateUserTotalScore(String userId) {
    log.info("Cập nhật tổng điểm cho user {}", userId);

    // 1. Lấy User
    User user = usersRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());

    // 2. Lấy hoặc tạo Score cho user
    Score score = scoresRepository
      .findByUserId(userId)
      .orElseGet(() ->
        Score.builder()
          .user(user)
          .totalScore(0.0)
          .solvedEasy(0)
          .solvedMedium(0)
          .solvedHard(0)
          .build()
      );

    // 3. Lấy tất cả submission của user (chỉ lấy những submission đã hoàn thành)
    List<Submission> userSubmissions = submissionsRepository.findAll((root, query, cb) ->
      cb.and(cb.equal(root.get("user").get("id"), userId), cb.isNull(root.get("deletedTimestamp")))
    );

    // 4. Group theo exerciseId, lấy điểm cao nhất mỗi bài
    Map<String, Double> highestScorePerExercise = userSubmissions
      .stream()
      .collect(
        Collectors.groupingBy(
          sub -> sub.getExercise().getId(),
          Collectors.mapping(Submission::getScore, Collectors.maxBy(Double::compare))
        )
      )
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue().isPresent())
      .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));

    // 5. Tính tổng điểm
    double totalScore = highestScorePerExercise
      .values()
      .stream()
      .mapToDouble(Double::doubleValue)
      .sum();

    // 6. Đếm số bài solved theo độ khó (chỉ tính các bài AC)
    int solvedEasy = 0;
    int solvedMedium = 0;
    int solvedHard = 0;

    for (Map.Entry<String, Double> entry : highestScorePerExercise.entrySet()) {
      String exerciseId = entry.getKey();
      // Tìm submission có điểm cao nhất của exercise này để lấy difficulty
      Submission bestSubmission = userSubmissions
        .stream()
        .filter(
          sub ->
            sub.getExercise().getId().equals(exerciseId) && sub.getScore().equals(entry.getValue())
        )
        .findFirst()
        .orElse(null);

      if (bestSubmission != null && bestSubmission.getIsAccepted()) {
        Difficulty difficulty = bestSubmission.getExercise().getDifficulty();
        switch (difficulty) {
          case EASY -> solvedEasy++;
          case MEDIUM -> solvedMedium++;
          case HARD -> solvedHard++;
        }
      }
    }

    // 7. Cập nhật Score
    score.setTotalScore(totalScore);
    score.setSolvedEasy(solvedEasy);
    score.setSolvedMedium(solvedMedium);
    score.setSolvedHard(solvedHard);

    scoresRepository.save(score);

    log.info(
      "Đã cập nhật điểm cho user {}: totalScore={}, easy={}, medium={}, hard={}",
      userId,
      totalScore,
      solvedEasy,
      solvedMedium,
      solvedHard
    );
  }
}
