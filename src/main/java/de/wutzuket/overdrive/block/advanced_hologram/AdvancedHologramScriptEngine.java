package de.wutzuket.overdrive.block.advanced_hologram;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AdvancedHologramScriptEngine {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int MAX_OBJECTS = 512;
    private static final int MAX_TRANSITIONS = 512;
    private static final float DEFAULT_ALPHA = 0.55F;
    private static final double PICK_RANGE = 8.0D;

    private static final int MAX_INSTRUCTIONS_PER_RESUME = 100_000;
    private static final int MAX_CONSECUTIVE_BUDGET_HITS = 600;
    private static final int INSTRUCTION_BUDGET_YIELD = -1;

    private static final String SLEEP_DEFINITION = """
            function sleep(duration)
              if duration == nil then duration = 1 end
              if duration < 1 then duration = 1 end
              local co = coroutine.running()
              if co then
                coroutine.yield(duration)
              end
            end
            """;

    private static final String READ_MARKER = "__READ__|";

    private static final String READ_DEFINITION = """
            function read(prompt)
              if prompt == nil then prompt = "> " end
              return coroutine.yield("__READ__|" .. prompt)
            end
            """;

    private final AdvancedHologramBlockEntity blockEntity;
    private final Globals globals;
    private final LuaValue coroutineLib;
    private final List<LuaValue> objects = new ArrayList<>();
    private final List<Transition> transitions = new ArrayList<>();

    private LuaValue currentCoroutine = LuaValue.NIL;
    private long loadedRevision = -1L;
    private boolean running;
    private boolean waitingForInput;
    private int engineTick;
    private int waitTicks;

    private boolean instructionLimitHit;
    private int consecutiveBudgetHits;
    private final Map<LuaValue, Boolean> clickHeld = new HashMap<>();

    private final VarArgFunction instructionLimitHook = new VarArgFunction() {
        @Override
        public Varargs invoke(Varargs args) {
            instructionLimitHit = true;
            globals.yield(LuaValue.valueOf(INSTRUCTION_BUDGET_YIELD));
            return LuaValue.NONE;
        }
    };

    public AdvancedHologramScriptEngine(AdvancedHologramBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.globals = JsePlatform.debugGlobals();
        removeUnsafeLibraries();
        installSleep();
        installRead();
        installPrint();
        installSignalFunctions();
        this.coroutineLib = globals.get("coroutine");
        registerShapeFunction("cube", false);
        registerShapeFunction("button", true);
        registerTextFunction();
    }

    private void removeUnsafeLibraries() {
        globals.set("io", LuaValue.NIL);
        globals.set("os", LuaValue.NIL);
        globals.set("debug", LuaValue.NIL);
        globals.set("package", LuaValue.NIL);
    }

    private void installSleep() {
        globals.load(SLEEP_DEFINITION, "sleep_definition").call();
    }

    private void installRead() {
        globals.load(READ_DEFINITION, "read_definition").call();
    }

    private void installPrint() {
        globals.set("print", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i <= args.narg(); i++) {
                    if (i > 1) {
                        builder.append(" ");
                    }
                    builder.append(args.arg(i).tojstring());
                }
                blockEntity.addConsoleLine(builder.toString());
                return LuaValue.NONE;
            }
        });
    }

    private void installSignalFunctions() {
        globals.set("getSignalStrength", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (args.arg(1).isstring()) {
                    String directionName = args.arg(1).tojstring().toLowerCase();
                    Direction direction = switch (directionName) {
                        case "north" -> Direction.NORTH;
                        case "south" -> Direction.SOUTH;
                        case "west" -> Direction.WEST;
                        case "east" -> Direction.EAST;
                        case "up" -> Direction.UP;
                        case "down" -> Direction.DOWN;
                        default -> null;
                    };
                    if (direction != null) {
                        return LuaValue.valueOf(blockEntity.getSignalStrength(direction));
                    }
                    return LuaValue.valueOf(0);
                }
                return LuaValue.valueOf(blockEntity.getSignalStrength());
            }
        });
    }

    public void tick(String script, boolean shouldRun, boolean powered) {
        globals.set("is_powered", LuaValue.valueOf(powered));

        if (!shouldRun) {
            if (running) {
                stop();
                blockEntity.setRenderElements(List.of());
                blockEntity.addConsoleLine("Script stopped");
            }
            return;
        }

        if (!running || loadedRevision != blockEntity.getScriptRevision()) {
            start(script);
            if (!running) {
                return;
            }
        }

        engineTick++;
        advanceTransitions();
        updateInteractionState();

        if (waitTicks > 0) {
            waitTicks--;
        } else if (!waitingForInput) {
            resume();
        }

        rebuildSnapshot();
    }

    private void start(String script) {
        stop();
        loadedRevision = blockEntity.getScriptRevision();
        try {
            LuaValue chunk = globals.load(script == null ? "" : script, "advanced_hologram");
            currentCoroutine = coroutineLib.get("create").call(chunk);
            LuaThread.State state = ((LuaThread) currentCoroutine).state;
            state.hookfunc = instructionLimitHook;
            state.hookcount = MAX_INSTRUCTIONS_PER_RESUME;
            state.hookcall = false;
            state.hookline = false;
            state.hookrtrn = false;
            running = true;
            engineTick = 0;
            waitTicks = 0;
            instructionLimitHit = false;
            consecutiveBudgetHits = 0;
            boolean client = blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide;
            LOGGER.info("[Holo] script start client={} running={}", client, running);
            blockEntity.setLastError(null);
            blockEntity.addConsoleLine("Script started");
            resume();
        } catch (LuaError error) {
            fail("Script error: " + error.getMessage());
        }
    }

    private void stop() {
        running = false;
        waitingForInput = false;
        currentCoroutine = LuaValue.NIL;
        objects.clear();
        transitions.clear();
        engineTick = 0;
        waitTicks = 0;
        instructionLimitHit = false;
        consecutiveBudgetHits = 0;
        clickHeld.clear();
        blockEntity.setPendingPrompt(null);
        blockEntity.setRenderElements(List.of());
    }

    private void resume() {
        resumeInternal(LuaValue.NIL);
    }

    public void submitInput(String input) {
        if (!waitingForInput || currentCoroutine.isnil()) {
            return;
        }
        waitingForInput = false;
        blockEntity.setPendingPrompt(null);
        blockEntity.addConsoleLine(input == null ? "" : input);
        resumeInternal(LuaValue.valueOf(input == null ? "" : input));
        rebuildSnapshot();
    }

    private void resumeInternal(LuaValue argument) {
        if (!running || currentCoroutine.isnil() || waitingForInput) {
            return;
        }
        instructionLimitHit = false;
        try {
            Varargs results = argument.isnil()
                    ? coroutineLib.get("resume").invoke(currentCoroutine)
                    : coroutineLib.get("resume").invoke(currentCoroutine, argument);
            if (!results.arg1().toboolean()) {
                fail("Script error: " + results.arg(2).tojstring());
                return;
            }

            String status = coroutineLib.get("status").call(currentCoroutine).tojstring();
            if (status.equals("dead")) {
                running = false;
                loadedRevision = blockEntity.getScriptRevision();
                blockEntity.setLastError(null);
                blockEntity.setRunning(false);
                return;
            }

            if (instructionLimitHit) {
                instructionLimitHit = false;
                consecutiveBudgetHits++;
                if (consecutiveBudgetHits >= MAX_CONSECUTIVE_BUDGET_HITS) {
                    fail("Script exceeded instruction budget (possible infinite loop) - add sleep(1) inside loops");
                    return;
                }
            } else {
                consecutiveBudgetHits = 0;
            }

            if (results.narg() >= 2 && results.arg(2).isstring()) {
                String value = results.arg(2).tojstring();
                if (value.startsWith(READ_MARKER)) {
                    waitingForInput = true;
                    blockEntity.setPendingPrompt(value.substring(READ_MARKER.length()));
                    blockEntity.addConsoleLine("\u00a76> " + blockEntity.getPendingPrompt());
                    return;
                }
            }

            if (results.narg() >= 2 && results.arg(2).isnumber()) {
                waitTicks = Math.max(1, results.arg(2).toint());
            } else {
                waitTicks = 1;
            }
        } catch (LuaError error) {
            fail("Script error: " + error.getMessage());
        }
    }

    private void fail(String message) {
        LOGGER.error(message);
        blockEntity.setLastError(message);
        blockEntity.addConsoleLine("\u00a7c" + message);
        loadedRevision = blockEntity.getScriptRevision();
        blockEntity.setRunning(false);
        stop();
    }

    private void advanceTransitions() {
        if (transitions.isEmpty()) {
            return;
        }
        Iterator<Transition> iterator = transitions.iterator();
        while (iterator.hasNext()) {
            Transition transition = iterator.next();
            double progress = (engineTick - transition.startTick) / transition.duration;
            if (progress >= 1.0D) {
                transition.apply(1.0D);
                iterator.remove();
            } else {
                transition.apply(smoothstep(progress));
            }
        }
    }

    private static double smoothstep(double t) {
        t = Math.max(0.0D, Math.min(1.0D, t));
        return t * t * (3.0D - 2.0D * t);
    }

    private void updateInteractionState() {
        Level level = blockEntity.getLevel();
        if (level == null || !level.isClientSide) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        Vec3 origin = minecraft.player.getEyePosition(1.0F);
        Vec3 look = minecraft.player.getViewVector(1.0F);

        boolean attacking = minecraft.options.keyUse.isDown();
        LuaValue hovered = LuaValue.NIL;
        double best = PICK_RANGE;
        for (LuaValue object : objects) {
            if (!object.get("is_button").toboolean()) {
                continue;
            }
            Double hit = intersectLocal(object, origin, look);
            if (hit != null && hit <= best) {
                best = hit;
                hovered = object;
            }
        }

        for (LuaValue object : objects) {
            if (!object.get("is_button").toboolean()) {
                continue;
            }
            boolean hover = !hovered.isnil() && hovered == object;
            object.set("_hovered", LuaValue.valueOf(hover));
            boolean wasAttacking = clickHeld.getOrDefault(object, false);
            if (hover && attacking && !wasAttacking) {
                object.set("_clicked", LuaValue.valueOf(true));
            }
            if (!hover || !attacking) {
                object.set("_clicked", LuaValue.valueOf(false));
            }
            clickHeld.put(object, attacking && hover);
        }
    }

    private @Nullable Double intersectLocal(LuaValue object, Vec3 origin, Vec3 look) {
        double tx = object.get("x").todouble();
        double ty = object.get("y").todouble();
        double tz = object.get("z").todouble();
        double rx = Math.toRadians(object.get("rx").todouble());
        double ry = Math.toRadians(object.get("ry").todouble());
        double rz = Math.toRadians(object.get("rz").todouble());
        double sx = object.get("sx").todouble() / 2.0D;
        double sy = object.get("sy").todouble() / 2.0D;
        double sz = object.get("sz").todouble() / 2.0D;

        Vec3 center = blockEntity.getBlockPos().getCenter();
        double cx = center.x + tx;
        double cy = center.y + ty;
        double cz = center.z + tz;

        Vector3f originLocal = new Vector3f(
                (float) (origin.x - cx), (float) (origin.y - cy), (float) (origin.z - cz));
        Vector3f directionLocal = new Vector3f((float) look.x, (float) look.y, (float) look.z);

        originLocal.rotateX((float) -rx);
        originLocal.rotateY((float) -ry);
        originLocal.rotateZ((float) -rz);
        directionLocal.rotateX((float) -rx);
        directionLocal.rotateY((float) -ry);
        directionLocal.rotateZ((float) -rz);

        return slabIntersection(originLocal, directionLocal, sx, sy, sz);
    }

    private static @Nullable Double slabIntersection(Vector3f origin, Vector3f direction, double sx, double sy, double sz) {
        double ox = origin.x;
        double oy = origin.y;
        double oz = origin.z;
        double dx = direction.x;
        double dy = direction.y;
        double dz = direction.z;

        double tMin = 0.0D;
        double tMax = Double.MAX_VALUE;

        double[] o = {ox, oy, oz};
        double[] d = {dx, dy, dz};
        double[] ext = {sx, sy, sz};

        for (int i = 0; i < 3; i++) {
            if (Math.abs(d[i]) < 1.0e-6D) {
                if (o[i] < -ext[i] || o[i] > ext[i]) {
                    return null;
                }
            } else {
                double inverse = 1.0D / d[i];
                double t1 = (-ext[i] - o[i]) * inverse;
                double t2 = (ext[i] - o[i]) * inverse;
                if (t1 > t2) {
                    double tmp = t1;
                    t1 = t2;
                    t2 = tmp;
                }
                tMin = Math.max(tMin, t1);
                tMax = Math.min(tMax, t2);
                if (tMin > tMax) {
                    return null;
                }
            }
        }
        return tMin;
    }

    private void rebuildSnapshot() {
        List<HoloElement> elements = new ArrayList<>(objects.size());
        for (LuaValue object : objects) {
            float alpha = DEFAULT_ALPHA;
            if (object.get("is_button").toboolean()) {
                boolean active = object.get("_hovered").toboolean() || object.get("_clicked").toboolean();
                alpha = active ? DEFAULT_ALPHA : DEFAULT_ALPHA * 0.6F;
            }
            boolean isText = object.get("is_text").toboolean();
            String alignment = isText ? object.get("alignment").tojstring() : "left";
            elements.add(new HoloElement(
                    object.get("x").todouble(),
                    object.get("y").todouble(),
                    object.get("z").todouble(),
                    isText ? 1.0D : object.get("sx").todouble(),
                    isText ? 1.0D : object.get("sy").todouble(),
                    isText ? 1.0D : object.get("sz").todouble(),
                    object.get("rx").todouble(),
                    object.get("ry").todouble(),
                    object.get("rz").todouble(),
                    (float) object.get("r").todouble(),
                    (float) object.get("g").todouble(),
                    (float) object.get("b").todouble(),
                    alpha,
                    isText ? object.get("text").tojstring() : null,
                    isText ? object.get("scale").todouble() : 1.0D,
                    alignment
            ));
        }
        blockEntity.setRenderElements(elements);
    }

    private void registerShapeFunction(final String name, final boolean interactive) {
        globals.set(name, new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (!running) {
                    return LuaValue.NIL;
                }
                if (objects.size() >= MAX_OBJECTS) {
                    throw new LuaError("Too many hologram objects (max " + MAX_OBJECTS + ")");
                }

                boolean shortForm = args.arg(7).istable();
                double x = args.optdouble(1, 0.0D);
                double y = args.optdouble(2, 0.0D);
                double z = args.optdouble(3, 0.0D);
                double sizeX = args.optdouble(4, 1.0D);
                double sizeY = args.optdouble(5, 1.0D);
                double sizeZ = args.optdouble(6, 1.0D);
                double rotX;
                double rotY;
                double rotZ;
                float[] color;
                if (shortForm) {
                    rotX = 0.0D;
                    rotY = 0.0D;
                    rotZ = 0.0D;
                    color = readColor(args.arg(7));
                } else {
                    rotX = args.optdouble(7, 0.0D);
                    rotY = args.optdouble(8, 0.0D);
                    rotZ = args.optdouble(9, 0.0D);
                    color = readColor(args.arg(10));
                }

                LuaTable object = new LuaTable();
                object.set("x", LuaValue.valueOf(x));
                object.set("y", LuaValue.valueOf(y));
                object.set("z", LuaValue.valueOf(z));
                object.set("sx", LuaValue.valueOf(sizeX));
                object.set("sy", LuaValue.valueOf(sizeY));
                object.set("sz", LuaValue.valueOf(sizeZ));
                object.set("rx", LuaValue.valueOf(rotX));
                object.set("ry", LuaValue.valueOf(rotY));
                object.set("rz", LuaValue.valueOf(rotZ));
                object.set("r", LuaValue.valueOf(color[0]));
                object.set("g", LuaValue.valueOf(color[1]));
                object.set("b", LuaValue.valueOf(color[2]));
                if (interactive) {
                    object.set("is_button", LuaValue.TRUE);
                    object.set("_hovered", LuaValue.FALSE);
                    object.set("_clicked", LuaValue.FALSE);
                    final LuaTable buttonObject = object;
                    object.set("isClicked", new VarArgFunction() {
                        @Override
                        public Varargs invoke(Varargs args) {
                            boolean clicked = buttonObject.get("_clicked").toboolean();
                            if (clicked) {
                                buttonObject.set("_clicked", LuaValue.FALSE);
                            }
                            return LuaValue.valueOf(clicked);
                        }
                    });
                    object.set("isHovered", new VarArgFunction() {
                        @Override
                        public Varargs invoke(Varargs args) {
                            return LuaValue.valueOf(buttonObject.get("_hovered").toboolean());
                        }
                    });
                }
                object.set("transition", buildTransitionFunction(object));
                object.set("del", buildDelFunction(object));

                objects.add(object);
                return object;
            }
        });
    }

    private void registerTextFunction() {
        globals.set("text", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (!running) {
                    return LuaValue.NIL;
                }
                if (objects.size() >= MAX_OBJECTS) {
                    throw new LuaError("Too many hologram objects (max " + MAX_OBJECTS + ")");
                }

                double x = args.optdouble(1, 0.0D);
                double y = args.optdouble(2, 0.0D);
                double z = args.optdouble(3, 0.0D);
                double scale = args.optdouble(4, 1.0D);
                double rotX = args.optdouble(5, 0.0D);
                double rotY = args.optdouble(6, 0.0D);
                double rotZ = args.optdouble(7, 0.0D);
                String textStr = args.arg(8).isstring() ? args.arg(8).tojstring() : "";
                float[] color = readColor(args.arg(9));
                String alignment = args.arg(10).isstring() ? args.arg(10).tojstring() : "left";

                LuaTable object = new LuaTable();
                object.set("is_text", LuaValue.TRUE);
                object.set("x", LuaValue.valueOf(x));
                object.set("y", LuaValue.valueOf(y));
                object.set("z", LuaValue.valueOf(z));
                object.set("scale", LuaValue.valueOf(scale));
                object.set("rx", LuaValue.valueOf(rotX));
                object.set("ry", LuaValue.valueOf(rotY));
                object.set("rz", LuaValue.valueOf(rotZ));
                object.set("text", LuaValue.valueOf(textStr));
                object.set("sx", LuaValue.valueOf(1.0D));
                object.set("sy", LuaValue.valueOf(1.0D));
                object.set("sz", LuaValue.valueOf(1.0D));
                object.set("r", LuaValue.valueOf(color[0]));
                object.set("g", LuaValue.valueOf(color[1]));
                object.set("b", LuaValue.valueOf(color[2]));
                object.set("alignment", LuaValue.valueOf(alignment));
                object.set("transition", buildTransitionFunction(object));
                object.set("del", buildDelFunction(object));
                object.set("setText", new VarArgFunction() {
                    @Override
                    public Varargs invoke(Varargs a) {
                        String newText = a.arg(1).isstring() ? a.arg(1).tojstring() : "";
                        object.set("text", LuaValue.valueOf(newText));
                        return object;
                    }
                });
                object.set("setColor", new VarArgFunction() {
                    @Override
                    public Varargs invoke(Varargs a) {
                        float[] c = readColor(a.arg(1));
                        object.set("r", LuaValue.valueOf(c[0]));
                        object.set("g", LuaValue.valueOf(c[1]));
                        object.set("b", LuaValue.valueOf(c[2]));
                        return object;
                    }
                });
                object.set("setAlignment", new VarArgFunction() {
                    @Override
                    public Varargs invoke(Varargs a) {
                        String align = a.arg(1).isstring() ? a.arg(1).tojstring() : "left";
                        object.set("alignment", LuaValue.valueOf(align));
                        return object;
                    }
                });

                objects.add(object);
                return object;
            }
        });
    }

    private VarArgFunction buildTransitionFunction(final LuaTable object) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                int base = args.arg1().raweq(object) ? 2 : 1;
                if (!running) {
                    return object;
                }
                if (transitions.size() >= MAX_TRANSITIONS) {
                    throw new LuaError("Too many transitions (max " + MAX_TRANSITIONS + ")");
                }

                boolean isText = object.get("is_text").toboolean();
                double targetX = args.optdouble(base, object.get("x").todouble());
                double targetY = args.optdouble(base + 1, object.get("y").todouble());
                double targetZ = args.optdouble(base + 2, object.get("z").todouble());
                double targetRotX, targetRotY, targetRotZ;
                float[] targetColor;
                double duration;
                String targetText = null;
                String targetAlignment = null;

                if (isText) {
                    double targetScale = args.optdouble(base + 3, object.get("scale").todouble());
                    targetRotX = args.optdouble(base + 4, object.get("rx").todouble());
                    targetRotY = args.optdouble(base + 5, object.get("ry").todouble());
                    targetRotZ = args.optdouble(base + 6, object.get("rz").todouble());
                    if (args.arg(base + 7).isstring()) {
                        targetText = args.arg(base + 7).tojstring();
                    }
                    targetColor = readColor(args.arg(base + 8));
                    duration = args.optdouble(base + 9, 20.0D);
                    object.set("sx", LuaValue.valueOf(targetScale));
                    object.set("sy", LuaValue.valueOf(targetScale));
                    object.set("sz", LuaValue.valueOf(targetScale));
                } else {
                    double targetSizeX = args.optdouble(base + 3, object.get("sx").todouble());
                    double targetSizeY = args.optdouble(base + 4, object.get("sy").todouble());
                    double targetSizeZ = args.optdouble(base + 5, object.get("sz").todouble());
                    targetRotX = args.optdouble(base + 6, object.get("rx").todouble());
                    targetRotY = args.optdouble(base + 7, object.get("ry").todouble());
                    targetRotZ = args.optdouble(base + 8, object.get("rz").todouble());
                    targetColor = readColor(args.arg(base + 9));
                    duration = args.optdouble(base + 10, 20.0D);
                    object.set("sx", LuaValue.valueOf(targetSizeX));
                    object.set("sy", LuaValue.valueOf(targetSizeY));
                    object.set("sz", LuaValue.valueOf(targetSizeZ));
                }
                if (duration < 1.0D) {
                    duration = 1.0D;
                }

                transitions.removeIf(t -> t.object == object);

                Transition transition = new Transition(object, engineTick, duration);
                transition.setStart(
                        object.get("x").todouble(), object.get("y").todouble(), object.get("z").todouble(),
                        object.get("sx").todouble(), object.get("sy").todouble(), object.get("sz").todouble(),
                        object.get("rx").todouble(), object.get("ry").todouble(), object.get("rz").todouble(),
                        object.get("r").todouble(), object.get("g").todouble(), object.get("b").todouble()
                );
                transition.setTarget(targetX, targetY, targetZ,
                        object.get("sx").todouble(), object.get("sy").todouble(), object.get("sz").todouble(),
                        targetRotX, targetRotY, targetRotZ,
                        targetColor[0], targetColor[1], targetColor[2]);
                if (targetText != null) {
                    transition.setTargetText(targetText);
                }

                transitions.add(transition);
                return object;
            }
        };
    }

    private VarArgFunction buildDelFunction(final LuaTable object) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                objects.remove(object);
                transitions.removeIf(t -> t.object == object);
                clickHeld.remove(object);
                return LuaValue.NONE;
            }
        };
    }

    private static float[] readColor(LuaValue arg) {
        float[] color = new float[]{1.0F, 1.0F, 1.0F};
        if (arg.istable()) {
            for (int i = 1; i <= 3; i++) {
                LuaValue component = arg.get(i);
                if (component.isnumber()) {
                    color[i - 1] = (float) component.todouble();
                }
            }
        }
        for (int i = 0; i < color.length; i++) {
            if (color[i] > 1.0F) {
                color[i] /= 255.0F;
            }
        }
        return color;
    }

    private static final class Transition {
        private final LuaValue object;
        private final double startTick;
        private final double duration;
        private final double[] start = new double[9];
        private final double[] target = new double[9];
        private final double[] startColor = new double[3];
        private final double[] targetColor = new double[3];
        private @Nullable String targetText;

        private Transition(LuaValue object, int startTick, double duration) {
            this.object = object;
            this.startTick = startTick;
            this.duration = duration;
        }

        private void setTargetText(String targetText) {
            this.targetText = targetText;
        }

        private void setStart(double x, double y, double z,
                              double sizeX, double sizeY, double sizeZ,
                              double rotX, double rotY, double rotZ,
                              double r, double g, double b) {
            start[0] = x;
            start[1] = y;
            start[2] = z;
            start[3] = sizeX;
            start[4] = sizeY;
            start[5] = sizeZ;
            start[6] = rotX;
            start[7] = rotY;
            start[8] = rotZ;
            startColor[0] = r;
            startColor[1] = g;
            startColor[2] = b;
        }

        private void setTarget(double x, double y, double z,
                               double sizeX, double sizeY, double sizeZ,
                               double rotX, double rotY, double rotZ,
                               double r, double g, double b) {
            target[0] = x;
            target[1] = y;
            target[2] = z;
            target[3] = sizeX;
            target[4] = sizeY;
            target[5] = sizeZ;
            target[6] = rotX;
            target[7] = rotY;
            target[8] = rotZ;
            targetColor[0] = r;
            targetColor[1] = g;
            targetColor[2] = b;
        }

        private void apply(double t) {
            object.set("x", LuaValue.valueOf(lerp(start[0], target[0], t)));
            object.set("y", LuaValue.valueOf(lerp(start[1], target[1], t)));
            object.set("z", LuaValue.valueOf(lerp(start[2], target[2], t)));
            object.set("sx", LuaValue.valueOf(lerp(start[3], target[3], t)));
            object.set("sy", LuaValue.valueOf(lerp(start[4], target[4], t)));
            object.set("sz", LuaValue.valueOf(lerp(start[5], target[5], t)));
            object.set("rx", LuaValue.valueOf(lerp(start[6], target[6], t)));
            object.set("ry", LuaValue.valueOf(lerp(start[7], target[7], t)));
            object.set("rz", LuaValue.valueOf(lerp(start[8], target[8], t)));
            object.set("r", LuaValue.valueOf(lerp(startColor[0], targetColor[0], t)));
            object.set("g", LuaValue.valueOf(lerp(startColor[1], targetColor[1], t)));
            object.set("b", LuaValue.valueOf(lerp(startColor[2], targetColor[2], t)));
            if (targetText != null && t >= 1.0D) {
                object.set("text", LuaValue.valueOf(targetText));
            }
        }

        private static double lerp(double from, double to, double t) {
            return from + (to - from) * t;
        }
    }
}
