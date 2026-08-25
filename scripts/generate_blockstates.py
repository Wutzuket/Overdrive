"""Generate multipart blockstate JSONs for casing blocks."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
BLOCKSTATES = ROOT / "src/main/resources/assets/overdrive/blockstates"

CASINGS = ["accelerator_casing", "ad_casing", "elena_casing", "pump_casing"]

DIRECTIONS = ["north", "south", "east", "west", "up", "down"]

for casing in CASINGS:
    parts = [{"apply": {"model": f"overdrive:block/{casing}"}}]

    for d in DIRECTIONS:
        parts.append({
            "when": {d: "true"},
            "apply": {"model": f"overdrive:block/{casing}_sprite_{d}"},
        })

    bs = {"multipart": parts}
    path = BLOCKSTATES / f"{casing}.json"
    path.write_text(json.dumps(bs, indent=2) + "\n", encoding="utf-8")

print(f"Generated {len(CASINGS)} blockstate files")
