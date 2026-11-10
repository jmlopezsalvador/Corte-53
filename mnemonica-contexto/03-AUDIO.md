# 03 · Audio

## 1. Decisión clave: clips pregrabados, no síntesis del sistema

La primera versión usaba la Web Speech API (`speechSynthesis`). **En iPhone no sonaba**, ni en Safari ni en Chrome. Se sustituyó por clips MP3 pregrabados que se reproducen como audio normal. En la app nativa conviene mantener los clips como recurso por tres motivos: voz idéntica en todos los dispositivos, funcionamiento sin conexión y control exacto de los tiempos. El TTS del sistema queda como opción futura, no como base.

## 2. Inventario (`audio/`, 112 ficheros)

| Clave | Cantidad | Dice | Ejemplo |
|---|---|---|---|
| `n1` … `n52` | 52 | el número en palabras | `n22` → «veintidós», `n31` → «treinta y uno» |
| `n53` | 1 | «cincuenta y tres» | restas y cortes |
| `c` + código | 52 | la carta | `c7D` → «siete de diamantes», `cQH` → «reina de corazones», `cTC` → «diez de tréboles», `cAP` → «as de picas» |
| `minus` | 1 | «menos» | |
| `kAla` | 1 | «a la posición» | propuesta de cortes |
| `kEsta` | 1 | «está en la» | |
| `kEs` | 1 | «es» | |
| `kEnla` | 1 | «en la» | |
| `kUp` | 1 | «cortar arriba» | |
| `kDown` | 1 | «cortar abajo» | |

El texto de cada clip está en `datos/audio-manifest.json`. Total ≈ 820 KB.

### Cómo se generaron (por si hay que añadir frases con la misma voz)
- Motor: **Kokoro v1.0** en ONNX int8 (`kokoro-onnx` 0.6.1, modelo `kokoro-v1.0.int8.onnx` + `voices-v1.0.bin`). Voz **`ef_dora`** (femenina, español), `lang='es'`, `speed=1.0`. Al texto se le añade un punto final («veintidós.») para que la entonación cierre.
- Postproceso con ffmpeg:
  ```
  -af "silenceremove=start_periods=1:start_threshold=-45dB,areverse,
       silenceremove=start_periods=1:start_threshold=-45dB,areverse,
       loudnorm=I=-16:TP=-1.5,adelay=40"
  -ac 1 -ar 24000 -b:a 40k   →  MP3
  ```
  Quita el silencio inicial y final, normaliza el volumen a −16 LUFS y deja 40 ms de margen inicial para que el arranque no se corte.

## 3. Reproducción

- Un único reproductor reutilizado: se cambia la fuente y se reproduce; el siguiente paso se encadena al terminar (`onended`).
- **Velocidad:** se aplica como `playbackRate` (0,6–1,4×) conservando el tono (`preservesPitch`).
- **Salvaguarda:** si un clip no avisa de que ha terminado, se continúa a los `6000 / velocidad` ms.
- **Errores:** si el sistema bloquea la reproducción, se para el ciclo y se muestra un aviso para volver a tocar el botón.
- **Pantalla encendida** (Wake Lock) mientras suena.
- **Cancelación:** token por motor (ver `02`, Motores de audio).

En la app: precarga de los 112 clips, sesión de audio de tipo *playback* (que suene con el interruptor de silencio y, si se quiere, en segundo plano o con la pantalla bloqueada) y gestión de interrupciones (llamadas, otras apps).

## 4. Secuencias por pantalla

Notación: `clave` = reproducir clip · `[x s]` = pausa · `{…}` = cambio visual en ese instante. Entre clips encadenados sin pausa indicada no hay espera, salvo donde se indica 0,05 s.

### Escuchar en audio
Parámetros: `inner` (pausa entre número y carta), `gap` (pausa entre cartas), `rate`.

| Modo | Secuencia por carta |
|---|---|
| Recitar | `{carta con nº}` n{p} → [inner] → c{carta} → [gap] |
| Nº → carta | `{dorso con nº}` n{p} → [inner] → `{carta con nº}` c{carta} → [gap] |
| Carta → nº | `{carta sin nº}` c{carta} → [inner] → `{aparece el nº}` n{p} → [gap] |
| Solo números | `{dorso con nº}` n{p} → [gap] |
| Solo cartas | `{carta sin nº}` c{carta} → [gap] |

Al acabar la lista: si «Repetir al terminar», se regenera y sigue; si no, se para y el botón pasa a «▶ Otra vez».

### Cortes con voz
Parámetros: `think`, `step`, `next`, `rate`. Entre clips de una misma frase hay 0,05 s.

```
Propuesta:   c{carta}  kAla  n{D}                       «siete de diamantes, a la posición cuarenta y ocho»
[think]
{revelación visual completa}
Si D > C:    kEsta n{C}                 [step]          «está en la tres»
             n{D} minus n{C} kEs n{d}   [step]          «cuarenta y ocho menos tres es cuarenta y cinco»
             n53 minus n{d} kEs n{N}    [step]          «cincuenta y tres menos cuarenta y cinco es ocho»
             c{carta N} kEnla n{N}      [step]          «cinco de corazones, en la ocho»
             kUp                                         «cortar arriba»
Si D < C:    kEsta n{C}                 [step]
             n{C} minus n{D} kEs n{N}   [step]
             c{carta N} kEnla n{N}      [step]
             kDown
[next]  → nuevo escenario y vuelta a empezar
```
La voz dice los números, no las letras C, D y N.

### Restas con voz
Parámetros: `think`, `gap`, `rate`.
```
n{a} [0,05] minus [0,05] n{b}  → [think] → {muestra resultado} n{res} → [gap] → nueva resta
```

## 5. Ideas de mejora de audio para la app
- Volver a grabar frases enteras habituales (por ejemplo «cuarenta y ocho menos tres») para una prosodia más natural, o usar TTS en el dispositivo como alternativa configurable.
- Elegir entre voz masculina y femenina (Kokoro tiene otras voces en español).
- Respetar el audio de otras apps (atenuar o pausar).
