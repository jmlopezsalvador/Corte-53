# 02 · Dominio y lógica

## 1. El orden Mnemónica

La posición 1 es la carta superior del mazo boca abajo. El orden completo también está en `datos/stack.json`.

```
 1 4♣   2 2♥   3 7♦   4 3♣   5 4♥   6 6♦   7 A♠   8 5♥   9 9♠  10 2♠  11 Q♥  12 3♦  13 Q♣
14 8♥  15 6♠  16 5♠  17 9♥  18 K♣  19 2♦  20 J♥  21 3♠  22 8♠  23 6♥  24 10♣ 25 5♦  26 K♦
27 2♣  28 3♥  29 8♦  30 5♣  31 K♠  32 J♦  33 8♣  34 10♠ 35 K♥  36 J♣  37 7♠  38 10♥ 39 A♦
40 4♠  41 7♥  42 4♦  43 A♣  44 9♣  45 J♠  46 Q♦  47 7♣  48 Q♠  49 10♦ 50 6♣  51 A♥  52 9♦
```

### Codificación usada en el código

```js
// T = 10, P = picas (Spades), C = tréboles, H = corazones, D = diamantes
STACK = ['4C','2H','7D','3C','4H','6D','AP','5H','9P','2P','QH','3D','QC','8H','6P','5P','9H','KC','2D','JH',
         '3P','8P','6H','TC','5D','KD','2C','3H','8D','5C','KP','JD','8C','TP','KH','JC','7P','TH','AD','4P',
         '7H','4D','AC','9C','JP','QD','7C','QP','TD','6C','AH','9D'];
// carta en la posición p  →  STACK[p-1]
```

- **Nombre largo:** «As/2…10/Jota/Reina/Rey de tréboles/corazones/diamantes/picas». Ejemplo: «Reina de corazones».
- **Nombre corto:** rango + símbolo. Ejemplo: «Q♥», «10♣». Los símbolos llevan el selector de variación U+FE0E para que no se pinten como emoji.
- **Color:** ♥ y ♦ rojos; ♣ y ♠ negros.

Un modelo tipado recomendado para la app: `Suit {CLUBS, HEARTS, DIAMONDS, SPADES}`, `Rank {ACE, TWO … TEN, JACK, QUEEN, KING}`, `Card(rank, suit)`, y `MnemonicaStack` con `cardAt(position)` y `positionOf(card)`. La versión web no tiene índice inverso porque no lo necesita, pero la app lo agradecerá.

---

## 2. Selección de posiciones (menú principal)

```js
function positions() {                     // rango + filtro
  const a = [];
  for (let i = cfg.from; i <= cfg.to; i++) {
    const c = STACK[i-1];
    if (cfg.filt === 'suit' && c[1] !== cfg.suit) continue;
    if (cfg.filt === 'rank' && c[0] !== cfg.rank) continue;
    a.push(i);
  }
  return a;
}
function buildList() {                     // lista de la sesión
  const a = positions();
  return cfg.order === 'rnd' ? shuffle(a) : a;   // Fisher-Yates
}
```

Lo usan Empezar, Escuchar (y cada vuelta de «Repetir al terminar») y el sorteo de carta en Cortes. Restas no lo usa.

---

## 3. Cortes: carta al número

### Nomenclatura
- **C:** posición actual de la carta pedida.
- **D:** posición de destino.
- **N:** posición de la carta de referencia que se lleva arriba o abajo al cortar.

### Fórmulas

| Caso | Movimiento de la carta | Fórmula | Corte |
|---|---|---|---|
| D < C | sube | **N = C − D** | la carta de N queda **abajo del todo** |
| D > C | baja | **N = 53 − (D − C)** | la carta de N queda **arriba del todo** |
| D = C | — | — | no hace falta cortar (la app nunca lo propone) |

### Por qué funciona
Un corte equivale a pasar k cartas de arriba abajo; la carta de C acaba en C − k (o C − k + 52 si da ≤ 0).
- D < C: k = C − D = N. La carta de N es la última del bloque pasado, así que queda abajo del todo.
- D > C: k = 52 − (D − C) = N − 1. La carta de N es la primera que no se pasa, así que queda arriba del todo.

