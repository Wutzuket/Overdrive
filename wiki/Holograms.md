# Holograms

Overdrive provides two ways to visualize structures and information.

## Hologram Block

Use the Hologram Block to display multiblock structures in the world. It is
useful for checking layouts without repeatedly placing and removing machine
components.

## Advanced Hologram Block

The Advanced Hologram Block runs Lua scripts and can render:

- Colored 3D cubes
- Interactive buttons
- Floating text
- Animated objects

Scripts can also react to player interaction and redstone signals. The block
has an in-game editor, script file management and a console for printed output
and `read()` input.

Scripts are stored in the world's `holograms/` directory. A script must remain
running, usually with a loop and `sleep()`, if its holograms should stay
visible or continue updating.

For the complete Lua API, limits, error messages and examples, see the
[Advanced Hologram Lua Guide](../ADVANCED_HOLOGRAM_LUA.md).

## Example

```lua
print("Overdrive hologram online!")

cube(0, 0, 0, 1, 1, 1, {1, 0.2, 0.2})
text(0, 1.5, 0, 0.03, 0, 0, 0, "Hello from Overdrive", {1, 1, 1}, "center")

while true do
    sleep(1)
end
```
