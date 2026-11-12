---
navigation:
  title: Cooling Tower
  icon: overdrive:cooling_tower_controller
  parent: index.md
item_ids:
  - overdrive:cooling_tower_controller
  - overdrive:cooling_tower_casing
  - overdrive:cooling_tower_input_port
  - overdrive:cooling_tower_output_port
categories:
  - Machines
  - Cooling
---

# Cooling Tower

The cooling tower condenses hot media again. It is the counterpart of the
[annihilation reactor](annihilation-reactor.md): the reactor heats coolant
with its waste heat, the tower cools it down.

## Structure

The tower is a **5 x 5 x 9** shell with a hyperbolic waist. The wall radius
is 2 blocks at the base and at the top and only 1 block in the layers 3 to 5.

* In the layers 3 to 5 the outer 5x5 edge must stay **empty**.
* The inside of the tower must stay **empty**.
* The controller sits in the wall of the base layer and looks **away** from the
  tower. The tower centre is 2 blocks behind the controller.
* The **input port** is placed at centre + (2, 1, 0).
* The **output port** is placed at centre + (-2, 7, 0).

The port positions are **not** rotated with the controller. They always stay
on the X axis, no matter which way the controller faces. They are also
**fixed**: at those two positions only the matching port is accepted, every
other position of the wall must be casing.

<GameScene zoom="1.5" interactive={true}>
  <ImportStructure src="../structures/cooling_tower.snbt" />
  <BlockAnnotation x="2" y="0" z="4" color="#ffaa00">
    **Controller**<br />Sits in the base wall and looks away from the tower.
  </BlockAnnotation>
  <BlockAnnotation x="4" y="1" z="2" color="#55ff55">
    **Cooling Tower Input Port**<br />Feeds hot coolant into the tower. Fixed position: it can not be replaced by another port.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="7" z="2" color="#55ff55">
    **Cooling Tower Output Port**<br />Collects the condensed medium. Fixed position: it can not be replaced by another port.
  </BlockAnnotation>
</GameScene>

## Required components

| Block | Amount |
|---|---|
| <ItemLink id="overdrive:cooling_tower_controller" /> | 1 |
| <ItemLink id="overdrive:cooling_tower_input_port" /> | 1, at centre + (2, 1, 0) |
| <ItemLink id="overdrive:cooling_tower_output_port" /> | 1, at centre + (-2, 7, 0) |
| <ItemLink id="overdrive:cooling_tower_casing" /> | the rest (117) |

In creative mode you can fill the shell with casing and ports by right
clicking the controller with the block in your hand.

## Operation

| Property | Value |
|---|---|
| Energy | none |
| Input / output tank | 8,000 mB each |
| Steam to water | 1 mB to 1 mB, 4 ticks |
| Superheated cryofluid to cryofluid | 1,000 mB to 1,000 mB, 1 tick |

The tower works without electricity. It simply needs the correct medium in the
input port and free space in the output tank.
