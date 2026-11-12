#!/usr/bin/env python3
"""Erzeugt die SNBT-Strukturdateien fuer das Overdrive-Handbuch (GuideME).

Die Geometrie der Multiblocks ist 1:1 aus der Strukturpruefung der Mod
uebernommen:

  * AnnihilationControllerEntity.isTorusWall / isTorusHollow / checkStructure
  * CoolingTowerControllerEntity.checkStructure
  * ParticleAcceleratorControllerEntity.checkStructure
  * ADControllerEntity.checkStructure
  * ELENAControllerEntity.checkStructure
  * VacuumPumpBlockEntity.checkStructure
  * ElectronGunEntity.findTungstenTarget / findOutputPort

Die erzeugten Dateien landen unter
src/main/resources/assets/overdrive/guides/overdrive/guide/structures/
und werden in den Handbuchseiten ueber <ImportStructure src="..." /> eingebunden.

Aufruf:  python tools/generate_guide_structures.py
"""

from __future__ import annotations

import math
import os
from typing import Dict, Iterable, List, Optional, Tuple

Pos = Tuple[int, int, int]
Block = Tuple[str, Optional[Dict[str, str]]]

DATA_VERSION = 3955  # Minecraft 1.21.1

HERE = os.path.dirname(os.path.abspath(__file__))
OUT_DIR = os.path.join(
    HERE, "..", "src", "main", "resources", "assets", "overdrive",
    "guides", "overdrive", "guide", "structures",
)


# --------------------------------------------------------------------------
# SNBT-Ausgabe
# --------------------------------------------------------------------------
def write_snbt(path: str, blocks: Dict[Pos, Block], notes: Dict[str, Pos]) -> None:
    """Schreibt eine StructureTemplate-Datei im SNBT-Format.

    GuideME laedt .snbt-Dateien ueber NbtUtils.snbtToStructure(). Das erwartet
    das gepackte Vanilla-Format: die Palette ist eine Liste von Strings
    ("block{property:value}") und die Bloecke stehen unter "data" mit dem
    Zustand als String.

    blocks:  Position -> (Block-ID, Properties oder None)
    notes:   Name -> Position (nur fuer die Ausgabe, nicht in der Datei)
    """
    if not blocks:
        raise ValueError("leere Struktur: %s" % path)

    min_x = min(p[0] for p in blocks)
    min_y = min(p[1] for p in blocks)
    min_z = min(p[2] for p in blocks)
    max_x = max(p[0] for p in blocks)
    max_y = max(p[1] for p in blocks)
    max_z = max(p[2] for p in blocks)

    size = (max_x - min_x + 1, max_y - min_y + 1, max_z - min_z + 1)

    palette: List[str] = []
    palette_index: Dict[str, int] = {}

    def index_of(block: Block) -> int:
        name, props = block
        key = name
        if props:
            key += "{%s}" % ",".join("%s:%s" % (k, v) for k, v in sorted(props.items()))
        if key not in palette_index:
            palette_index[key] = len(palette)
            palette.append(key)
        return palette_index[key]

    entries: List[Tuple[Pos, int]] = []
    for pos in sorted(blocks):
        shifted = (pos[0] - min_x, pos[1] - min_y, pos[2] - min_z)
        entries.append((shifted, index_of(blocks[pos])))

    lines: List[str] = []
    lines.append("{")
    lines.append("  DataVersion: %d," % DATA_VERSION)
    lines.append("  size: [%d, %d, %d]," % size)
    lines.append("  palette: [")
    for i, state in enumerate(palette):
        lines.append('    "%s"%s' % (state, "," if i < len(palette) - 1 else ""))
    lines.append("  ],")
    lines.append("  data: [")
    for i, (pos, state) in enumerate(entries):
        lines.append('    {pos: [%d, %d, %d], state: "%s"}%s'
                     % (pos[0], pos[1], pos[2], palette[state],
                        "," if i < len(entries) - 1 else ""))
    lines.append("  ],")
    lines.append("  entities: []")
    lines.append("}")

    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write("\n".join(lines) + "\n")

    print("  %-28s %5d Bloecke  Groesse %dx%dx%d"
          % (os.path.basename(path), len(blocks), size[0], size[1], size[2]))
    for name in sorted(notes):
        pos = notes[name]
        print("      %-22s -> [%d, %d, %d]"
              % (name, pos[0] - min_x, pos[1] - min_y, pos[2] - min_z))


