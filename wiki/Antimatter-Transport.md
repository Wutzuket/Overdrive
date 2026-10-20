# Antimatter Transport and Storage

Overdrive ships its own transport and storage system for **antimatter**. It is
completely independent from Minecraft's fluid system: there is no
`FlowingFluid`, no `FluidType` and no bucket. Antimatter is a registered data
type that is moved around with dedicated **cells**.

## Antimatter types

Antimatter types live in their own registry (`overdrive:antimatter`). Every
type has a registry name, a display name and a color that is used for the
glowing stream inside the pipes and for the tank gauge. The types correspond to
the substances of the mod:

| Type | Color | Produced by |
| --- | --- | --- |
| `overdrive:antiprotons` | violet | Particle Accelerator |
| `overdrive:decelerated_antiprotons` | orange | AD Ring |
| `overdrive:cooled_antiprotons` | light blue | ELENA Ring |
| `overdrive:positrons` | magenta | Electron Gun |

A tank or a pipe can only ever hold **one** type at a time. Machines that want
to be connected to the pipe network have to implement the
`IAntimatterHandler` interface, which is exposed through the
`overdrive:antimatter_handler` block capability.

The antiprotons and the positrons are **no fluids**. There are no fluid blocks
and no buckets for them, they are only moved with the antimatter pipe network
and the antimatter cells. The recipes of the machines work with the antimatter
types, the JSON format is:

```json
{
  "type": "overdrive:ad_recipe",
  "input_antimatter": { "type": "overdrive:antiprotons", "amount": 20000 },
  "energy_cost": 100000000,
  "process_time": 1,
  "output_antimatter": { "type": "overdrive:decelerated_antiprotons", "amount": 20000 }
}
```

## Antimatter Pipe

The pipe is the transport element. It is a transparent glass tube that connects
to every neighbouring pipe and to every machine that implements
`IAntimatterHandler`.

### Side configuration

Every one of the six sides can be configured independently. Use the **Overdrive
wrench** and click on the arm you want to change. The mode cycles through:

| Mode | Behaviour |
| --- | --- |
| Normal | The side does not take anything out of the machine and does not feed it either. It only passes the antimatter through the pipe network. |
| Pull | The pipe takes antimatter out of the machine on this side. |
| Push | The pipe feeds antimatter into the machine on this side. |
| Disabled | The side is blocked and rendered with a metal cap. |

Clicking the center of a pipe cycles the mode of all connected sides at once.

Only a side that is configured as **pull** takes antimatter out of a machine, so
a machine is never emptied by accident. To move antimatter from a machine into
a tank, set the side at the machine to **pull** and the side at the tank to
**push**. Everything in between can stay on normal.

A pulling side always takes the antimatter out of the machine. When there is no
pushing side that can accept it, the antimatter waits in the pipes until it can
be delivered. The pipes are the buffer of the network.

When the transfer is done the source is empty, the target holds the whole amount
and no antimatter is left over in the pipes.

### How the sides look

The configuration of a side is visible directly on the pipe, it is part of the
model and not an overlay:

- A **pull** side gets a wide cyan funnel around the end of the tube, the pipe
  flares out towards the machine it takes the antimatter from.
- A **push** side gets a small orange spout that reaches into the tube, it
  points into the machine it feeds.
- A **disabled** side gets a metal cap and is completely closed.

The wrench shows the new mode of the side above the hotbar, so it is visible
that the configuration really changed.

The glowing stream inside the pipe is drawn thinner the less antimatter is in
it, so a pipe that only holds a little shows a thin thread instead of a
completely filled tube.

### Rendering

- A pipe with two opposite connections is rendered as **one continuous tube**
  without the bulky center cube.
- Every other combination (corners, intersections, dead ends) uses the closed
  center cube. A pipe with a pulling or a pushing side always uses the center
  cube, so the funnel is always visible.
- The tube ends that would be visible inside the center block are not rendered
  at all, so the player never sees intersecting tube ends or internal walls.
- A glowing stream in the color of the transported antimatter is rendered
  inside the glass tube.

### Networks

All connected pipes are merged into a single **antimatter network**. The
network is ticked once per server tick and executes the whole transport in one
place, so large pipe networks do not become a performance problem. A network
also keeps its pipes synchronized: the antimatter is distributed evenly over all
segments of the network.

## Antimatter Tank

The tank stores antimatter and has its own gui with an input slot, an output
slot and a gauge that shows the type and the fill level of the tank. Right
click the tank to open it.

The tank is a solid block of metal with a frame around it. How much antimatter
is inside of it is shown by the gauge in its gui. The tank is a single block,
it does not combine with other tanks.

### Creative tanks

For every type of antimatter there is a **creative tank** that is created
automatically. Right click a normal antimatter tank with it and the tank becomes
creative: it always holds a full amount of its antimatter and never runs out,
so it can feed a machine forever. The gui of the tank shows that it is creative.
### Cell mechanics

Antimatter can only be moved by hand with **cells**:

- An **empty cell** in the input slot is filled from the tank. The filled cell
  is moved to the output slot. The empty cell takes over the type of the tank,
  so it can be used for every type of antimatter.
- A **full cell** in the input slot is emptied into the tank. The empty cell is
  moved to the output slot.
- The type of the cell has to match the content of the tank. A cell that holds
  positrons is never processed by a tank that holds antiprotons and the other
  way around.

There is one cell item for every type of antimatter plus the universal empty
cell. A full cell of a type is the item of that type, so the tooltip always
shows what is inside of it. When a cell is emptied it becomes the universal
**Empty Antimatter Cell** again.

