# Development

Overdrive is built with Java 21, Gradle and the NeoForge ModDev plugin.

## Build

On Windows:

```bat
gradlew.bat build
```

On Linux and macOS:

```bash
./gradlew build
```

The compiled JAR is written to `build/libs/`.

## Development runs

```bash
./gradlew runClient
./gradlew runServer
./gradlew runData
```

Generated resources are written to `src/generated/resources/`.

## Values in the machine GUIs

The values shown in a machine GUI are sent with data slots
(`AbstractContainerMenu.addDataSlots`). The server sends them every tick for
the open menu, so the GUI is always up to date.

The screens must therefore read the values from the menu and not from the
block entity. The client only gets the block entity when the chunk is loaded,
so a value that is read from there stays at its old value until the chunk is
reloaded.

A machine GUI follows this pattern:

- The menu holds a `ContainerData` with one index per value and stores the
  received values in an `int[]`.
- The getters of the menu read that `int[]`.
- The screen only uses the getters of the menu.

`MenuSyncTest` checks that path for every machine menu.

## Bug reports

Please include:

- Overdrive, Minecraft and NeoForge versions
- Other installed mods
- The complete crash report or relevant log section
- Reproduction steps
- Whether the issue occurs in a new test world
