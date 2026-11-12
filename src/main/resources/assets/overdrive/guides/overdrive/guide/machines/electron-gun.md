---
navigation:
  title: Electron Gun
  icon: overdrive:electron_gun
  parent: index.md
item_ids:
  - overdrive:electron_gun
  - overdrive:tungsten_target
  - overdrive:tungsten_mesh
  - overdrive:tungsten_casing
  - overdrive:positron_vacuum_output_port
categories:
  - Machines
  - Antimatter
---

# Electron Gun

The electron gun produces **positrons**. Together with the cooled
antiprotons of the [ELENA ring](elena-ring.md) they are the fuel of the
[annihilation reactor](annihilation-reactor.md).

## Structure

The machine is a straight line of three blocks in the direction the electron
gun is facing:

| Distance | Block |
|---|---|
| 0 | <ItemLink id="overdrive:electron_gun" /> |
| 1 | <ItemLink id="overdrive:tungsten_target" /> |
| 2 | <ItemLink id="overdrive:positron_vacuum_output_port" /> |

The blocks must stand directly next to each other. The
<ItemLink id="overdrive:positron_vacuum_output_port" /> is both the antimatter
output and the vacuum port of the machine.

Both positions are **fixed**: the target has to stand exactly one block in
front of the gun and the positron port exactly two blocks in front of it.
Nothing else is accepted there.

The vacuum does **not** go into the positron port - it goes into the
**electron gun itself**. The <ItemLink id="overdrive:vacuum_pipe" /> therefore
has to touch the gun, for example behind it. Only then does the gun see the
vacuum level of the pump network.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../structures/electron_gun.snbt" />
  <BlockAnnotation x="0" y="0" z="2" color="#ffaa00">
    **Electron Gun**<br />Shoots at the target and takes the vacuum from the pipe.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="0" z="1" color="#ff5555">
    **Tungsten Target**<br />Holds the tungsten mesh that is used up by every operation.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="0" z="0" color="#55ff55">
    **Positron Vacuum Output Port**<br />Stores the positrons and leads them into the antimatter network. Fixed position: it can not be replaced by another port.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="0" z="3" color="#5599ff">
    **Vacuum Pipe**<br />Feeds the vacuum into the electron gun.
  </BlockAnnotation>
</GameScene>

## Required components

| Block | Amount |
|---|---|
| <ItemLink id="overdrive:electron_gun" /> | 1 |
| <ItemLink id="overdrive:tungsten_target" /> | 1 |
| <ItemLink id="overdrive:positron_vacuum_output_port" /> | 1 |
| <ItemLink id="overdrive:tungsten_mesh" /> | 1 per operation |
| <ItemLink id="overdrive:tungsten_casing" /> | optional, only counts towards the vacuum volume |

## Operation

| Property | Value |
|---|---|
| Energy buffer | 500,000 FE |
| Energy rate | up to 50,000 FE/t (default 50,000) |
| Vacuum required | 99 % |
| Result per operation | 4,000 mB positrons |
| Process time | 1 tick |
| Mesh wear | 100 per operation (1x) |

Every operation consumes durability from the
<ItemLink id="overdrive:tungsten_mesh" /> in the target. A fresh mesh has 100
durability, so it survives exactly one operation at rate 1x. Insert a new mesh
before the machine runs dry.

The <ItemLink id="overdrive:positron_vacuum_output_port" /> has a tank of
500,000 mB. Right click it with an empty
<ItemLink id="overdrive:empty_antimatter_cell" /> to fill the cell, or with a
filled cell to empty it.

## Failure

If the vacuum drops below 99 % the machine explodes.
