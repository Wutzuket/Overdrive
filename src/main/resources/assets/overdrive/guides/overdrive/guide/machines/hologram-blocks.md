---
navigation:
  title: Hologram Blocks
  icon: overdrive:hologram_block
  parent: index.md
item_ids:
  - overdrive:hologram_block
  - overdrive:advanced_hologram_block
categories:
  - Machines
---

# Hologram Blocks

Both hologram blocks visualise structures in the world. They are the easiest
way to plan a multiblock: build the blueprint as a hologram, then copy the
real blocks onto it.

## Hologram Block

The <ItemLink id="overdrive:hologram_block" /> shows a JSON model that is
stored in the `holograms` folder of your world.

Right click the block to open its GUI:

| Control | What it does |
|---|---|
| File list | All JSON models in the world's `holograms/` folder |
| Offset X / Y / Z | Moves the hologram relative to the block |
| Scale | Scales the model |
| Rotation X / Y / Z | Rotates the model |
| Spin | Rotates the hologram automatically |
| New file | Creates a new model from the JSON you insert (up to 65,536 characters) |

<BlockImage id="overdrive:hologram_block" scale="2" />

## Advanced Hologram Block

The <ItemLink id="overdrive:advanced_hologram_block" /> runs **Lua** scripts
and can display translucent cubes, clickable buttons and floating text.

| Control | What it does |
|---|---|
| Script list | All `.lua` files in the world's `holograms/` folder |
| New Script | Creates an empty script |
| Edit | Opens the built-in code editor |
| Save | Writes the editor content back to the file |
| Delete | Deletes the selected script |
| Start / Stop | Starts or stops the script and clears the holograms |
| Console | Shows everything the script prints and accepts input for `read()` |

The most important commands of the script API:

| Command | Purpose |
|---|---|
| `cube(...)` | Creates a translucent coloured cube |
| `button(...)` | Creates a cube the player can click |
| `text(...)` | Creates floating text |
| `transition(...)` | Animates objects |
| `sleep(t)` | Pauses the script |
| `print(...)` / `read(...)` | Console output and input |
| `is_powered` | Redstone power of the block |
| `getSignalStrength(dir)` | Redstone signal of a side |

Limits: 512 objects and transitions per script, 100,000 instructions per
step and a pick range of 8 blocks.

<BlockImage id="overdrive:advanced_hologram_block" scale="2" />

## Recipes

<RecipeFor id="overdrive:hologram_block" />
<RecipeFor id="overdrive:advanced_hologram_block" />
