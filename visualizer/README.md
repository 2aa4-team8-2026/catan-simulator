# Catan visualizer

This repository provides a Python visualization script, `light_visualizer.py`, for rendering a Catan board from JSON files.

---

## Overview

The visualizer reads JSON files of a Catan board and game state, and renders the board as an image.

### Input files
- `base_map.json` — defines the board layout.
- `state.json` — defines the game state (roads, buildings, etc.).
### Output files
- `scraped_boards/board0.png` — example output image.

---

## Setup instructions

### 1. Create and activate a Python virtual environment
```bash
python3.11 -m venv .venv
source .venv/bin/activate
```

### 2. Install required dependencies
```bash
pip install -r requirements.txt
```

### 3. Clone the Catanatron repository
```bash
git clone -b gym-rendering https://github.com/bcollazo/catanatron.git
```
### 4. Install dependencies for Catanatron
```bash
cd catanatron
pip install -e ".[web,gym,dev]"
```

### 5. Run the script
`visualize.py` is a wrapper for `light_visualizer.py` that reads `baseMapPath` and `statePath` from a game config JSON.

```bash
python3 visualize.py [config]
```

Default config:
- `src/main/resources/team8/catan/config/game-config.json`

Supported options:
- `--watch`  
  Watch the state file and re-render on changes.
- `--interval <seconds>`  
  Polling interval for watch mode (default: `0.5`).
- `--output-dir <path>`  
  Output directory for rendered images (default: `scraped_boards`).
- `--scale <float>`  
  Render scale passed to the renderer (default: `1.0`).

Examples:
```bash
# One-time render using default config
python3 visualize.py

# One-time render using a specific config
python3 visualize.py src/main/resources/team8/catan/config/game-config.json

# Watch mode
python3 visualize.py --watch
```

Notes:
- Paths in the config are resolved from the repository root.
- If dependencies are missing (for example `numpy`), the wrapper prints a clear install error.
