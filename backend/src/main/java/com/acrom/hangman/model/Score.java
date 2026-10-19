package com.acrom.hangman.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scores")
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    public Long id;

    @Column(nullable = false, length = 20)
    @JsonProperty("player")
    public String player;

    @Column(nullable = false)
    @JsonProperty("points")
    public int points;

    @Column(nullable = false)
    @JsonProperty("word")
    public String word;

    @Column(nullable = false)
    @JsonProperty("won")
    public boolean won;

    @JsonProperty("createdAt")
    public LocalDateTime createdAt = LocalDateTime.now();
}
