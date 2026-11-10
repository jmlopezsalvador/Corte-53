# Entrenador Mnemónica · paquete de contexto

Este paquete documenta el entrenador web de la baraja **Mnemónica de Juan Tamariz** para llevarlo a una app móvil nativa. Contiene la herramienta original funcionando y las especificaciones para rehacerla sin perder comportamiento.

## Qué es la herramienta

Una app de un solo archivo HTML, sin servidor ni dependencias, para memorizar la relación **posición ↔ carta** de las 52 cartas del orden Mnemónica y entrenar los cálculos que se usan con él en magia:

- **Entrenamiento con tarjetas:** número → carta, carta → número o mixto; secuencial o aleatorio; filtrable por rango, palo o valor.
- **Escuchar en audio:** recitado con voz grabada en varios modos.
- **Entrenar cortes:** dada una carta y una posición, qué carta hay que cortar y si va arriba o abajo. Modo manual y modo con voz.
- **Entrenar restas:** diferencia entre dos posiciones o complemento a 53, con voz.
- **Baraja completa** y **Glosario de fórmulas** como consulta.

## Cómo usar este paquete

1. Abre `mnemonica.html` en un navegador para ver la referencia viva. Es la fuente de verdad del comportamiento.
2. Lee los documentos en orden. Están escritos como especificación para implementar.
3. Lee el código en `fuente/` (JS, CSS y marcado separados y sin los audios en base64), no en `mnemonica.html`, que pesa ~840 KB por los audios incrustados.
4. Usa `audio/` y `datos/` directamente como recursos de la app.

## Contenido

| Ruta | Qué es |
|---|---|
| `mnemonica.html` | La herramienta completa (audios incrustados). Referencia de comportamiento. |
| `01-FUNCIONALIDADES.md` | Especificación pantalla por pantalla: controles, flujos, textos, valores por defecto. |
| `02-DOMINIO-Y-LOGICA.md` | El orden Mnemónica, codificación de cartas, filtros, fórmulas de corte, restas, estado y persistencia. |
| `03-AUDIO.md` | Inventario de clips, secuencias de locución de cada modo, temporizaciones y cómo se generaron. |
| `04-UI-Y-DISENO.md` | Identidad visual, tokens de color y tipografía, dibujo de cartas, gestos y animaciones. |
| `05-GUIA-REFACTORIZACION.md` | Arquitectura propuesta, correspondencia web → móvil, lecciones aprendidas y criterios de aceptación. |
| `fuente/app.js` | Toda la lógica (≈570 líneas). |
| `fuente/styles.css` | Estilos. |
| `fuente/markup.html` | Marcado de todas las pantallas. |
| `audio/*.mp3` | 112 locuciones en español (52 números + 53, 52 cartas, 7 frases). |
| `datos/stack.json` | El orden completo con posición, código, nombre y símbolo. |
| `datos/audio-manifest.json` | Clave de cada clip → texto que dice. |

## Glosario rápido

- **Posición:** 1 a 52, contando desde la carta superior del mazo boca abajo.
- **C, D, N:** en los cortes, C es la posición actual de la carta, D la de destino y N la de la carta de referencia para cortar.
- **Complemento a 53:** 53 − x.
- **Código de carta:** rango + palo. Rangos `A 2…9 T J Q K` (T = 10). Palos `C` tréboles, `H` corazones, `D` diamantes, `P` picas.