En la nota de la revelación, «pasas k cartas de arriba abajo» usa k = N − 1 si es arriba y k = N si es abajo.

### Algoritmo

```js
function cutFor(C, D) {        // C ≠ D
  if (D > C) { const d = D - C; return { N: 53 - d, up: true,  d }; }
  else       {                  return { N: C - D, up: false, d: C - D }; }
}
// carta a cortar: STACK[N-1]
```

Rango de N: si D > C, N está entre 2 y 52 (d ≤ 51); si D < C, entre 1 y 51.

### Texto de la revelación (literal)
Caso D > C:
```
El {C♠} está en la {C}: C = {C}.
Destino D = {D}. D > C: tienes que bajar la carta.
D − C = {D} − {C} = {d}.
N = 53 − (D − C) = 53 − {d} = {N}.
En N = {N} está el {carta N}.
↑ Cortar arriba
El {nombre carta N} queda arriba del todo (pasas {N−1} cartas de arriba abajo).
```
Caso D < C:
```
El {C♠} está en la {C}: C = {C}.
Destino D = {D}. D < C: tienes que subir la carta.
N = C − D = {C} − {D} = {N}.
En N = {N} está el {carta N}.
↓ Cortar abajo
El {nombre carta N} queda abajo del todo (pasas {N} cartas de arriba abajo).
```
Los números resultado van en dorado y negrita, y la frase «tienes que subir/bajar la carta» en cursiva verde agua.

### Verificación hecha
En la web se comprobaron 800 escenarios al azar (667 pares C/D distintos) simulando el corte sobre el mazo: en todos la carta acaba en D. **Haz en la app un test unitario exhaustivo de los 52×51 pares** (ver `05-GUIA-REFACTORIZACION.md`).

---

## 4. Restas

```js
// Entre dos números
x = rand(1..52); do { y = rand(1..52) } while (y === x);
a = max(x,y); b = min(x,y); res = a - b;      // 1..51
// Complemento a 53
a = 53; b = rand(1..52); res = 53 - b;        // 1..52
```

Como los resultados están siempre en 1–52, cualquier resultado tiene su clip de audio (`n1`…`n52`). El 53 tiene su propio clip (`n53`).

---

## 5. Estado de las sesiones

### Entrenamiento con tarjetas
```js
ses = {
  list: [posiciones],            // orden de la sesión
  i: índice actual,
  res: [null|true|false],        // marca por carta (opcional)
  dirs: ['n2c'|'c2n'],           // por carta; en Mixto se sortea al crear la sesión
  flipped, revealed,             // estado visual de la carta actual
  t0: Date.now()                 // para el tiempo final
}
```
Aciertos = nº de `true`; fallos = nº de `false`; sin marcar = nº de `null`. «Repasar fallos» crea una sesión nueva con las posiciones marcadas `false`.

### Cortes
`C = { n, p, t, shown, q, up, cc }`: contador, posición actual, destino, si está revelado, N, sentido del corte y la carta de N.

### Restas
`R = { mode, think, gap, rate, on, n, a, b, res, shown }`.

### Motores de audio
Cada pantalla con voz tiene su propio estado (`A`, `K`, `R`) con un token `tok` que se incrementa al parar. Cada callback diferido comprueba `tok === actual && on` antes de seguir, lo que cancela de forma segura temporizadores y audios pendientes al pausar, cambiar de modo o salir.

---

## 6. Persistencia

Hoy se usa `localStorage` (envuelto en try/catch). En la app, el equivalente sería DataStore, UserDefaults o similar.

| Clave | Contenido | Defectos |
|---|---|---|
| `mnemo-cfg` | `{order, dir, from, to, filt, suit, rank}` | `seq, n2c, 1, 52, all, P, 8` |
| `mnemo-audio` | `{mode, gap, inner, rate, loop, show}` | `say, 1.5, 0.5, 1, false, true` |
| `mnemo-cortes` | `{mode, think, step, next, rate}` | `manual, 5, 0.75, 2, 1` |
| `mnemo-restas` | `{mode, think, gap, rate}` | `diff, 3, 1.5, 1` |

No se guarda historial de sesiones ni estadísticas. Es una buena mejora para la app (ver guía).
