---
navigation:
  title: Overdrive Handbook
  icon: overdrive:wrench
categories:
  - Overview
---

# Overdrive Handbook

Overdrive adds large-scale energy, antimatter and cooling technology to Minecraft.
This handbook explains every machine and multiblock of the mod, including the
exact block layout you have to build.

Hold <KeyBind id="key.guideme.guide" /> while hovering an item to jump straight
to the page that describes it. Every item name written as
<ItemLink id="overdrive:vacuum_pump" /> is a link to its own page.

## The production chain

1. **Resources** – mine <ItemLink id="overdrive:aluminum_ore" />,
   <ItemLink id="overdrive:iridium_ore" /> and <ItemLink id="overdrive:tungsten_ore" />
   and smelt them into ingots.
2. **Vacuum** – build a <ItemLink id="overdrive:vacuum_pump" /> and connect it with
   <ItemLink id="overdrive:vacuum_pipe" /> to every machine that needs a vacuum.
   All advanced machines require at least 90 % vacuum.
3. **Antimatter chain** – the <ItemLink id="overdrive:particle_accelerator_controller" />
   turns liquid iridium into antiprotons, the
   <ItemLink id="overdrive:ad_controller" /> decelerates them and the
   <ItemLink id="overdrive:elena_controller" /> cools them down.
   The <ItemLink id="overdrive:electron_gun" /> produces positrons.
4. **Annihilation** – the <ItemLink id="overdrive:annihilation_controller" />
   burns antiprotons and positrons into raw energy. It needs coolant from the
   <ItemLink id="overdrive:coolant_input_port" /> and a working
   <ItemLink id="overdrive:cooling_tower_controller" /> to keep the core cool.
5. **Cooling** – the <ItemLink id="overdrive:cryo_distiller" /> produces cryofluid,
   the <ItemLink id="overdrive:cooling_tower_controller" /> condenses steam back
   into water or superheated cryofluid into cryofluid.

Antimatter is not a Minecraft fluid. It travels through its own
<ItemLink id="overdrive:antimatter_pipe_basic" /> network and is stored in
<ItemLink id="overdrive:antimatter_tank" /> blocks and
<ItemLink id="overdrive:empty_antimatter_cell" /> cells.

## Machines

<Row>
  <ItemImage id="overdrive:vacuum_pump" />
  <ItemImage id="overdrive:particle_accelerator_controller" />
  <ItemImage id="overdrive:ad_controller" />
  <ItemImage id="overdrive:elena_controller" />
</Row>

<Row>
  <ItemImage id="overdrive:electron_gun" />
  <ItemImage id="overdrive:annihilation_controller" />
  <ItemImage id="overdrive:cooling_tower_controller" />
  <ItemImage id="overdrive:cryo_distiller" />
</Row>

<Row>
  <ItemImage id="overdrive:circuit_press" />
  <ItemImage id="overdrive:coal_generator" />
  <ItemImage id="overdrive:scanner" />
  <ItemImage id="overdrive:antimatter_tank" />
</Row>

| Machine | Purpose |
|---|---|
| [Vacuum System](machines/vacuum-system.md) | Evacuates the vacuum network for every multiblock. |
| [Particle Accelerator](machines/particle-accelerator.md) | Liquid iridium to antiprotons. |
| [AD Ring](machines/ad-ring.md) | Decelerates antiprotons. |
| [ELENA Ring](machines/elena-ring.md) | Cools antiprotons down. |
| [Electron Gun](machines/electron-gun.md) | Electron gun producing positrons. |
| [Annihilation Reactor](machines/annihilation-reactor.md) | Burns antimatter into energy. |
| [Cooling Tower](machines/cooling-tower.md) | Condenses steam and hot cryofluid. |
| [Cryo Distiller](machines/cryo-distiller.md) | Produces cryofluid. |
| [Circuit Press](machines/circuit-press.md) | Presses circuit boards and scanner panels. |
| [Coal Generator](machines/coal-generator.md) | Small starter generator. |
| [Scanner](machines/scanner.md) | Crafts multiblock components in 3x3x3 patterns. |
| [Antimatter Transport](machines/antimatter-transport.md) | Pipes, tanks and cells for antimatter. |
| [Hologram Blocks](machines/hologram-blocks.md) | Visualises structures in the world. |
| [Materials and Fluids](resources/materials.md) | Ores, ingots, blocks and fluids. |

## All pages

<SubPages />
