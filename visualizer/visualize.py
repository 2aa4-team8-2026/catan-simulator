"""CLI wrapper around light_visualizer.py."""

from __future__ import annotations

import argparse
import json
import os
from pathlib import Path
import time

DEFAULT_CONFIG_PATH = "src/main/resources/team8/catan/config/game-config.json"
REPO_ROOT = Path(__file__).resolve().parents[1]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Render Catan board images using paths from a game config JSON."
    )
    parser.add_argument(
        "config",
        nargs="?",
        default=DEFAULT_CONFIG_PATH,
        help=f"Path to game config JSON (default: {DEFAULT_CONFIG_PATH}).",
    )
    parser.add_argument(
        "--watch",
        action="store_true",
        help="Watch state file changes and re-render automatically.",
    )
    parser.add_argument(
        "--interval",
        type=float,
        default=0.5,
        help="Watch polling interval in seconds (default: 0.5).",
    )
    parser.add_argument(
        "--output-dir",
        default="scraped_boards",
        help="Directory for rendered PNGs (default: scraped_boards).",
    )
    parser.add_argument(
        "--scale",
        type=float,
        default=1.0,
        help="Render scale passed to the visualizer (default: 1.0).",
    )
    return parser.parse_args()


def resolve_path_from_repo(configured_path: str) -> str:
    raw = Path(configured_path)
    if raw.is_absolute():
        return str(raw.resolve())
    return str((REPO_ROOT / raw).resolve())


def load_paths_from_config(config_path: str) -> tuple[str, str]:
    config_file = Path(resolve_path_from_repo(config_path))
    if not config_file.exists():
        raise FileNotFoundError(f"Config not found: {config_file}")

    with config_file.open("r", encoding="utf-8") as file:
        config_data = json.load(file)

    base_map_path = config_data.get("baseMapPath", "base_map.json")
    state_path = config_data.get("statePath", "state.json")

    resolved_base_map = resolve_path_from_repo(base_map_path)
    resolved_state = resolve_path_from_repo(state_path)
    return resolved_base_map, resolved_state


def render_once(base_map: str, state: str, output_dir: str, scale: float) -> bool:
    if not os.path.exists(base_map):
        print(f"Base map not found: {base_map}")
        return False
    if not os.path.exists(state):
        print(f"State file not found: {state}")
        return False

    try:
        from light_visualizer import visualize_board_from_json
    except ModuleNotFoundError as ex:
        print(f"Missing visualizer dependency: {ex}. Install required Python packages first.")
        return False

    visualize_board_from_json(
        map_json_path=base_map,
        state_json_path=state,
        output_dir=output_dir,
        render_scale=scale,
    )
    return True


def main() -> int:
    args = parse_args()
    try:
        base_map_path, state_path = load_paths_from_config(args.config)
    except (FileNotFoundError, json.JSONDecodeError, OSError) as ex:
        print(f"Failed to load config: {ex}")
        return 1

    if not args.watch:
        return 0 if render_once(base_map_path, state_path, args.output_dir, args.scale) else 1

    print("Visualizer wrapper started in watch mode.")
    print(f"config={args.config}")
    print(f"base_map={base_map_path}")
    print(f"state={state_path}")
    print(f"output_dir={args.output_dir}")

    last_mtime = None
    try:
        while True:
            if os.path.exists(state_path):
                mtime = os.path.getmtime(state_path)
                if mtime != last_mtime:
                    last_mtime = mtime
                    render_once(base_map_path, state_path, args.output_dir, args.scale)
            time.sleep(args.interval)
    except KeyboardInterrupt:
        print("\nStopped.")
        return 0


if __name__ == "__main__":
    raise SystemExit(main())