The empty cells can be stacked up to 16, a filled cell never stacks because
every cell holds its own amount of antimatter. The tank only processes one cell
per cycle, so several empty cells in the input slot are filled one after the
other and no antimatter is created out of nothing.

## Machine ports

The machines of the mod store the antimatter in their own tanks, their ports
expose the antimatter capability directly. The antimatter pipe network can
therefore be plugged into them:

| Port | Antimatter |
| --- | --- |
| Particle Accelerator – Output | Antiprotons |
| AD Ring – Input | Antiprotons |
| AD Ring – Output | Decelerated Antiprotons |
| ELENA Ring – Input | Decelerated Antiprotons |
| ELENA Ring – Output | Cooled Antiprotons |
| Electron Gun – Output | Positrons |
| Positron Vacuum Output Port | Positrons |
| Annihilation Reactor – Cell Docking Port | Antiprotons and Positrons |

One antimatter unit equals one mB of the matching fluid. The antiprotons and
the positrons are not blocks and there are no buckets for them, they are only
moved with the antimatter pipe network and the antimatter cells. The ports of
the machines therefore only offer the antimatter capability and no fluid
capability anymore.

Set the side of the machine to **pull** and the side of the tank to **push** to
move the antimatter of a machine into a tank.

The JEI recipes of the accelerator, the AD ring, the ELENA ring and the
annihilation reactor show the antimatter type that belongs to the substance of
the recipe.

## Recipes

- **Antimatter Pipe** – 6 pipes from iron ingots and glass
- **Antimatter Tank** – iron ingots and glass
- **Antimatter Cell** – an iron ingot and a piece of glass
- **Antiprotons Cell** – an empty cell and an amethyst shard
- **Positrons Cell** – an empty cell, an amethyst shard and redstone
- **Decelerated Antiprotons Cell** – an empty cell, an amethyst shard and coal
- **Cooled Antiprotons Cell** – an empty cell, an amethyst shard and ice

The pipe and the tank are configured with the **Overdrive wrench**.

## Adding a new type of antimatter

A new type of antimatter is created with a single line in
`ODAntimatters.TYPES`:

```java
new Antimatter(Overdrive.id("my_antimatter"), "antimatter.overdrive.my_antimatter", 0xFF00FF00),
```

Everything else happens automatically:

- the type is registered in the `overdrive:antimatter` registry
- a **cell item** (`overdrive:my_antimatter_cell`) is registered, it never stacks
- the **icon of the cell is drawn procedurally** in the color of the type, there is
  no texture file for it
- the cell shows up in the **creative tab**
- the type shows up in the **JEI** ingredient list and in the recipes

Only the display name needs a translation (`antimatter.overdrive.my_antimatter`).

## Pipe tiers

There are four tiers of the antimatter pipe. Every tier has its own color and
its own transfer rate, the rates can be changed in the configuration.

| Tier | Color | Rate | Config |
| --- | --- | --- | --- |
| Basic Antimatter Pipe | green | 4 000 mB/t (4 B/t) | `antimatter.pipeBasicRate` |
| Advanced Antimatter Pipe | red | 8 000 mB/t (8 B/t) | `antimatter.pipeAdvancedRate` |
| Elite Antimatter Pipe | blue | 16 000 mB/t (16 B/t) | `antimatter.pipeEliteRate` |
| Ultimate Antimatter Pipe | purple | 32 000 mB/t (32 B/t) | `antimatter.pipeUltimateRate` |

The slowest pipe of a line limits the speed of the whole line.

The tier only changes the color of the **center** of the pipe, the glass tube
and the caps keep their normal look. All tiers share the same models, the
color is applied as a tint on the center. The straight pipes, that are drawn
as one long tube, get a small colored ring in the middle.

## The One Probe

## JEI

The antimatter is registered in JEI like a fluid:

- every type has its own entry in the ingredient list
- the recipes show a gauge with the fill level in the color of the antimatter
- the amount is written under the slot and shown again in the tooltip when you hover
  over the slot (for example `Antimatter: Antiprotons (20000 mB)`)

Looking at a pipe with the The One Probe shows the content of the **whole
network** the pipe belongs to, not only of the pipe that is looked at. Like in
Mekanism only the bar is displayed, the amount can be read by hovering over it.
The bar uses the color of the pipe tier.

The tank shows its own content in the same way.

## Configuration

The common Overdrive configuration file contains the following values:

| Key | Default | Description |
| --- | --- | --- |
| `antimatter.tankCapacity` | 32000 | Capacity of a single tank. It should be at least as big as the rate of the fastest pipe, because a pipe can only take out what the tank holds |
| `antimatter.cellCapacity` | 4000 | Capacity of a single cell |
| `antimatter.pipeBasicRate` | 4000 | mB a basic pipe moves per tick and side (4 buckets) |
| `antimatter.pipeAdvancedRate` | 8000 | mB an advanced pipe moves per tick and side (8 buckets) |
| `antimatter.pipeEliteRate` | 16000 | mB an elite pipe moves per tick and side (16 buckets) |
| `antimatter.pipeUltimateRate` | 32000 | mB an ultimate pipe moves per tick and side (32 buckets per tick) |

### Throughput

A pipe moves at most its own rate, and the slowest pipe of a line sets the
tempo for the whole line. A machine can only give away what its tank holds,
so the capacity of the tank also limits the flow: with the default values an
ultimate pipe really moves about 32 000 mB per tick.

The antimatter is not spread out by diffusion, it is carried through the line
towards the machine that takes it. All pipes of a network therefore hold
roughly the same amount, no matter how long the line is, and the glowing
stream in the pipes is equally thick everywhere. Only a small part is always
on the way, that is the part that is in transit in this tick.
