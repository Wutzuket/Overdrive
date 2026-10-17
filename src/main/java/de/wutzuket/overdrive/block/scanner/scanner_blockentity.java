package de.wutzuket.overdrive.block.scanner;

import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.block.scanner.recipe.scanner_match;
import de.wutzuket.overdrive.block.scanner.recipe.scanner_recipe_manager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class scanner_blockentity extends BlockEntity implements IEnergyStorage {
    private static final int MAX_ENERGY = 100_000;
    private static final String TAG_ENERGY = "energy";
    private static final int ANIMATION_DURATION = 40;
    private static final int HOLD_DURATION = 10;

    private int energy = 0;

    private @Nullable ResourceLocation matchedRecipeId;
    private @Nullable ResourceLocation matchedResultId;
    private int matchedResultCount = 0;
    private @Nullable Direction matchedFacing;
    private boolean matchedMirrored = false;
    private boolean animating = false;
    private boolean waitingToDrop = false;
    private int animationTicks = 0;
    private boolean consumed = false;
    private final List<CompoundTag> animationSnapshot = new ArrayList<>();

    public scanner_blockentity(BlockPos blockPos, BlockState blockState) {
        super(ODBlockEntities.SCANNER_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, scanner_blockentity blockEntity) {
        if (blockEntity.animating) {
            blockEntity.animationTicks++;
            if (blockEntity.animationTicks >= ANIMATION_DURATION) {
                blockEntity.animating = false;
                blockEntity.waitingToDrop = true;
                blockEntity.animationTicks = 0;
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            } else {
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        if (blockEntity.waitingToDrop) {
            blockEntity.animationTicks++;
            if (blockEntity.animationTicks >= HOLD_DURATION && level instanceof ServerLevel serverLevel) {
                if (blockEntity.matchedRecipeId != null) {
                    scanner_recipe_manager.INSTANCE.getRecipe(blockEntity.matchedRecipeId).consume(serverLevel, pos, state, blockEntity.matchedFacing == null ? state.getValue(BlockStateProperties.FACING) : blockEntity.matchedFacing, blockEntity.matchedMirrored);
                }
                blockEntity.waitingToDrop = false;
                blockEntity.consumed = true;
                blockEntity.matchedRecipeId = null;
                blockEntity.matchedResultId = null;
                blockEntity.matchedResultCount = 0;
                blockEntity.matchedFacing = null;
                blockEntity.matchedMirrored = false;
                blockEntity.animationTicks = 0;
                blockEntity.animationSnapshot.clear();
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            } else {
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        scanner_match match = scanner_recipe_manager.INSTANCE.findMatch(level, pos, state);
        ResourceLocation newRecipeId = match == null ? null : match.recipe().id();
        ResourceLocation newResultId = match == null ? null : match.recipe().resultId();
        int newResultCount = match == null ? 0 : match.recipe().resultCount();
        Direction newFacing = match == null ? null : match.facing();
        boolean newMirrored = match != null && match.mirrored();

        if (!Objects.equals(blockEntity.matchedRecipeId, newRecipeId)
                || !Objects.equals(blockEntity.matchedResultId, newResultId)
                || blockEntity.matchedResultCount != newResultCount
                || !Objects.equals(blockEntity.matchedFacing, newFacing)
                || blockEntity.matchedMirrored != newMirrored) {
            blockEntity.matchedRecipeId = newRecipeId;
            blockEntity.matchedResultId = newResultId;
            blockEntity.matchedResultCount = newResultCount;
            blockEntity.matchedFacing = newFacing;
            blockEntity.matchedMirrored = newMirrored;
            blockEntity.consumed = false;
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        if (match != null && !blockEntity.consumed && level.hasNeighborSignal(pos)) {
            int recipeEnergy = match.recipe().energy();
            if (recipeEnergy > 0 && blockEntity.energy < recipeEnergy) {
                return;
            }
            blockEntity.energy -= recipeEnergy;
            if (level instanceof ServerLevel serverLevel) {
                blockEntity.beginAnimation(serverLevel, pos, state, match);
            }
            blockEntity.animating = true;
            blockEntity.waitingToDrop = false;
            blockEntity.animationTicks = 0;
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private void beginAnimation(ServerLevel level, BlockPos pos, BlockState state, scanner_match match) {
        animationSnapshot.clear();
        for (int layer = 0; layer < 3; layer++) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (!match.recipe().isActiveCell(layer, row, col)) {
                        continue;
                    }

                    BlockPos cellPos = match.recipe().getCellPos(pos, state, layer, row, col, match.mirrored(), match.facing());
                    BlockState cellState = level.getBlockState(cellPos);

                    CompoundTag cellTag = new CompoundTag();
                    cellTag.putInt("Layer", layer);
                    cellTag.putInt("Row", row);
                    cellTag.putInt("Col", col);
                    cellTag.putString("BlockId", BuiltInRegistries.BLOCK.getKey(cellState.getBlock()).toString());
                    animationSnapshot.add(cellTag);

                    level.destroyBlock(cellPos, false);
                }
            }
        }
    }

    public @Nullable ResourceLocation getMatchedRecipeId() {
        return matchedRecipeId;
    }

    public @Nullable ResourceLocation getMatchedResultId() {
        return matchedResultId;
    }

    public int getMatchedResultCount() {
        return matchedResultCount;
    }

    public @Nullable Direction getMatchedFacing() {
        return matchedFacing;
    }

    public boolean isMatchedMirrored() {
        return matchedMirrored;
    }

    public boolean isAnimating() {
        return animating;
    }

    public boolean isWaitingToDrop() {
        return waitingToDrop;
    }

    public int getAnimationTicks() {
        return animationTicks;
    }

    public float getAnimationProgress(float partialTick) {
        return Math.min(1.0F, (animationTicks + partialTick) / (float) ANIMATION_DURATION);
    }

    public List<CompoundTag> getAnimationSnapshot() {
        return animationSnapshot;
    }

    public int getEnergy() {
        return energy;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int accepted = Math.min(maxReceive, MAX_ENERGY - energy);
        if (!simulate) {
            energy += accepted;
            setChanged();
        }
        return accepted;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return MAX_ENERGY;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(TAG_ENERGY, energy);
        if (matchedRecipeId != null) {
            tag.putString("MatchedRecipeId", matchedRecipeId.toString());
        }
        if (matchedResultId != null) {
            tag.putString("MatchedResultId", matchedResultId.toString());
        }
        tag.putInt("MatchedResultCount", matchedResultCount);
        if (matchedFacing != null) {
            tag.putString("MatchedFacing", matchedFacing.getName());
        }
        tag.putBoolean("MatchedMirrored", matchedMirrored);
        tag.putBoolean("Animating", animating);
        tag.putBoolean("WaitingToDrop", waitingToDrop);
        tag.putInt("AnimationTicks", animationTicks);
        tag.putBoolean("Consumed", consumed);

        ListTag snapshot = new ListTag();
        for (CompoundTag cellTag : animationSnapshot) {
            snapshot.add(cellTag.copy());
        }
        tag.put("AnimationSnapshot", snapshot);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.contains(TAG_ENERGY, CompoundTag.TAG_INT) ? tag.getInt(TAG_ENERGY) : 0;
        matchedRecipeId = tag.contains("MatchedRecipeId") ? ResourceLocation.parse(tag.getString("MatchedRecipeId")) : null;
        matchedResultId = tag.contains("MatchedResultId") ? ResourceLocation.parse(tag.getString("MatchedResultId")) : null;
        matchedResultCount = tag.contains("MatchedResultCount") ? tag.getInt("MatchedResultCount") : 0;
        matchedFacing = tag.contains("MatchedFacing") ? Direction.byName(tag.getString("MatchedFacing")) : null;
        matchedMirrored = tag.contains("MatchedMirrored") && tag.getBoolean("MatchedMirrored");
        animating = tag.contains("Animating") && tag.getBoolean("Animating");
        waitingToDrop = tag.contains("WaitingToDrop") && tag.getBoolean("WaitingToDrop");
        animationTicks = tag.contains("AnimationTicks") ? tag.getInt("AnimationTicks") : 0;
        consumed = tag.contains("Consumed") && tag.getBoolean("Consumed");
        animationSnapshot.clear();
        if (tag.contains("AnimationSnapshot")) {
            ListTag snapshot = tag.getList("AnimationSnapshot", 10);
            for (int i = 0; i < snapshot.size(); i++) {
                animationSnapshot.add(snapshot.getCompound(i).copy());
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
