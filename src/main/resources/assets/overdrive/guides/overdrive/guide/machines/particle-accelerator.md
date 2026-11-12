---
navigation:
  title: Particle Accelerator
  icon: overdrive:particle_accelerator_controller
  parent: index.md
item_ids:
  - overdrive:particle_accelerator_controller
  - overdrive:accelerator_casing
  - overdrive:accelerator_energy_input_port
  - overdrive:accelerator_vacuum_input_port
  - overdrive:accelerator_output
  - overdrive:iridium_target
categories:
  - Machines
  - Antimatter
---

# Particle Accelerator

The particle accelerator is the first machine of the antimatter chain. It
converts **liquid iridium** into **antiprotons**.

## Structure

The accelerator is a huge circular ring with an outer radius of **25 blocks**
and **three layers**. The ring centre is exactly 25 blocks in front of the
controller, in the direction the controller is facing.

* The **floor** and the **roof** layer are completely filled with
  <ItemLink id="overdrive:accelerator_casing" /> inside the ring band.
* The **middle layer** only has an outer wall and an inner wall. The wide
  tunnel between them must stay **empty**.
* The controller sits on the outer wall and faces the ring centre.
* Every position of the two walls can be replaced with one of the five
  functional blocks. The accelerator needs at least one target, one output,
  one energy port and one vacuum port.

The ring band is everything between 21.5 and 25.5 blocks from the centre.

<GameScene zoom="0.4" interactive={true}>
  <ImportStructure src="../structures/particle_accelerator.snbt" />
  <PortSlots controller="overdrive:particle_accelerator_controller" />
  <BlockAnnotation x="25" y="1" z="50" color="#ffaa00">
    **Controller**<br />Sits on the outer wall and looks at the ring centre.
  </BlockAnnotation>
  <BlockAnnotation x="25" y="1" z="3" color="#ff5555">
    **Iridium Target**<br />Holds 100,000 mB of liquid iridium and must be filled before the machine runs. Can be replaced with: <ItemLink id="overdrive:accelerator_energy_input_port" />, <ItemLink id="overdrive:accelerator_vacuum_input_port" /> or <ItemLink id="overdrive:accelerator_output" />.
  </BlockAnnotation>
  <BlockAnnotation x="26" y="1" z="3" color="#55ff55">
    **Accelerator Output**<br />Collects the antiprotons and feeds them into the antimatter pipe network. Can be replaced with: <ItemLink id="overdrive:accelerator_energy_input_port" />, <ItemLink id="overdrive:accelerator_vacuum_input_port" /> or <ItemLink id="overdrive:iridium_target" />.
  </BlockAnnotation>
  <BlockAnnotation x="27" y="1" z="3" color="#55ff55">
    **Accelerator Energy Input Port**<br />Supplies the accelerator with Forge Energy. Can be replaced with: <ItemLink id="overdrive:accelerator_vacuum_input_port" />, <ItemLink id="overdrive:accelerator_output" /> or <ItemLink id="overdrive:iridium_target" />.
  </BlockAnnotation>
  <BlockAnnotation x="28" y="1" z="3" color="#55ff55">
    **Accelerator Vacuum Input Port**<br />Brings the vacuum level of the pump into the ring. Can be replaced with: <ItemLink id="overdrive:accelerator_energy_input_port" />, <ItemLink id="overdrive:accelerator_output" /> or <ItemLink id="overdrive:iridium_target" />.
  </BlockAnnotation>
</GameScene>

## Required components

| Block | Amount | Position |
|---|---|---|
| <ItemLink id="overdrive:particle_accelerator_controller" /> | 1 | outer wall, middle layer, faces the centre |
| <ItemLink id="overdrive:iridium_target" /> | at least 1 | wall, middle layer |
| <ItemLink id="overdrive:accelerator_output" /> | at least 1 | wall, middle layer |
| <ItemLink id="overdrive:accelerator_energy_input_port" /> | at least 1 | wall, middle layer |
| <ItemLink id="overdrive:accelerator_vacuum_input_port" /> | at least 1 | wall, middle layer |
| <ItemLink id="overdrive:accelerator_casing" /> | the rest | floor, roof and walls |

The complete ring consists of **1500 blocks**.

## Operation

| Property | Value |
|---|---|
| Energy buffer | 50,000,000,000 FE |
| Energy rate | 1 – 800,000,000 FE/t (default 250,000,000) |
| Vacuum required | 90 % |
| Output tank | 500,000 mB |
| Target tank | 100,000 mB liquid iridium |

The standard recipe converts 1,000 mB liquid iridium into 20,000 mB
antiprotons per tick. The output block collects the antiprotons and pushes
them into the antimatter pipes – there are no buckets in this mod.

## Failure

If the structure breaks or the vacuum is lost, the accelerator explodes. The
destroy chance per layer is 15 % at the bottom, 50 % in the middle and 85 % at
the top. The behaviour can be switched off in the common configuration.
