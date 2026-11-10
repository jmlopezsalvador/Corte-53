# 05 · Guía de refactorización a app móvil

La especificación de este paquete no depende de la tecnología. Las referencias a Kotlin y Compose son orientativas: encajan con una base Kotlin (Android) que luego se lleva a iOS, como Kotlin Multiplatform con Compose Multiplatform, pero toda la lógica de dominio se puede portar tal cual a cualquier stack.

## 1. Arquitectura propuesta

```
domain/                      ← puro, sin dependencias de plataforma, 100% testeable
  Card, Rank, Suit
  MnemonicaStack             cardAt(pos), positionOf(card)
  PositionFilter             rango + palo/valor → List<Int>
  SessionBuilder             orden secuencial/aleatorio, dirección por carta (Mixto)
  CutCalculator              cutFor(C, D) → {N, up, d, cartasPasadas}
  SubtractionGenerator       diff | c53
  SpokenSequence             construye la lista [clip | pausa | evento visual] de cada modo
audio/
  ClipPlayer                 reproduce por clave, velocidad con tono conservado, precarga
  SequenceRunner             ejecuta una SpokenSequence; play/pausa/cancelar; emite eventos visuales
data/
  SettingsRepository         los 4 grupos de ajustes con sus defectos
ui/
  screens: Menu, Train, Result, Listen, Cuts, Subtractions, Deck, Glossary
  components: PlayingCard, CardBack, PositionBadge, Segmented, ChipRow, LabeledSlider
```

Ideas clave:
- **Separar «qué se dice» de «cómo suena».** En la web cada pantalla tiene su motor casi duplicado (`speak`, `kSay`, `rSay`). En la app, un único `SequenceRunner` que recibe una lista de pasos (`Clip("n12")`, `Pause(1.5)`, `Visual(Reveal)`) y la ejecuta con cancelación. Cada modo solo genera su lista. Así se testea la secuencia sin audio.
- **Cancelación estructurada:** sustituir el token manual por corrutinas o tareas canceladas al pausar, cambiar de modo o salir de la pantalla.
- **Una sola fuente de verdad para las fórmulas:** `CutCalculator` alimenta el entrenamiento, la locución y los ejemplos del glosario.

## 2. Correspondencia web → móvil

| Web | Móvil |
|---|---|
| Un HTML con pantallas que se muestran y ocultan | Navegación con pila (atrás del sistema = «Volver»/«Salir») |
| `localStorage` | DataStore / UserDefaults (o multiplataforma) |
| Audios en base64 dentro del HTML | Ficheros en recursos (`audio/*.mp3`), precargados |
| `HTMLAudioElement` + `playbackRate` | Reproductor nativo con velocidad y tono conservado |
| Wake Lock API | Mantener pantalla encendida durante la reproducción |
| Eventos táctiles a mano | Detectores de gestos nativos (swipe, tap) |
| Teclado (espacio, flechas, A/F) | Prescindible; opcional en tabletas con teclado |

## 3. Lecciones aprendidas en la web (no repetir)
1. **El TTS del navegador no sonó en iPhone.** Usar clips propios desde el principio.
2. **Marcar acierto o fallo debe ser opcional.** El usuario quiere poder avanzar y retroceder libremente.
3. **Revelación sin ruido:** carta grande y número en un círculo, sin «Nº» ni nombre de la carta. Es para crear un anclaje visual.
4. **En «Carta → número» la carta no gira:** solo aparece el número.
5. **Nomenclatura coherente** entre glosario y entrenamiento (C, D, N, subir/bajar, corte arriba/abajo).
6. **Los tiempos de voz se configuran en la propia pantalla**, no en el menú.
7. **El usuario itera mucho.** Conviene que añadir un modo, una sección del glosario o una frase de voz sea barato.

## 4. Mejoras naturales para la versión móvil (no existen en la web)
- **Estadísticas por carta:** aciertos y fallos históricos, cartas débiles y repaso espaciado de las peores.
- **Historial de sesiones** con tiempo y porcentaje.
- **Audio en segundo plano** o con pantalla bloqueada para el modo Escuchar, con controles en la pantalla de bloqueo.
- **Glosario extensible** con más secuencias (por ejemplo, número a carta mediante cálculos, o estimación de cortes).
- **Ilustraciones originales** de figuras, si se quiere enriquecer la carta.

## 5. Criterios de aceptación (paridad con la web)

### Dominio (tests unitarios)
- [ ] `STACK` tiene 52 cartas únicas y coincide con `datos/stack.json`.
- [ ] `positionOf(cardAt(p)) == p` para p en 1..52.
- [ ] Filtros: rango 1–13 + picas → [7, 9, 10]; valor 8 en 1–52 → [14, 22, 29, 33]; rango 1–5 + valor K → [].
- [ ] **Cortes, exhaustivo:** para los 52×51 pares (C, D) con C ≠ D, aplicar el corte (pasar k cartas de arriba abajo, con k = N − 1 si es arriba y k = N si es abajo) deja la carta original en D, y la carta de N queda arriba o abajo según corresponda.
- [ ] Ejemplos del glosario: (C=52, D=10) → N=42, 4♦, abajo; (C=3, D=10) → N=46, Q♦, arriba.
- [ ] Restas: «diff» siempre con a > b, ambos en 1..52; «c53» con a = 53 y b en 1..52.
- [ ] Secuencias habladas: para (C=3, D=48) la secuencia de cortes es `c7D kAla n48 | kEsta n3 | n48 minus n3 kEs n45 | n53 minus n45 kEs n8 | c5H kEnla n8 | kUp`.

### Funcionales
- [ ] Menú con los 4 ajustes y los 6 accesos; el botón Empezar muestra el número de cartas y se desactiva con 0.
- [ ] Entrenamiento: giro en Nº → carta, aparición del número en Carta → número, marcar opcional, anterior/siguiente, deslizar, «Terminar» en la última y resumen con «sin marcar».
- [ ] Repasar fallos y repetir sesión.
- [ ] Escuchar: 5 modos, 3 deslizadores (el de número-carta oculto en los modos «solo»), repetir, mostrar u ocultar carta, ↺.
- [ ] Cortes: manual y con voz, 4 deslizadores, ›, deslizar, explicación con la nomenclatura exacta.
- [ ] Restas: 2 tipos, manual por toque, cantar con 3 deslizadores, ›.
- [ ] Baraja completa y glosario.
- [ ] Todos los ajustes persisten entre aperturas.
- [ ] Todo funciona sin conexión.

## 6. Prompt de arranque sugerido para Claude Code

> Lee `README.md` y los documentos `01` a `05` de esta carpeta. Abre `fuente/app.js` como referencia de comportamiento. Implementa primero la capa `domain` con sus tests (incluido el test exhaustivo de cortes) y el `SequenceRunner` de audio usando los clips de `audio/`. Después construye las pantallas en este orden: Menú, Entrenamiento con Resultado, Baraja, Glosario, Escuchar, Cortes y Restas. Respeta los textos, los valores por defecto y la nomenclatura de los documentos.
