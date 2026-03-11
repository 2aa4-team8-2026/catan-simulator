# Team 8: Assignment 1

Course: SFWRENG 2AA4\
Instructor: Dr. Istvan David\
Group Members: Andrew Lian, Mitchell Fong, Nicolas Tran, Nilay Goyal

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=2aa4-team8-2026_assignment-1&metric=alert_status&token=9e295a2e200ee61b49aabb146c40eefe99976294)](https://sonarcloud.io/summary/new_code?id=2aa4-team8-2026_assignment-1)

### Catan Simulator

Build and run from the repo root:

```bash
mvn compile
java -cp target/classes team8.catan.app.Demonstrator src/main/resources/team8/catan/config/game-config.json
```

The simulator reads its settings from `src/main/resources/team8/catan/config/game-config.json`, including the board map path, state output path, and optional `humanPlayerIndex`.

### Visualization

First follow the instructions in `visualizer/README.md` to install dependencies. Then, render the latest board state using using the config generated from `src/`:

```bash
python visualize.py
```

Use `python visualize.py --watch` to re-render whenever `state.json` changes.
