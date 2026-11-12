---
navigation:
  title: ELENA Ring
  icon: overdrive:elena_controller
  parent: index.md
item_ids:
  - overdrive:elena_controller
  - overdrive:elena_casing
  - overdrive:elena_energy_input_port
  - overdrive:elena_vacuum_port
  - overdrive:elena_input_port
  - overdrive:elena_output_port
categories:
  - Machines
  - Antimatter
---

# ELENA Ring

The ELENA ring is the last stage of the antimatter chain. It **cools down**
the decelerated antiprotons so that they can be used as fuel in the
[annihilation reactor](annihilation-reactor.md).

## Structure

The ELENA ring is a square ring with a Chebyshev radius of **2 blocks** and
**three layers**. The ring centre is 2 blocks in front of the controller, in
the direction the controller is facing.

* The **floor** and the **roof** layer consist of
  <ItemLink id="overdrive:elena_casing" /> only.
* The **middle layer** holds the outer wall (radius 2) and the inner wall
  (radius 1).
* Every position of the middle layer can be replaced with one of the four
  ports. The ring needs at least one energy port, one vacuum port, one input
  port and one output port.
* The inside of the ring stays empty.

The complete ring consists of **72 blocks**.

<GameScene zoom="2" interactive={true}>
  <ImportStructure src="../structures/elena_ring.snbt" />
  <PortSlots controller="overdrive:elena_controller" />
  <BlockAnnotation x="2" y="1" z="4" color="#ffaa00">
    **Controller**<br />Sits on the outer wall and looks at the ring centre.
  </BlockAnnotation>
  <BlockAnnotation x="3" y="1" z="4" color="#55ff55">
    **ELENA Energy Input Port**<br />Supplies the ring with Forge Energy. Can be replaced with: <ItemLink id="overdrive:elena_vacuum_port" />, <ItemLink id="overdrive:elena_input_port" /> or <ItemLink id="overdrive:elena_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="1" y="1" z="4" color="#55ff55">
    **ELENA Vacuum Port**<br />Brings the vacuum level of the pump into the ring. Can be replaced with: <ItemLink id="overdrive:elena_energy_input_port" />, <ItemLink id="overdrive:elena_input_port" /> or <ItemLink id="overdrive:elena_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="4" y="1" z="4" color="#55ff55">
    **ELENA Input Port**<br />Antimatter pipes end here and fill the controller tank. Can be replaced with: <ItemLink id="overdrive:elena_energy_input_port" />, <ItemLink id="overdrive:elena_vacuum_port" /> or <ItemLink id="overdrive:elena_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="1" z="4" color="#55ff55">
    **ELENA Output Port**<br />Pipes pull the cooled antiprotons out of the controller tank. Can be replaced with: <ItemLink id="overdrive:elena_energy_input_port" />, <ItemLink id="overdrive:elena_vacuum_port" /> or <ItemLink id="overdrive:elena_input_port" />.
  </BlockAnnotation>
</GameScene>

## Required components

| Block | Amount |
|---|---|
| <ItemLink id="overdrive:elena_controller" /> | 1 |
| <ItemLink id="overdrive:elena_energy_input_port" /> | at least 1 |
| <ItemLink id="overdrive:elena_vacuum_port" /> | at least 1 |
| <ItemLink id="overdrive:elena_input_port" /> | at least 1 |
| <ItemLink id="overdrive:elena_output_port" /> | at least 1 |
| <ItemLink id="overdrive:elena_casing" /> | the rest |

## Operation

| Property | Value |
|---|---|
| Energy buffer | 10,000,000,000 FE |
| Energy rate | 1 – 100,000,000 FE/t (default 50,000,000) |
| Vacuum required | 90 % |
| Input / output tank | 500,000 mB each |

The standard recipe converts 20,000 mB decelerated antiprotons into 20,000 mB
cooled antiprotons per tick.
