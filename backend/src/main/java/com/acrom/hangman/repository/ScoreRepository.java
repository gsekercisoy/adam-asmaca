package com.acrom.hangman.repository;

import com.acrom.hangman.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findTop10ByPointsGreaterThanOrderByPointsDesc(int min);
}
