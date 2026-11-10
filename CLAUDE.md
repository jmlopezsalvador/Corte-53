# Corte 53

App móvil para **Android y iOS** con un único código en Kotlin Multiplatform + Compose Multiplatform.
El usuario habla español: responde y comenta el código en español.

> **Estado:** esqueleto KMP creado y compilando en Android (`com.corte53`); capa `domain/` + `SettingsStore` hechos con 11 tests
> en verde; las 8 pantallas hechas y probadas en el emulador (Menú, Entrenamiento+Resultado, Escuchar, Cortes, Restas, Baraja, Glosario). Es un **refactor** del entrenador web
> «Mnemónica» (un HTML de un solo archivo) a app nativa. Repo: https://github.com/jmlopezsalvador/Corte-53.git
> La fuente de verdad funcional es la carpeta **`mnemonica-contexto/`**.

## Reglas para Claude
- **Fuente de verdad funcional:** `mnemonica-contexto/` (es un refactor, no se diseña desde cero). Orden de lectura:
  `README.md` → `01-FUNCIONALIDADES.md` → `02-DOMINIO-Y-LOGICA.md` → `03-AUDIO.md` → `04-UI-Y-DISENO.md` →
  `05-GUIA-REFACTORIZACION.md`. La referencia de comportamiento viva es `mnemonica-contexto/mnemonica.html` (ábrelo en un
  navegador; **no lo leas entero**: pesa ~840 KB por los audios en base64). El código legible está en `fuente/`
  (`app.js`, `styles.css`, `markup.html`).
- Respetar **textos literales, valores por defecto y nomenclatura** (C, D, N, subir/bajar, corte arriba/abajo) de esos
  documentos. No inventar funcionalidad que no esté en ellos; las «mejoras naturales» del doc 05 §4 solo si el usuario las pide.
- Los recursos (`audio/*.mp3`: 112 clips, `datos/stack.json`, `datos/audio-manifest.json`) se copian desde
  `mnemonica-contexto/` a los recursos de la app; la carpeta `mnemonica-contexto/` se queda como referencia, no se edita.
- La base técnica y de arquitectura es **`ARQUITECTURA.md`** (sigue ese documento para montar el proyecto; es la fuente de
  verdad de versiones, estructura, Gradle, CI de iOS y reglas de código). Si algo choca con él, consultar al usuario.
- Sustituir `com.miapp` / `MiApp` de `ARQUITECTURA.md` por el paquete y nombre reales. **Provisional:** `com.corte53` /
  `Corte53` (confirmar con el usuario antes de publicar nada).
- **Antes de lanzar el workflow de iOS en GitHub Actions, preguntar siempre al usuario** (cuesta cuota de macOS; se acumulan
  mejoras). **No se lanza hasta haber validado la app en el emulador Android local.** El workflow se deja para el final.

## Qué hace la app
Entrenador de la baraja **Mnemónica de Juan Tamariz** (detalle en `mnemonica-contexto/01` y `02`). Sirve para memorizar la
relación **posición ↔ carta** de las 52 cartas y entrenar los cálculos de magia que se usan con ella. Todo funciona sin
conexión, sin cuentas; ajustes solo en local.
- **Menú:** 4 ajustes (Orden secuencial/aleatorio, Qué te enseño número/carta/mixto, Posiciones desde–hasta, Cartas todas/palo/valor)
  y 6 accesos.
- **Empezar:** tarjetas con giro 3D (Nº → carta) o aparición del número (Carta → nº), marcar acierto/fallo opcional,
  navegación libre y por deslizamiento, resultado con «Repasar fallos».
