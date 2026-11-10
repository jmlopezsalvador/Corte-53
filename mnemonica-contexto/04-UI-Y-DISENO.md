# 04 · UI y diseño

## 1. Identidad

Mesa de juego: fondo de **tapete verde** con un leve degradado radial más claro arriba, cartas de papel marfil, acentos **dorados** y tipografía serif para títulos y números. Es un tema oscuro único; no hay modo claro.

### Tokens de color

| Token | Valor | Uso |
|---|---|---|
| `felt` | `#0f3d2e` | fondo base |
| `felt2` | `#17604a` | centro del degradado radial (50% 25%) |
| `ink` | `#f3efe3` | texto principal |
| `muted` | `#b7c8be` | texto secundario, etiquetas, ayudas |
| `gold` | `#e0b84f` | botón primario, selección de chips, números destacados, barra de progreso |
| `red` | `#c8102e` | palos rojos sobre la carta |
| `black` | `#151515` | palos negros sobre la carta |
| `paper` | `#fffdf7` | cara de la carta, segmento seleccionado |
| `ok` | `#2e9e5b` | botón «Acerté» |
| `ko` | `#c0392b` | botón «Fallé» |
| extra | `#7be0a2` | contador de aciertos, «↑ Cortar arriba» |
| extra | `#ff9a8f` | contador de fallos, palos rojos sobre fondo oscuro |
| extra | `#ffc46b` | «↓ Cortar abajo» |
| extra | `#9fd8c0` | anotaciones en cursiva (subir/bajar la carta) |
| dorso | `#8b1a2b` | dorso de la carta, con rayado diagonal blanco al 13% y filete dorado |
| badge | `#222` | círculo de posición, con borde del color del tapete |

Superficies secundarias: negro translúcido (`rgba(0,0,0,.22–.25)`) para controles segmentados, paneles de solución y pizarra de restas. Bordes de botones secundarios en blanco al 30%.

### Tipografía
- **Serif** (Georgia / Times): título «Mnemónica», índices y figuras de las cartas, números grandes (dorso, círculo del destino, pizarra de restas, fórmulas del glosario, títulos de sección).
- **Sans del sistema:** toda la interfaz.
- Etiquetas de sección en MAYÚSCULAS, 0,75 rem, espaciado 0,15 em, color `muted`.
- Cifras tabulares en contadores y números alineados.

## 2. Componentes

- **Control segmentado:** fondo translúcido, radio 12, opción seleccionada en `paper` con texto `felt`. Se usa en Orden, Qué te enseño, Cartas, modos de audio, Manual/Con voz y tipo de resta. Los modos de audio, al ser 5, ocupan dos filas.
- **Chips:** píldoras con borde blanco al 25%; seleccionado = relleno dorado con texto `felt`. Los chips de palos rojos llevan el texto en `#ff9a8f` cuando no están seleccionados.
- **Botón primario:** dorado, radio 14, alto ≈ 52 px, ancho completo. **Secundario:** transparente con borde. **Icono** (↺, ›): cuadrado de 64 px junto al primario.
- **Deslizadores:** pista dorada (`accent-color`), con la etiqueta en mayúsculas y el valor al lado en minúsculas («1,5 s», «1,0×», coma decimal española).
- **Avisos:** texto `muted` bajo los controles; se ocultan cuando no aplican.

## 3. Dibujo de la carta

Todo es vectorial y se escala con un único ancho `w`:

- Proporción **1 : 1,4**, radio 7% de `w`, sombra suave, tamaño de fuente base `w/10`.
- **Índices:** rango (1,45 em, negrita) sobre el palo (1,1 em), arriba a la izquierda, y el mismo bloque girado 180° abajo a la derecha.
- **Pips** de 2 a 10 (tamaño 2,1 em), posiciones en % del ancho y el alto; los de la mitad inferior (y > 50) van girados 180°:

```
2:  (50,20) (50,80)
3:  (50,20) (50,50) (50,80)
4:  (30,20) (70,20) (30,80) (70,80)
5:  (30,20) (70,20) (50,50) (30,80) (70,80)
6:  (30,20) (70,20) (30,50) (70,50) (30,80) (70,80)
7:  (30,20) (70,20) (50,35) (30,50) (70,50) (30,80) (70,80)
8:  (30,20) (70,20) (50,35) (30,50) (70,50) (50,65) (30,80) (70,80)
9:  (30,20) (70,20) (30,40) (70,40) (50,50) (30,60) (70,60) (30,80) (70,80)
10: (30,20) (70,20) (50,30) (30,40) (70,40) (30,60) (70,60) (50,70) (30,80) (70,80)
```
- **As:** un único palo central enorme (5,5 em).
- **Figuras (J, Q, K):** marco interior (16% vertical, 19% horizontal) con la letra grande (4,6 em) y el palo debajo, sobre un degradado dorado muy suave. Las figuras de la app de referencia del usuario llevan ilustraciones clásicas; si la app quiere ilustraciones, tienen que ser originales o de dominio público, no copiadas de una baraja comercial.
- **Círculo de posición:** círculo de `0,28·w` en la esquina superior derecha, desplazado `−0,07·w` hacia fuera, fondo `#222`, borde del color del tapete, número blanco en negrita a `0,14·w`. Al revelarse aparece con fundido y escala (0,5 → 1, curva con rebote).
- **Dorso con número:** dorso granate con filete dorado y un círculo `paper` central con el número en serif y el color del tapete.

### Tamaños
- Tarjeta de entrenamiento: `min(64vw, 250px, (alto útil − 390px)/1,4)`.
- Escuchar: `min(46vw, 180px)`.
- Cortes: carta y recuadro de destino a `min(36vw, 150px)`; miniatura de la carta a cortar de 76 px.
- Baraja completa: 4 columnas.

## 4. Movimiento
- Giro 3D de la tarjeta: `rotateY(180°)` en 0,45 s, `cubic-bezier(.3,.7,.3,1)`, con perspectiva de 1200 px. Al cambiar de carta se resetea sin animación.
- Aparición del círculo de posición: 0,25 s de opacidad y 0,3 s de escala con ligero rebote.
- Botones «Fallé/Acerté»: fundido de 0,2 s; ocupan su sitio aunque estén ocultos, para que la interfaz no salte.
- Se respeta «reducir movimiento» desactivando las transiciones.

## 5. Gestos e interacción
| Pantalla | Gesto | Acción |
|---|---|---|
| Entrenamiento | toque en la carta | revelar / ocultar |
| Entrenamiento | deslizar ← / → | siguiente / anterior |
| Cortes | toque en la propuesta | revelar (manual) |
| Cortes | deslizar ← | siguiente escenario |
| Restas | toque en la pizarra | revelar / siguiente (si no está cantando) |

Umbral de deslizamiento: 50 px y movimiento horizontal dominante (> 1,5 veces el vertical).

## 6. Disposición
Columna única de 520 px como máximo, centrada, con 16 px de margen lateral y zonas seguras respetadas. Cada pantalla tiene una barra superior de tres zonas: volver o salir a la izquierda, título o progreso en el centro y contador a la derecha. Los botones principales quedan abajo, empujados por un espaciador flexible.
