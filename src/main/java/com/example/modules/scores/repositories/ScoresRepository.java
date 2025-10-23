package com.example.modules.scores.repositories;

import com.example.modules.scores.entities.Score;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoresRepository extends JpaRepository<Score, String> {
  Optional<Score> findByUserId(String userId);
}
