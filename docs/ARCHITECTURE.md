# Architecture

The application that opens is Tristam Island. `MainActivity` loads `assets/island/coast.json` and shows `IslandApp`. `IslandEngine` is the only writer of that save. The file is `tristam-island.db`.

One Android application module, `:app`. The Kotlin package is `com.tristamisland`.

## Packages

- `com.tristamisland.domain.island` loads the document, validates it, and resolves an action. It does not import Android or Compose. It does not roll a die.
- `com.tristamisland.data` stores one island save with a revision. `commit` writes a state it is given when that revision still matches. It does not call the engine.
- `com.tristamisland.presentation` draws the menu and the game. It sends the id of a visible action. It does not write the location text.

`domain` must not import Android or Compose. `DomainBoundaryTest` fails the build if it does.

## Who writes the state

`IslandSession` keeps one turn at a time behind its own mutex. It calls `IslandEngine` first. A rejection returns the same record and does not write. An accepted state goes to `IslandGameStore.commit`, which writes only when the stored revision still matches. A conflict reloads the save and does not replace it with the turn that lost.

The numbers are in [GAME_RULES.md](GAME_RULES.md). The place graph for the first act is in [ACT_1_MAP.md](ACT_1_MAP.md).

## Sound

Background music is a looping `MediaPlayer` in `presentation`. `TristamApp` owns that player for the process. `MainActivity` marks it in the foreground in `onStart` and leaves the foreground in `onStop`. A configuration change does not pause it. Leaving the app does.

Each act carries `musicRegion`. The coast act is `lower_island`, the plateau is `plateau`, the mansion is `mansion`, and the laboratory is `laboratory`. The screen reads that id from the current place, including during the prologue, and changes tracks only when the id changes. The menu, the guide, and the credits use `res/raw/island_on_the_horizon.mp3`. `lower_island` plays `res/raw/lower_coast.mp3`. `plateau` plays `res/raw/plateau.mp3`. `mansion` plays `res/raw/mansion.mp3`. `laboratory` plays `res/raw/laboratory.mp3`. A region with no track fades the current one out over about 1.8 seconds. A different track crossfades for the same duration. The same region keeps the player across locations and actions.

The scene plays at 0.20. Journal, inventory, and progress keep the track and lower it to 0.15, then the scene returns to 0.20 without restarting. `MusicPreference` remembers when the listener turns music off. A later region does not turn it back on.

The domain stores the region id and does not select tracks. It does not import Android audio types.

## Screen orientation

The manifest sets `android:screenOrientation="portrait"` on the activity and `android:appCategory="game"` on the application. The app targets API 37. Marking the application as a game keeps the portrait lock on large screens as well as on phones.

## What is deliberately missing

No Hilt. No narrator. No network client. No die. `TristamApp` owns the island database and the background music. `MainActivity` passes the island store to `IslandViewModelFactory`.

## Build

AGP 9.3.1 with built-in Kotlin, raised to Kotlin 2.4.21 from the root `buildscript` classpath. The Compose compiler plugin and the serialization plugin match that Kotlin version. Room is compiled with KSP. `org.jetbrains.kotlin.android` is not applied. `kotlinx-serialization-json` 1.11.0 decodes the island document.

`compileSdk` is API 37 minor 0 because the installed platform directory is `android-37.0`. `minSdk` is 26. JDK 17.
