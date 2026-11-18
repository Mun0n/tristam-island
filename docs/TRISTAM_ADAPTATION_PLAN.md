# Plan de adaptación de Tristam Island

Este documento fija la arquitectura de la aplicación independiente y el orden en que se podrá cambiar el código copiado de Valdeumbría. Esta fase crea el proyecto, comprueba que la copia sigue compilando y no borra subsistemas.

## Requisito innegociable

La aplicación es local, funciona sin conexión y no usa inteligencia artificial.

Tristam Island no utilizará inteligencia artificial para generar, ampliar, resumir, traducir ni interpretar la historia durante la partida. La historia ya existe en la obra de Hugo Labrande. Se transforma antes, a mano, en contenido local estructurado. Todo lo necesario para jugar viaja dentro de la aplicación.

El diseño final no incluye `AdventureBackend`, `HttpAdventureBackend`, narradores remotos, Groq ni ninguna dependencia de un modelo de lenguaje. No hay entrada de texto libre ni una opción del tipo «Intentar otra cosa». No se generan descripciones de escena en tiempo de ejecución. Cada localización tiene textos escritos y revisados de antemano. Cada acción contextual trae en los datos del juego sus requisitos, sus efectos, sus cambios de estado y su texto de respuesta. Los puzles se resuelven con reglas, inventario, banderas, objetos, estados del mundo y eventos. El mismo estado y la misma acción producen el mismo resultado, salvo una mecánica aleatoria del original que se decida conservar de forma explícita. El guardado persiste el estado del mundo, no textos generados.

No hay un diseño alternativo con IA. Los archivos de narración remota siguen hoy en el árbol porque esta fase no borra subsistemas. Dejan de formar parte del camino del jugador en la fase que los retira.

## Estado

Este directorio es solo Tristam Island. El 18 de noviembre de 2026 salieron de aquí Valdeumbría, el puente, el dado, el narrador y `backend/`. Ese código está en el repositorio `dungeon-master-ai`. El dado de cristal está en su rama `feature/cinematic-d20`.

El proyecto vive en `/Users/mun0n/tristam-island-android`, en la rama `feature/tristam-foundation`. El nombre visible es `tristam-island`. En el teléfono la aplicación se llama Tristam Island. El `applicationId` y el paquete Kotlin son `com.tristamisland`.

La partida que se abre recorre la costa, el pueblo, la mansión y el laboratorio hasta el rescate. No hay reloj, vida, dado, ficha de personaje, texto libre ni narrador. El motor que resuelve esa partida es `IslandEngine`.

Las fases de abajo describen la copia inicial, cuando el código del otro juego todavía estaba en este árbol. No vuelvas a traerlo.

## Qué se reutiliza de verdad

La matriz usa tres verbos.

- **Reutilizar**: el comportamiento actual sirve para Tristam Island sin convertir un rasgo de Valdeumbría en una obligación.
- **Adaptar**: la pieza se queda, pero hay que separar de ella un reloj, una muerte, un dado, un personaje o una llamada de red.
- **Retirar**: no entra en el diseño final. Sigue en el árbol hasta la fase que lo quite.

### Dominio de aventura

