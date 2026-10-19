package com.acrom.hangman.repository;

import com.acrom.hangman.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
