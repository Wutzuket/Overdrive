package de.wutzuket.overdrive.block.press;

import de.wutzuket.overdrive.config.ODConfig;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.menu.CircuitPressMenu;
import de.wutzuket.overdrive.recipe.CircuitPressRecipe;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class CircuitPressBlockEntity extends BlockEntity implements IEnergyStorage, MenuProvider {
    private int energy;
    private int progress;
    private boolean active;
    private @Nullable CircuitPressRecipe recipe;
    private final ItemStackHandler items = new ItemStackHandler(5) {
        @Override protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot < 4; }
        @Override public int getSlotLimit(int slot) { return ODConfig.CIRCUIT_PRESS_ITEM_HANDLER_CAPACITY.get(); }
    };

    public CircuitPressBlockEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.CIRCUIT_PRESS.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;
        pullEnergyFromNeighbors();
        CircuitPressRecipe current = findRecipe();
        boolean wasActive = active;
        if (current == null || !hasEnergy(current) || !canOutput(current.output())) {
            recipe = current;
            active = false;
            progress = 0;
            if (wasActive || current != null) sync(3);
            return;
        }
        recipe = current;
        active = true;
        energy -= current.energyPerTick();
        progress++;
        if (progress >= current.processTime()) {
            consumeInputs(current);
            ItemStack existing = items.getStackInSlot(4);
            if (existing.isEmpty()) {
                items.setStackInSlot(4, current.output().copy());
            } else {
                existing.grow(current.output().getCount());
                items.setStackInSlot(4, existing);
            }
            progress = 0;
        }
        setChanged();
        sync(wasActive ? 2 : 3);
    }

    private void sync(int flags) {
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), flags);
        }
    }

    private void pullEnergyFromNeighbors() {
        if (level == null || energy >= getMaxEnergy()) return;
        int needed = getMaxEnergy() - energy;
        for (Direction direction : Direction.values()) {
            IEnergyStorage neighbor = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    worldPosition.relative(direction), direction.getOpposite());
            if (neighbor == null || !neighbor.canExtract()) continue;
            int accepted = receiveEnergy(neighbor.extractEnergy(needed, true), true);
            if (accepted <= 0) continue;
            int extracted = neighbor.extractEnergy(accepted, false);
            receiveEnergy(extracted, false);
            needed -= extracted;
            if (needed <= 0) return;
        }
    }

    private boolean hasEnergy(CircuitPressRecipe current) {
        return energy >= current.energyPerTick();
    }

    private boolean canOutput(ItemStack output) {
        ItemStack existing = items.getStackInSlot(4);
        return existing.isEmpty() || (ItemStack.isSameItemSameComponents(existing, output)
                && existing.getCount() + output.getCount() <= Math.min(existing.getMaxStackSize(), 64));
    }

    private @Nullable CircuitPressRecipe findRecipe() {
        if (level == null) return null;
        for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
            if (holder.value() instanceof CircuitPressRecipe candidate
                    && candidate.getType() == ODRecipeTypes.CIRCUIT_PRESS.get()
                    && matches(candidate)) return candidate;
        }
        return null;
    }

    private boolean matches(CircuitPressRecipe candidate) {
        boolean[] used = new boolean[4];
        for (Ingredient ingredient : candidate.ingredients()) {
            boolean found = false;
            for (int slot = 0; slot < 4; slot++) {
                if (!used[slot] && ingredient.test(items.getStackInSlot(slot))) {
                    used[slot] = true;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private void consumeInputs(CircuitPressRecipe current) {
        boolean[] used = new boolean[4];
        for (Ingredient ingredient : current.ingredients()) {
            for (int slot = 0; slot < 4; slot++) {
                if (!used[slot] && ingredient.test(items.getStackInSlot(slot))) {
                    used[slot] = true;
                    items.extractItem(slot, 1, false);
                    break;
                }
            }
        }
    }

    public ItemStackHandler getItems() { return items; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return recipe == null ? ODConfig.CIRCUIT_PRESS_PROCESS_TIME.get() : recipe.processTime(); }
    public int getEnergyPerTick() { return recipe == null ? ODConfig.CIRCUIT_PRESS_ENERGY_PER_TICK.get() : recipe.energyPerTick(); }
    public boolean isActive() { return active; }
    public int getMaxEnergy() { return ODConfig.CIRCUIT_PRESS_MAX_ENERGY.get(); }

    @Override public Component getDisplayName() { return Component.translatable("container.overdrive.circuit_press"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CircuitPressMenu(id, inventory, this);
    }
    @Override public int receiveEnergy(int maxReceive, boolean simulate) {
        int accepted = Math.min(maxReceive, getMaxEnergy() - energy);
        if (!simulate) { energy += accepted; setChanged(); }
        return Math.max(0, accepted);
    }
    @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
    @Override public int getEnergyStored() { return energy; }
    @Override public int getMaxEnergyStored() { return getMaxEnergy(); }
    @Override public boolean canExtract() { return false; }
    @Override public boolean canReceive() { return true; }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putInt("progress", progress);
        tag.putBoolean("active", active);
        tag.put("items", items.serializeNBT(registries));
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
        progress = tag.getInt("progress");
        active = tag.getBoolean("active");
        items.deserializeNBT(registries, tag.getCompound("items"));
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
