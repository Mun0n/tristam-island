# Development workflow

This file chooses how deep a check has to be. It does not replace the review in [AGENTS.md](../AGENTS.md). Milestone status stays where [ROADMAP.md](ROADMAP.md) puts it.

Use the smallest level that covers the change. Do not repeat a check that already passed when the code it covers has not changed, unless there is a concrete regression risk.

## Level 1 — A small change

Run only the tests that cover the code that changed.

Do not start an emulator. Do not walk the screens. Do not take screenshots. Do not rebuild the whole application when the changed tests can run without that rebuild.

## Level 2 — A finished behavior

Run the unit and integration tests that cover that behavior.

Check the changed behavior by hand, and only that behavior. Use an emulator only when the change is visible in the interface and the tests cannot show it.

## Level 3 — Closing a milestone

Run the full unit test suite. Build the application. Run the device checks the milestone asks for.

Do not start the next milestone unless it was requested. Do not mark a milestone complete unless the close was requested.

## Git

`main` is the line that can be built and played.

Start each feature or fix on its own branch from `main`. Name it `feature/<short-name>` or `fix/<short-name>`. Commit on that branch.

When the quality gate for that change has passed, merge the branch into `main`. A one-line rename or a docs typo can stay on `main`.

Do not force-push `main`. Do not rewrite a branch that has already been pushed unless asked.
