---
navigation:
  title: Annihilation Reactor
  icon: overdrive:annihilation_controller
  parent: index.md
item_ids:
  - overdrive:annihilation_controller
  - overdrive:annihilation_heavy_casing
  - overdrive:reactor_core_block
  - overdrive:core_containment_glass
  - overdrive:cell_docking_port
  - overdrive:annihilation_vacuum_port
  - overdrive:coolant_input_port
  - overdrive:steam_output_port
  - overdrive:annihilation_energy_output
  - overdrive:corium_block
categories:
  - Machines
  - Antimatter
---

# Annihilation Reactor

The annihilation reactor is the heart of Overdrive. It annihilates **cooled
antiprotons** and **positrons** and converts them into raw energy.

## Structure

The reactor is a **25 x 25 x 25** torus. The torus has a main radius of 8
blocks and a tube radius of 4 blocks, which results in 2132 wall blocks in
nine layers.

* The **wall** of the torus consists of
  <ItemLink id="overdrive:annihilation_heavy_casing" /> and the ports listed
  below. No other block and no air is allowed on the wall.
* Every position of the wall can be replaced with one of the five ports. The
  reactor needs two cell docking ports and one of every other port.
* The **inside of the tube** must stay **empty**.
* The **core** is a 5x5x5 cube in the middle of the torus. Its inner 3x3x3
  cells must be <ItemLink id="overdrive:reactor_core_block" />, the shell
  around it <ItemLink id="overdrive:core_containment_glass" />.

The controller sits on the wall and looks towards the core, so the ring centre
is 12 blocks in front of it.

<GameScene zoom="0.5" interactive={true}>
  <ImportStructure src="../structures/annihilation_reactor.snbt" />
  <PortSlots controller="overdrive:annihilation_controller" />
  <BlockAnnotation x="12" y="4" z="0" color="#ffaa00">
    **Controller**<br />Sits on the torus wall and looks towards the core.
  </BlockAnnotation>
  <BlockAnnotation x="12" y="4" z="12" color="#ff5555">
    **Reactor Core**<br />5x5x5: 27 core blocks inside, containment glass around it.
  </BlockAnnotation>
  <BlockAnnotation x="12" y="8" z="20" color="#55ff55">
    **Cell Docking Port**<br />Loads antiprotons or positrons. Two of them are required. Can be replaced with: <ItemLink id="overdrive:annihilation_vacuum_port" />, <ItemLink id="overdrive:coolant_input_port" />, <ItemLink id="overdrive:steam_output_port" /> or <ItemLink id="overdrive:annihilation_energy_output" />.
  </BlockAnnotation>
  <BlockAnnotation x="13" y="8" z="20" color="#55ff55">
    **Cell Docking Port**<br />The second cell docking port. Can be replaced with: <ItemLink id="overdrive:annihilation_vacuum_port" />, <ItemLink id="overdrive:coolant_input_port" />, <ItemLink id="overdrive:steam_output_port" /> or <ItemLink id="overdrive:annihilation_energy_output" />.
  </BlockAnnotation>
  <BlockAnnotation x="14" y="8" z="20" color="#55ff55">
    **Annihilation Vacuum Port**<br />Needs 90 % vacuum from the pump network. Can be replaced with: <ItemLink id="overdrive:cell_docking_port" />, <ItemLink id="overdrive:coolant_input_port" />, <ItemLink id="overdrive:steam_output_port" /> or <ItemLink id="overdrive:annihilation_energy_output" />.
  </BlockAnnotation>
  <BlockAnnotation x="15" y="8" z="20" color="#55ff55">
    **Coolant Input Port**<br />Water or cryofluid for the cooling circuit. Can be replaced with: <ItemLink id="overdrive:cell_docking_port" />, <ItemLink id="overdrive:annihilation_vacuum_port" />, <ItemLink id="overdrive:steam_output_port" /> or <ItemLink id="overdrive:annihilation_energy_output" />.
  </BlockAnnotation>
  <BlockAnnotation x="16" y="8" z="20" color="#55ff55">
    **Steam Output Port**<br />Collects the heated coolant. Can be replaced with: <ItemLink id="overdrive:cell_docking_port" />, <ItemLink id="overdrive:annihilation_vacuum_port" />, <ItemLink id="overdrive:coolant_input_port" /> or <ItemLink id="overdrive:annihilation_energy_output" />.
  </BlockAnnotation>
  <BlockAnnotation x="17" y="8" z="20" color="#55ff55">
    **Annihilation Energy Output**<br />Pushes the generated energy to all neighbours. Can be replaced with: <ItemLink id="overdrive:cell_docking_port" />, <ItemLink id="overdrive:annihilation_vacuum_port" />, <ItemLink id="overdrive:coolant_input_port" /> or <ItemLink id="overdrive:steam_output_port" />.
  </BlockAnnotation>
</GameScene>

## Required components

| Block | Amount |
|---|---|
| <ItemLink id="overdrive:annihilation_controller" /> | 1, on the wall |
| <ItemLink id="overdrive:cell_docking_port" /> | 2 |
| <ItemLink id="overdrive:annihilation_vacuum_port" /> | 1 |
| <ItemLink id="overdrive:coolant_input_port" /> | 1 |
| <ItemLink id="overdrive:steam_output_port" /> | 1 |
| <ItemLink id="overdrive:annihilation_energy_output" /> | 1 |
| <ItemLink id="overdrive:annihilation_heavy_casing" /> | the rest of the wall (2125) |
| <ItemLink id="overdrive:reactor_core_block" /> | 27 |
| <ItemLink id="overdrive:core_containment_glass" /> | 98 |

## Operation

| Property | Value |
|---|---|
| Internal buffer | 16,000,000,000,000 FE |
| Vacuum required | 90 % |
| Burn rate | from 0.1 upwards, adjustable in the controller GUI |
| Default recipe | 10 mB cooled antiprotons + 10 mB positrons |
| Energy per tick | 285,714 FE x burn rate |
| Temperature limit | 5,000 K |
| Threshold temperature | 3,000 K |

The reactor heats up with `19 x sqrt(burn rate)` Kelvin per tick. The cooling
circuit removes heat again: water takes away 1 K per mB, cryofluid 50 K per
mB. The <ItemLink id="overdrive:steam_output_port" /> has three dump modes
(idle, dump, dump excess) that can be switched in its GUI.

Each <ItemLink id="overdrive:cell_docking_port" /> holds 2,000,000 mB. One port
takes antiprotons, the other positrons.

## Failure

If the core overheats to 5,000 K it melts down: the whole 5x5x5 core turns
into <ItemLink id="overdrive:corium_block" /> and a pit is blasted into the
ground. The core cannot be salvaged, the reactor has to be rebuilt. The
failure mode can be switched between meltdown, explosion and none in the
common configuration.
