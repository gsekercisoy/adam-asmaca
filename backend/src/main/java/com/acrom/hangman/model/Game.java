package com.acrom.hangman.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
public class Game {

    public enum Status { PLAYING, WON, LOST }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    public Long id;

    @Column(nullable = false)
    @JsonIgnore
    public String word;      // istemciye asla donmez (oyun bitince maske acilir)

    @Column(nullable = false)
    @JsonProperty("hint")
    public String hint;

    @Column(length = 200)
    @JsonIgnore
    public String guessedCsv = "";

    @Column(nullable = false)
    @JsonProperty("wrong")
    public int wrong = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @JsonProperty("status")
    public Status status = Status.PLAYING;

    @Column(length = 20)
    @JsonProperty("player")
    public String player = "MISAFIR";

    @JsonProperty("createdAt")
    public LocalDateTime createdAt = LocalDateTime.now();

    public boolean isGuessed(String letter) {
        return (("," + guessedCsv + ",").contains("," + letter + ","));
    }

    public void addGuessed(String letter) {
        guessedCsv = guessedCsv.isEmpty() ? letter : guessedCsv + "," + letter;
    }
}
