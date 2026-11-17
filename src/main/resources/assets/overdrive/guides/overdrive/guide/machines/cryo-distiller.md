---
navigation:
  title: Cryo Distiller
  icon: overdrive:cryo_distiller
  parent: index.md
item_ids:
  - overdrive:cryo_distiller
categories:
  - Machines
  - Cooling
---

# Cryo Distiller

The cryo distiller produces **cryofluid** from water. Cryofluid is the
coolant of choice for the [annihilation reactor](annihilation-reactor.md)
because it removes far more heat per millibucket than water.

## Structure

The distiller is a single block. It needs a **catalyst block directly below
it** – without that block the machine does not work. The GUI shows which
catalyst is expected.

<BlockImage id="overdrive:cryo_distiller" scale="2" />

## Operation

| Property | Value                                      |
|---|--------------------------------------------|
| Energy buffer | 1,000,000 FE                               |
| Energy per tick | 10,000 FE                                  |
| Input / output tank | 32,000 mB each                             |
| Process time | 20 ticks (blue ice), 60 ticks (packed ice) |

The standard recipe turns 250 mB of water into 100 mB of cryofluid with a
catalyst of blue ice or packed ice.

## Recipes

<RecipeFor id="overdrive:cryo_distiller" />
