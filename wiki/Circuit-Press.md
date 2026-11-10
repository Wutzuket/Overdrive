# Circuit Press

The Circuit Press is an electrically powered processing machine for producing
circuit boards and other configurable recipes. It accepts up to four item
inputs in a 2x2 layout and produces one item output.

## Crafting

Craft the Circuit Press in a crafting table:

```text
Iron Ingot | Iron Ingot   | Iron Ingot
Copper Ingot | Iron Block | Copper Ingot
Redstone   | Iron Ingot   | Iron Ingot
```

The exact recipe and all processing recipes are also available in JEI.

## Using the press

1. Place the Circuit Press and provide it with Forge Energy (FE).
2. Put the recipe ingredients into the four input slots.
3. Wait for the progress arrow to complete.
4. Collect the result from the output slot.

The machine can pull energy from compatible energy-storage blocks next to it.
The left-hand energy bar shows the stored energy. Hover over it to see the
current energy, capacity and consumption. Hover over the progress arrow to see
the recipe's processing progress and energy usage.

The Circuit Press has its own recipe type, so press recipes are separate from
vanilla crafting recipes and are shown in the JEI Circuit Press category.
Recipes may use between one and four ingredients and always have one item
output.

## Microcrafting chain

The press produces the circuit board and the scanner panel:

```text
Aluminum Ingot + Copper Ingot + Redstone -> Circuit Board
Circuit Board + Glass Pane + Redstone -> Scanner Panel (2)
```

The electronic components of the rest of the mod are crafted from vanilla
materials. Every machine needs the redstone tier that belongs to it, an
aluminum ingot and often a copper ingot:

| Tier | Component |
| --- | --- |
| Basic | Redstone |
| Advanced | Comparator |
| Elite | Redstone Block |
| Ultimate | Beacon |

Basic components such as the vacuum pump, cooling tower, targets, pipes and
the scanner itself are still crafted at a crafting table. The high-tier
machines - the particle accelerator, AD, ELENA and the annihilation reactor
parts - are built only with the Scanner and require the matching redstone tier
plus aluminum ingots and copper ingots.

## Configuration

The common configuration file contains a `circuit_press` section:

| Option | Default | Meaning |
| --- | ---: | --- |
| `maxEnergy` | `1000000` FE | Internal energy capacity |
| `energyPerTick` | `100` FE/t | Default energy consumption while processing |
| `processTime` | `100` ticks | Default processing time |
| `slotCapacity` | `64` items | Maximum stack size in each machine slot |

Recipe values for `energy_per_tick` and `process_time` override the defaults
when they are greater than zero. Stop the game before editing the config and
restart it after making changes.

## Scanner and JEI

The Overdrive scanner includes a Circuit Press recipe for discovering the
machine. JEI lists the machine's crafting recipe, its processing recipes and
the required energy and processing time.

Scanner recipes can also require items. Place the required items either as
dropped items in the 3x3x3 structure (on top of a solid block) or inside the
scanner's item slots, then trigger the scanner with redstone and FE. JEI shows
the item ingredients next to the structure preview.