# --------------------------------------------------------------------------
# Geometrie-Helfer (aus der Mod uebernommen)
# --------------------------------------------------------------------------
def is_torus_wall(dx: int, dy: int, dz: int, major: int = 8, tube: int = 4) -> bool:
    horiz = math.sqrt(float(dx * dx + dz * dz))
    dist_to_ring = abs(horiz - major)
    tube_dist = math.sqrt(dist_to_ring * dist_to_ring + float(dy * dy))
    return abs(tube_dist - tube) < 0.9


def torus_wall_cells(outer: int = 12, major: int = 8, tube: int = 4
                     ) -> List[Pos]:
    cells = []
    for x in range(-outer, outer + 1):
        for y in range(-outer, outer + 1):
            for z in range(-outer, outer + 1):
                if is_torus_wall(abs(x), abs(y), abs(z), major, tube):
                    cells.append((x, y, z))
    return cells


def pick_angle(cells: Iterable[Pos], degrees: float) -> Pos:
    """Waehlt die Zelle mit dem gewuenschten Winkel (Grad, 0 = +X) aus."""
    target = math.radians(degrees)
    best = None
    best_delta = None
    for cell in cells:
        angle = math.atan2(cell[2], cell[0])
        delta = abs((angle - target + math.pi) % (2 * math.pi) - math.pi)
        if best_delta is None or delta < best_delta:
            best_delta = delta
            best = cell
    assert best is not None
    return best


