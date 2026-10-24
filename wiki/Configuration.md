# Configuration

Overdrive uses NeoForge's common configuration system. Configuration files are
created in the instance's `config` directory after the game is started.

The common configuration controls values such as:

- Annihilation generator burn rate
- Reactor temperature and cooling behavior
- Particle accelerator settings
- Vacuum pump volume and energy consumption
- Cooling tower processing
- Circuit Press energy capacity, energy consumption, processing time and slot
  capacity

Stop the game before editing a configuration file. After changing values,
restart the game and verify the result in a test world before using the
settings in a long-running production setup.

Configuration changes affect gameplay balance and may change the amount of
energy, coolant or processing time required by a machine.

## Circuit Press

The `circuit_press` section controls the Circuit Press. The default values are
`maxEnergy = 1000000`, `energyPerTick = 100`, `processTime = 100` and
`slotCapacity = 64`. Values are measured in FE, FE/t, ticks and items
respectively. Individual press recipes can override the default energy usage
and processing time.

## Vacuum pump

The `vacuum` section controls the vacuum pump. The pump measures the volume of
everything that is connected to its vacuum network: its own structure, the
vacuum pipes and the blocks of the connected multiblocks (particle accelerator,
AD ring, ELENA ring, annihilation reactor, tungsten machine). The volume
determines how long the evacuation takes and how much energy the pump needs.

| Option | Meaning |
| --- | --- |
| `baseFePerTick` | Base consumption while evacuating or leaking (FE/t). |
| `fePerBlock` | Additional consumption per block of volume (FE/t), used in `LINEAR` mode. |
| `scaling` | `LINEAR` (`base + fePerBlock * volume`) or `EXPONENTIAL` (`base * exponentialFactor ^ volume`). |
| `exponentialFactor` | Factor per block of volume, used in `EXPONENTIAL` mode. |
| `stableFactor` | Share of the consumption while the vacuum is stable. |
| `evacTicksPerBlock` | Ticks of evacuation per block of volume. |
| `countMachineBlocks` | If false, only the pump structure and the pipes are counted. |