| Pieza | Archivos | Decisión |
| --- | --- | --- |
| Carga del documento y rechazo de campos desconocidos | `domain/adventure/load/AdventureLoader.kt` | Reutilizar |
| Identificadores únicos, referencias rotas y forma del documento | `domain/adventure/load/AdventureValidator.kt` | Adaptar: un documento sin reloj, sin muerte y sin atributos debe ser válido |
| Efectos de bandera, objeto, lugar, pista, movimiento y evento | `SET_FLAG`, `ADD_ITEM`, `REMOVE_ITEM`, `DISCOVER_LOCATION`, `MOVE_TO`, `ADD_CLUE`, `TRIGGER_EVENT`, `RECORD_ATTEMPT` en `AdventureEffects.kt` | Reutilizar |
| Efectos de vida, relación, prueba, estado de personaje y final | `CHANGE_LIFE`, `SET_RELATIONSHIP`, `ADD_EVIDENCE`, `SET_NPC_STATE`, `SET_ENDING` | Adaptar: siguen existiendo para un documento que los use; Tristam Island no los emite |
| Requisitos de bandera, objeto, pista y lugar descubierto | `AdventureRequirements.kt` | Reutilizar esos campos |
| Requisitos de relación, estado de personaje, minutos y medianoche | Los mismos `Requirement` | Adaptar: pasan a ser de un sistema opcional. Tristam Island no los rellena |
| Localización, objeto, bandera, pista, acción, evento y prólogo | `AdventureModels.kt` | Adaptar: la acción necesita texto visible y categoría; la localización necesita descripción condicional |
| Reloj, chequeo, deducción y contrato de escena para un modelo | `NarrativeClock`, `CheckDefinition`, `Deduction`, `SceneContract` | Retirar del diseño de Tristam. El contrato de escena existe para limitar lo que ve un modelo |
| Estado de partida | `state/AdventureState.kt` | Adaptar. Hoy exige `Character`, minutos, medianoche, relaciones, pruebas y deducciones |
| Motor de resolución | `engine/AdventureEngine.kt` | Adaptar. El orden de efectos se conserva. La tirada, el coste de tiempo y la medianoche dejan de ser obligatorios |
| Prólogo como pasajes escritos, sin dado y sin tiempo | `engine/Prologue.kt` | Adaptar para la introducción del accidente |
| Oferta de decisiones y acceso a una acción | `engine/DecisionOffer.kt`, `ActionAccess` | Adaptar: la oferta sale de las acciones cuyo requisito se cumple, sin tope de cuatro ni texto libre |
| Deducción | `engine/ConclusionCheck.kt`, `AdventureEngine.deduce` | Retirar |
| Vigilancia del texto generado | `narration/CanonGuard.kt`, `NarrationView.kt`, `TechnicalIdentifiers.kt` | Retirar. No hay texto que vigilar si nadie lo genera |
| Números fijos de Valdeumbría | `AdventureRules.kt`: hora 22, 120 minutos, dificultades 8/12/16/20 | Retirar como obligación. El patrón de identificador se reutiliza |

`AdventureEngine.resolve` hoy hace cuatro cosas que Tristam Island no puede heredar como obligación: pide un `Character` con vida para `newGame`, tira un `Die` cuando la acción tiene `check`, suma `timeCostMinutes` y dispara el evento de medianoche al cruzar 120 minutos. El resultado de los efectos, una vez decidido el éxito, es determinista. Esa parte es la que se reutiliza.

### Persistencia

| Pieza | Archivos | Decisión |
| --- | --- | --- |
| Una instantánea confirmada y un número de revisión | `AdventureGameStore`, `committedRevision`, `revisionToken` | Reutilizar |
| El cerrojo que une la comprobación de revisión y la resolución | `RoomAdventureGameStore.commitIfCurrent` | Reutilizar |
| Transacción que sustituye la ranura entera | `AdventureGameDao`, `RoomAdventureGameStore.write` | Adaptar al estado nuevo |
| Filas de narración pendiente, prosa generada e interacciones | `AdventurePendingNarrationEntity`, `AdventureSceneProseEntity`, `AdventureInteractionEntity` | Retirar. El guardado no persiste texto generado |
| Reloj, muerte, relaciones, pruebas, hipótesis y deducciones en tablas | Entidades correspondientes en `AppDatabase` versión 6 | Adaptar o retirar según el sistema opcional |
| Partida del puente | `SavedGameStore`, `SavedGameDao`, `SavedGameEntity` | Retirar del producto. No es el motor de la isla |

La base nueva pertenece a esta aplicación. No hace falta migrar las partidas guardadas de Valdeumbría en el teléfono, porque el identificador de aplicación cambiará y cada app tendrá su propio fichero. Las pruebas de migración del puente se conservan hasta que ese módulo se retire, para no dejar la copia rota.

### Sesión

`AdventureSession` hace dos trabajos en la misma clase.

El que se conserva es el cerrojo `gate`, una sola instantánea y la llamada a `commitIfCurrent`. `choose(actionId)` ya resuelve una acción conocida. `continuePrologue()` ya avanza un pasaje escrito sin tirar el dado. `restore()` ya vuelve a mostrar lo guardado sin volver a resolver.

El que se retira es todo lo que habla con `AdventureBackend`: `submitText`, `retryNarration`, la fase `NARRATION_PENDING`, la fase `SCENE_PAUSED` cuando espera prosa generada, y `noteHypothesis`. `start(character)` se adapta para abrir una partida sin clase, sin atributos y sin vida. El constructor deja de recibir `AdventureBackend`.

