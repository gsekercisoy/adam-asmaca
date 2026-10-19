import React, { useEffect, useState } from 'react';
import { api } from './api.js';

const ALPH = 'abcçdefgğhıijklmnoöprsştuüvyz'.split('');

export default function App() {
  const [state, setState] = useState(null); // sunucudan gelen oyun durumu
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');
  const [player, setPlayer] = useState(() => localStorage.getItem('hangmanPlayer') || '');
  const [board, setBoard] = useState([]);

  const loadBoard = () => api.get('/leaderboard').then(setBoard).catch(() => {});

  useEffect(() => { loadBoard(); }, []);

  const newGame = async () => {
    setErr('');
    setBusy(true);
    try {
      const s = await api.post('/games', { player: player.trim() });
      if (player.trim()) localStorage.setItem('hangmanPlayer', player.trim());
      setState(s);
    } catch (e) { setErr(e.message); }
    setBusy(false);
  };

  const guess = async (ch) => {
    if (!state || state.status !== 'PLAYING' || busy) return;
    if ((state.guessed || []).includes(ch)) return;
    setBusy(true);
    try {
      const s = await api.post(`/games/${state.id}/guess`, { letter: ch });
      setState(s);
      if (s.status !== 'PLAYING') loadBoard();
    } catch (e) { setErr(e.message); }
    setBusy(false);
  };

  useEffect(() => {
    const onKey = (e) => {
      const ch = e.key.toLowerCase();
      if (ALPH.includes(ch)) guess(ch);
      if (e.key === 'Enter' && (!state || state.status !== 'PLAYING')) newGame();
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  });

  if (!state) {
    return (
      <div className="app">
        <h1>🎩 Adam Asmaca v2</h1>
        <p className="lead">Kelimeler ve oyuncu durumu Java REST API'de saklanır — istemci kelimeyi asla görmez.</p>
        <div className="panel start">
          <label>Oyuncu adı (skor tablosuna yazılır)</label>
          <input
            value={player}
            onChange={e => setPlayer(e.target.value)}
            placeholder="MISAFIR"
            maxLength={20}
          />
          <button className="btn primary" onClick={newGame} disabled={busy}>
            {busy ? '…' : 'Oyunu Başlat'}
          </button>
          <Leaders board={board} />
        </div>
        {err && <div className="err">{err}</div>}
      </div>
    );
  }

  const guessedSet = new Set(state.guessed || []);
  const lives = state.lives;

  return (
    <div className="app">
      <h1>🎩 Adam Asmaca v2</h1>
      <div className="hud">
        <span>{state.player}</span>
        <span>Kalan hak: <b>{lives}</b></span>
        <span>Oyun #{state.id}</span>
      </div>

      <div className="stage">
        <svg width="170" height="200" viewBox="0 0 170 200">
          <path className="frame" d="M20 190 L90 190 L45 190 L45 20 L120 20 L120 40" />
          <g id="man">
            <path className="part showf" d="M120 40 a13 13 0 1 0 0.1 0" style={{ visibility: lives <= 5 ? 'visible' : 'hidden' }} />
            <path className="part showf" d="M120 66 L120 120" style={{ visibility: lives <= 4 ? 'visible' : 'hidden' }} />
            <path className="part showf" d="M120 80 L98 98" style={{ visibility: lives <= 3 ? 'visible' : 'hidden' }} />
            <path className="part showf" d="M120 80 L142 98" style={{ visibility: lives <= 2 ? 'visible' : 'hidden' }} />
            <path className="part showf" d="M120 120 L102 155" style={{ visibility: lives <= 1 ? 'visible' : 'hidden' }} />
            <path className="part showf" d="M120 120 L138 155" style={{ visibility: lives <= 0 ? 'visible' : 'hidden' }} />
          </g>
        </svg>

        <div className="right-col">
          <div className="word">
            {state.masked.split('').map((ch, i) => (
              <div key={i} className={`letter ${ch === '_' ? '' : 'shown'}`}>{ch === '_' ? '' : ch}</div>
            ))}
          </div>
          <div className="hintline">İpucu: {state.hint}</div>
          <div className="msg">
            {state.status === 'WON' && <span className="won">Kazandın! 🎉 +{state.points} puan</span>}
            {state.status === 'LOST' && <span className="lost">Kaybettin 💀 — cevap: {state.word}</span>}
            {state.status === 'PLAYING' && (state.message || 'Harf seç')}
          </div>
        </div>
      </div>

      {err && <div className="err">{err}</div>}

      <div className="keys">
        {ALPH.map(ch => {
          const isGuessed = guessedSet.has(ch);
          const inWord = state.status !== 'PLAYING' && state.word && state.word.includes(ch);
          return (
            <button
              key={ch}
              className={isGuessed ? (inWord ? 'hit' : 'miss') : ''}
              onClick={() => guess(ch)}
              disabled={isGuessed || state.status !== 'PLAYING' || busy}
            >
              {ch.toUpperCase()}
            </button>
          );
        })}
      </div>

      {(state.status !== 'PLAYING') && (
        <button className="btn primary" onClick={newGame}>Yeni Oyun</button>
      )}

      <Leaders board={board} />
    </div>
  );
}

function Leaders({ board }) {
  return (
    <div className="panel board-panel">
      <h2>🏆 Skor Tablosu (sunucudan)</h2>
      <table className="tbl">
        <thead><tr><th>#</th><th>Oyuncu</th><th>Puan</th><th>Kelime</th></tr></thead>
        <tbody>
          {board.map((s, i) => (
            <tr key={s.id}>
              <td>{i + 1}</td>
              <td>{s.player}</td>
              <td className="num">{s.points}</td>
              <td className="muted">{s.won ? s.word : '—'}</td>
            </tr>
          ))}
          {board.length === 0 && <tr><td colSpan={4} className="muted">Henüz skor yok</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
