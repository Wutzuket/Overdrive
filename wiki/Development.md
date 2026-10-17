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

## Bug reports

Please include:

- Overdrive, Minecraft and NeoForge versions
- Other installed mods
- The complete crash report or relevant log section
- Reproduction steps
- Whether the issue occurs in a new test world