Hasta ese corte, la clase se queda como está. No se llama al backend desde el contenido de Tristam Island. `local.properties` de esta copia solo contiene `sdk.dir`. No define `adventure.backendUrl` ni `narrator.baseUrl`, así que el cliente HTTP queda ausente aunque la clase siga compilando.

### Interfaz

| Pieza | Archivos | Decisión |
| --- | --- | --- |
| Menú, continuar, nueva partida y confirmación antes de borrar | `GamePicker.kt`, `MenuCatalog.kt` | Adaptar: el flujo se queda; el arte, la palabra marca y el laboratorio de dados se retiran |
| Cabecera, ilustración, texto, resultado y filas de acción | `SceneHeader`, `LocationPicture`, `ChronicleCard`, `DecisionRow` en `ValdumbriaScreen.kt` | Adaptar la estructura. La pantalla deja de conocer la vida, el reloj y la tirada |
| Barra inferior de escena, diario, inventario y estado | `PlayBar` | Reutilizar la estructura con páginas de la isla |
| Inventario | `InventorySheet`, `inventorySlots` en `SceneReading.kt` | Adaptar: objetos transportados y objetos usables, sin las seis plazas ni el monedero de Valdeumbría |
| Diario | Secciones de pista, lugar e historial reciente | Adaptar a descubrimientos, pistas y objetivo. Se retiran hipótesis, conclusiones y pruebas |
| Estado | `StatusSheet`, `ValdumbriaStatus.kt` | Adaptar a acto, puntuación, progreso y opciones. Se retiran atributos, vida y reloj de Inés |
| Movimiento reducido y altura de pulsación | `reducedMotion()`, filas de 48 dp, `semantics` | Reutilizar |
| Tema, tipografía y formas | `presentation/theme/Theme.kt`, `res/font/` | Adaptar: se puede conservar la carga de fuentes. La paleta gótica y el contenido visual se sustituyen |
| Música y ciclo de vida | `BackgroundMusic.kt`, `MainActivity.onStart` / `onStop` | El ciclo se reutiliza. El menú usa `island_on_the_horizon.mp3`. Cada acto tiene su pista: `lower_coast.mp3` en `lower_island`, `plateau.mp3` en `plateau`, `mansion.mp3` en `mansion` y `laboratory.mp3` en `laboratory` |
| Vibración | `HapticDiceFeedback.kt` | Retirar con el dado. No hay otra vibración de juego |
| Compositor de texto libre | `ActionComposer`, `HypothesisComposer` | Retirar |

No hay Navigation de Jetpack. El menú cambia de modo con estado de Compose. Esa forma se conserva.

### Compilación y pruebas

Se reutilizan el wrapper, `gradle/libs.versions.toml`, el módulo único `:app`, JDK 17, `minSdk` 26 y `compileSdk` 37. Filament y los `BuildConfig` de URLs salieron con el dado y el backend.

`DomainBoundaryTest` se reutiliza: `domain` no importa Android ni Compose.

Las pruebas de motor, cargador, validador, dados con `ScriptedDie`, almacén y sesión se ejecutan mientras el código copiado siga ahí. Cuando un sistema deje de ser obligatorio, la prueba que lo exigía se cambia en el mismo corte. Las pruebas `*Live*` ya se saltan salvo `ADVENTURE_LIVE_LLM=1`. No forman parte de la comprobación de esta aplicación y no se vuelven a activar.

## Obligaciones de Valdeumbría que Tristam Island no hereda

Estas piezas están acopladas al otro juego. Un documento de la isla no debe rellenarlas con una hora falsa, un final de muerte falso ni unos atributos falsos.