def ring_cells(radius: int, inner: int, height: int = 3) -> List[Pos]:
    """Quadratischer Ring (Chebyshev) mit drei Lagen, Zentrum im Ursprung."""
    cells = []
    for x in range(-radius, radius + 1):
        for z in range(-radius, radius + 1):
            max_dist = max(abs(x), abs(z))
            if max_dist < inner or max_dist > radius:
                continue
            for dy in range(-(height // 2), height // 2 + 1):
                cells.append((x, dy, z))
    return cells


def accelerator_band(radius: int = 25) -> List[Tuple[int, int]]:
    """Ringband des Beschleunigers: dx^2+dz^2 zwischen 463 und 650."""
    cells = []
    for x in range(-(radius + 2), radius + 3):
        for z in range(-(radius + 2), radius + 3):
            s = x * x + z * z
            if 463 <= s <= 650:
                cells.append((x, z))
    return cells


# --------------------------------------------------------------------------
# Strukturen
# --------------------------------------------------------------------------
def build_vacuum_pump() -> Tuple[Dict[Pos, Block], Dict[str, Pos]]:
    """3x3x3-Wuerfel hinter der Pumpe (back = -facing).

    facing=north -> back=south -> der Wuerfel waechst nach +Z.

    Die beiden Ports stehen nebeneinander, damit man auf einen Blick sieht,
    wo die Ports hinpassen.
    """
    blocks: Dict[Pos, Block] = {}
    notes: Dict[str, Pos] = {}

    def put(pos: Pos, block: Block) -> None:
        blocks[pos] = block

    put((0, 0, 0), ("overdrive:vacuum_pump", {"facing": "north"}))
    notes["pumpe"] = (0, 0, 0)
    put((-1, 0, 1), ("overdrive:vacuum_energy_port", None))
    notes["energie-port"] = (-1, 0, 1)
    # Der Ausgangsport sitzt direkt neben dem Energie-Port.
    put((-1, 0, 2), ("overdrive:vacuum_output_port", None))
    notes["vakuum-port"] = (-1, 0, 2)
    # Das Rohr steckt hinter dem Ausgangsport und fuehrt ins Netz
    put((-1, 0, 3), ("overdrive:vacuum_pipe", {"north": "true"}))
    notes["rohr"] = (-1, 0, 3)

    for x in range(-1, 2):
        for y in range(-1, 2):
            for z in range(0, 3):
                if (x, y, z) in blocks:
                    continue
                put((x, y, z), ("overdrive:pump_casing", None))
    return blocks, notes


def build_particle_accelerator() -> Tuple[Dict[Pos, Block], Dict[str, Pos]]:
    """Ring mit Aussenradius 25, drei Lagen, Tunnel in der Mittelebene."""
    blocks: Dict[Pos, Block] = {}
    notes: Dict[str, Pos] = {}

    band = accelerator_band(25)

    for dx, dz in band:
        s = dx * dx + dz * dz
        outer_wall = s >= 601
        inner_wall = s <= 506
        # Boden und Deckel: nur Gehaeuse (oder Ausgang)
        for dy in (-1, 1):
            blocks[(dx, dy, dz)] = ("overdrive:accelerator_casing", None)
        if not (outer_wall or inner_wall):
            # Tunnel: muss Luft bleiben
            continue
        blocks[(dx, 0, dz)] = ("overdrive:accelerator_casing", None)

    # Controller sitzt auf der aeusseren Wand, zeigt zum Ringzentrum (0,0,0).
    blocks[(0, 0, 25)] = ("overdrive:particle_accelerator_controller", {"facing": "north"})
    notes["controller"] = (0, 0, 25)
    # Die Ports und das Target stehen nebeneinander auf der inneren Wand, damit
    # man auf einen Blick sieht, wo sie hinpassen. Das Target muss auf der
    # inneren Wand liegen, sonst steht es nicht im Strahlweg.
    inner_wall = [(dx, 0, dz) for (dx, dz) in band if dx * dx + dz * dz <= 506]
    ports = [
        ("target", (0, 0, -22), "overdrive:iridium_target"),
        ("ausgang", (1, 0, -22), "overdrive:accelerator_output"),
        ("energie-port", (2, 0, -22), "overdrive:accelerator_energy_input_port"),
        ("vakuum-port", (3, 0, -22), "overdrive:accelerator_vacuum_input_port"),
    ]
    for name, pos, block in ports:
        if pos not in inner_wall:
            raise ValueError("Beschleuniger: %s liegt nicht auf der inneren Wand" % (pos,))
        blocks[pos] = (block, None)
        notes[name] = pos
    return blocks, notes


def build_square_ring(radius: int, inner: int, casing: str, controller: str,
                      energy_port: str, vacuum_port: str,
                      input_port: str, output_port: str
                      ) -> Tuple[Dict[Pos, Block], Dict[str, Pos]]:
    """Quadratischer Ring (AD / ELENA) mit drei Lagen."""
    blocks: Dict[Pos, Block] = {}
    notes: Dict[str, Pos] = {}

    for x, dy, z in ring_cells(radius, inner):
        blocks[(x, dy, z)] = ("overdrive:" + casing, None)

    controller_pos = (0, 0, radius)
    blocks[controller_pos] = ("overdrive:" + controller, {"facing": "north"})
    notes["controller"] = controller_pos
    # Die Ports stehen nebeneinander auf der Kante neben dem Controller, damit
    # man auf einen Blick sieht, wo ein Port hinpasst. Sie liegen alle auf der
    # mittleren Lage, dort koennen die Ports der Ringe stehen. Wenn die Kante
    # nicht breit genug ist, kommen die restlichen Ports von der anderen Seite.
    slots: List[Pos] = []
    for offset in range(1, radius + 1):
        if len(slots) >= 4:
            break
        slots.append((offset, 0, radius))
    for offset in range(1, radius + 1):
        if len(slots) >= 4:
            break
        slots.append((-offset, 0, radius))

    ports = [
        ("energie-port", slots[0], energy_port),
        ("vakuum-port", slots[1], vacuum_port),
        ("eingang", slots[2], input_port),
        ("ausgang", slots[3], output_port),
    ]
    used = {controller_pos}
    for name, pos, block in ports:
        if pos in used:
            raise ValueError("Port %s kollidiert bei %s" % (name, pos))
        used.add(pos)
        blocks[pos] = ("overdrive:" + block, None)
        notes[name] = pos
    return blocks, notes


def build_electron_gun() -> Tuple[Dict[Pos, Block], Dict[str, Pos]]:
    """Kanone -> Target -> Positronenport (je 1 Block Abstand).

    Die Kanone blickt in ihrer Blickrichtung auf das Target, das Target
    steht einen Block davor, der Positronen-Port zwei Bloecke davor
    (ElectronGunEntity.findTungstenTarget / findOutputPort).

    Das Vakuumrohr steckt HINTER der Kanone: die Kanone selbst ist der
    Vakuum-Node des Vakuumnetzes, nicht der Positronen-Port
    (VacuumPipeBlock.isConnectable).

    facing=north heisst: das Target liegt bei z=-1, der Port bei z=-2.
    """
    blocks: Dict[Pos, Block] = {}
    notes: Dict[str, Pos] = {}

    blocks[(0, 0, 0)] = ("overdrive:electron_gun", {"facing": "north"})
    notes["kanone"] = (0, 0, 0)
    # Das Target blickt zurueck auf die Kanone.
    blocks[(0, 0, -1)] = ("overdrive:tungsten_target", {"facing": "south"})
    notes["target"] = (0, 0, -1)
    blocks[(0, 0, -2)] = ("overdrive:positron_vacuum_output_port", None)
    notes["port"] = (0, 0, -2)
    # Das Vakuumrohr steckt hinter der Kanone und fuehrt ins Vakuumnetz.
    blocks[(0, 0, 1)] = ("overdrive:vacuum_pipe", {"north": "true"})
    notes["rohr"] = (0, 0, 1)
    return blocks, notes


def build_annihilation_reactor() -> Tuple[Dict[Pos, Block], Dict[str, Pos]]:
    """25x25x25 Torus mit 5x5x5-Core in der Mitte."""
    blocks: Dict[Pos, Block] = {}
    notes: Dict[str, Pos] = {}

    wall = torus_wall_cells(12, 8, 4)
    # Die Ports liegen auf der oberen Lage des Torus, dicht nebeneinander in
    # einer Linie. Die Zellen (x, 4, 8) liegen alle auf der Toruswand.
    top_row = [(x, 4, 8) for x in range(0, 6)]

    blocks[(0, 0, -12)] = ("overdrive:annihilation_controller", {"facing": "south"})
    notes["controller"] = (0, 0, -12)

    ports = [
        ("zelle-1", 0, "overdrive:cell_docking_port"),
        ("zelle-2", 1, "overdrive:cell_docking_port"),
        ("vakuum-port", 2, "overdrive:annihilation_vacuum_port"),
        ("kuehlmittel-ein", 3, "overdrive:coolant_input_port"),
        ("dampf-aus", 4, "overdrive:steam_output_port"),
        ("energie-aus", 5, "overdrive:annihilation_energy_output"),
    ]
    used = set()
    for name, index, block in ports:
        pos = top_row[index]
        if pos not in wall:
            raise ValueError("Reaktor: %s liegt nicht auf der Toruswand" % (pos,))
        if pos in blocks or pos in used:
            raise ValueError("Port %s kollidiert mit %s" % (name, pos))
        used.add(pos)
        blocks[pos] = (block, None)
        notes[name] = pos

    for cell in wall:
        if cell in blocks:
            continue
        blocks[cell] = ("overdrive:annihilation_heavy_casing", None)

    # Core: 5x5x5, innere 3x3x3 Core-Blocke, Schale Containment-Glas
    for x in range(-2, 3):
        for y in range(-2, 3):
            for z in range(-2, 3):
                shell = abs(x) == 2 or abs(y) == 2 or abs(z) == 2
                if shell:
                    blocks[(x, y, z)] = ("overdrive:core_containment_glass", None)
                else:
                    blocks[(x, y, z)] = ("overdrive:reactor_core_block", None)
    notes["core-mitte"] = (0, 0, 0)
    return blocks, notes


def build_cooling_tower() -> Tuple[Dict[Pos, Block], Dict[str, Pos]]:
    """5x5x9 Turm mit hyperbolischer Taille (Radius 1 in den Lagen 3-5)."""
    blocks: Dict[Pos, Block] = {}
    notes: Dict[str, Pos] = {}

    height = 9

    def radius_of(y: int) -> int:
        if y == 0 or y == height - 1:
            return 2
        if 3 <= y <= 5:
            return 1
        return 2

    for y in range(height):
        r = radius_of(y)
        for x in range(-r, r + 1):
            for z in range(-r, r + 1):
                if abs(x) != r and abs(z) != r:
                    continue  # Innenraum muss Luft bleiben
                blocks[(x, y, z)] = ("overdrive:cooling_tower_casing", None)

    controller_pos = (0, 0, 2)
    blocks[controller_pos] = ("overdrive:cooling_tower_controller", {"facing": "south"})
    notes["controller"] = controller_pos
    blocks[(2, 1, 0)] = ("overdrive:cooling_tower_input_port", None)
    notes["eingang"] = (2, 1, 0)
    blocks[(-2, 7, 0)] = ("overdrive:cooling_tower_output_port", None)
    notes["ausgang"] = (-2, 7, 0)
    return blocks, notes


# --------------------------------------------------------------------------
# Validierung
# --------------------------------------------------------------------------
def parse_snbt(path: str) -> Tuple[Dict[Pos, str], Tuple[int, int, int]]:
    """Liest die erzeugte Datei wieder ein und prueft das Format."""
    blocks: Dict[Pos, str] = {}
    size = (0, 0, 0)
    palette: List[str] = []
    section = None
    with open(path, encoding="utf-8") as fh:
        for raw in fh:
            line = raw.strip()
            if line.startswith("size:"):
                size = tuple(int(v) for v in line.split("[")[1].split("]")[0].split(","))
            elif line.startswith("palette:"):
                section = "palette"
            elif line.startswith("data:"):
                section = "data"
            elif line.startswith("}"):
                section = None
            elif section == "palette" and line.startswith('"'):
                palette.append(line.strip().strip(",").strip('"'))
            elif section == "data" and line.startswith("{"):
                body = line.strip().strip(",").strip("{}")
                pos = body.split("pos:")[1].split("]")[0].strip(" [")
                state = body.split('state: "')[1].split('"')[0]
                p = tuple(int(v) for v in pos.split(","))
                blocks[p] = state
    return blocks, size


def validate(path: str, expected: Dict[Pos, str]) -> None:
    """Prueft die geschriebene Datei gegen die erwartete Belegung."""
    blocks, size = parse_snbt(path)
    min_x = min(p[0] for p in expected)
    min_y = min(p[1] for p in expected)
    min_z = min(p[2] for p in expected)
    shifted = {(p[0] - min_x, p[1] - min_y, p[2] - min_z): s for p, s in expected.items()}
    if blocks != shifted:
        missing = set(shifted) - set(blocks)
        extra = set(blocks) - set(shifted)
        raise ValueError("%s: Blockabweichung (fehlend %d, zusaetzlich %d)"
                         % (os.path.basename(path), len(missing), len(extra)))
    for pos in blocks:
        if not (0 <= pos[0] < size[0] and 0 <= pos[1] < size[1] and 0 <= pos[2] < size[2]):
            raise ValueError("%s: Position %s ausserhalb der Groesse %s"
                             % (os.path.basename(path), pos, size))
    return shifted


def validate_accelerator(blocks: Dict[Pos, str]) -> None:
    band = dict((p, True) for p in accelerator_band(25))
    for (dx, dz) in band:
        for dy in (-1, 0, 1):
            pos = (dx + 25, dy + 1, dz + 25)
            s = dx * dx + dz * dz
            if dy != 0:
                if pos not in blocks:
                    raise ValueError("Beschleuniger: Boden/Deckel unvollstaendig bei %s" % (pos,))
            elif s >= 601 or s <= 506:
                if pos not in blocks:
                    raise ValueError("Beschleuniger: Wand unvollstaendig bei %s" % (pos,))
            elif pos in blocks:
                raise ValueError("Beschleuniger: Tunnel %s ist nicht leer" % (pos,))
    if len(blocks) != 1500:
        raise ValueError("Beschleuniger: %d Bloecke statt 1500" % len(blocks))


def validate_reactor(blocks: Dict[Pos, str]) -> None:
    wall = 0
    for x in range(-12, 13):
        for y in range(-4, 5):
            for z in range(-12, 13):
                dx, dy, dz = abs(x), abs(y), abs(z)
                pos = (x + 12, y + 4, z + 12)
                if dx <= 2 and dy <= 2 and dz <= 2:
                    continue
                if is_torus_wall(dx, dy, dz):
                    wall += 1
                    if pos not in blocks:
                        raise ValueError("Reaktor: Wandzelle %s leer" % (pos,))
    if wall != 2132:
        raise ValueError("Reaktor: %d Wandzellen statt 2132" % wall)
    core = [p for p in blocks if abs(p[0] - 12) <= 2 and abs(p[1] - 4) <= 2 and abs(p[2] - 12) <= 2]
    if len(core) != 125:
        raise ValueError("Reaktor: %d Core-Zellen statt 125" % len(core))


def validate_tower(blocks: Dict[Pos, str]) -> None:
    for y in range(9):
        r = 2 if (y == 0 or y == 8) else (1 if 3 <= y <= 5 else 2)
        for x in range(-r, r + 1):
            for z in range(-r, r + 1):
                pos = (x + 2, y, z + 2)
                wall = abs(x) == r or abs(z) == r
                if wall and pos not in blocks:
                    raise ValueError("Kuehlturm: Wandzelle %s leer" % (pos,))
                if not wall and pos in blocks:
                    raise ValueError("Kuehlturm: Innenraum %s ist nicht leer" % (pos,))
    if len(blocks) != 120:
        raise ValueError("Kuehlturm: %d Bloecke statt 120" % len(blocks))


def validate_vacuum_pump(blocks: Dict[Pos, str]) -> None:
    """Der Wuerfel ist 3x3x3. Die beiden Ports stehen nebeneinander."""
    if len(blocks) != 28:
        raise ValueError("Vakuumpumpe: %d Bloecke statt 28" % len(blocks))
    ports = [p for p, s in blocks.items() if s.startswith("overdrive:vacuum_output_port")]
    if len(ports) != 1:
        raise ValueError("Vakuumpumpe: %d Ausgangsports statt 1" % len(ports))
    # Der Wuerfel reicht von z=0 bis z=2 (0-basiert). Der Port sitzt in der
    # hinteren Ebene, direkt neben dem Energie-Port.
    port = ports[0]
    if port[2] != 2:
        raise ValueError("Vakuumpumpe: Ausgangsport %s liegt nicht in der hinteren Ebene" % (port,))
    energy = [p for p, s in blocks.items() if s.startswith("overdrive:vacuum_energy_port")]
    if len(energy) != 1:
        raise ValueError("Vakuumpumpe: %d Energieports statt 1" % len(energy))
    if abs(energy[0][0] - port[0]) > 1 or abs(energy[0][1] - port[1]) > 1:
        raise ValueError("Vakuumpumpe: die Ports %s und %s liegen nicht nebeneinander"
                         % (energy[0], port))
    # Alles andere liegt im Wuerfel (z<=2) oder ist das Rohr dahinter (z>2).
    for pos in blocks:
        if pos[2] > 2 and not blocks[pos].startswith("overdrive:vacuum_pipe"):
            raise ValueError("Vakuumpumpe: Block %s liegt ausserhalb des Wuerfels" % (pos,))
    # Die Pumpe sitzt in der Mitte der vorderen Ebene.
    pumps = [p for p, s in blocks.items() if s.startswith("overdrive:vacuum_pump")]
    if pumps != [(1, 1, 0)]:
        raise ValueError("Vakuumpumpe: Pumpe sitzt bei %s statt (1, 1, 0)" % (pumps,))


def validate_electron_gun(blocks: Dict[Pos, str]) -> None:
    """Kanone, Target und Port liegen in einer Reihe: Kanone -> +1 Target
    -> +2 Positronen-Port. Das Vakuumrohr steckt hinter der Kanone."""
    if len(blocks) != 4:
        raise ValueError("Elektronenkanone: %d Bloecke statt 4" % len(blocks))
    states = {p: s for p, s in blocks.items()}
    gun = (0, 0, 2)
    target = (0, 0, 1)
    port = (0, 0, 0)
    pipe = (0, 0, 3)
    if not states.get(gun, "").startswith("overdrive:electron_gun"):
        raise ValueError("Elektronenkanone: Kanone fehlt bei %s" % (gun,))
    if not states.get(target, "").startswith("overdrive:tungsten_target"):
        raise ValueError("Elektronenkanone: Target fehlt bei %s" % (target,))
    if not states.get(port, "").startswith("overdrive:positron_vacuum_output_port"):
        raise ValueError("Elektronenkanone: Positronen-Port fehlt bei %s" % (port,))
    if not states.get(pipe, "").startswith("overdrive:vacuum_pipe"):
        raise ValueError("Elektronenkanone: Rohr fehlt bei %s" % (pipe,))
    if "facing:north" not in states[gun]:
        raise ValueError("Elektronenkanone: Kanone blickt nicht nach Norden")
    # Das Rohr muss an der Kanone haengen, nicht am Positronen-Port.
    if pipe[2] <= gun[2]:
        raise ValueError("Elektronenkanone: Das Vakuumrohr muss hinter der Kanone liegen")


# --------------------------------------------------------------------------
def main() -> None:
    print("Erzeuge GuideME-Strukturen:")

    structures = [
        ("vacuum_pump.snbt", build_vacuum_pump()),
        ("particle_accelerator.snbt", build_particle_accelerator()),
        ("ad_ring.snbt", build_square_ring(
            4, 3, "ad_casing", "ad_controller", "ad_energy_input_port",
            "ad_vacuum_port", "ad_input_port", "ad_output_port")),
        ("elena_ring.snbt", build_square_ring(
            2, 1, "elena_casing", "elena_controller", "elena_energy_input_port",
            "elena_vacuum_port", "elena_input_port", "elena_output_port")),
        ("electron_gun.snbt", build_electron_gun()),
        ("annihilation_reactor.snbt", build_annihilation_reactor()),
        ("cooling_tower.snbt", build_cooling_tower()),
    ]

    for name, (blocks, notes) in structures:
        path = os.path.join(OUT_DIR, name)
        write_snbt(path, blocks, notes)

    print("\nValidierung:")
    for name, (blocks, _notes) in structures:
        path = os.path.join(OUT_DIR, name)
        expected = {}
        for pos, (block_id, props) in blocks.items():
            key = block_id
            if props:
                key += "{%s}" % ",".join("%s:%s" % (k, v) for k, v in sorted(props.items()))
            expected[pos] = key
        shifted = validate(path, expected)
        if name == "particle_accelerator.snbt":
            validate_accelerator(shifted)
        elif name == "annihilation_reactor.snbt":
            validate_reactor(shifted)
        elif name == "cooling_tower.snbt":
            validate_tower(shifted)
        elif name == "vacuum_pump.snbt":
            validate_vacuum_pump(shifted)
        elif name == "electron_gun.snbt":
            validate_electron_gun(shifted)
        print("  %-28s ok" % name)
    print("Alle Strukturen sind gueltig.")


if __name__ == "__main__":
    main()
