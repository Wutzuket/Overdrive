package de.wutzuket.overdrive.block.antimatter;

import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.antimatter.AntimatterNetwork;
import de.wutzuket.overdrive.antimatter.ODAntimatters;
import de.wutzuket.overdrive.antimatter.PipeTier;
import de.wutzuket.overdrive.antimatter.PipeSideMode;
import de.wutzuket.overdrive.config.ODConfig;
import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Block entity of the antimatter pipe. It stores the configuration of all six sides and the
 * antimatter that is currently buffered inside the pipe.
 */
public class AntimatterPipeBlockEntity extends BlockEntity {

    private final PipeSideMode[] modes = new PipeSideMode[Direction.values().length];

    @Nullable
    private Antimatter localType;
    private int localAmount;

    @Nullable
    private transient AntimatterNetwork network;
    /** The fill level of the whole network, it is sent to the client with the content. */
    private float networkFill;

    /** The amount that was sent to the client the last time, used to avoid useless updates. */
    private transient int lastSyncedAmount;
    /** The type that was sent to the client the last time. */
    @Nullable
    private transient Antimatter syncedType;

    public AntimatterPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.ANTIMATTER_PIPE.get(), pos, state);
        for (Direction direction : Direction.values()) {
            modes[direction.ordinal()] = PipeSideMode.NORMAL;
        }
    }

    // --- side configuration ------------------------------------------------

    public PipeSideMode getSideMode(Direction direction) {
        PipeSideMode mode = modes[direction.ordinal()];
        return mode == null ? PipeSideMode.NORMAL : mode;
    }

    public void setSideMode(Direction direction, PipeSideMode mode) {
        modes[direction.ordinal()] = mode;
        setChanged();
    }

    /** Cycles the mode of the given side: normal -&gt; push -&gt; pull -&gt; disabled. */
    public PipeSideMode cycleSideMode(Direction direction) {
        PipeSideMode next = getSideMode(direction).next();
        setSideMode(direction, next);
        return next;
    }

    /** Cycles the mode of every side that currently has a connection. */
    public Map<Direction, PipeSideMode> cycleAllSideModes() {
        Map<Direction, PipeSideMode> changed = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            if (getBlockState().getValue(AntimatterPipeBlock.CONNECTIONS.get(direction)) == PipeConnection.NONE) {
                continue;
            }
            modes[direction.ordinal()] = getSideMode(direction).next();
            changed.put(direction, modes[direction.ordinal()]);
        }
        setChanged();
        return changed;
    }

    /** @return true when antimatter can flow through the given side. */
    public boolean isSideOpen(Direction direction) {
        PipeConnection connection = getBlockState().getValue(AntimatterPipeBlock.CONNECTIONS.get(direction));
        return connection == PipeConnection.OPEN || connection == PipeConnection.PULL
                || connection == PipeConnection.PUSH;
    }

    /** @return true when the given side is connected to a neighbour but blocked. */
    public boolean isSideDisabled(Direction direction) {
        return getBlockState().getValue(AntimatterPipeBlock.CONNECTIONS.get(direction)) == PipeConnection.DISABLED;
    }

    // --- buffered content --------------------------------------------------

    @Nullable
    public Antimatter getLocalType() {
        return localType;
    }

    public int getLocalAmount() {
        return localAmount;
    }

    /** @return the tier of this pipe, it decides the transfer rate and the buffer size. */
    public PipeTier getTier() {
        if (getBlockState().getBlock() instanceof AntimatterPipeBlock pipe) {
            return pipe.getTier();
        }
        return PipeTier.BASIC;
    }

    public int getTransferRate() {
        return getTier().getTransferRate();
    }

    public int getBufferCapacity() {
        return getTier().getBufferCapacity();
    }

    /** @return the fill level of this pipe in the range 0..1. */
    public float getFillLevel() {
        return (float) localAmount / getBufferCapacity();
    }

    /**
     * @return the fill level of the whole network in the range 0..1. The antimatter is spread over
     *         the whole network, so every pipe of a network shows the same level and the stream in
     *         the pipes is equally thick everywhere.
     */
    public float getNetworkFillLevel() {
        if (level != null && !level.isClientSide && network != null) {
            return network.getFillLevel();
        }
        return networkFill;
    }

    public void setLocalType(@Nullable Antimatter type) {
        this.localType = type;
        setChanged();
        syncContent();
    }

    public void insertLocal(int amount) {
        if (amount <= 0) {
            return;
        }
        localAmount = Math.min(getBufferCapacity(), localAmount + amount);
        setChanged();
        syncContent();
    }

    public void extractLocal(int amount) {
        if (amount <= 0) {
            return;
        }
        localAmount -= amount;
        if (localAmount <= 0) {
            localAmount = 0;
            localType = null;
        }
        setChanged();
        syncContent();
    }

    /**
     * Sends the content of the pipe to the client, but only when it changed enough to be visible.
     * The amount is needed to draw the glowing stream inside the pipe.
     */
    private void syncContent() {
        if (level == null || level.isClientSide) {
            return;
        }
        int threshold = Math.max(1, getBufferCapacity() / 16);
        if (Math.abs(localAmount - lastSyncedAmount) < threshold && syncedType == localType) {
            return;
        }
        lastSyncedAmount = localAmount;
        syncedType = localType;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /** Used by the network to apply a change of the buffered content. */
    public void setLocalContent(@Nullable Antimatter type, int amount) {
        this.localType = amount > 0 ? type : null;
        this.localAmount = Math.max(0, amount);
    }

    // --- network -----------------------------------------------------------

    @Nullable
    public AntimatterNetwork getNetwork() {
        return network;
    }

    public void setNetwork(@Nullable AntimatterNetwork network) {
        this.network = network;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AntimatterPipeBlockEntity pipe) {
        if (level.isClientSide) {
            return;
        }
        // The block state only holds the derived connection information. Whenever it does not match
        // the configuration of the pipe anymore (for example because a neighbouring block was
        // replaced) it is refreshed here.
        if (state.getBlock() instanceof AntimatterPipeBlock pipeBlock) {
            BlockState expected = pipeBlock.getConnectionState(level, pos, state);
            if (!expected.equals(state)) {
                level.setBlockAndUpdate(pos, expected);
                return;
            }
        }
        if (pipe.network == null) {
            AntimatterNetwork.rebuildNetwork(pipe);
        }
        AntimatterNetwork active = pipe.network;
        if (active != null && level instanceof ServerLevel serverLevel) {
            active.tick(serverLevel);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            AntimatterNetwork.rebuildNetwork(this);
        }
    }

    /**
     * Rebuilds the network membership of this pipe. Has to be called after the configuration of a
     * side changed because that can split or merge pipe networks.
     */
    public void refreshNetwork() {
        AntimatterNetwork previous = network;
        if (previous != null) {
            previous.remove(this);
        }
        AntimatterNetwork.rebuildNetwork(this);
        if (previous != null) {
            previous.splitComponents();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        AntimatterNetwork previous = network;
        if (previous != null) {
            previous.remove(this);
            previous.splitComponents();
        }
    }

    // --- syncing -----------------------------------------------------------

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        int[] modeArray = new int[modes.length];
        for (int i = 0; i < modes.length; i++) {
            modeArray[i] = getSideMode(Direction.values()[i]).ordinal();
        }
        tag.putIntArray("modes", modeArray);
        tag.putInt("amount", localAmount);
        tag.putFloat("netFill", getNetworkFillLevel());
        if (localType != null) {
            tag.putString("type", localType.id().toString());
        } else {
            tag.remove("type");
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        int[] modeArray = tag.getIntArray("modes");
        for (int i = 0; i < modes.length; i++) {
            int ordinal = i < modeArray.length ? modeArray[i] : PipeSideMode.NORMAL.ordinal();
            PipeSideMode[] values = PipeSideMode.values();
            modes[i] = ordinal >= 0 && ordinal < values.length ? values[ordinal] : PipeSideMode.NORMAL;
        }
        localAmount = Math.max(0, tag.getInt("amount"));
        networkFill = tag.getFloat("netFill");
        localType = null;
        if (tag.contains("type")) {
            localType = ODAntimatters.get(ResourceLocation.parse(tag.getString("type")));
        }
        if (localAmount <= 0) {
            localType = null;
        }
    }
}
