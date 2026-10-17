package de.wutzuket.overdrive.block.accelerator;

import de.wutzuket.overdrive.fluid.ODFluids;
import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class IridiumTargetEntity extends BlockEntity {
    public static final int TANK_CAPACITY = 100_000;

    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return !stack.isEmpty() && stack.is(ODFluids.LIQUID_IRIDIUM_SOURCE.get());
        }
    };

    public IridiumTargetEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.IRIDIUM_TARGET.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, IridiumTargetEntity be) {
    }

    public FluidTank getTank() {
        return tank;
    }

    public int fill(FluidStack stack, IFluidHandler.FluidAction action) {
        return tank.fill(stack, action);
    }

    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        return tank.drain(maxDrain, action);
    }

    /** Zaehlt als gueltiges Target, sobald fluessiges Iridium eingefuellt ist (Pipes oder Eimer). */
    public boolean hasValidBlock() {
        return !tank.isEmpty() && tank.getFluid().is(ODFluids.LIQUID_IRIDIUM_SOURCE.get());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fluid", tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("fluid")) {
            tank.readFromNBT(registries, tag.getCompound("fluid"));
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
