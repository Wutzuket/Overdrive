package de.wutzuket.overdrive.block.wolfram;

import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.menu.WolframTargetMenu;
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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class WolframTargetEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    public WolframTargetEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.WOLFRAM_TARGET.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WolframTargetEntity be) {
    }

    public boolean hasMesh() {
        ItemStack stack = itemHandler.getStackInSlot(0);
        return stack.is(ODItems.WOLFRAM_MESH.get());
    }

    public boolean damageMesh(int cost) {
        if (level == null || level.isClientSide) return true;
        ItemStack stack = itemHandler.getStackInSlot(0);
        if (!stack.is(ODItems.WOLFRAM_MESH.get())) return true;

        net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) level;
        stack.hurtAndBreak(cost, serverLevel, null, item -> {
            itemHandler.setStackInSlot(0, ItemStack.EMPTY);
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        });
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return !itemHandler.getStackInSlot(0).isEmpty();
    }

    public IItemHandler getItemHandler() { return itemHandler; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overdrive.wolfram_target");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new WolframTargetMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
