---
navigation:
  title: Antimatter Transport
  icon: overdrive:antimatter_tank
  parent: index.md
item_ids:
  - overdrive:antimatter_pipe_basic
  - overdrive:antimatter_pipe_advanced
  - overdrive:antimatter_pipe_elite
  - overdrive:antimatter_pipe_ultimate
  - overdrive:antimatter_tank
  - overdrive:empty_antimatter_cell
categories:
  - Machines
  - Antimatter
---

# Antimatter Transport

Antimatter is not a Minecraft fluid. It has its own pipe network, its own
tanks and its own cells. There are no buckets and no fluid handlers involved.

## The four antimatter types

| Type | Cell | Produced by |
|---|---|---|
| Antiprotons | <ItemLink id="overdrive:empty_antimatter_cell" /> | [Particle Accelerator](particle-accelerator.md) |
| Decelerated antiprotons | <ItemLink id="overdrive:empty_antimatter_cell" /> | [AD Ring](ad-ring.md) |
| Cooled antiprotons | <ItemLink id="overdrive:empty_antimatter_cell" /> | [ELENA Ring](elena-ring.md) |
| Positrons | <ItemLink id="overdrive:empty_antimatter_cell" /> | [Electron Gun](electron-gun.md) |

There is only **one** cell item. Its content is stored inside the item, so the
same cell is shown as *Antiprotons Cell*, *Decelerated Antiprotons Cell*,
*Cooled Antiprotons Cell* or *Positrons Cell* depending on what was filled
into it.

## Pipes

Four tiers share the same block but differ in colour and transfer rate:

| Pipe | Transfer per side and tick |
|---|---|
| <ItemLink id="overdrive:antimatter_pipe_basic" /> | 4,000 mB/t |
| <ItemLink id="overdrive:antimatter_pipe_advanced" /> | 8,000 mB/t |
| <ItemLink id="overdrive:antimatter_pipe_elite" /> | 16,000 mB/t |
| <ItemLink id="overdrive:antimatter_pipe_ultimate" /> | 32,000 mB/t |

A pipe buffers four times of its transfer rate. Use the
<ItemLink id="overdrive:wrench" /> on a pipe to change the mode of a side:

| Mode | Behaviour |
|---|---|
| Normal | Passes the antimatter through |
| Push | Pushes into the neighbouring machine |
| Pull | Takes it out of the neighbouring machine |
| Disabled | Closed, shown with a metal cap |

## Tanks and cells

The <ItemLink id="overdrive:antimatter_tank" /> stores **one** type of
antimatter with a capacity of 32,000 mB. The <ItemLink id="overdrive:empty_antimatter_cell" />
holds 4,000 mB and takes over the type of the tank it was filled from. A
filled cell is dropped back into the tank to empty it.

For every antimatter type there is a creative tank item. Right click a normal
tank with it to turn that tank into an endless source.

## Recipes

<RecipeFor id="overdrive:antimatter_tank" />
<RecipeFor id="overdrive:empty_antimatter_cell" />
<RecipeFor id="overdrive:antimatter_pipe_basic" />
