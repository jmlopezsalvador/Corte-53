# 01 · Funcionalidades

Especificación de cada pantalla tal como funciona en `mnemonica.html`. Los textos entre comillas son los literales de la interfaz (en español). Todos los ajustes se recuerdan entre sesiones (ver `02-DOMINIO-Y-LOGICA.md`, Persistencia).

Mapa de pantallas:

```
Menú principal
├── Empezar ............ Entrenamiento con tarjetas
│                         └── Resultado de sesión
├── Escuchar en audio .. Recitado por voz
├── Entrenar cortes .... Carta al número (manual / con voz)
├── Entrenar restas .... Diferencias y complemento a 53 (con voz)
├── Ver la baraja completa
└── Glosario de fórmulas
```

---

## 1. Menú principal (`#setup`)

Cabecera: título «Mnemónica», subtítulo «Entrenador · Tamariz» y los cuatro palos decorativos.

### Ajustes (afectan a Empezar, Escuchar y Cortes)

| Ajuste | Opciones | Defecto | Ayuda mostrada |
|---|---|---|---|
| **Orden** | Secuencial · Aleatorio | Secuencial | «Una detrás de otra, de la primera posición a la última.» / «Todas las del rango, barajadas y sin repetir.» |
| **Qué te enseño** | Número · Carta · Mixto | Número | «Te muestro el número y tú dices la carta.» / «Te muestro la carta y tú dices su número.» / «A veces número, a veces carta, al azar.» |
| **Posiciones** | Desplegables *desde* y *hasta* (1–52) + atajos «Todas», «1–13», «14–26», «27–39», «40–52» | 1–52 | Si *desde* > *hasta* se intercambian. El atajo activo se resalta. |
| **Cartas** | Todas · Un palo · Un valor | Todas | Con «Un palo» aparecen 4 chips (♠ Picas, ♥ Corazones, ♣ Tréboles, ♦ Diamantes). Con «Un valor», 13 chips (A, 2…10, J, Q, K). Solo se ve la fila del filtro activo. |

Bajo el filtro se lista qué posiciones entran: «Posiciones: 14, 22, 29, 33.» Con «Todas»: «Todas las cartas del rango.» Si no queda ninguna: «Ninguna carta con ese filtro en el rango elegido.»

El rango y el filtro se combinan (intersección). Ejemplo: rango 1–13 + solo picas = posiciones 7, 9, 10.

### Botones

1. **«Empezar · N cartas»** (primario). Si N = 0, dice «Sin cartas que entrenar» y no hace nada.
2. **«Escuchar en audio»**
3. **«Entrenar cortes»**
4. **«Entrenar restas»**
5. **«Ver la baraja completa»**
6. **«Glosario de fórmulas»**

---

## 2. Entrenamiento con tarjetas (`#train`)

### Lista de la sesión
- Las posiciones que pasan rango + filtro. En aleatorio, barajadas sin repetición (Fisher-Yates).
- En modo **Mixto**, cada carta de la lista sortea al empezar la sesión (50 %) si se pregunta por número o por carta.

### Barra superior
«✕ Salir» (vuelve al menú sin resumen) · «i / n» · contadores «✓ aciertos» y «✗ fallos». Debajo, una barra de progreso con ancho i/n.

### Tarjeta
- **Número → carta:** se ve el **dorso** con la posición en un círculo grande. Al tocar, la tarjeta **gira en 3D** (0,45 s) y muestra la **carta grande** con la posición en un círculo negro en su esquina superior derecha.
- **Carta → número:** se ve la **carta grande sin número**. Al tocar, la carta **no gira**: aparece el círculo con la posición (fundido + escala).
- Tocar otra vez alterna entre ocultar y mostrar.

Texto de ayuda: «¿Qué carta está en esta posición?» / «¿En qué posición está esta carta?». Tras revelar: «¿Lo sabías?». Si la carta ya estaba marcada se añade «· marcada ✓» o «· marcada ✗».

### Marcar (opcional)
Tras la primera revelación aparecen **«✗ Fallé»** y **«✓ Acerté»**. Marcar guarda el resultado de esa carta y avanza. Si vuelves a una carta marcada, el botón elegido se resalta y puedes cambiar la marca.