- **Escuchar en audio:** recitado con voz grabada en 5 modos (Recitar, Nº→carta, Carta→nº, Solo números, Solo cartas).
- **Entrenar cortes** («carta al número»): dada una carta y una posición, qué carta cortar y si va arriba o abajo. Manual y con voz.
- **Entrenar restas:** diferencia entre dos posiciones o complemento a 53, con voz.
- **Baraja completa** (rejilla 4 columnas) y **Glosario de fórmulas** (extensible por secciones).
- Voz con **clips mp3 propios** (el TTS del navegador no sonó en iPhone): `n1..n53`, `cXY` por carta, frases `kAla kEsta kEs kEnla kUp kDown`, `minus`.
- Dominio (`domain/`, puro y testeable): `MnemonicaStack`, `PositionFilter`, `SessionBuilder`, `CutCalculator`,
  `SubtractionGenerator`, `SpokenSequence`. Audio: `ClipPlayer` + **un único `SequenceRunner`** con cancelación estructurada
  (cada modo solo genera su lista `Clip | Pause | Visual`). Criterios de aceptación y tests exigidos en el doc 05 §5
  (incluido el test exhaustivo de cortes 52×51).
- Orden de construcción sugerido: dominio + tests + `SequenceRunner` → Menú → Entrenamiento/Resultado → Baraja → Glosario →
  Escuchar → Cortes → Restas.
- Específico de plataforma (`expect/actual`): reproductor de audio con velocidad 0,6–1,4× **con tono conservado**,
  mantener pantalla encendida mientras suena, almacenamiento de ajustes (`KeyValueStore`), barras del sistema.

## Arquitectura (resumen de `ARQUITECTURA.md`)
- **Un solo código** en `shared/commonMain` (lógica pura + interfaz Compose). Solo lo que depende del sistema va con
  `expect`/`actual` (`androidMain` / `iosMain`). Regla de oro: si no es estrictamente del sistema, va en común.
- `shared/`: módulo multiplataforma con TODO el código.
  - `commonMain/.../domain/`: lógica pura, sin UI ni Android.
  - `commonMain/.../audio/`: `SequenceRunner` y contratos del reproductor.
  - `commonMain/.../ui/`: pantallas, ajustes, `Platform.kt` (`expect`), `SettingsStore` + interfaz `KeyValueStore`.
  - `commonMain/.../App.kt`: raíz Compose común.
  - `androidMain` / `iosMain`: las `actual` (SharedPreferences / NSUserDefaults, reproductor, barras del sistema) y `MainViewController` (iOS).
  - Tests en `commonTest` con `kotlin.test` (se ejecutan en JVM; opcionalmente en el simulador de iOS).
- `app/`: solo la envoltura Android (`MainActivity`, manifiesto, recursos).
- `iosApp/`: envoltura iOS en SwiftUI + `project.yml` (XcodeGen genera el `.xcodeproj` en CI; no se versiona).
- `.github/workflows/ios.yml`: en un Mac de GitHub (`macos-15`) compila y publica un `.ipa` sin firmar para firmarlo con
  Sideloadly/AltStore. Solo se lanza a mano (`workflow_dispatch`).
- En Windows los destinos de iOS se ignoran (`kotlin.native.ignoreDisabledTargets=true`); Android compila y testea igual.

## Reglas de código para que compile en iOS
- En `commonMain`: nada de `java.*` ni `android.*`. Usar `bignum` (`com.ionspin.kotlin:bignum:0.3.10`),
  `kotlinx-datetime` (0.6.1), `kotlin.math.*`. Sin `String.format`, `Locale`, `SimpleDateFormat`, `Thread`, `synchronized`.
- Sin recursos de Android (`R.string`) en común; textos en el código.
- Iconos dibujados con `Canvas`/`Path` de Compose (no `material-icons-extended`).
- Lógica de dominio pura y sin UI, con tests en `commonTest`.
- Lo del sistema (audio, sensores, vibración, portapapeles, red…): interfaz en común + implementación por plataforma inyectada desde el punto de entrada.
- El fallo de iOS solo se ve en Actions (~20 min): revisar con cuidado antes de lanzarlo.

