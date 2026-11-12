---
navigation:
  title: AD Ring
  icon: overdrive:ad_controller
  parent: index.md
item_ids:
  - overdrive:ad_controller
  - overdrive:ad_casing
  - overdrive:ad_energy_input_port
  - overdrive:ad_vacuum_port
  - overdrive:ad_input_port
  - overdrive:ad_output_port
categories:
  - Machines
  - Antimatter
---

# AD Ring

The antiproton decelerator is the second stage of the antimatter chain. It
**decelerates** the antiprotons produced by the
[particle accelerator](particle-accelerator.md) so that they can be cooled
down afterwards.

## Structure

The AD ring is a square ring with a Chebyshev radius of **4 blocks** and
**three layers**. The ring centre is 4 blocks in front of the controller, in
the direction the controller is facing.

* The **floor** and the **roof** layer consist of
  <ItemLink id="overdrive:ad_casing" /> only.
* The **middle layer** holds the outer wall (radius 4) and the inner wall
  (radius 3). Both are made of casing and the functional blocks.
* Every position of the middle layer can be replaced with one of the four
  ports. The ring needs at least one energy port, one vacuum port, one input
  port and one output port.
* The inside of the ring stays empty.

The complete ring consists of **168 blocks**.

<GameScene zoom="1.5" interactive={true}>
  <ImportStructure src="../structures/ad_ring.snbt" />
  <PortSlots controller="overdrive:ad_controller" />
  <BlockAnnotation x="4" y="1" z="8" color="#ffaa00">
    **Controller**<br />Sits on the outer wall and looks at the ring centre.
  </BlockAnnotation>
  <BlockAnnotation x="5" y="1" z="8" color="#55ff55">
    **AD Energy Input Port**<br />Supplies the ring with Forge Energy. Can be replaced with: <ItemLink id="overdrive:ad_vacuum_port" />, <ItemLink id="overdrive:ad_input_port" /> or <ItemLink id="overdrive:ad_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="6" y="1" z="8" color="#55ff55">
    **AD Vacuum Port**<br />Brings the vacuum level of the pump into the ring. Can be replaced with: <ItemLink id="overdrive:ad_energy_input_port" />, <ItemLink id="overdrive:ad_input_port" /> or <ItemLink id="overdrive:ad_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="7" y="1" z="8" color="#55ff55">
    **AD Input Port**<br />Antimatter pipes end here and fill the controller tank. Can be replaced with: <ItemLink id="overdrive:ad_energy_input_port" />, <ItemLink id="overdrive:ad_vacuum_port" /> or <ItemLink id="overdrive:ad_output_port" />.
  </BlockAnnotation>
  <BlockAnnotation x="8" y="1" z="8" color="#55ff55">
    **AD Output Port**<br />Pipes pull the decelerated antiprotons out of the controller tank. Can be replaced with: <ItemLink id="overdrive:ad_energy_input_port" />, <ItemLink id="overdrive:ad_vacuum_port" /> or <ItemLink id="overdrive:ad_input_port" />.
  </BlockAnnotation>
</GameScene>

## Required components

| Block | Amount |
|---|---|
| <ItemLink id="overdrive:ad_controller" /> | 1 |
| <ItemLink id="overdrive:ad_energy_input_port" /> | at least 1 |
| <ItemLink id="overdrive:ad_vacuum_port" /> | at least 1 |
| <ItemLink id="overdrive:ad_input_port" /> | at least 1 |
| <ItemLink id="overdrive:ad_output_port" /> | at least 1 |
| <ItemLink id="overdrive:ad_casing" /> | the rest |

## Operation

| Property | Value |
|---|---|
| Energy buffer | 20,000,000,000 FE |
| Energy rate | 1 – 200,000,000 FE/t (default 100,000,000) |
| Vacuum required | 90 % |
| Input / output tank | 500,000 mB each |

The standard recipe converts 20,000 mB antiprotons into 20,000 mB decelerated
antiprotons per tick. The input and output ports have no buffer of their own,
they lead directly to the tanks of the controller.

## Failure

A broken structure or a loss of vacuum makes the ring explode and destroys the
layers with 15 %, 50 % and 85 % probability. This can be disabled in the
common configuration.
