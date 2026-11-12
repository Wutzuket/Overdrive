---
navigation:
  title: Circuit Press
  icon: overdrive:circuit_press
  parent: index.md
item_ids:
  - overdrive:circuit_press
  - overdrive:circuit_board
  - overdrive:scanner_panel
categories:
  - Machines
---

# Circuit Press

The circuit press is the first machine of the microcrafting chain. It presses
the <ItemLink id="overdrive:circuit_board" /> and the
<ItemLink id="overdrive:scanner_panel" /> that the
[scanner](scanner.md) needs for the high tier machine parts.

## Structure

The press is a single block with **four input slots** and **one output
slot**. It pulls its energy directly from adjacent energy storages, so you do
not have to build a cable network around it.

<BlockImage id="overdrive:circuit_press" scale="2" />

## Operation

| Property | Value |
|---|---|
| Energy buffer | 1,000,000 FE |
| Energy per tick | 100 FE (circuit board), 300 FE (scanner panels) |
| Process time | 100 ticks (circuit board), 120 ticks (scanner panels) |
| Slot capacity | 64 items |

Both values can be changed in the common configuration.

## Recipes

| Recipe | Result |
|---|---|
| aluminium ingot + copper ingot + redstone | 1 x <ItemLink id="overdrive:circuit_board" /> |
| circuit board + glass pane + redstone | 2 x <ItemLink id="overdrive:scanner_panel" /> |

<RecipeFor id="overdrive:circuit_press" />
