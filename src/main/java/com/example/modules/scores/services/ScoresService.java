package com.example.modules.scores.services;

import com.example.modules.exercises.enums.Difficulty;
import com.example.modules.scores.dtos.ScoreResponseDTO;
import com.example.modules.scores.dtos.ScoresSearchDTO;
import com.example.modules.scores.entities.Score;
import com.example.modules.scores.repositories.ScoresRepository;
import com.example.modules.scores.utils.ScoresMapper;
import com.example.modules.scores.utils.ScoresSpecification;
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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoresService {

  private final ScoresRepository scoresRepository;
  private final SubmissionsRepository submissionsRepository;
  private final UsersRepository usersRepository;
  private final ScoresMapper scoresMapper;

  /**
   * Lấy tất cả scores với pagination và filter
   *
   * @param scoresSearchDTO DTO chứa các tham số tìm kiếm và pagination
   * @return Page of ScoreResponseDTO
   */
  public Page<ScoreResponseDTO> getAllScores(ScoresSearchDTO scoresSearchDTO) {
    log.info("Lấy danh sách scores với filter: {}", scoresSearchDTO);

    return scoresRepository
      .findAll(
        ScoresSpecification.builder()
          .withUserId(scoresSearchDTO.getUserId())
          .withUserEmail(scoresSearchDTO.getUserEmail())
          .withMinScore(scoresSearchDTO.getMinScore())
          .withMaxScore(scoresSearchDTO.getMaxScore())
          .notDeleted()
          .build(),
        scoresSearchDTO.toPageRequest()
      )
      .map(scoresMapper::toScoreResponseDTO);
  }

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

    // tôi nghĩ là bonus thêm nữa: +10 nếu thời gian submit nhỏ hơn thời gian yêu cầu của đề bài
    // if(submission.getTime() < submission.getExercise().get)) {
    //   bonus += 10;
    //   log.info("Bonus +10: Thời gian submit nhỏ hơn thời gian yêu cầu của đề bài cho submission {}", submission.getId());
    // }

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
   * Cập nhật điểm cho User dựa trên submission mới (cộng dần)
   * Tổng điểm User = Tổng điểm cao nhất mỗi bài
   *
   * @param newSubmission Submission vừa hoàn thành
   */
  @Transactional
  public void updateUserScoreBySubmission(Submission newSubmission) {
    String userId = newSubmission.getUser().getId();
    String exerciseId = newSubmission.getExercise().getId();
    double newScore = newSubmission.getScore();
    boolean isNewAccepted = newSubmission.getIsAccepted();

    log.info(
      "Cập nhật điểm cho user {} với submission mới (exercise: {}, score: {}, AC: {})",
      userId,
      exerciseId,
      newScore,
      isNewAccepted
    );

    // 1. Lấy hoặc tạo Score cho user
    Score score = scoresRepository
      .findByUserId(userId)
      .orElseGet(() ->
        Score.builder()
          .user(newSubmission.getUser())
          .totalScore(0.0)
          .solvedEasy(0)
          .solvedMedium(0)
          .solvedHard(0)
          .build()
      );

    // 2. Lấy tất cả submission CŨ của exercise này (không bao gồm submission hiện tại)
    List<Submission> previousSubmissions = submissionsRepository.findAll((root, query, cb) ->
      cb.and(
        cb.equal(root.get("user").get("id"), userId),
        cb.equal(root.get("exercise").get("id"), exerciseId),
        cb.notEqual(root.get("id"), newSubmission.getId()),
        cb.isNull(root.get("deletedTimestamp"))
      )
    );

    // 3. Tính toán cập nhật điểm
    if (previousSubmissions.isEmpty()) {
      // CASE 1: Lần đầu làm bài này
      log.info("Lần đầu làm bài {} -> cộng {} điểm", exerciseId, newScore);
      score.setTotalScore(score.getTotalScore() + newScore);

      // Nếu AC thì tăng solved count
      if (isNewAccepted) {
        incrementSolvedCount(score, newSubmission.getExercise().getDifficulty());
      }
    } else {
      // CASE 2: Đã làm bài này rồi
      // Lấy điểm cao nhất và trạng thái AC của các submission cũ
      double oldHighestScore = previousSubmissions
        .stream()
        .mapToDouble(Submission::getScore)
        .max()
        .orElse(0.0);

      boolean wasAcceptedBefore = previousSubmissions.stream().anyMatch(Submission::getIsAccepted);

      log.info(
        "Đã làm bài {} trước đó - điểm cao nhất cũ: {}, đã AC: {}",
        exerciseId,
        oldHighestScore,
        wasAcceptedBefore
      );

      // Nếu điểm mới cao hơn điểm cũ -> cộng thêm phần chênh lệch
      if (newScore > oldHighestScore) {
        double scoreDiff = newScore - oldHighestScore;
        log.info("Điểm mới cao hơn -> cộng thêm {} điểm", scoreDiff);
        score.setTotalScore(score.getTotalScore() + scoreDiff);
      }

      // Nếu chưa từng AC mà bây giờ AC -> tăng solved count
      if (!wasAcceptedBefore && isNewAccepted) {
        log.info("Lần đầu AC bài {} -> tăng solved count", exerciseId);
        incrementSolvedCount(score, newSubmission.getExercise().getDifficulty());
      }
    }

    // 4. Lưu Score
    scoresRepository.save(score);

    log.info(
      "Đã cập nhật điểm cho user {}: totalScore={}, easy={}, medium={}, hard={}",
      userId,
      score.getTotalScore(),
      score.getSolvedEasy(),
      score.getSolvedMedium(),
      score.getSolvedHard()
    );
  }

  /**
   * Tăng solved count theo độ khó
   */
  private void incrementSolvedCount(Score score, Difficulty difficulty) {
    switch (difficulty) {
      case EASY -> score.setSolvedEasy(score.getSolvedEasy() + 1);
      case MEDIUM -> score.setSolvedMedium(score.getSolvedMedium() + 1);
      case HARD -> score.setSolvedHard(score.getSolvedHard() + 1);
    }
    log.info("Tăng solved {} -> {}", difficulty, getSolvedCountByDifficulty(score, difficulty));
  }

  /**
   * Lấy solved count theo độ khó
   */
  private int getSolvedCountByDifficulty(Score score, Difficulty difficulty) {
    return switch (difficulty) {
      case EASY -> score.getSolvedEasy();
      case MEDIUM -> score.getSolvedMedium();
      case HARD -> score.getSolvedHard();
    };
  }

  /**
   * Tính toán lại toàn bộ điểm cho User (dùng khi cần recalculate)
   * Tổng điểm User = Tổng điểm cao nhất mỗi bài
   *
   * @param userId ID của user
   */
  @Transactional
  public void recalculateUserTotalScore(String userId) {
    log.info("Tính toán lại toàn bộ điểm cho user {}", userId);

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
      "Đã tính toán lại điểm cho user {}: totalScore={}, easy={}, medium={}, hard={}",
      userId,
      totalScore,
      solvedEasy,
      solvedMedium,
      solvedHard
    );
  }
}
