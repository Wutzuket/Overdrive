package de.wutzuket.overdrive.block.wolfram;

import de.wutzuket.overdrive.block.vacuum.IVacuumNode;
import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class PositronVacuumOutputPortEntity extends BlockEntity implements IVacuumNode, MenuProvider {
    private float vacuum = 0;
    private final FluidTank tank = new FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    public PositronVacuumOutputPortEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.POSITRON_VACUUM_OUTPUT_PORT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PositronVacuumOutputPortEntity be) {
        IVacuumNode.propagate(be);
        if (!level.isClientSide) level.sendBlockUpdated(pos, state, state, 2);
    }

    public FluidTank getTank() { return tank; }
    public IItemHandler getItemHandler() { return itemHandler; }

    public boolean canAcceptItem(ItemStack stack) {
        ItemStack existing = itemHandler.getStackInSlot(0);
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, stack)) return false;
        return existing.getCount() + stack.getCount() <= existing.getMaxStackSize();
    }

    public void insertItem(ItemStack stack) {
        ItemStack existing = itemHandler.getStackInSlot(0);
        if (existing.isEmpty()) {
            itemHandler.setStackInSlot(0, stack.copy());
        } else {
            existing.grow(stack.getCount());
        }
    }

    @Override public float getVacuumLevel() { return vacuum; }
    @Override public void setVacuumLevel(float v) { this.vacuum = v; setChanged(); }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overdrive.positron_vacuum_output_port");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new de.wutzuket.overdrive.menu.PositronVacuumOutputPortMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("vacuum", vacuum);
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.put("Inventory", itemHandler.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        vacuum = tag.getFloat("vacuum");
        if (tag.contains("tank")) tank.readFromNBT(registries, tag.getCompound("tank"));
        if (tag.contains("Inventory")) itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
