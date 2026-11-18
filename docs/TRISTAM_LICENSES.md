# Licencias y exclusiones

Este repositorio es la aplicación independiente de Tristam Island. La copia inicial del código se tomó de `/Users/mun0n/dungeon-master-ai` en el commit `9e5551d`, el 13 de noviembre de 2026. Ese directorio no se modifica desde aquí y este repositorio no tiene remoto hacia él.

## Código de esta aplicación

El código Kotlin, Gradle y las pruebas de la isla son de esta aplicación. La historia, los nombres, las ilustraciones y la música de Valdeumbría salieron de este árbol el 18 de noviembre de 2026 y están en el repositorio `dungeon-master-ai`.

## Dependencias que hoy compila la copia

AndroidX, Jetpack Compose, Room, Kotlinx Serialization, corrutinas y JUnit llegan por Gradle y conservan la licencia de cada componente, en general Apache 2.0. Filament salió con el dado. No hay dependencia de un modelo de lenguaje.

## Recursos ya empaquetados

| Recurso | Aviso en el árbol | Uso previsto |
| --- | --- | --- |
| Playfair Display, Newsreader, Space Grotesk | `app/src/main/assets/licenses/*-OFL.txt` | La carga de fuentes se puede conservar. La paleta visual de la isla será propia |
| Material Symbols Outlined | `app/src/main/assets/licenses/material-symbols-apache.txt` | Iconos de interfaz, licencia Apache 2.0 |
| Ilustraciones `valdumbria_*`, retratos de clase, objetos y menú de Valdeumbría | Salieron de este árbol | Están en `dungeon-master-ai`, no en esta aplicación |
| `drawable-nodpi/menu_storm.jpg` | Arte de esta aplicación | Acuarela nocturna del menú, hecha para esta edición. No es la portada de Labrande |
| `drawable-nodpi/island_*.jpg` | Arte de esta aplicación, generado el 13 de noviembre de 2026 a partir de `menu_storm.jpg` | Ilustraciones de la costa baja. No son la portada de Labrande |
| `res/raw/island_on_the_horizon.mp3` | Hecha para esta edición el 13 de noviembre de 2026. El archivo de origen se titula Windswept Cliffs, dice «made with suno» y nombra a heartlesskalimba534 | Música del menú, la guía y los créditos |
| `res/raw/lower_coast.mp3` | Hecha para esta edición el 13 de noviembre de 2026. El archivo de origen se titula reflective reading, dice «made with suno» y nombra a heartlesskalimba534 | Ambientación de la región `lower_island`, el Acto I. No se reinicia al cambiar de lugar |
| `res/raw/plateau.mp3` | Hecha para esta edición el 16 de noviembre de 2026 con Suno, a partir del prompt de la meseta | Ambientación de la región `plateau`, el Acto 2 |
| `res/raw/mansion.mp3` | Hecha para esta edición el 16 de noviembre de 2026 con Suno, a partir del prompt de la mansión | Ambientación de la región `mansion`, el Acto 3 |
| `res/raw/laboratory.mp3` | Hecha para esta edición el 16 de noviembre de 2026 con Suno, a partir del prompt del laboratorio | Ambientación de la región `laboratory`, el Acto 4 |

## Obra original de Tristam Island

La obra de Hugo Labrande, publicada en [github.com/hlabrand/tristam-island](https://github.com/hlabrand/tristam-island), está dedicada al dominio público con CC0 1.0. El archivo de referencia es `tristam-en.inf`. También son referencia de diseño, bajo esa misma dedicación, los mapas, el diagrama de dependencias, el walkthrough, el postmortem y las pistas en texto.

CC0 no obliga a atribuir. Esta aplicación acredita a Hugo Labrande como creador de la obra original en los créditos. La adaptación al español, las acciones contextuales, la interfaz y el código de la aplicación son trabajo de este proyecto.

El fuente no se ejecuta dentro de la aplicación. No hay intérprete Z-machine. No se copia el archivo Inform al APK como juego jugable. Se usa para escribir, fuera de la partida, el contenido local.

CC0 no concede derechos de marca. El nombre visible, el icono y la ficha de Google Play se revisan antes de publicar.

## Exclusiones

No se importa, no se modifica y no se distribuye la portada de Karen Christie y Stephen F. Winsor. El README del repositorio original dice que esa portada aparece en algunos binarios.

Quedan fuera de la aplicación:

- `binaries/`, tanto la demo como el juego completo y los manuales de plataforma.
- `feelies/postcard-tristam.jpg` y cualquier otra imagen del repositorio original, hasta que se compruebe una por una que no contiene la portada.
- El zip de IF Archive `tristam-island.zip`, por el mismo motivo.
- PunyInform, en `PunyInform-v3/` y `myPunyInform/`. Es MIT, copyright 2019 Fredrik Ramsberg y Johan Berntsson. La adaptación no distribuye el intérprete ni una parte sustancial de esa biblioteca. Si en el futuro se distribuyera, el aviso MIT iría con ella.

El texto de las pistas y del postmortem puede consultarse al escribir el diario en español. El diario de la aplicación es texto nuevo, no una copia de `invisiclues.txt`.

## Lo que no entra por la partida

No hay servicio de Groq ni de ningún otro modelo. No hay clave de API en el repositorio. `local.properties` de esta copia solo señala el SDK de Android y está ignorado por Git. El contenido que el jugador lee está escrito antes de publicar y viaja dentro de la aplicación.
