# Machines and Systems

## Particle accelerator

The particle accelerator processes high-energy inputs and is used in the
antimatter production chain. Its controller, casing, energy input and vacuum
components must be assembled as a valid multiblock.

## Antimatter systems

The antimatter systems provide advanced processing for antiprotons, positrons
and related fluids. The accelerator and supporting extraction systems prepare
the inputs used by the annihilation generator.

## Antimatter transport and storage

Antimatter itself is transported and stored with a completely custom system
that is independent from Minecraft's fluids: transparent pipes with a per side
configuration, a dedicated tank and cells that are filled through the tank gui.
See [Antimatter Transport and Storage](Antimatter-Transport) for the details.

## Annihilation generator

The annihilation generator combines antimatter inputs with coolant to produce
energy and processed fluids. Its burn rate and temperature behavior can be
configured in the common Overdrive configuration. Provide reliable coolant
processing and monitor the reactor temperature when increasing the burn rate.

## Vacuum pump

The vacuum pump evacuates a vacuum network of pipes. The volume of the network
consists of the pump structure, the pipes and the blocks of every multiblock
that is connected to it (particle accelerator, AD ring, ELENA ring, annihilation
reactor, tungsten machine). The bigger the volume, the longer the pump needs
to evacuate it and the more energy it consumes per tick. The consumption can be
scaled linearly or exponentially in the common configuration, see
[Configuration](Configuration).

## Cooling tower

The cooling tower processes hot fluids and supports the thermal management
required by advanced machines. It is part of the progression toward stable
high-energy production.

## Cryo distiller

The cryo distiller processes ice-based inputs into cryofluid. Cryogenic
processing supplies coolant for systems that operate at high temperatures.

## Scanner

The scanner recognizes supported multiblock components and provides access to
scanner recipes. It is useful when learning the structure of a new machine or
checking which blocks belong to a multiblock system.

## Hologram blocks

The standard Hologram Block can display multiblock structures in-world. The
Advanced Hologram Block adds programmable Lua-based displays and interaction.
