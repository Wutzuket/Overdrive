# In-Game Handbook

Overdrive ships a complete in-game handbook for every machine and multiblock.
It is rendered by [GuideME](https://modrinth.com/mod/guideme), an optional
integration: without GuideME the mod works exactly as before, the handbook is
simply not available.

## Getting the handbook

* Install [GuideME](https://modrinth.com/mod/guideme) for your Minecraft
  version.
* The handbook appears in the **Overdrive: Antimatter** creative tab as
  *Overdrive Handbook*.
* Alternatively give yourself the guide item with
  `/guideme give @s overdrive:guide`.

## Using it

* Press **G** while hovering an item to jump to the page that describes it.
  The relevant pages are linked through the `item_ids` frontmatter.
* Item names written as links inside the handbook lead to the matching page.
* Every multiblock page contains a **3D scene** of the complete structure,
  including annotations that explain the individual components. The scenes are
  interactive: you can rotate and zoom them with the mouse.
* **Ports are marked in green.** Hovering a green block shows what the port
  does and which other ports are accepted at that position. Every green
  marked casing can be replaced by a port - the tooltip lists them.
* The recipes of the machines are shown with the usual JEI recipe widgets.

## Contents

| Page | Content |
|---|---|
| Vacuum System | 3x3x3 pump structure, pipes, ports, volume and energy |
| Particle Accelerator | Ring with radius 25, three layers, 1500 blocks |
| AD Ring | Square ring with radius 4, three layers, 168 blocks |
| ELENA Ring | Square ring with radius 2, three layers, 72 blocks |
| Electron Gun | Electron gun, target and positron port in a line |
| Annihilation Reactor | 25x25x25 torus with the 5x5x5 core |
| Cooling Tower | 5x5x9 tower with hyperbolic waist and fixed port slots |
| Cryo Distiller | Single block machine with catalyst below |
| Circuit Press | Microcrafting for circuit boards and scanner panels |
| Coal Generator | Small starter generator |
| Scanner | 3x3x3 scan patterns and item requirements |
| Antimatter Transport | Pipes, tanks and cells |
| Hologram Blocks | Blueprint holograms and the Lua hologram block |
| Materials and Fluids | Ores, ingots, storage blocks and fluids |

The handbook is available in English and in German. German players get the
translated pages automatically.

## Source of the guide

The pages live in the mod resources:

```
src/main/resources/assets/overdrive/guides/overdrive/guide/**/*.md
src/main/resources/assets/overdrive/guides/overdrive/guide/_de_de/**/*.md
```

The guide itself is registered in the code
(`de.wutzuket.overdrive.integration.guideme.GuideMeGuide`). That is required
because the pages use the own tag `<PortSlots />`, which marks all positions
of a structure that can be replaced by a port - like Modern Industrialization
does it. A data driven guide (`guideme_guides/guide.json`) cannot have own
tags and would override the registered guide anyway.

The 3D scenes are generated from the structure rules of the mod by
`tools/generate_guide_structures.py`. Run it with

```bash
python tools/generate_guide_structures.py
```

to rebuild and validate the `.snbt` structure files after a change to one of
the multiblock structures.
