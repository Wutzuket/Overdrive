package de.wutzuket.overdrive.block.accelerator;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import de.wutzuket.overdrive.antimatter.AntimatterTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class AcceleratorOutputEntity extends BlockEntity {
    // Muss 14.000 mB/Op (Max-Rate-Rezept) plus Puffer aufnehmen
    private static final int TANK_CAPACITY = 500_000;

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final AntimatterTank fluidTank = new AntimatterTank(TANK_CAPACITY);

    public AcceleratorOutputEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.ACCELERATOR_OUTPUT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AcceleratorOutputEntity be) {
    }

    public AntimatterTank getAntimatterTank() {
        return fluidTank;
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public int getAntimatterAmount() {
        return fluidTank.getAmount(0);
    }

    public int getAntimatterCapacity() {
        return fluidTank.getCapacity(0);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        CompoundTag fluidTag = new CompoundTag();
        fluidTank.saveToNBT(fluidTag);
        tag.put("fluid", fluidTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        if (tag.contains("fluid")) {
            fluidTank.loadFromNBT(tag.getCompound("fluid"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