### Navegación libre
- **«‹ Anterior»** (desactivado en la primera) y **«Siguiente ›»** (en la última pasa a **«Terminar ›»**). No exigen marcar.
- **Deslizar** sobre la tarjeta: izquierda = siguiente, derecha = anterior. Umbral 50 px y horizontal dominante (|dx| > 1,5·|dy|). Tras un deslizamiento se ignora el toque durante 500 ms para que no gire la carta.
- Teclado (escritorio): espacio/Enter gira, ← → navegan, A = acierto, F = fallo.

### Resultado (`#end`)
- Si se marcó alguna: «aciertos / marcadas», «X% de aciertos · m:ss min», y «· N sin marcar» si las hay.
- Si no se marcó ninguna: «N cartas» y «Recorrido sin marcar · m:ss min».
- «Para repasar»: chips con las falladas, ordenadas por posición (número + carta corta, rojo para ♥♦). Si no hubo fallos (y se marcó algo): «¡Sin fallos! 🎩».
- Botones: **«Repasar fallos»** (solo si hay fallos; nueva sesión con esas posiciones, barajadas si el orden es aleatorio), **«Repetir sesión»**, **«Menú»**.

---

## 3. Escuchar en audio (`#audio`)

Usa el rango, el orden y el filtro del menú. La información se muestra bajo la carta: «Posiciones 1–52 · secuencial · solo picas».

### Controles
- **«▶ Reproducir»** / **«❚❚ Pausa»** / **«▶ Continuar»** / al terminar **«▶ Otra vez»**.
- **↺** vuelve al principio (regenera la lista).
- **Modo** (5 opciones, ver tabla).
- **Pausa entre número y carta:** 0–5 s, paso 0,1, defecto 0,5. Se oculta en «Solo números» y «Solo cartas».
- **Pausa entre cartas:** 0,5–6 s, paso 0,5, defecto 1,5.
- **Velocidad de la voz:** 0,6–1,4×, paso 0,1, defecto 1,0.
- **«Repetir al terminar»** (defecto no). Al repetir se regenera la lista, con nuevo barajado si es aleatorio.
- **«Mostrar la carta mientras suena»** (defecto sí). Si se desmarca, se oculta la zona visual.

| Modo | Etiqueta | Ayuda | Pantalla |
|---|---|---|---|
| `say` | Recitar | «Dice el número y la carta seguidos: «uno, cuatro de tréboles».» | Carta con número |
| `n2c` | Nº → carta | «Dice el número y, tras la pausa, la carta.» | Dorso con número → carta con número |
| `c2n` | Carta → nº | «Dice la carta y, tras la pausa, el número.» | Carta sin número → aparece el número |
| `onum` | Solo números | «Lee solo los números. Tú dices la carta en la pausa.» | Dorso con número |
| `ocard` | Solo cartas | «Lee solo las cartas. Tú dices el número en la pausa.» | Carta sin número |

La pantalla se mantiene encendida mientras suena (Wake Lock). Las secuencias exactas están en `03-AUDIO.md`.

---

## 4. Entrenar cortes (`#cuts`)

Técnica «carta al número»: te dan una carta y una posición y tienes que calcular el corte que deja esa carta en esa posición.

### Escenario
- La carta sale al azar de las posiciones que pasan rango + filtro del menú (si no queda ninguna, de las 52).
- La posición de destino sale al azar entre 1 y 52, distinta de la actual.
- Contador «Nº n» arriba a la derecha (se reinicia al entrar).
- Visual: carta grande **sin su número** → flecha → recuadro discontinuo «POSICIÓN» con el destino en grande.
- Pregunta: «¿Qué carta cortas y adónde la llevas?».

### Revelación
- Aparece el círculo con la posición actual sobre la carta.
- Panel con los pasos (formato exacto en `02-DOMINIO-Y-LOGICA.md`):
  - El 7♦ está en la 3: **C = 3**.
  - Destino **D = 48**. D > C: *tienes que bajar la carta*.
  - D − C = 48 − 3 = **45**.
  - N = 53 − (D − C) = 53 − 45 = **8**.
  - En N = 8 está el 5♥.
