---
navigation:
  title: Coal Generator
  icon: overdrive:coal_generator
  parent: index.md
item_ids:
  - overdrive:coal_generator
categories:
  - Machines
---

# Coal Generator

The coal generator is the small starter generator of Overdrive. It burns fuel
like a vanilla furnace and pushes the energy into its neighbours.

<BlockImage id="overdrive:coal_generator" scale="2" />

## Operation

| Property | Value             |
|---|-------------------|
| Energy buffer | 40,000 FE         |
| Output | 200 FE/t          |
| Fuel slots | 1                 |
| Auto push | yes (configurable) |

Additional fuels can be added in the common configuration. The generator
burns every fuel as long as a vanilla furnace would burn it.

When the energy buffer is full and no energy can be pushed to a neighbour, the
generator stops burning. The fuel keeps its remaining burn time and the
generator continues as soon as there is room in the buffer again.

## Recipes

<RecipeFor id="overdrive:coal_generator" />
