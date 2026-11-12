---
navigation:
  title: Materials and Fluids
  icon: overdrive:aluminum_ingot
  parent: index.md
item_ids:
  - overdrive:aluminum_ore
  - overdrive:deepslate_aluminum_ore
  - overdrive:raw_aluminum
  - overdrive:aluminum_ingot
  - overdrive:aluminum_nugget
  - overdrive:aluminum_block
  - overdrive:iridium_ore
  - overdrive:deepslate_iridium_ore
  - overdrive:raw_iridium
  - overdrive:iridium_ingot
  - overdrive:iridium_block
  - overdrive:tungsten_ore
  - overdrive:deepslate_tungsten_ore
  - overdrive:raw_tungsten
  - overdrive:tungsten_ingot
  - overdrive:tungsten_block
  - overdrive:simple_frame
  - overdrive:creative_energy_cube
  - overdrive:wrench
categories:
  - Resources
---

# Materials and Fluids

## Ores

| Ore | Tool | World generation |
|---|---|---|
| <ItemLink id="overdrive:aluminum_ore" />, <ItemLink id="overdrive:deepslate_aluminum_ore" /> | stone | 14 veins per chunk, size 9, Y 0 – 64, in every biome |
| <ItemLink id="overdrive:iridium_ore" />, <ItemLink id="overdrive:deepslate_iridium_ore" /> | iron | none – iridium has to be provided by a data pack or mod pack |
| <ItemLink id="overdrive:tungsten_ore" />, <ItemLink id="overdrive:deepslate_tungsten_ore" /> | diamond | 8 veins per chunk, size 8, Y -16 – 48, in every overworld biome |

The aluminium generation can be switched off in the common configuration.

## Smelting

Raw ore is smelted in a furnace or a blast furnace. Every raw ore needs
**200 ticks** and gives 0.7 experience. One ingot gives nine nuggets.

<RecipeFor id="overdrive:aluminum_ingot" />
<RecipeFor id="overdrive:iridium_ingot" />
<RecipeFor id="overdrive:tungsten_ingot" />

## Storage blocks

Nine ingots make a block, a block makes nine ingots again.

<Row>
  <ItemImage id="overdrive:aluminum_block" />
  <ItemImage id="overdrive:iridium_block" />
  <ItemImage id="overdrive:tungsten_block" />
</Row>

## Fluids

| Fluid | Temperature | Note |
|---|---|---|
| <ItemLink id="overdrive:cryofluid_bucket" /> | – | coolant for the reactor, 50 K per mB |
| <ItemLink id="overdrive:steam_bucket" /> | 500 K | heated coolant |
| <ItemLink id="overdrive:superheated_cryofluid_bucket" /> | 600 K | heated cryofluid |
| <ItemLink id="overdrive:liquid_iridium_bucket" /> | 1200 K | fuel of the particle accelerator |

## Utility blocks

The <ItemLink id="overdrive:simple_frame" /> is a filler block that the
scanner recipes use as a stand-in for the missing cells of a pattern.

The <ItemLink id="overdrive:creative_energy_cube" /> is an infinite energy
source for testing. It pushes the maximum amount of energy into all six sides
every five ticks and cannot be destroyed.

The <ItemLink id="overdrive:wrench" /> switches the sides of the vacuum pipes
and the antimatter pipes and takes apart blocks that are on the `wrenchable`
tag.