- Resultado: miniatura de la carta a cortar con su posición, **«↑ Cortar arriba»** (verde) o **«↓ Cortar abajo»** (naranja) y la nota «El 5 de corazones queda arriba del todo (pasas 7 cartas de arriba abajo).».

### Modo (selector dentro de la página)
- **Manual** (defecto): botón «Revelar» → «Siguiente ›». Tocar la carta también revela. Deslizar a la izquierda = siguiente.
- **Con voz:** botón «▶ Cantar» / «❚❚ Pausa» y botón **›** para saltar a otro escenario. Ciclo automático infinito: propuesta hablada → pausa para pensar → revelación (visual + hablada paso a paso) → pausa → siguiente.
  - **Pausa para pensar:** 0–15 s, paso 0,5, defecto 5.
  - **Pausa entre pasos de la explicación:** 0–4 s, paso 0,25, defecto 0,75.
  - **Pausa hasta el siguiente:** 0,5–8 s, paso 0,5, defecto 2.
  - **Velocidad de la voz:** 0,6–1,4×, defecto 1.
  - Los controles y el botón › solo se ven en «Con voz». Cambiar de modo detiene el audio.

No hay acierto ni fallo en esta pantalla.

---

## 5. Entrenar restas (`#subs`)

- **Tipo** (selector dentro de la página):
  - **Entre dos números** (defecto): dos números distintos al azar entre 1 y 52; se muestra siempre mayor − menor (resultado 1–51).
  - **Complemento a 53:** 53 − x, con x al azar entre 1 y 52 (resultado 1–52).
- Pizarra grande: «35 − 12 = ?». El «?» va en gris y el resultado aparece en dorado.
- **Manual** (sin reproducir): tocar la pizarra revela el resultado («Toca para ver el resultado»); tocar otra vez da la siguiente («Toca para la siguiente»).
- **«▶ Cantar»**: ciclo infinito. Dice «treinta y cinco menos doce», espera, muestra y dice el resultado, espera y pasa a otra. **«❚❚ Pausa»** lo detiene.
- **›** salta a otra resta (y sigue cantando si estaba cantando). Cambiar el tipo hace lo mismo.
- **Pausa hasta el resultado:** 0–8 s, paso 0,5, defecto 3.
- **Pausa hasta la siguiente:** 0,5–6 s, paso 0,5, defecto 1,5.
- **Velocidad de la voz:** 0,6–1,4×, defecto 1.
- Contador «Nº n».

---

## 6. Ver la baraja completa (`#deck`)

Rejilla de 4 columnas con las 52 cartas en orden y la posición en un círculo negro en la esquina superior derecha de cada una (mismo estilo que la revelación).

---

## 7. Glosario de fórmulas (`#gloss`)

Pantalla de lectura, pensada para crecer por **secciones**. Hoy tiene una: **«Carta al número»**.

- Intro: te piden una carta y una posición y con un solo corte tienes que dejarla ahí. C = posición actual, D = destino, N = posición de la carta de referencia para cortar.
- **Destino anterior a la carta** · etiqueta «D < C · corte abajo» · anotación *(cuando tienes que subir la carta)*
  - Fórmula **N = C − D**. «Resta directa: corta dejando **abajo del todo** la carta que está en la posición N.»
  - Ejemplo: 9♦ (C = 52) a la D = 10. N = 52 − 10 = 42 → en la 42 está el 4♦ → corta dejando el 4♦ abajo.
- **Destino posterior a la carta** · etiqueta «D > C · corte arriba» · anotación *(cuando tienes que bajar la carta)*
  - Fórmula **N = 53 − (D − C)**. «Resta la diferencia y saca su complemento a 53: corta dejando **arriba del todo** la carta que está en la posición N.»
  - Ejemplo: 7♦ (C = 3) a la D = 10. N = 53 − (10 − 3) = 46 → en la 46 está la Q♦ → corta dejando la Q♦ arriba.
- Nota: «Si D = C la carta ya está en su sitio: no hay que cortar.»

La nomenclatura (C, D, N, subir/bajar, corte arriba/abajo) tiene que ser **idéntica** en el glosario y en el entrenamiento de cortes.
