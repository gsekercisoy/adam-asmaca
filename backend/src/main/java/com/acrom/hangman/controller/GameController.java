package com.acrom.hangman.controller;

import com.acrom.hangman.model.Score;
import com.acrom.hangman.service.HangmanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GameController {

    private final HangmanService service;

    public GameController(HangmanService service) {
        this.service = service;
    }

    @PostMapping("/games")
    public Map<String, Object> newGame(@RequestBody(required = false) Map<String, String> body) {
        String player = body != null ? body.get("player") : null;
        return service.create(player);
    }

    @GetMapping("/games/{id}")
    public Map<String, Object> state(@PathVariable Long id) {
        return service.state(id);
    }

    @PostMapping("/games/{id}/guess")
    public Map<String, Object> guess(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return service.guess(id, body.get("letter"));
    }

    @GetMapping("/leaderboard")
    public List<Score> leaderboard() {
        return service.leaderboard();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> bad(IllegalArgumentException ex) {
        Map<String, String> err = new java.util.LinkedHashMap<>();
        err.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(err);
    }
}
