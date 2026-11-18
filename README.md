# tristam-island

Adaptación Android de Tristam Island. La partida es local, determinista y funciona sin conexión. No hay un modelo de lenguaje en el camino del jugador.

- Plan y licencias: [docs/TRISTAM_ADAPTATION_PLAN.md](docs/TRISTAM_ADAPTATION_PLAN.md), [docs/TRISTAM_LICENSES.md](docs/TRISTAM_LICENSES.md)
Dungeon Master AI es otro repositorio: [github.com/Mun0n/dungeon-master-ai](https://github.com/Mun0n/dungeon-master-ai). El dado de cristal está en su rama `feature/cinematic-d20`.

- Reglas de este repositorio: [AGENTS.md](AGENTS.md)

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Requiere JDK 17 y Android SDK platform 37.0.
