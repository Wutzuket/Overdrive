---
navigation:
  title: Vacuum System
  icon: overdrive:vacuum_pump
  parent: index.md
item_ids:
  - overdrive:vacuum_pump
  - overdrive:pump_casing
  - overdrive:vacuum_energy_port
  - overdrive:vacuum_output_port
  - overdrive:vacuum_pipe
categories:
  - Machines
---

# Vacuum System

The <ItemLink id="overdrive:vacuum_pump" /> evacuates a network of
<ItemLink id="overdrive:vacuum_pipe" /> blocks. Every multiblock that needs a
vacuum – accelerator, AD ring, ELENA ring, electron gun and annihilation
reactor – reads its vacuum level from that network.

A machine starts working at **90 % vacuum**; the electron gun needs
**99 %**.

## Structure

The pump is the centre of a solid **3x3x3 block** that is built *behind* the
pump, opposite of the direction the pump is facing. The block behind the pump
itself is part of the structure, so the cube reaches three blocks deep.

The cube contains exactly **two ports** and nothing else:

* one <ItemLink id="overdrive:vacuum_energy_port" /> for the energy supply,
* one <ItemLink id="overdrive:vacuum_output_port" /> for the vacuum network.

Both ports can be placed on **any** free position of the cube. Every casing
inside the cube can be replaced by a port - the scene below only shows one
possible arrangement. The pump needs exactly one energy port and one output
port.

All remaining blocks are <ItemLink id="overdrive:pump_casing" /> or
<ItemLink id="overdrive:vacuum_pipe" />. Air inside the cube makes the pump
invalid.

<GameScene zoom="2" interactive={true}>
  <ImportStructure src="../structures/vacuum_pump.snbt" />
  <PortSlots controller="overdrive:vacuum_pump" />
  <BlockAnnotation x="1" y="1" z="0" color="#ffaa00">
    **Vacuum Pump**<br />Faces away from the structure.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="1" z="0" color="#55ff55">
    **Vacuum Energy Port**<br />Feeds the pump with Forge Energy. Can be replaced with: <ItemLink id="overdrive:vacuum_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="2" y="1" z="2" color="#55ff55">
    **Vacuum Output Port**<br />Leads into the vacuum network. Can be replaced with: <ItemLink id="overdrive:vacuum_energy_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="2" y="1" z="3" color="#5599ff">
    **Vacuum Pipe**<br />Needs a pipe or a machine port next to the output port, otherwise the pump reports a leak.
  </BlockAnnotation>
</GameScene>

## Energy and volume

| Property | Value |
|---|---|
| Energy buffer | 200,000 FE |
| Base consumption | 100 FE/t |
| Consumption per volume block | 0.2 FE/t (configurable) |
| Consumption while stable | 10 % of the above |
| Evacuation time | approx. 7.4 ticks per volume block |

The volume of the network is the pump itself (27 blocks) plus all connected
pipes plus the blocks of every connected multiblock:

| Machine | Volume |
|---|---|
| Particle accelerator | 1500 |
| AD ring | 168 |
| ELENA ring | 72 |
| Annihilation reactor | 1532 |

The bigger the network, the longer the pump needs to evacuate it and the more
energy it consumes. The scaling can be changed from linear to exponential in
the common configuration.

## Pipes and network rules

* <ItemLink id="overdrive:vacuum_pipe" /> connects the pump with the vacuum
  ports of the machines. A pipe may be used in all four directions.
* Use the <ItemLink id="overdrive:wrench" /> to disable single pipe sides. A
  disabled side separates the network.
* The <ItemLink id="overdrive:vacuum_output_port" /> must touch a pipe or a
  machine vacuum port. Without that connection the pump switches to the
  status **Leak** and stops working.
* Without a pump signal the network loses 0.01 vacuum per tick after a short
  grace period.

## Recipes

<RecipeFor id="overdrive:vacuum_pump" />
<RecipeFor id="overdrive:vacuum_pipe" />
