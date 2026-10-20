package de.wutzuket.overdrive.antimatter;

import de.wutzuket.overdrive.block.antimatter.AntimatterPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A connected group of antimatter pipes. All pipes of a network share their content, so the
 * actual transport is executed once per server tick by the network instead of by every single
 * pipe. This keeps the performance impact low even for huge pipe networks.
 */
public class AntimatterNetwork {

    private final Set<AntimatterPipeBlockEntity> pipes = new LinkedHashSet<>();

    /** Game time of the last tick of this network. Used to make sure a network ticks only once. */
    private long lastTick = Long.MIN_VALUE;

    private int totalAmount;
    private int totalCapacity;

    /** The distance of every pipe to the closest machine that takes the antimatter. */
    private final Map<AntimatterPipeBlockEntity, Integer> sinkDistance = new HashMap<>();

    @Nullable
    private Antimatter type;

    public AntimatterNetwork() {
    }

    // --- membership -------------------------------------------------------

    public void add(AntimatterPipeBlockEntity pipe) {
        if (pipes.add(pipe)) {
            pipe.setNetwork(this);
        }
    }

    public void remove(AntimatterPipeBlockEntity pipe) {
        if (pipes.remove(pipe) && pipe.getNetwork() == this) {
            pipe.setNetwork(null);
        }
    }

    public boolean contains(AntimatterPipeBlockEntity pipe) {
        return pipes.contains(pipe);
    }

    public Set<AntimatterPipeBlockEntity> getPipes() {
        return pipes;
    }

    public int size() {
        return pipes.size();
    }

    /** Merges another network into this one. */
    public void merge(AntimatterNetwork other) {
        if (other == this) {
            return;
        }
        for (AntimatterPipeBlockEntity pipe : new LinkedHashSet<>(other.pipes)) {
            other.remove(pipe);
            add(pipe);
        }
        other.pipes.clear();
    }

    /**
     * Rebuilds the membership of the given pipe. The pipe joins the network of one of its
     * neighbours or creates a brand new one. When two different networks would be connected they
     * are merged into a single one.
     */
    public static void rebuildNetwork(AntimatterPipeBlockEntity pipe) {
        Level level = pipe.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }

        AntimatterNetwork current = pipe.getNetwork();
        if (current != null) {
            current.remove(pipe);
        }

        AntimatterNetwork target = null;
        for (Direction direction : Direction.values()) {
            BlockEntity neighbour = level.getBlockEntity(pipe.getBlockPos().relative(direction));
            if (!(neighbour instanceof AntimatterPipeBlockEntity neighbourPipe)) {
                continue;
            }
            if (!pipe.isSideOpen(direction)) {
                continue;
            }
            AntimatterNetwork other = neighbourPipe.getNetwork();
            if (other == null || other == target) {
                continue;
            }
            if (target == null) {
                target = other;
            } else {
                target.merge(other);
            }
        }

