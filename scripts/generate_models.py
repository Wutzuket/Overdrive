"""Generate sprite overlay models for casing blocks.

Each sprite is a thin quad slightly offset from the block face to avoid z-fighting.
"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MODELS = ROOT / "src/main/resources/assets/overdrive/models/block"

CASINGS = ["accelerator_casing", "ad_casing", "elena_casing", "pump_casing"]

DIRECTIONS = {
    "north": {
        "from": [0, 0, -0.5],
        "to": [16, 16, 0],
        "face": "north",
    },
    "south": {
        "from": [0, 0, 16],
        "to": [16, 16, 16.5],
        "face": "south",
    },
    "east": {
        "from": [16, 0, 0],
        "to": [16.5, 16, 16],
        "face": "east",
    },
    "west": {
        "from": [-0.5, 0, 0],
        "to": [0, 16, 16],
        "face": "west",
    },
    "up": {
        "from": [0, 16, 0],
        "to": [16, 16.5, 16],
        "face": "up",
    },
    "down": {
        "from": [0, -0.5, 0],
        "to": [16, 0, 16],
        "face": "down",
    },
}

for casing in CASINGS:
    for dir_name, cfg in DIRECTIONS.items():
        model = {
            "parent": "minecraft:block/block",
            "textures": {
                "particle": f"overdrive:block/{casing}_sprite",
                "sprite": f"overdrive:block/{casing}_sprite",
            },
            "elements": [
                {
                    "from": cfg["from"],
                    "to": cfg["to"],
                    "shade": False,
                    "faces": {
                        cfg["face"]: {
                            "uv": [0, 0, 16, 16],
                            "texture": "#sprite",
                        }
                    },
                }
            ],
        }

        path = MODELS / f"{casing}_sprite_{dir_name}.json"
        path.write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")

print(f"Generated {len(CASINGS) * len(DIRECTIONS)} sprite models")