## Compilar y probar (Windows)
- Kotlin 2.0.21, Compose Multiplatform 1.7.1, AGP 8.7.3, Gradle 8.9, JDK 17, minSdk 26, target/compileSdk 35, iOS mínimo 15.0.
- `JAVA_HOME` apunta al JDK 17: `C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot`.
- SDK de Android en `C:\Users\jmlop\AppData\Local\Android\Sdk` (`local.properties`, ignorado por git).
- `./gradlew.bat :shared:testDebugUnitTest assembleDebug` → APK en `app/build/outputs/apk/debug/app-debug.apk` (se instala a mano).
- Emulador AVD `calcu` (Android 15), reutilizable. Notas: poner la zona horaria `Europe/Madrid` (arranca en GMT); el teclado
  Gboard del emulador puede salir en negro (es del emulador, no de la app).
- `gh` en Windows: `C:\Program Files\GitHub CLI\gh.exe` (hace falta `gh auth login` una vez).

## Flujo de trabajo
1. Desarrollar y probar en Android/JVM en local; **validar en el emulador Android** (incluido el audio) antes de pensar en iOS.
2. Commit y subida a la rama de iOS (aparte de `main`).
3. **Preguntar antes de lanzar Actions** y acumular mejoras: `gh workflow run ios.yml --ref <rama>`.
4. Descargar el `.ipa` e instalarlo con Sideloadly (Apple ID gratis; caduca a los 7 días; activar Modo desarrollador en el iPhone).

## Pendiente
- Hecho: esqueleto KMP, dominio, secuencias habladas y ajustes. Repo git local con `origin` = `Corte-53` (sin subir aún).
- Hecho: `audio/` con `SpeechEngine` (TTS nativo: `AndroidSpeechEngine`, `IosSpeechEngine` sin compilar) y `SequenceRunner` (junta clips seguidos en una frase; textos en `ClipTexts`). Los mp3 NO se usan aún: quedan como respaldo opcional.
- Pantallas en `shared/.../ui/` (`MenuScreen`, `TrainScreen`+`ResultScreen`, `VoiceScreens` = Escuchar/Cortes/Restas con `VoiceController`, `InfoScreens` = Baraja/Glosario, `Cards.kt` = cartas vectoriales, `Components.kt` = controles y colores `Pal`). Navegación simple en `App.kt` (`Dest`).
- Voz: sección «Voz» al final del menú (`VoiceSettings.kt`): automática (prefiere voz instalada, España primero) o elegir; indica si es local o necesita internet; botón «Probar voz»; `VoiceWarning` en Escuchar/Cortes/Restas si la voz es de red o no hay voz en español. `voiceId` guardado en `AppSettings`.
- Icono Android hecho (adaptive: fondo `#9F0311` + `drawable-nodpi/ic_launcher_foreground.png`, origen `data/icon.png`). Falta el de iOS (iOS se deja para más adelante).
- Repaso contra el HTML hecho: flujos probados en emulador (marcar, deslizar, resultado, repasar fallos, glosario, cortes con voz, persistencia). La voz se para al pasar la app a segundo plano (`OnAppStopped`; en iOS aún no implementado).
- iOS preparado (sin compilar nunca): icono 1024 (`iosApp/iosApp/Assets.xcassets`), sesión de audio playback en `iOSApp.swift`, `OnAppStopped` y `IosSpeechEngine` en `iosMain`, workflow `ios.yml`. Rama `ios` creada. **Compila en Actions** (ejecución 42834779948, ~15 min, .ipa de 11 MB descargado en `Downloads/Corte53-ios/`); aún sin probar en un iPhone. Lanzar de nuevo solo previa pregunta (`gh workflow run ios.yml --ref ios`). Ojo: `gradlew` debe seguir con permiso de ejecución en git (`git update-index --chmod=+x`).
- Mp3 sin usar (respaldo opcional).
- Confirmar nombre/paquete definitivos (hoy `Corte53` / `com.corte53`) y rama de iOS.
- Falta el icono de iOS (`iosApp/.../AppIcon.appiconset`, 1024×1024 sin transparencia desde `data/icon.png`).
- Validar en emulador Android; solo después, workflow de iOS (preguntar antes) y firma del `.ipa`.
