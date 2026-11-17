#!/usr/bin/env python3
"""Passt die BlockAnnotation-Positionen der Handbuchseiten an die Ports an.

Die Ports der Maschinen stehen in den Strukturen. Die Seiten zeigen sie mit
Annotationen, und deren Position muss zum Port passen.

Wichtig: Die Zuordnung laeuft ueber die Reihenfolge der Port-Annotationen in
der Seite, nicht ueber die Koordinaten. Sonst tauschen sich die Texte der
Annotationen, weil zwei Ports auf einer Linie liegen.

Aufruf: python tools/update_guide_annotations.py
"""

from __future__ import annotations

import os
import re
from typing import Dict, List, Tuple

HERE = os.path.dirname(os.path.abspath(__file__))
GUIDE = os.path.join(
    HERE, "..", "src", "main", "resources", "assets", "overdrive",
    "guides", "overdrive", "guide",
)
STRUCTURES = os.path.join(GUIDE, "structures")

# Seite -> Reihenfolge der Port-Annotationen in der Seite.
PORT_ORDER: Dict[str, List[str]] = {
    "machines/ad-ring.md": [
        "overdrive:ad_energy_input_port",
        "overdrive:ad_vacuum_port",
        "overdrive:ad_input_port",
        "overdrive:ad_output_port",
    ],
    "machines/elena-ring.md": [
        "overdrive:elena_energy_input_port",
        "overdrive:elena_vacuum_port",
        "overdrive:elena_input_port",
        "overdrive:elena_output_port",
    ],
    "machines/particle-accelerator.md": [
        "overdrive:iridium_target",
        "overdrive:accelerator_output",
        "overdrive:accelerator_energy_input_port",
        "overdrive:accelerator_vacuum_input_port",
    ],
    "machines/annihilation-reactor.md": [
        "overdrive:cell_docking_port",
        "overdrive:cell_docking_port",
        "overdrive:annihilation_vacuum_port",
        "overdrive:coolant_input_port",
        "overdrive:steam_output_port",
        "overdrive:annihilation_energy_output",
    ],
    "machines/vacuum-system.md": [
        "overdrive:vacuum_energy_port",
        "overdrive:vacuum_output_port",
    ],
}

# Seite -> weitere Annotationen, die auch einem Block der Struktur folgen.
OTHER_BLOCKS: Dict[str, List[str]] = {
    "machines/vacuum-system.md": ["overdrive:vacuum_pipe"],
}

# Seite -> Struktur der Maschine.
STRUCTURE_OF = {
    "machines/ad-ring.md": "ad_ring.snbt",
    "machines/elena-ring.md": "elena_ring.snbt",
    "machines/particle-accelerator.md": "particle_accelerator.snbt",
    "machines/annihilation-reactor.md": "annihilation_reactor.snbt",
    "machines/vacuum-system.md": "vacuum_pump.snbt",
}

ANNOTATION = re.compile(
    r'<BlockAnnotation x="(-?\d+)" y="(-?\d+)" z="(-?\d+)"'
)


def read_structure(name: str) -> Dict[Tuple[int, int, int], str]:
    """Liest die Bloecke einer SNBT-Struktur."""
    with open(os.path.join(STRUCTURES, name), encoding="utf-8") as fh:
        text = fh.read()

    blocks: Dict[Tuple[int, int, int], str] = {}
    palette: List[str] = []
    section = None
    for raw in text.splitlines():
        line = raw.strip()
        if line.startswith("palette:"):
            section = "palette"
        elif line.startswith("data:"):
            section = "data"
        elif line.startswith("}"):
            section = None
        elif section == "palette" and line.startswith('"'):
            palette.append(line.strip().rstrip(",").strip('"'))
        elif section == "data" and line.startswith("{"):
            body = line.strip().rstrip(",").strip("{}")
            pos = body.split("pos:")[1].split("]")[0].strip(" [")
            state = body.split('state: "')[1].split('"')[0]
            p = tuple(int(v) for v in pos.split(","))
            blocks[p] = state
    return blocks


def port_positions(structure: str, port_ids: List[str]) -> List[Tuple[int, int, int]]:
    """Die Positionen der Ports in der Struktur, in der Reihenfolge der Liste."""
    blocks = read_structure(structure)
    found: List[Tuple[Tuple[int, int, int], str]] = []
    for port_id in port_ids:
        hits = [pos for pos, state in blocks.items() if state == port_id]
        if not hits:
            raise ValueError("%s: der Port %s fehlt" % (structure, port_id))
        found.append((hits[0], port_id))

    # Die Cell Docking Ports gibt es zweimal, sie stehen in der Struktur
    # nebeneinander. Der erste gehoert zur ersten Annotation.
    result: List[Tuple[int, int, int]] = []
    used = set()
    for pos, _ in found:
        if pos in used:
            continue
        used.add(pos)
        result.append(pos)
    return result


def update(path: str, page: str) -> int:
    structure = STRUCTURE_OF[page]
    blocks = read_structure(structure)

    # Die Positionen der Ports in der Struktur, in der Reihenfolge der Liste.
    wanted = list(PORT_ORDER[page]) + list(OTHER_BLOCKS.get(page, []))
    positions = []
    used = set()
    for block_id in wanted:
        hits = [pos for pos, state in blocks.items()
                if state == block_id and pos not in used]
        if not hits:
            raise ValueError("%s: %s fehlt in der Struktur" % (structure, block_id))
        used.add(hits[0])
        positions.append(hits[0])

    with open(path, encoding="utf-8") as fh:
        lines = fh.readlines()

    # Die Port-Annotationen sind alle Annotationen ausser der des Controllers
    # und der des Kerns. Der Controller und der Kern stehen auf festen
    # Bloecken, die Ports koennen vertauscht sein.
    fixed = set()
    for pos, state in blocks.items():
        if "controller" in state or state in ("overdrive:vacuum_pump",
                                              "overdrive:reactor_core_block"):
            fixed.add(pos)

    annotations = []
    for number, line in enumerate(lines):
        if ANNOTATION.search(line):
            annotations.append(number)

    port_annotations = []
    for number in annotations:
        match = ANNOTATION.search(lines[number])
        pos = (int(match.group(1)), int(match.group(2)), int(match.group(3)))
        if pos not in fixed:
            port_annotations.append(number)

    if len(port_annotations) != len(positions):
        raise ValueError("%s: %d Annotationen fuer Ports, aber %d Ports in der Struktur"
                         % (path, len(port_annotations), len(positions)))

    changed = 0
    for number, pos in zip(port_annotations, positions):
        line = lines[number]
        new_line = ANNOTATION.sub(
            '<BlockAnnotation x="%d" y="%d" z="%d"' % pos, line, count=1)
        if new_line != line:
            lines[number] = new_line
            changed += 1

    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.writelines(lines)
    return changed


def controller_position(structure: str) -> Tuple[int, int, int]:
    """Die Position des Controllers in einer Struktur."""
    blocks = read_structure(structure)
    for pos, state in blocks.items():
        if "controller" in state or state == "overdrive:vacuum_pump":
            return pos
    raise ValueError("%s: der Controller fehlt" % structure)


def main() -> None:
    for page in PORT_ORDER:
        for folder in ("", "_de_de"):
            path = os.path.join(GUIDE, folder, page.replace("/", os.sep))
            if not os.path.exists(path):
                print("  fehlt: %s" % path)
                continue
            count = update(path, page)
            print("  %-46s %d Annotationen angepasst"
                  % (os.path.join(folder, page), count))


if __name__ == "__main__":
    main()
