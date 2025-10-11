package com.example.modules.submissions.services;

import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.submissions.repositories.SubmissionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SubmissionLimitService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final SubmissionsRepository submissionsRepository;
  private final ExercisesRepository exerciseRepository;

  private static final String PREFIX = "submission_count:";

  public void checkAndIncrease(String userId, String exerciseId) {
    String key = PREFIX + exerciseId + ":" + userId;
    Integer count = (Integer) redisTemplate.opsForValue().get(key);

    if (count == null) {
      count = submissionsRepository.countByUserIdAndExerciseId(userId, exerciseId);
      redisTemplate.opsForValue().set(key, count);
    }

    Exercise exercise = exerciseRepository
      .findById(String.valueOf(exerciseId))
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));

    if (count >= exercise.getMaxSubmissions() && exercise.getMaxSubmissions() != 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You get max submission limit");
    }

    // .opsForValue() -> get the value operations for simple key-value access ( like Number, String, Object)
    redisTemplate.opsForValue().increment(key);
  }
}
