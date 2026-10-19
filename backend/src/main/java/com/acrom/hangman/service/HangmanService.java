package com.acrom.hangman.service;

import com.acrom.hangman.model.Game;
import com.acrom.hangman.model.Score;
import com.acrom.hangman.repository.GameRepository;
import com.acrom.hangman.repository.ScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

@Service
public class HangmanService {

    private static final int MAX_WRONG = 6;

    private static final Object[][] WORDS = {
            {"yazilim", "Bilgisayar programlarının sanatı"},
            {"ankara", "Başkent"},
            {"karpuz", "Yaz meyvesi, çekirdeği savrulur"},
            {"kitaplik", "Evi donatan ahşap mobilya"},
            {"bulut", "Gökte süzülen beyaz ağ"},
            {"gitar", "6 telli çalgı"},
            {"cömlek", "Toprak testi"},
            {"mutfak", "Evde yemek yapılan"},
            {"yildiz", "Gecede parlayan"},
            {"bisiklet", "Pedallı iki tekerlekli"},
            {"deniz", "Yazın gidilen mavi"},
            {"cilek", "Kırmızı, kokulu meyve"},
            {"bilgisayar", "Masa üstünde duran alet"},
            {"gokkusagi", "Yağmur sonrası renkler"},
            {"maydanoz", "Tabaklara konan yeşil"},
            {"kutuphane", "Kitapların evi"},
            {"zeytin", "Sallanan, salamuralı"},
            {"ogrenci", "Okulda tahsilini yürüten"}
    };

    private static final String ALPHABET = "abcçdefgğhıijklmnoöprsştuüvyz";

    private final GameRepository games;
    private final ScoreRepository scores;
    private final Random rnd = new Random();

    public HangmanService(GameRepository games, ScoreRepository scores) {
        this.games = games;
        this.scores = scores;
    }

    @Transactional
    public Map<String, Object> create(String player) {
        if (player != null && !player.trim().isEmpty()) {
            player = player.trim();
            if (player.length() > 20) player = player.substring(0, 20);
        } else {
            player = "MISAFIR";
        }

        Object[] picked = WORDS[rnd.nextInt(WORDS.length)];
        Game g = new Game();
        g.word = String.valueOf(picked[0]);
        g.hint = String.valueOf(picked[1]);
        g.player = player;
        games.save(g);
        return toState(g, null);
    }

    @Transactional
    public Map<String, Object> guess(Long id, String letter) {
        Game g = games.findById(id).orElse(null);
        if (g == null) throw new IllegalArgumentException("Oyun bulunamadı");

        if (g.status != Game.Status.PLAYING) {
            throw new IllegalArgumentException("Oyun zaten bitti");
        }
        if (letter == null || letter.length() != 1) {
            throw new IllegalArgumentException("Tek harf gönderin");
        }
        String l = letter.toLowerCase().trim();
        if (ALPHABET.indexOf(l.charAt(0)) < 0) {
            throw new IllegalArgumentException("Türkçe alfabede olmayan harf: " + letter);
        }

        String message;
        if (g.isGuessed(l)) {
            message = "'" + l + "' zaten tahmin edilmiş";
        } else {
            g.addGuessed(l);
            if (g.word.contains(l)) {
                boolean allFound = true;
                for (char c : g.word.toCharArray()) {
                    if (!g.isGuessed(String.valueOf(c))) { allFound = false; break; }
                }
                if (allFound) {
                    g.status = Game.Status.WON;
                    saveScore(g, true);
                    message = "Kazandın!";
                } else {
                    message = "'" + l + "' var!";
                }
            } else {
                g.wrong++;
                if (g.wrong >= MAX_WRONG) {
                    g.status = Game.Status.LOST;
                    saveScore(g, false);
                    message = "Kaybettin — kelime: " + g.word;
                } else {
                    message = "'" + l + "' yok";
                }
            }
            games.save(g);
        }

        return toState(g, message);
    }

    public Map<String, Object> state(Long id) {
        Game g = games.findById(id).orElse(null);
        if (g == null) throw new IllegalArgumentException("Oyun bulunamadı");
        return toState(g, null);
    }

    @Transactional(readOnly = true)
    public java.util.List<Score> leaderboard() {
        return scores.findTop10ByPointsGreaterThanOrderByPointsDesc(0);
    }

    static String mask(Game g) {
        StringBuilder sb = new StringBuilder();
        for (char c : g.word.toCharArray()) {
            if (g.isGuessed(String.valueOf(c))) sb.append(c);
            else sb.append('_');
        }
        return sb.toString();
    }

    static int points(Game g) {
        return (MAX_WRONG - g.wrong) * 10 + g.word.length() * 5;
    }

    static Map<String, Object> toState(Game g, String message) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", g.id);
        out.put("masked", mask(g));
        out.put("hint", g.hint);
        out.put("wrong", g.wrong);
        out.put("lives", MAX_WRONG - g.wrong);
        out.put("status", g.status.name());
        out.put("player", g.player);
        out.put("guessed", g.guessedCsv.isEmpty() ? new String[0] : g.guessedCsv.split(","));
        out.put("message", message != null ? message : "");
        if (g.status != Game.Status.PLAYING) {
            out.put("word", g.word); // oyun bitti, kelimeyi açıkla
            out.put("points", points(g));
        } else {
            out.put("points", 0);
        }
        return out;
    }

    private void saveScore(Game g, boolean won) {
        Score s = new Score();
        s.player = g.player;
        s.points = won ? points(g) : 0;
        s.word = g.word;
        s.won = won;
        scores.save(s);
    }
}