| Obligación actual | Dónde se impone | Destino |
| --- | --- | --- |
| Reloj de las 22:00 a medianoche | `AdventureRules.START_HOUR`, `NarrativeClock`, `minutesElapsed` | Sistema opcional. La isla no lo declara |
| Evento de medianoche | `clock.midnightEventId`, el motor al cruzar 120 minutos | Sistema opcional. La isla no lo declara |
| Salud, muerte y final de muerte | `Character.health`, `deathEndingId`, `CHANGE_LIFE` | Sistema opcional. La isla no lo declara |
| Clases, atributos y creación de personaje | `CharacterModels.kt`, `ValdumbriaCreation` | Se retira del camino del jugador |
| Tirada d20, dificultad y modificador | `Dice.kt`, `CheckDefinition`, Filament, `DiceRollOverlay` | Sistema opcional. La isla no declara `check` |
| Narración por IA o backend | `AdventureBackend`, `HttpAdventureBackend`, `backend/` | Se retira |
| Narrador del puente | `Narrator`, `HttpNarrator` | Se retira |
| Entrada libre interpretada | `submitText`, `SceneContract.freeAction` | Se retira. No queda un sustituto |
| Deducciones, relaciones y pruebas detectivescas | `Deduction`, `relationships`, `evidence` | Se retiran salvo que un puzle concreto de la isla necesite una pista escrita, que entonces es una entrada de diario |
| Historia, nombres, ilustraciones y música | `valdumbria.json`, `res/drawable-nodpi/`, `PlayerCopy`, `b_minor_arpeggio.mp3` | Se retiran del producto. El puente `GuardiansBridge` también |

## Esquema versión 2

El esquema 1 sigue siendo el de Valdeumbría y sigue cargando `assets/adventures/valdumbria.json`. No se rompe para hacer sitio a la isla.

El esquema 2 añade sistemas opcionales. Un bloque ausente significa que esa regla no existe. No se sustituye por un valor centinela.

| Sistema | Si el documento lo trae | Si no lo trae |
| --- | --- | --- |
| `clock` | Hora inicial, coste de tiempo y, si se nombra, un evento al cruzar un umbral | No hay minutos ni evento horario. El motor no avanza un reloj |
| `vitality` | Vida, cambio de vida y un final cuando la vida llega a cero | No hay vida, ni muerte, ni `deathEndingId` |
| `checks` | Atributo, dificultad y un dado inyectado | Ninguna acción tira. El motor de la isla no recibe un `Die` |
| `cast` | Clase, atributos y creación | La partida empieza en la introducción, sin ficha |
| `narration` | No existe en la versión 2 | El texto de la acción es `response`, escrito en el documento. El texto del lugar es `description` |

Lo que la versión 2 exige de un documento de Tristam Island:

- `schemaVersion` 2, `id`, `title` y `startLocationId`.
- Localizaciones con nombre, acto y una o varias descripciones según el estado.
- Acciones contextuales con identificador, texto visible en español, categoría, requisitos, efectos y texto de respuesta. Si mueven, llevan destino.
- Inventario y padre de cada objeto. Un objeto puede estar en el suelo, en la mano, dentro de otro o ausente.
- Banderas y contadores.
- Eventos que se disparan una vez, entre ellos el paso de acto.
- Puzles con una o varias acciones que conceden la misma resolución.
- Diario: entradas que aparecen cuando se cumple un requisito.
- Puntuación, con el máximo 150 del original, como dato del documento y no como una constante de otro juego.

Una acción puede nombrar `equivalentActionIds` cuando el ascensor y la escalera, o las dos entradas de la valla, resuelven el mismo paso. El validador comprueba que cada alternativa siga existiendo y que sus efectos concedan el mismo estado de puzle.

Las categorías visibles son examinar, recoger, utilizar, combinar, hablar o llamar, moverse y acción de puzle. Cada lugar muestra solo las acciones cuyos requisitos se cumplen. No se muestra la lista de verbos del parser.

## Cómo se representa la isla

No se integra un intérprete Z-machine. No se traduce solo el Inform a Kotlin. `tristam-en.inf` es la referencia de diseño, igual que los mapas, el diagrama de dependencias y el walkthrough.

Una extracción mecánica puede listar las 54 localizaciones de clase, las salidas y los 186 `Object`. Las rutinas `before` se anotan y se reescriben como acciones. El océano, `OnlyRoomWaistDeep`, es una localización con rumbo y profundidad, no ocho salas.

El estado de partida guarda el lugar, los lugares visitados, el inventario, el padre y los atributos de cada objeto, las banderas, los contadores, los puzles resueltos, el acto, la puntuación, las entradas de diario ya descubiertas y la lista de acciones resueltas. Restaurar ese estado no vuelve a aplicar efectos.

El original limita el transporte a tres objetos más la mochila (`MAX_CARRIED` y `SACK_OBJECT`). El corte de la playa debe poder expresar esa capacidad, porque coger la flor ya la consulta.

