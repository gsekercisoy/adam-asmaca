# Adam Asmaca v2

React (Vite) ön yüz + Java (Spring Boot 2.7) REST API. Kelimeler sunucuda saklanır,
**istemci kelimeyi asla görmez** — tahminler tek harf POST istekleriyle yapılır.

## Özellikler

- Oyun durumu sunucuda: `games` tablosu (kelime, tahminler, hata sayısı, durum)
- Türkçe alfabe doğrulaması; 6 hata limiti; kazanınca puan = (kalan hak × 10) + (kelime uzunluğu × 5)
- Kelime yalnızca **oyun bitince** yanıtla açıklanır (PLAYING durumunda JSON'a hiç girmez)
- Skor tablosu: sadece kazananlar puanı >0 sıralanır

## Çalıştırma

```bash
cd backend && mvn spring-boot:run     # API :8080
cd frontend && npm install && npm run dev   # UI :5173 (proxy → 8080)
```

## API

| Metot | Yol | Açıklama |
|---|---|---|
| POST | `/api/games` | `{player?}` → `{id, masked, hint, lives:6, status}` |
| GET | `/api/games/{id}` | Mevcut durum |
| POST | `/api/games/{id}/guess` | `{letter}` → `{masked, wrong, lives, status, message}` |
| GET | `/api/leaderboard` | En iyi 10 (puan > 0) |

Hatalı istek (çoklu harf / yabancı harf / biten oyuna tahmin) → 400.

v1 tek dosyalık HTML sürümü git geçmişinde korunmaktadır.