        if (target == null) {
            target = new AntimatterNetwork();
        }
        target.add(pipe);
    }

    /**
     * Splits the network into its connected components. This is needed when a pipe is removed or
     * when a connection is disabled because the remaining pipes are no longer connected to each
     * other.
     */
    public void splitComponents() {
        if (pipes.size() <= 1) {
            return;
        }
        AntimatterPipeBlockEntity any = pipes.iterator().next();
        Level level = any.getLevel();
        if (level == null) {
            return;
        }

        java.util.Set<AntimatterPipeBlockEntity> visited = new java.util.HashSet<>();
        java.util.List<java.util.Set<AntimatterPipeBlockEntity>> components = new java.util.ArrayList<>();

        for (AntimatterPipeBlockEntity start : new java.util.ArrayList<>(pipes)) {
            if (visited.contains(start)) {
                continue;
            }
            java.util.Set<AntimatterPipeBlockEntity> component = new LinkedHashSet<>();
            java.util.Deque<AntimatterPipeBlockEntity> queue = new java.util.ArrayDeque<>();
            queue.add(start);
            visited.add(start);
            while (!queue.isEmpty()) {
                AntimatterPipeBlockEntity pipe = queue.poll();
                component.add(pipe);
                for (Direction direction : Direction.values()) {
                    if (!pipe.isSideOpen(direction)) {
                        continue;
                    }
                    BlockEntity neighbour = level.getBlockEntity(pipe.getBlockPos().relative(direction));
                    if (neighbour instanceof AntimatterPipeBlockEntity neighbourPipe
                            && pipes.contains(neighbourPipe) && visited.add(neighbourPipe)) {
                        queue.add(neighbourPipe);
                    }
                }
            }
            components.add(component);
        }

        // the first component keeps this network, every other component gets its own one
        for (int i = 1; i < components.size(); i++) {
            AntimatterNetwork network = new AntimatterNetwork();
            for (AntimatterPipeBlockEntity pipe : components.get(i)) {
                remove(pipe);
                network.add(pipe);
            }
        }
    }

    // --- ticking ----------------------------------------------------------

    /**
     * Executes the central transport of the whole network. This is called by every pipe of the
     * network but only runs once per server tick.
     */
    public void tick(ServerLevel level) {
        if (pipes.isEmpty()) {
            return;
        }
        long gameTime = level.getGameTime();
        if (lastTick == gameTime) {
            return;
        }
        lastTick = gameTime;

        recalculate();
        updateSinkDistances(level);

        // 1) handle the connections to machines
        for (AntimatterPipeBlockEntity pipe : pipes) {
            if (pipe.isRemoved()) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                if (!pipe.isSideOpen(direction)) {
                    continue;
                }
                transferToMachine(level, pipe, direction);
            }
        }

        // 2) carry the antimatter through the pipes towards the machines, so all pipes of the
        // line hold the same amount
        equalize(level);
    }

    /**
     * Takes antimatter out of the network. The content of the preferred pipe is used first, if
     * that one is empty the content of the other pipes of the network is taken, so that no
     * antimatter gets stuck in a pipe that is far away from the machine.
     *
     * @return the amount of antimatter that was taken out of the network
     */
    public int takeContent(AntimatterPipeBlockEntity preferred, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int taken = takeFrom(preferred, amount);
        if (taken < amount) {
            for (AntimatterPipeBlockEntity other : pipes) {
                if (other == preferred) {
                    continue;
                }
                taken += takeFrom(other, amount - taken);
                if (taken >= amount) {
                    break;
                }
            }
        }
        return taken;
    }

    private int takeFrom(AntimatterPipeBlockEntity pipe, int amount) {
        int available = pipe.getLocalAmount();
        if (available <= 0) {
            return 0;
        }
        int taken = Math.min(available, amount);
        pipe.extractLocal(taken);
        return taken;
    }

    /** @return the antimatter handler of the machine next to the given side or {@code null}. */
    private @Nullable IAntimatterHandler getMachine(ServerLevel level, AntimatterPipeBlockEntity pipe,
                                                    Direction direction) {
        BlockPos neighbourPos = pipe.getBlockPos().relative(direction);
        if (level.getBlockEntity(neighbourPos) instanceof AntimatterPipeBlockEntity) {
            // pipes of the same network are handled by the equalization step
            return null;
        }
        return level.getCapability(ODAntimatterCapability.ANTIMATTER_HANDLER, neighbourPos, direction.getOpposite());
    }

    private void recalculate() {
        int amount = 0;
        int capacity = 0;
        Antimatter found = null;
        for (AntimatterPipeBlockEntity pipe : pipes) {
            amount += pipe.getLocalAmount();
            capacity += pipe.getBufferCapacity();
            if (found == null && pipe.getLocalAmount() > 0) {
                found = pipe.getLocalType();
            }
        }
        this.totalAmount = amount;
        this.totalCapacity = Math.max(1, capacity);
        this.type = found;
    }

    /** @return the total amount of antimatter units currently held by the pipes of this network. */
    public int getTotalAmount() {
        return totalAmount;
    }

    /**
     * @return the fill level of the whole network in the range 0..1. All pipes of a network show
     *         the same level, because the antimatter is spread over the whole network.
     */
    public float getFillLevel() {
        return totalCapacity <= 0 ? 0 : (float) totalAmount / totalCapacity;
    }

    /** @return the total capacity of the pipes of this network. */
    public int getTotalCapacity() {
        return totalCapacity;
    }

    /** @return the antimatter type currently stored in this network. */
    @Nullable
    public Antimatter getType() {
        return type;
    }

    /**
     * Moves antimatter between pipes of this network so all of them hold roughly the same amount.
     * The antimatter is not spread out by diffusion, it is carried towards the machine that takes
     * it, so the pipes all show the same fill level and the throughput does not get smaller the
     * longer the line is.
     */
    private void equalize(ServerLevel level) {
        // the pipes are handled from the source to the machine, so the antimatter really travels
        // along the line instead of piling up at the place it comes in
        List<AntimatterPipeBlockEntity> ordered = new ArrayList<>(pipes);
        ordered.sort(Comparator.comparingInt((AntimatterPipeBlockEntity pipe) -> {
            Integer distance = sinkDistance.get(pipe);
            return distance == null ? Integer.MAX_VALUE : distance;
        }).reversed());
        int target = pipes.isEmpty() ? 0 : totalAmount / pipes.size();
        for (AntimatterPipeBlockEntity pipe : ordered) {
            if (pipe.isRemoved()) {
                continue;
            }
            AntimatterPipeBlockEntity downstream = getDownstream(level, pipe);
            if (downstream == null) {
                // this pipe has no way to a machine, so the content is spread out over the pipes
                balanceWithoutSink(level, pipe);
                continue;
            }
            // every pipe should hold the same amount, the part that is too much is carried on to
            // the machine, so the whole line stays evenly filled and the throughput does not get
            // smaller the longer the line is
            int surplus = pipe.getLocalAmount() - target;
            if (surplus <= 0) {
                continue;
            }
            // Der langsamste Rohr der Strecke bestimmt das Tempo
            int rate = Math.min(pipe.getTransferRate(), downstream.getTransferRate());
            int moved = Math.min(rate, surplus);
            moved = Math.min(moved, pipe.getLocalAmount());
            moved = Math.min(moved, downstream.getBufferCapacity() - downstream.getLocalAmount());
            if (moved <= 0) {
                continue;
            }
            if (downstream.getLocalType() == null) {
                downstream.setLocalType(pipe.getLocalType());
            }
            pipe.extractLocal(moved);
            downstream.insertLocal(moved);
        }
    }

    /**
     * Spreads the content over the pipes of a part of the network that does not lead to a
     * machine. Only half of the difference is moved, so the content does not jump back and forth.
     */
    private void balanceWithoutSink(ServerLevel level, AntimatterPipeBlockEntity pipe) {
        for (Direction direction : Direction.values()) {
            if (!pipe.isSideOpen(direction)) {
                continue;
            }
            BlockEntity neighbour = level.getBlockEntity(pipe.getBlockPos().relative(direction));
            if (!(neighbour instanceof AntimatterPipeBlockEntity neighbourPipe)) {
                continue;
            }
            if (!contains(neighbourPipe)) {
                continue;
            }
            int diff = pipe.getLocalAmount() - neighbourPipe.getLocalAmount();
            if (diff <= 0) {
                continue;
            }
            int rate = Math.min(pipe.getTransferRate(), neighbourPipe.getTransferRate());
            int moved = Math.min(rate, diff / 2 + 1);
            moved = Math.min(moved, pipe.getLocalAmount());
            moved = Math.min(moved, neighbourPipe.getBufferCapacity() - neighbourPipe.getLocalAmount());
            if (moved <= 0) {
                continue;
            }
            if (neighbourPipe.getLocalType() == null) {
                neighbourPipe.setLocalType(pipe.getLocalType());
            }
            pipe.extractLocal(moved);
            neighbourPipe.insertLocal(moved);
        }
    }

    /**
     * @return the neighbour of the pipe that is closer to a machine, so the antimatter flows that
     *         way. {@code null} when the pipe does not lead to a machine at all.
     */
    private @Nullable AntimatterPipeBlockEntity getDownstream(ServerLevel level,
                                                              AntimatterPipeBlockEntity pipe) {
        Integer distance = sinkDistance.get(pipe);
        if (distance == null) {
            return null;
        }
        AntimatterPipeBlockEntity best = null;
        int bestDistance = distance;
        for (Direction direction : Direction.values()) {
            if (!pipe.isSideOpen(direction)) {
                continue;
            }
            BlockEntity neighbour = level.getBlockEntity(pipe.getBlockPos().relative(direction));
            if (!(neighbour instanceof AntimatterPipeBlockEntity neighbourPipe) || !contains(neighbourPipe)) {
                continue;
            }
            Integer neighbourDistance = sinkDistance.get(neighbourPipe);
            if (neighbourDistance != null && neighbourDistance < bestDistance) {
                bestDistance = neighbourDistance;
                best = neighbourPipe;
            }
        }
        return best;
    }

    /**
     * Calculates for every pipe how many pipes it has to pass to reach a machine that takes the
     * antimatter. The antimatter is carried along this way.
     */
    private void updateSinkDistances(ServerLevel level) {
        sinkDistance.clear();
        Deque<AntimatterPipeBlockEntity> queue = new ArrayDeque<>();
        for (AntimatterPipeBlockEntity pipe : pipes) {
            for (Direction direction : Direction.values()) {
                if (pipe.getSideMode(direction) == PipeSideMode.PUSH && pipe.isSideOpen(direction)) {
                    // a pipe that pushes into a machine is the end of the line
                    if (!(level.getBlockEntity(pipe.getBlockPos().relative(direction))
                            instanceof AntimatterPipeBlockEntity)) {
                        sinkDistance.put(pipe, 0);
                        queue.add(pipe);
                        break;
                    }
                }
            }
        }
        while (!queue.isEmpty()) {
            AntimatterPipeBlockEntity pipe = queue.poll();
            int distance = sinkDistance.get(pipe);
            for (Direction direction : Direction.values()) {
                if (!pipe.isSideOpen(direction)) {
                    continue;
                }
                BlockEntity neighbour = level.getBlockEntity(pipe.getBlockPos().relative(direction));
                if (!(neighbour instanceof AntimatterPipeBlockEntity neighbourPipe) || !contains(neighbourPipe)) {
                    continue;
                }
                if (sinkDistance.containsKey(neighbourPipe)) {
                    continue;
                }
                sinkDistance.put(neighbourPipe, distance + 1);
                queue.add(neighbourPipe);
            }
        }
    }

    /**
     * Handles the connection between a pipe and a machine (anything that exposes an
     * {@link IAntimatterHandler}).
     */
    private void transferToMachine(ServerLevel level, AntimatterPipeBlockEntity pipe, Direction direction) {
        IAntimatterHandler handler = getMachine(level, pipe, direction);
        if (handler == null) {
            return;
        }

        int rate = Math.max(1, pipe.getTransferRate());
        PipeSideMode mode = pipe.getSideMode(direction);

        boolean pull;
        switch (mode) {
            case PUSH -> pull = false;
            case PULL -> pull = true;
            default -> {
                // A normal side never takes anything out of a machine, the player has to
                // configure the side as pull for that. This keeps the machines safe from
                // accidentally emptying themselves into a pipe.
                return;
            }
        }

        if (pull) {
            // A pulling side always takes the antimatter out of the machine. If nothing in the
            // network can take it again, the antimatter waits in the pipes until it can be
            // delivered.
            if (!handler.canInsert()) {
                return;
            }
            Antimatter machineType = handler.getAntimatterInTank(0);
            if (machineType == null || handler.getAmount(0) <= 0) {
                return;
            }
            if (pipe.getLocalType() != null && !pipe.getLocalType().equals(machineType)) {
                // the pipe already holds a different type of antimatter
                return;
            }
            int wanted = Math.min(rate, pipe.getBufferCapacity() - pipe.getLocalAmount());
            if (wanted <= 0) {
                return;
            }
            int extracted = handler.extractAntimatter(0, wanted, false);
            if (extracted > 0) {
                if (pipe.getLocalType() == null) {
                    pipe.setLocalType(machineType);
                }
                pipe.insertLocal(extracted);
            }
        } else {
            if (!handler.canExtract()) {
                return;
            }
            // the type has to be read before the content is taken out of the pipes, an empty pipe
            // does not know its type anymore
            Antimatter pushType = pipe.getLocalType();
            if (pushType == null) {
                return;
            }
            int space = handler.getCapacity(0) - handler.getAmount(0);
            if (space <= 0) {
                return;
            }
            int wanted = Math.min(rate, space);
            if (wanted <= 0) {
                return;
            }
            // the content is taken out of the pipe that is connected to the machine, the transport
            // brings the rest of the network to this pipe
            int available = takeFrom(pipe, wanted);
            if (available <= 0) {
                return;
            }
            int accepted = handler.insertAntimatter(0, pushType, available, false);
            if (accepted < available) {
                // the machine did not take everything, put the rest back
                pipe.insertLocal(available - accepted);
            }
        }
    }
}