El fuente usa `random` en descripciones de color: el tamaño del pez, el gesto del albatros, los rebotes de un guijarro, una canción o un libro. Esas frases se fijan en el texto español escrito de antemano. El selector de canciones de la gramola (`random(20)` y `RandomSong`) se revisa cuando llegue ese puzle. Si la canción concreta no cambia el estado, se elige un texto. Si lo cambia, la elección pasa a ser una acción contextual. No se conserva un dado por costumbre.

## Arquitectura final

Un módulo `:app`. Tres paquetes.

`domain` carga el documento, lo valida y resuelve una acción. No importa Android. No importa un cliente HTTP. No recibe un dado cuando el documento no tiene chequeos.

`data` guarda una ranura con revisión. `commitIfCurrent` sigue siendo el único sitio donde la resolución y la escritura comparten cerrojo. No hay narración pendiente.

`presentation` dibuja el menú y la partida. Manda el identificador de una acción visible. No redacta el lugar ni interpreta una frase.

El contenido de la isla vive en `assets/island/`. Un script de apoyo puede vivir en `tools/extract/` y no entra en el APK. No hay módulo `backend`. El permiso `INTERNET` se quita al retirar el cliente HTTP.

La pantalla queda en este orden: cabecera y localización, ilustración propia, descripción escrita, resultado de la acción anterior, acciones contextuales y barra inferior. El diario, el inventario y el estado leen el mismo `IslandState` que el guardado.

La página Progreso dibuja el mapa del sitio en el que estás. Al aire libre es la costa y el pueblo. Dentro de la mansión, o dentro del laboratorio, ese mapa se quita y empieza el plano de ese interior. No nombra un lugar antes de pisarlo. Pulsar un lugar conocido lleva allí. El océano es un solo punto del mapa de fuera, colocado mar adentro de la orilla en la que estás, y pulsarlo desde una orilla mete en el agua.

## Migración por fases

Cada fase sale de `main` cuando `main` exista como línea jugable de esta aplicación, con el nombre `feature/<nombre>`, y se fusiona solo después de sus pruebas. Esta fase está en `feature/tristam-foundation` y aún no se fusiona: espera revisión. No hay force-push. No hay remoto hasta que se pida.

### Fase 0. Esta copia

Proyecto independiente, este plan y `docs/TRISTAM_LICENSES.md`. Sin conversión y sin borrados.

Pruebas: `./gradlew :app:testDebugUnitTest` y `./gradlew :app:assembleDebug` sobre el código copiado. El resultado está al final.

### Fase 1. Identidad y esquema opcional

Cambiar `applicationId`, nombre visible y, si hace falta, el paquete, sin tocar Valdeumbría. Introducir el esquema 2 al lado del 1. Un documento sin `clock`, sin `vitality` y sin `checks` carga y se valida. `valdumbria.json` sigue cargando como versión 1.

Pruebas: las del cargador y del validador para ambos esquemas. Una partida de la versión 1 sigue resolviendo como ahora. Una partida mínima de la versión 2 resuelve un movimiento y un objeto sin crear un personaje, sin avanzar minutos y sin tirar.

### Fase 2. Sesión sin narración

`AdventureSession` se queda con el cerrojo, `choose`, el prólogo y `restore`. Se le quita `AdventureBackend`. El texto mostrado es el `response` guardado en el documento. El almacén deja de escribir prosa generada.

Pruebas: dos turnos concurrentes sobre la misma revisión, y solo uno escribe. Restaurar no vuelve a aplicar el efecto. Una acción rechazada no cambia el estado. No queda ninguna llamada a `interpret`, `narrate` o `scene` en el camino del jugador.

### Fase 3. Corte vertical de la playa

Contenido escrito en español para la introducción del accidente, `Immaculatebeach`, `Beachnearforest` y `Edgeofthewoods`. La cerilla empieza en el inventario. La flor se puede coger si hay capacidad. El diario registra ese descubrimiento. Oeste y norte cambian de lugar. El guardado restaura el punto exacto. Ilustración de reserva, sin portada ajena.

Pruebas de principio a fin sobre ese documento: el camino entre las tres playas, la flor, la capacidad llena, el diario y un ciclo de guardar y restaurar. El validador del corte no admite un destino roto, un identificador duplicado ni una acción sin respuesta.

### Fase 4. Retirar lo que la isla no usa

Quitar del camino del jugador el backend, el narrador, el texto libre, el dado, Filament, la creación de personaje, el reloj obligatorio, la vida y el puente. Quitar el permiso de red y la pista de música de Valdeumbría. Sustituir la paleta y las ilustraciones.

