#!/usr/bin/env python3
"""Passt die BlockAnnotation-Positionen der Handbuchseiten an die neuen
Port-Positionen der Strukturen an.

Die Ports stehen jetzt gruppiert nebeneinander, die Reaktor-Ports liegen auf
der oberen Lage des Torus. Die Seiten zeigen die Ports mit ihren
Annotationen, also muessen die Koordinaten der Annotationen mitziehen.

Aufruf: python tools/update_guide_annotations.py
"""

from __future__ import annotations

import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
GUIDE = os.path.join(
    HERE, "..", "src", "main", "resources", "assets", "overdrive",
    "guides", "overdrive", "guide",
)

# Datei -> { alte Position: neue Position }
MOVES = {
    "machines/ad-ring.md": {
        (3, 1, 8): (7, 1, 8),     # AD Input Port
        (2, 1, 8): (8, 1, 8),     # AD Output Port
    },
    "machines/elena-ring.md": {
        (4, 1, 2): (3, 1, 4),     # ELENA Energy Input Port
        (0, 1, 2): (4, 1, 4),     # ELENA Vacuum Port
        (2, 1, 0): (1, 1, 4),     # ELENA Input Port
        (2, 1, 3): (0, 1, 4),     # ELENA Output Port
    },
    "machines/annihilation-reactor.md": {
        (24, 4, 12): (12, 8, 20),  # Cell Docking Port
        (12, 4, 24): (13, 8, 20),  # Cell Docking Port (zweites)
        (20, 4, 20): (14, 8, 20),  # Annihilation Vacuum Port
        (3, 4, 21): (15, 8, 20),   # Coolant Input Port
        (3, 4, 3): (16, 8, 20),    # Steam Output Port
        (20, 4, 4): (17, 8, 20),   # Annihilation Energy Output
    },
    "machines/particle-accelerator.md": {
        (40, 1, 9): (26, 1, 3),    # Accelerator Output
        (5, 1, 40): (27, 1, 3),   # Accelerator Energy Input Port
        (45, 1, 10): (28, 1, 3),  # Accelerator Vacuum Input Port
    },
    "machines/vacuum-system.md": {
        (0, 1, 0): (0, 1, 1),      # Vacuum Energy Port
        (2, 1, 2): (0, 1, 2),      # Vacuum Output Port
        (2, 1, 3): (0, 1, 3),      # Vacuum Pipe
    },
}

ANNOTATION = re.compile(
    r'(<BlockAnnotation x=")(-?\d+)(" y=")(-?\d+)(" z=")(-?\d+)(")'
)


def update(path: str, moves: dict) -> int:
    with open(path, encoding="utf-8") as fh:
        text = fh.read()

    changed = 0

    def replace(match: "re.Match[str]") -> str:
        nonlocal changed
        old = (int(match.group(2)), int(match.group(4)), int(match.group(6)))
        new = moves.get(old)
        if new is None:
            return match.group(0)
        changed += 1
        return "%s%d%s%d%s%d%s" % (
            match.group(1), new[0], match.group(3),
            new[1], match.group(5), new[2], match.group(7),
        )

    updated = ANNOTATION.sub(replace, text)
    if changed:
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            fh.write(updated)
    return changed


def main() -> None:
    for name, moves in MOVES.items():
        for folder in ("", "_de_de"):
            path = os.path.join(GUIDE, folder, name.replace("/", os.sep))
            if not os.path.exists(path):
                print("  fehlt: %s" % path)
                continue
            count = update(path, moves)
            print("  %-46s %d Annotationen angepasst"
                  % (os.path.join(folder, name), count))


if __name__ == "__main__":
    main()
