# Advanced Hologram Block — Lua Programming Guide

A complete beginner-to-expert guide for scripting the **Advanced Hologram Block**
from the Overdrive mod. This block runs **Lua** scripts and turns them into
interactive 3D holograms: colored boxes, clickable buttons and floating text.

Everything here is based on the actual source code of the mod, so every command,
parameter, limit and error message in this document is real.

---

## Table of contents

1. [What is the Advanced Hologram Block?](#1-what-is-the-advanced-hologram-block)
2. [Your first script](#2-your-first-script)
3. [The basics of the Lua language](#3-the-basics-of-the-lua-language)
4. [What was REMOVED from Lua (important!)](#4-what-was-removed-from-lua-important)
5. [Available standard Lua libraries](#5-available-standard-lua-libraries)
6. [Full command reference (globals)](#6-full-command-reference-globals)
7. [Full object reference (cube / button / text)](#7-full-object-reference-cube--button--text)
8. [Colors](#8-colors)
9. [Animation with transition()](#9-animation-with-transition)
10. [Buttons and player interaction](#10-buttons-and-player-interaction)
11. [Text elements](#11-text-elements)
12. [Redstone signals](#12-redstone-signals)
13. [The console and read()](#13-the-console-and-read)
14. [Script lifecycle and execution limits](#14-script-lifecycle-and-execution-limits)
15. [Script files and ownership](#15-script-files-and-ownership)
16. [Troubleshooting and error messages](#16-troubleshooting-and-error-messages)
17. [Example scripts](#17-example-scripts)

---

## 1. What is the Advanced Hologram Block?

The Advanced Hologram Block is a block that runs a **Lua script** and renders the
result as holograms in the world around it. It can display:

- **Cubes** — translucent, colored 3D boxes.
- **Buttons** — interactive cubes the player can click with the right-click (use) key.
- **Text** — floating text in any color and alignment.

Think of the block as a tiny computer that:

1. Runs your script a little bit **every single game tick** (20 ticks = 1 second).
2. Lets your script build and move hologram objects.
3. Listens to the player (clicks, aiming) and to redstone signals.

The holograms do **not** block movement and cannot be destroyed; they are pure
visuals. Everything is drawn around the block itself (see [coordinates](#coordinate-system)).

### Where do I get it?

Place the **Advanced Hologram Block** in your world and right-click it to open
its GUI (menu). There you can write scripts, save them as files, and start/stop
them. No external tools are needed.

### How the GUI works

The GUI (opened with right-click) has these main parts:

| Control | What it does |
|---|---|
| **Script list** | Shows all `.lua` files inside the world's `holograms/` folder. |
| **New Script** | Creates a new empty script file and selects it. |
| **Edit** | Opens the selected script in the built-in code editor. |
| **Save** | Writes the editor content into the script file (only the owner may do this). |
| **Delete** | Deletes the selected script (two-click confirmation). |
| **Start** | Starts running the selected script. |
| **Stop** | Stops the running script and clears the holograms. |
| **Console** | Shows everything the script `print()`s, and accepts text for `read()`. |

> **Important:** Starting/stopping does **not** require redstone. The block runs
> any script you select and start manually. Redstone only matters if *your script*
> decides to use it (see [Redstone signals](#12-redstone-signals)).

---

## 2. Your first script

Right-click the block, create a new script called `hello.lua`, paste this, and
press **Start**:

```lua
print("Hologram script is running!")

cube(0, 0, 0, 1, 1, 1, {1, 0.2, 0.2})          -- a red 1x1x1 cube
text(0, 1.5, 0, 0.03, 0, 0, 0, "Hello World!", {1, 1, 1}, "center")

while true do
    sleep(1)
end
```

What happens:

- `print("...")` writes the message into the block's **console**.
- `cube(...)` creates a red box in the middle of the block.
- `text(...)` creates the words "Hello World!" one and a half blocks above center.
- The `while true` loop together with `sleep(1)` keeps the script alive forever.
  **Without a loop, the script would end immediately** and the objects vanish.

This is the single most important concept: **your script ends when it reaches the
last line.** If you want the hologram to stay or to move/respond, you must keep
the script running with a `while true do ... sleep(1) end` loop.

---

## 3. The basics of the Lua language

If you already know Lua, skip this section. Otherwise, here is the minimum you
need to read any script in this guide:

```lua
-- Variables (local is best to avoid conflicts)
local name = "Max"          -- a text (string)
local number = 42           -- a number
local fraction = 0.55       -- numbers can be fractions
local flag = true           -- true or false (boolean)
local nothing = nil         -- "missing"

-- Tables: Lua's version of a "list" or "object"
local list = {1, 2, 3}           -- a list
local color = {1.0, 0.5, 0.0}    -- colors are lists of 3 numbers (see Colors)
print(list[1])                   -- prints 1 (lists start at 1!)

-- Functions
local function add(a, b)
    return a + b
end
print(add(2, 3))                 -- prints 5

-- Conditions
local x = 5
if x > 3 then
    print("bigger")
elseif x == 3 then
    print("equal")
else
    print("smaller")
end

-- Loops
for i = 1, 5 do
    print(i)
end

-- While loop (this is what you use to keep a hologram alive)
-- local count = 0
-- while true do
--     count = count + 1
--     sleep(1)
-- end
```

The mod uses standard **Lua 5.1** (via the LuaJ library). Everything you know
about Lua 5.1 applies, within the limits described next.

---

## 4. What was REMOVED from Lua (important!)

The block runs your script in a **sandbox**. Some standard Lua libraries have
been deleted for safety. If you try to use them you will get an error like
`attempt to index global 'io' (a nil value)`.

These are **removed** and must **not** be used:

| Library | Removed? | Why |
|---|---|---|
| `io` | **NO LONGER EXISTS** | No file reading/writing. `io.open`, `io.read`, `io.write` etc. are gone. |
| `os` | **NO LONGER EXISTS** | No system access. `os.time`, `os.getenv`, `os.execute`, `os.clock` etc. are gone. |
| `debug` | **NO LONGER EXISTS** | No debugger access. |
| `package` | **NO LONGER EXISTS** | No module system. |

### What that means in practice

- **No file I/O**: You cannot `io.open`, `io.read`, `io.write`, save data, or load
  files from inside a script. Script data must live in the script itself.
- **No time functions**: `os.time()`, `os.clock()`, `os.date()`, `math.randomseed()`+... —
  all gone. To measure time, count visible frames using `ticks` (see example below).
- **No `require`, no `loadfile`, no `dofile`**: These all depend on the package/io
  libraries and will **not** work. You cannot import other Lua files.
- **No module system**: `module(...)` and `require(...)` are unavailable.

### What this means for your code style

- Put all helper functions directly in your script file.
- Use `local` variables and `local function`s everywhere to keep your script tidy.
- If you want randomness, you cannot seed properly and `math.random()` may return
  the same sequence every run. That is an intentional sandbox trade-off.

### How to count time without os.time()

```lua
local ticks = 0
while true do
    ticks = ticks + 1              -- advances once per game tick
    if ticks % 20 == 0 then        -- every second (20 ticks)
        print("One second passed")
    end
    sleep(1)
end
```

---

## 5. Available standard Lua libraries

Everything below still exists and works normally:

| Library | Notes |
|---|---|
| **base** | `print` (replaced, see below), `type`, `tostring`, `tonumber`, `pairs`, `ipairs`, `next`, `select`, `assert`, `error`, `pcall`, `xpcall`, `setmetatable`, `getmetatable`, `rawget`, `rawset`, `rawequal`, `rawlen`, `unpack`, `load`, `loadstring` (loadstring works, but `loadfile`/`dofile` don't), `collectgarbage`. |
| **math** | All of `math` (except anything needing os time). `math.random`, `math.floor`, `math.abs`, `math.max/min`, `math.pi`, `math.sin/cos/tan`, etc. |
| **string** | All of `string` (`upper`, `lower`, `sub`, `find`, `match`, `rep`, `format`, `len`, `byte`, `char`, `gsub`, `gmatch`). |
| **table** | `insert`, `remove`, `concat`, `unpack`, `sort`. |
| **coroutine** | Fully available (the mod uses it internally for `sleep` and `read`). |
| **`_G`** | The global table. |

> **Note:** `print` has been **replaced** by the mod's own version that writes to
> the block's console instead of the Minecraft log (see below).

---

## 6. Full command reference (globals)

These are the functions and variables your script can call directly.

### `cube(x, y, z, sizeX, sizeY, sizeZ, rotX, rotY, rotZ, {r,g,b})`

Creates a colored box.

| Argument | Type | Default | Meaning |
|---|---|---|---|
| `x, y, z` | number | `0` | Position **relative to the center of the block** (in blocks). |
| `sizeX, sizeY, sizeZ` | number | `1` | Full width/height/depth of the box (a size of 1 = 1 block). |
| `rotX, rotY, rotZ` | number | `0` | Rotation around the X/Y/Z axis **in degrees**. |
| `{r,g,b}` | table | `{1,1,1}` | Color (see [Colors](#8-colors)). The `{...}` table goes after the rotations. |

There is a **short form** without rotations:

```lua
cube(x, y, z, sizeX, sizeY, sizeZ, {r,g,b})
```

If the 7th argument is a table, it is treated as the color and rotations become `0`.

```
cube(0, 0, 0, 1, 1, 1, {1, 0, 0})            -- red box, no rotation
cube(0, 2, 0, 2, 0.5, 2, 0, 0, 45, {0, 1, 0}) -- green "floor", rotated 45deg on Z
```

Returns a **cube object** (see [section 7](#7-full-object-reference-cube--button--text)).

---

### `button(x, y, z, sizeX, sizeY, sizeZ, rotX, rotY, rotZ, {r,g,b})`

Identical to `cube`, but the box is **clickable** by the player.

- Returns a **button object** that has extra methods: `isClicked()` and `isHovered()`.
- Buttons glow slightly brighter when hovered or clicked (other objects are a bit
  more transparent).
- A click = the player aims at the button (within 8 blocks) and presses
  **right-click (use)**. See [Buttons and player interaction](#10-buttons-and-player-interaction).

```lua
local myButton = button(0, 0, 0, 1, 1, 0.2, 0, 0, 0, {0.2, 0.6, 1})
```

> **Trick:** The `cube()` and `button()` signatures are identical, so you can
> switch a cube to a button (or back) by just changing the function name.

---

### `text(x, y, z, scale, rotX, rotY, rotZ, "string", {r,g,b}, alignment)`

Creates floating text.

| Argument | Type | Default | Meaning |
|---|---|---|---|
| `x, y, z` | number | — | Position relative to block center. |
| `scale` | number | `1` | Text size. **Use small values** like `0.02`–`0.05`; `1.0` is enormous. |
| `rotX, rotY, rotZ` | number | `0` | Rotation in degrees. |
| `"string"` | string | `""` | The text to show. |
| `{r,g,b}` | table | `{1,1,1}` | Color. |
| `alignment` | string | `"left"` | `"left"`, `"center"` or `"right"` (how the text is anchored horizontally). |

```lua
text(0, 1.5, 0, 0.03, 0, 0, 0, "Score: 10", {1, 1, 0}, "center")
```

Returns a **text object** (see [section 7](#7-full-object-reference-cube--button--text)).

---

### `sleep(duration)`

Pauses the script.

| Argument | Type | Default | Meaning |
|---|---|---|---|
| `duration` | number | `1` | How long to wait, **in ticks** (20 ticks = 1 second). Minimum is 1. |

```lua
sleep(20)   -- wait one second
sleep(1)    -- wait one tick (this is how you run code "every tick")
```

- `sleep()` guarantees at least 1 tick.
- **You must call `sleep(1)` inside every loop.** If your loop runs more than
  ~100,000 instructions in a single tick too many times in a row, the script is
  stopped with an "infinite loop" error (see [section 14](#14-script-lifecycle-and-execution-limits)).

---

### `read(prompt)`

Asks the player for text input through the block's console.

| Argument | Type | Default | Meaning |
|---|---|---|---|
| `prompt` | string | `"> "` | The message shown before the input field. |

```lua
local answer = read("What is your name?")
print("Hello, " .. answer .. "!")
```

- The script **pauses** until the player types something in the console and
  presses Enter.
- The prompt is printed in the console with a gold `> ` prefix.
- Returns the text the player entered.

---

### `print(...)`

Writes to the block's console (the GUI panel). Works exactly like normal Lua
`print`: multiple arguments are joined with spaces.

```lua
print("Hello")                       --> Hello
print("Value is", 42)                --> Value is 42
print("Color:", 1, 0, 1)             --> Color: 1 0 1
```

- The console keeps the last **100 lines**.
- `print` never produces game errors.

---

### `getSignalStrength(direction)`

Returns the redstone signal strength **0–15** at the block.

| Argument | Type | Meaning |
|---|---|---|
| *(none)* | — | Returns the strongest signal seen from any of the six sides. |
| `"north"` / `"south"` / `"west"` / `"east"` / `"up"` / `"down"` | string | Returns only that side's signal. |

```lua
print(getSignalStrength())          -- overall strength 0..15
print(getSignalStrength("north"))   -- strength from the north side
if getSignalStrength() >= 5 then
    print("Strong redstone signal!")
end
```

---

### `is_powered` (global boolean)

A special variable that is **true** whenever the block receives *any* redstone
signal (like `getSignalStrength() > 0`). It is updated every tick automatically.

```lua
if is_powered then
    print("Block is receiving power")
else
    print("Block is off")
end
```

---

## 7. Full object reference (cube / button / text)

`cube()`, `button()` and `text()` return **objects**. An object is a Lua table
with fields (properties) and methods (functions you call on it).

### Object fields

These are plain values you can **read and write directly**:

| Field | cube | button | text | Meaning |
|---|---|---|---|---|
| `x, y, z` | ✓ | ✓ | ✓ | Position relative to block center. |
| `sx, sy, sz` | ✓ | ✓ | ✓ | Size per axis (for text: kept at 1, not used for the text size). |
| `rx, ry, rz` | ✓ | ✓ | ✓ | Rotation in degrees around each axis. |
| `r, g, b` | ✓ | ✓ | ✓ | Color components (see [Colors](#8-colors)). |
| `scale` | ✗ | ✗ | ✓ | Text size (the field that actually controls text rendering). |
| `text` | ✗ | ✗ | ✓ | The current text string. |
| `alignment` | ✗ | ✗ | ✓ | `"left"`, `"center"` or `"right"`. |

Examples of direct manipulation:

```lua
local box = cube(0, 0, 0, 1, 1, 1, {1, 1, 1})

while true do
    box.ry = box.ry + 2        -- spin faster/slower by changing the +2
    box.r = 1 - math.abs(box.r)  -- simple color logic
    sleep(1)
end
```

```lua
local mytext = text(0, 1, 0, 0.03, 0, 0, 0, "Hi", {1,1,1}, "center")
mytext.text = "Updated!"        -- change the message immediately
mytext.scale = 0.05             -- make it bigger immediately
mytext.alignment = "right"
```

> **Note:** modifying fields directly is instantaneous (no smooth animation).
> For smooth motion use `:transition()` (below).

### Object methods

#### `object:transition(x, y, z, ...)`

Smoothly animates the object to new values over a given duration using
**smoothstep** easing (slow start, fast middle, slow end).

There are **two different signatures** — one for cubes/buttons and one for text.
*Note: unlike `cube()`, there is no short form for `transition`.*

**For cubes and buttons:**

```lua
object:transition(x, y, z, sizeX, sizeY, sizeZ, rotX, rotY, rotZ, {r,g,b}, durationTicks)
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `x, y, z` | number | current | Target position. |
| `sizeX, sizeY, sizeZ` | number | current | Target size. |
| `rotX, rotY, rotZ` | number | current | Target rotation. |
| `{r,g,b}` | table | current | Target color. |
| `durationTicks` | number | `20` | How long the animation takes (ticks). Minimum is 1. |

**For text:**

```lua
object:transition(x, y, z, scale, rotX, rotY, rotZ, "newText", {r,g,b}, durationTicks)
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `x, y, z` | number | current | Target position. |
| `scale` | number | current | Target text scale (see caveat below). |
| `rotX, rotY, rotZ` | number | current | Target rotation. |
| `"newText"` | string | currently empty | New text — set when the animation finishes (only if you pass a string). |
| `{r,g,b}` | table | current | Target color. |
| `durationTicks` | number | `20` | Animation duration. Minimum is 1. |

Examples:

```lua
local box = cube(0, 0, 0, 1, 1, 1, {1, 0, 0})

while true do
    box:transition(0, 1.5, 0, 1, 1, 1, 0, 0, 180, {0, 1, 0}, 40)   -- moves up, spins, turns green over 2s
    sleep(40)
    box:transition(0, -1.5, 0, 2, 2, 2, 0, 0, 0, {1, 0, 0}, 40)    -- comes back down
    sleep(40)
end
```

```lua
local label = text(0, 2, 0, 0.03, 0, 0, 0, "Loading", {1,1,1}, "center")
label:transition(0, 2, 0, 0.03, 0, 0, 0, "Done!", {0,1,0}, 60)   -- color + text change after 3s
```

Important `transition` behavior:

- Position, **rotation** and **color** are animated smoothly.
- **Size changes are applied instantly** (the size snaps immediately to the target
  at the moment you call `transition`), while positions/rotations/colors ease.
- **Text scale caveat:** in the current implementation, the rendered text size is
  controlled by the `scale` field, and `:transition()`'s scale argument doesn't
  change that field. So to change text *size*, set `obj.scale = value` directly
  instead of trying to animate it.
- Calling `:transition()` again on the same object **replaces** the running
  animation (it does not stack).
- The eased *color* blends between the two colors over time.

Both call styles work: `object:transition(...)` and `object.transition(object, ...)`
(the engine detects it automatically).

#### `object:del()`

Removes the object from the hologram immediately and cancels any running
transition for it.

```lua
local box = cube(0, 0, 0, 1, 1, 1, {1, 0, 0})
box:del()   -- gone
```

A nice way to "hide" an object temporarily without deleting it is to move it far
away instead (used in the example game):

```lua
box:transition(0, 0, -100, 1, 1, 1, 0, 0, 0, {1,0,0}, 2)   -- "teleport" to z=-100
```

#### Button methods (buttons only)

- `button:isClicked()` — returns `true` exactly once when the player clicks it,
  then resets to `false` (the click is "consumed"). Use it to trigger actions.
  ```lua
  while true do
      if myButton:isClicked() then
          print("The button was clicked!")
      end
      sleep(1)
  end
  ```
- `button:isHovered()` — returns `true` while the player is aiming at the button
  (within 8 blocks), `false` otherwise.
  ```lua
  while true do
      if myButton:isHovered() then
          myButton.r = 1        -- brighten while pointed at
      else
          myButton.r = 0.2
      end
      sleep(1)
  end
  ```

> `isClicked()` only reads on the **client side** of the script (see
> [section 14](#14-script-lifecycle-and-execution-limits) for why), which is fine —
> that is also where clicks are detected.

#### Text methods (text only)

- `textObj:setText("new text")` — changes the displayed text instantly.
  ```lua
  myText:setText("Score: 15")
  ```
- `textObj:setColor({r,g,b})` — changes the color instantly.
  ```lua
  myText:setColor({1, 0.5, 0})
  ```
- `textObj:setAlignment("center")` — changes alignment (`"left"`, `"center"`,
  `"right"`).

All three return the text object, so you can chain them:

```lua
myText:setText("Ready"):setColor({0,1,0}):setAlignment("center")
```

---

## 8. Colors

Colors are normal Lua tables with **three numbers** (R, G, B) in this order.

Two formats are accepted everywhere a color is expected (`cube`, `button`,
`text`, `transition`, `setColor`):

```lua
{1.0, 0.0, 0.0}      -- float format: each value between 0.0 and 1.0
{255, 0, 0}          -- 0-255 format: values above 1.0 are divided by 255 automatically
```

Rule: **any value greater than 1 is treated as 0–255.**

| Color | Float | 0-255 |
|---|---|---|
| Red | `{1, 0, 0}` | `{255, 0, 0}` |
| Green | `{0, 1, 0}` | `{0, 255, 0}` |
| Blue | `{0, 0, 1}` | `{0, 0, 255}` |
| Yellow | `{1, 1, 0}` | `{255, 255, 0}` |
| White | `{1, 1, 1}` | `{255, 255, 255}` |
| Orange | `{1, 0.5, 0}` | `{255, 127, 0}` |
| Cyan | `{0.2, 0.8, 1}` | `{51, 204, 255}` |

You can store a color in a variable and reuse it:

```lua
local red   = {1, 0, 0}
local blue  = {0.2, 0.2, 1}

local b1 = cube(0, 0, 0, 1, 1, 1, red)
local b2 = cube(0, 2, 0, 1, 1, 1, blue)
```

---

## 9. Animation with transition

This section gives you a mental model for animations.

- The block ticks **20 times per second**.
- `transition` records the object's **start values** right now and the **target
  values** you gave it, then over `durationTicks` smoothly interpolates using
  **smoothstep** easing.
- Every tick the object's fields (`x/y/z`, rotation, color) are updated; the
  renderer draws the current values, giving smooth motion.

A classic animated demo:

```lua
local box = cube(0, 0, 0, 0.5, 0.5, 0.5, {0.3, 0.7, 1})

while true do
    -- move up & spin, over 1 second
    box:transition(0, 2, 0, 0.5, 0.5, 0.5, 0, 0, 360, {1, 0.3, 0.3}, 20)
    sleep(20)
    -- come back down & spin back, over 1 second
    box:transition(0, 0, 0, 0.5, 0.5, 0.5, 0, 0, 0, {0.3, 0.7, 1}, 20)
    sleep(20)
end
```

Notes:

- If you want an animation that never stops, put a `sleep(duration)` right after
  each `transition` so the loop waits for it to finish.
- Animations are **not interrupted by direct field writes** — but if you then call
  a new `transition`, everything is re-baselined from the object's *current*
  values at that moment.
- `del()` cancels the animation (because it deletes the object).

---

## 10. Buttons and player interaction

Buttons bring interactivity. Here is exactly how they work:

1. The engine raycasts from the **player's eye position** along their **look
   direction**.
2. A button is "hovered" when the ray hits it **within 8 blocks** of the block.
3. When the player aims at a hovered button and presses **right-click**
   (the "use item" key, i.e. what normally opens/uses things), the button
   registers a **click**.
4. Clicking a button slightly brightens it while hovered/clicked.

In your script you poll with `isClicked()` and `isHovered()` inside your main loop:

```lua
local btn = button(0, 0, 0, 1, 1, 0.3, 0, 0, 0, {0.2, 0.6, 1})

while true do
    if btn:isClicked() then
        print("You clicked the button!")
    end
    if btn:isHovered() then
        print("You are pointing at the button")
    end
    sleep(1)
end
```

> **`isClicked()` is "edge triggered":** it only returns `true` on the tick the
> click happens (right-click press), and returns `false` afterwards until the
> next click. Holding right-click does **not** repeat it.

### Positioning buttons for the player

Remember coordinates are **relative to the block center**. A good setup for a
"screen" in front of the block is to put buttons at a positive `z` offset:

```lua
local offsetZ = 2.0
local btn = button(0, 0, offsetZ, 0.6, 0.6, 0.2, 0, 0, 0, {0.4, 0.4, 0.4})
```

---

## 11. Text elements

- Scale: use tiny numbers (`0.01`–`0.1`). `0.03` is roughly the size used for
  normal UI text in the example game. `1.0` (the default) is gigantic.
- Alignment anchors the text horizontally:
  - `"left"` — starts at the position and grows to the right.
  - `"center"` — centered on the position (most useful).
  - `"right"` — ends at the position.
- Text renders as normal Minecraft font, always full-bright.

```lua
text(0, 2, 0, 0.04, 0, 0, 0, "CENTERED", {1, 1, 0}, "center")
text(-2, 1, 0, 0.025, 0, 0, 0, "LEFT", {1, 1, 1}, "left")
text(2, 1, 0, 0.025, 0, 0, 0, "RIGHT", {1, 1, 1}, "right")
```

---

## 12. Redstone signals

Redstone is purely **optional** — the block runs regardless, but your script can
react to power:

```lua
-- Only spin while powered
local inner = cube(0, 0, 0, 0.5, 0.5, 0.5, {0.2, 0.8, 1})
local outer = cube(0, 0, 0, 1.5, 0.5, 1.5, {1, 0.4, 0.8})

while true do
    if is_powered then
        inner.ry = inner.ry + 3
    end
    sleep(1)
end
```

Use `getSignalStrength()` (see [section 6](#6-full-command-reference-globals)) when
you need the exact 0–15 value:

```lua
while true do
    local s = getSignalStrength()
    if s >= 15 then
        print("Max signal!")
    elseif s > 0 then
        print("Signal:", s)
    else
        print("No signal")
    end
    sleep(20)
end
```

> `is_powered` is `getSignalStrength() > 0`. The power comes from the standard
> `hasNeighborSignal` rules (a powered wire/lever touching the block).

---

## 13. The console and read()

The block's console (in the GUI) works like a terminal:

- Every `print(...)` appears in the console (last 100 lines kept).
- When your script calls `read(...)`, the console shows the prompt (gold, prefixed
  with `> `) and enables its input field. Type something and press **Enter**.
- The entered text is returned by `read()`.

Interactive example:

```lua
print("Interactive demo")

local name = read("What is your name?")
print("Nice to meet you, " .. name .. "!")

local number = tonumber(read("Pick a number"))
if number ~= nil then
    print("Double your number is", number * 2)
else
    print("That wasn't a number!")
end
```

> `tonumber(...)` converts a string to a number. If the input was not a number it
> returns `nil`.

---

## 14. Script lifecycle and execution limits

### How the script runs

- The script runs as a Lua **coroutine** that the engine resumes a little each
  tick. `sleep` and `read` are implemented with `coroutine.yield`.
- On **Start**: the script is compiled and executed from the top, and all
  previously created hologram objects are cleared.
- If the script **finishes** (reaches its end) it stops; "Script started"/"Script
  stopped" messages appear in the console.
- **Stop** clears all hologram objects immediately.

### Client and server both run the script

The block runs the script **twice**: once on the **server** and once on the
**client (you)**.

- The **client** copy is what actually renders the holograms and reads mouse
  clicks — button logic (`isClicked`) effectively happens there.
- The **server** copy handles the authoritative state, the console output and
  `read()` input routing (your console input is also sent to the server).

Practical consequence: keep scripts **deterministic**. Avoid depending on things
like `math.random` for state that must be consistent across the two copies. For
button-driven games (like the tic-tac-toe example) this is harmless because both
copies run the same game logic and the client drives the rendering.

### Execution limits (hard numbers from the source)

| Limit | Value |
|---|---|
| Max hologram objects (cubes + buttons + texts) | **512** |
| Max simultaneous transitions | **512** |
| Max instructions per tick | **100 000** |
| Max consecutive budget hits before kill | **600** |
| Console line history | **100** lines |
| Button interaction reach | **8** blocks |
| Script file size cap (GUI) | **32 767** characters |

If you create more than 512 objects, the next `cube()`/`button()`/`text()` call
throws: `Too many hologram objects (max 512)`. Same for transitions.

### The "infinite loop" protection

The engine counts instructions per tick. If a loop runs **without `sleep()`**
and exceeds the budget often enough (600 consecutive hits), the script is killed
with this exact message:

```
Script exceeded instruction budget (possible infinite loop) - add sleep(1) inside loops
```

**Always include `sleep(1)` (or more) inside your loops** — that is both healthy
for your hologram (updates 20×/s) and required to avoid the kill switch:

```lua
while true do
    -- ... work ...
    sleep(1)
end
```

---

## 15. Script files and ownership

- Scripts are stored as `.lua` files in the world folder: `<save>/holograms/`.
  You never need to touch this folder manually (the GUI manages it), but it's
  useful to know where files live.
- The block scans this folder and lists every `.lua` file in the GUI.
- There is an `owners.json` inside `holograms/` that records which player created
  each file.
- **Ownership rules:**
  - The player who **created** a file is its owner and can **edit/delete/select** it.
  - Other players **cannot modify** a file owned by someone else (the server
    rejects the edit with a "not owner" error).
  - Files without an owner entry can be edited by anyone.
- A file is automatically **started** when you create or select it (running = true).
- When you create a file, a sanitized name is enforced (no forbidden characters,
  forced `.lua` extension, max 32 767 characters of content).

So: **every player can have their own scripts**, but can't mess with yours.

---

## 16. Troubleshooting and error messages

| Symptom | Cause / Fix |
|---|---|
| `attempt to index global 'io' (a nil value)` | You used `io.`. It's removed — no file access. Rewrite without files. |
| `attempt to index global 'os' (a nil value)` | You used `os.`. Removed — no time/system functions. Count ticks instead. |
| `attempt to call global 'require' (a nil value)` or *"cannot get file"* | `require`/`loadfile`/`dofile` need the removed package/io libraries. Put code directly in your script. |
| Script "does nothing" / objects disappear instantly | The script reached the end. Wrap it in `while true do ... sleep(1) end`. |
| `Script exceeded instruction budget (possible infinite loop) - add sleep(1) inside loops` | A loop runs without sleeping. Add `sleep(1)` inside it. |
| `Too many hologram objects (max 512)` | You exceeded 512 objects. Delete objects you no longer need with `:del()` or reuse them via transitions. |
| `Too many transitions (max 512)` | More than 512 animations queued at once. Cap concurrent `transition()` calls. |
| Button never clicks | Aim closer than 8 blocks and use right-click. Make sure the button isn't hidden (moved far away). |
| Text looks gigantic | Scale `1` is huge. Use `0.01`–`0.05`. |
| Colors look wrong | Your `{r,g,b}` order may be off, or values are invalid. See [Colors](#8-colors). |
| Player can't edit/delete a script | You aren't the owner (see [Section 15](#15-script-files-and-ownership)). |
| `Script error: ...` in console | A runtime Lua error. The message names the failing line/function. |
| Script doesn't restart when edited | Save (or reload) the file, then press **Stop** and **Start** again (or the running state may not auto-track external edits). |

### Debugging tips

- Use `print(...)` liberally — output goes to the console and survives via the
  100-line history.
- Test small: build one cube, then one button, then add animation, then logic.
- Keep helper functions in the same file (no imports allowed).
- Remember the coordinate **origin is the block center** — if you see nothing,
  your objects might be far away (like the `-100` hiding trick). Start at
  `0,0,0`.

---

## 17. Example scripts

### 17.1 Hello world with a looping message

```lua
print("Hello from the Advanced Hologram Block!")

cube(0, 0, 0, 1, 1, 1, {0.3, 0.8, 1})

local ticks = 0
while true do
    ticks = ticks + 1
    if ticks % 20 == 0 then
        print("Still running... tick " .. ticks)
    end
    sleep(1)
end
```

### 17.2 Spinning cube that reacts to redstone

```lua
local core = cube(0, 0, 0, 0.8, 0.8, 0.8, {0.2, 0.8, 1})
local shell = cube(0, 0, 0, 1.5, 1.5, 1.5, {1, 0.4, 0.8})

while true do
    if is_powered then
        core.ry = core.ry + 3
        core.r = 1
        core.g = 0.4
    else
        core.ry = core.ry + 1
        core.r = 0.2
        core.g = 0.8
    end
    shell.rx = shell.rx + 1
    sleep(1)
end
```

### 17.3 Click counter with a button and live text

```lua
local score = 0

local label = text(0, 2, 2, 0.04, 0, 0, 0, "Score: 0", {1, 1, 1}, "center")
local btn = button(0, 0, 2, 1, 1, 0.3, 0, 0, 0, {0.2, 0.6, 1})

local function showScore()
    label:setText("Score: " .. score)
end

while true do
    if btn:isClicked() then
        score = score + 1
        showScore()
        print("New score:", score)
    end
    if btn:isHovered() then
        label:setColor({1, 1, 0})
    else
        label:setColor({1, 1, 1})
    end
    sleep(1)
end
```

### 17.4 Animated boxes (transition + ease)

```lua
local a = cube(0, 0, 0, 0.6, 0.6, 0.6, {1, 0.3, 0.3})
local b = cube(0, 0, 0, 1.4, 1.4, 1.4, {0.3, 0.7, 1})

while true do
    a:transition(-1.5, 0, 0, 0.6, 0.6, 0.6, 0, 0, 360, {1, 0.3, 0.3}, 40)
    b:transition(1.5, 0, 0, 1.4, 1.4, 1.4, 0, 0, -360, {0.3, 0.7, 1}, 40)
    sleep(40)

    a:transition(1.5, 1.5, 0, 0.6, 0.6, 0.6, 0, 0, -360, {0.3, 1, 0.3}, 40)
    b:transition(-1.5, 0, 1.5, 1.4, 1.4, 1.4, 360, 0, 0, {1, 1, 0.3}, 40)
    sleep(40)

    a:transition(0, 0, 0, 0.6, 0.6, 0.6, 0, 0, 0, {1, 0.3, 0.3}, 40)
    b:transition(0, 0, 0, 1.4, 1.4, 1.4, 0, 0, 0, {0.3, 0.7, 1}, 40)
    sleep(40)
end
```

### 17.5 A practical game: "hit the button"

A simple reaction game combining everything (buttons, text, transitions, loops):

```lua
print("HIT THE BUTTON - Click it as fast as you can!")

local hits = 0
local target = button(0, 0, 2, 1.5, 1.5, 0.3, 0, 0, 0, {1, 0.2, 0.2})
local label  = text(0, 2.5, 2, 0.05, 0, 0, 0, "Click the red button!", {1, 1, 1}, "center")

local function resetTarget()
    -- "hide" far away, then animate back in at a random spot (x between -2 and 2)
    local newX = math.random(-200, 200) / 100
    target:transition(newX, math.random(-100, 100) / 100, 2, 1.5, 1.5, 0.3, 0, 0, 0, {1, 0.2, 0.2}, 5)
end

resetTarget()

while true do
    if target:isClicked() then
        hits = hits + 1
        label:setText("Score: " .. hits)
        print("Hit!", hits)
        resetTarget()
    end
    sleep(1)
end
```

### 17.6 Full interactive menu with two buttons

```lua
print("Switch the light cube with the buttons!")

local onColor = {1, 0.9, 0.2}    -- warm yellow
local offColor = {0.3, 0.3, 0.4} -- dim gray-blue
local light = cube(0, 1, 1, 0.6, 0.6, 0.6, offColor)

local onBtn  = button(0, 0.4, 2, 1, 0.6, 0.2, {0.2, 0.8, 0.3})
local offBtn = button(0, -0.4, 2, 1, 0.6, 0.2, {0.8, 0.2, 0.2})
local hint   = text(0, 1.6, 2, 0.03, 0, 0, 0, "Click a switch", {1, 1, 1}, "center")

while true do
    if onBtn:isClicked() then
        light:transition(0, 1, 1, 0.6, 0.6, 0.6, 0, 0, 0, onColor, 10)
        hint:setText("Light is ON")
        print("Light ON")
    end
    if offBtn:isClicked() then
        light:transition(0, 1, 1, 0.6, 0.6, 0.6, 0, 0, 0, offColor, 10)
        hint:setText("Light is OFF")
        print("Light OFF")
    end
    sleep(1)
end
```

> Notice how `button()` is used in its **short form** here: the 7th argument is a
> color table, so all rotations default to `0`.

---

## Appendix — Quick reference card

```
-- GLOBALS
cube(x, y, z, sx, sy, sz, rx, ry, rz, {r,g,b})          -- box
cube(x, y, z, sx, sy, sz, {r,g,b})                       -- box, short form (no rotation)
button(...)                                              -- same, but clickable
text(x, y, z, scale, rx, ry, rz, "text", {r,g,b}, align) -- floating text ("left"/"center"/"right")
sleep(ticks)                                             -- wait (min 1 tick)
read("prompt?")                                          -- get text input from console
print(...)                                               -- console output
getSignalStrength()                                      -- redstone 0..15 (all sides)
getSignalStrength("north")                               -- redstone 0..15 (one side)
is_powered                                               -- boolean, true if any redstone

-- OBJECT FIELDS (read & write)
x y z  sx sy sz  rx ry rz  r g b        -- cube/button/text
scale  text  alignment                  -- text only

-- OBJECT METHODS
obj:transition(x, y, z, sx, sy, sz, rx, ry, rz, {r,g,b}, dur)   -- cube/button
obj:transition(x, y, z, scale, rx, ry, rz, "text", {r,g,b}, dur) -- text
obj:del()
btn:isClicked()
btn:isHovered()
txt:setText("string")
txt:setColor({r,g,b})
txt:setAlignment("center")

-- REMOVED
io.*   os.*   debug.*   package.*   require   loadfile   dofile
```