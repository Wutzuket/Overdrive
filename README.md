# Overdrive

Overdrive is a Minecraft technology mod for **Minecraft 1.21.1** and
**NeoForge 21.1.x**. It adds large-scale energy, antimatter, cooling and
automation systems, together with in-game tools for discovering and viewing
multiblock recipes.

## Features

- Particle accelerator systems for producing and processing antimatter.
- Antimatter annihilation reactors with configurable burn rates, temperature
  handling and coolant processing.
- Cooling towers, cryogenic fluids, steam and liquid iridium.
- A custom antimatter transport and storage system with transparent pipes,
  per side configuration, networked transport and antimatter cells that is
  completely independent from Minecraft's fluid system.
- Cryo distillation for advanced fluid processing.
- A scanner that recognizes multiblock components and exposes scanner recipes.
- Hologram blocks for visualizing multiblock structures.
- An Advanced Hologram Block with Lua scripting, interactive buttons, text,
  animation and a built-in console.
- JEI integration for Overdrive recipes and machine categories.
- The One Probe integration and optional Connected Textures Mod support.
- New ores, resources, machines, blocks, fluids and crafting recipes.

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.1**
- Java **21**
- [Just Enough Items (JEI)](https://modrinth.com/mod/jei) is recommended for
  viewing recipes in-game.

The One Probe and Connected Textures Mod are optional integrations. The
development environment also includes Mekanism for testing some of the
multiblock and resource-processing systems.

## Building from source

Clone the repository and run the Gradle build:

```bash
./gradlew build
```

On Windows, use:

```bat
gradlew.bat build
```

The resulting mod JAR is written to `build/libs/`.

## Development

This project uses the NeoForge ModDev Gradle plugin. Useful run configurations
include:

```bash
./gradlew runClient
./gradlew runServer
./gradlew runData
./gradlew runGameTestServer
```

The generated data is written to `src/generated/resources/` when the data run
is executed.

## Advanced Hologram Block

The Advanced Hologram Block can run Lua scripts stored in the world's
`holograms/` directory. Scripts can create cubes, buttons and floating text,
react to player interaction and redstone signals, and print messages to the
block's console.

See [ADVANCED_HOLOGRAM_LUA.md](ADVANCED_HOLOGRAM_LUA.md) for the complete Lua
API reference, examples and execution limits.

## Documentation

The project documentation is available in the [wiki pages](wiki/Home.md).

## License

Overdrive is currently distributed under **All Rights Reserved**. See the
project metadata for the authoritative license information.