Pruebas: la suite que siga teniendo sentido, más el corte de la playa. `domain` sigue sin Android. Una búsqueda en el código del jugador no encuentra `AdventureBackend`, `HttpNarrator`, Groq ni `submitText`.

### Fase 5. El resto de la obra, por actos

Primero el grafo y el diagrama de puzles, contrastados con el walkthrough. El del acto 1 está en [ACT_1_MAP.md](ACT_1_MAP.md). Después el acto 1 completo, luego el poblado, la mansión y el laboratorio. Cada acto se traduce y se prueba antes de abrir el siguiente.

Pruebas: una ruta del walkthrough por tramo, las alternativas del ascensor y la escalera, las dos entradas de la valla y las dos vías del código. Al final, una ruta hasta el rescate. Los validadores añaden objetos imposibles de obtener, salas inalcanzables, puzles cuya solución desaparece y cadenas sin traducción.

## Corte vertical recomendado

La introducción es el texto que `Initialise` imprime antes de la primera mirada: la avioneta, la tormenta y el paracaídas. Las salas son la playa inmaculada, la playa junto al bosque y el linde del bosque. El objeto que ya está en la mano es la cerilla. El objeto que se recoge es la flor del linde. La bandera es haberla cogido. El diario escribe ese hallazgo. El movimiento es oeste y después norte.

Ese corte no adapta el acto 1 entero. No incluye el océano, el gusano ni el anzuelo. Sirve para ver el motor, el español, la interfaz y el guardado juntos.

## Riesgos

El motor actual parece genérico y no lo es del todo. Si se le pasa a la isla un reloj o una muerte de relleno, el validador estará contento y la partida mentirá. El esquema 2 tiene que tratar la ausencia como ausencia.

`AdventureSession` mezcla el cerrojo con el protocolo HTTP. Separarlos es el cambio delicado. Hay que hacerlo con las pruebas de revisión ya escritas, sin aprovechar para reescribir el cerrojo.

El Inform no es un formato de escenas. Automatizar las rutinas produciría acciones que parecen completas y fallan en el albatros, la capacidad o el banco de arena. La extracción se queda en el grafo.

Guardar solo el lugar y la lista de objetos pierde el gusano en el anzuelo, la carga del generador y el acto. El estado persistido tiene que ser el estado del mundo.

El `random` del fuente es sobre todo prosa. Fijarlo en la traducción mantiene la partida reproducible. La gramola hay que mirarla antes de dar por hecho que también es prosa.

La copia y Valdeumbría comparten hoy `applicationId`. Hasta la fase 1 no deben instalarse las dos a la vez.

## Comprobaciones de esta fase

JDK 17.0.15. Comando:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Directorio: `/Users/mun0n/tristam-island-android`, rama `feature/tristam-foundation`, sin remotos.

Resultado: la compilación y las pruebas terminaron bien en 5 minutos, el 13 de noviembre de 2026. `BUILD SUCCESSFUL`. 46 tareas ejecutadas. 282 pruebas en 40 clases: 0 fallos, 0 errores, 6 omitidas. Cinco se omiten porque `ADVENTURE_LIVE_LLM` no vale `1`: `FixtureWatchLiveLlmTest`, `ValdumbriaNarrationLiveTest`, `ValdumbriaConclusionLiveTest`, y en `ValdumbriaContinuityLiveTest` los métodos `archiveBoundaryNarrations` y `shortContinuitySequence`. El tercer método de esa clase, `preparedStatesStayOutsideThenMoveInside`, sí se ejecutó y pasó. La sexta omisión es `AdventureHttpTransportTest`, que espera `ADVENTURE_HTTP_IT=1`. No se llamó a ningún modelo ni a ningún servidor. `assembleDebug` generó el APK de depuración.

Después del cambio de nombre y del motor de la isla, `./gradlew :app:testDebugUnitTest` volvió a terminar bien: 289 pruebas en 41 clases, 0 fallos, 0 errores y las mismas 6 omisiones. `./gradlew :app:assembleDebug` también terminó bien. Las 7 pruebas nuevas están en `IslandEngineTest`.

Hay un aviso de compilación, no un fallo, en `FixtureWatchLiveLlmTest.kt` línea 255: una llamada sobre un `File?`. Viene de la copia y no se toca en esta fase.
