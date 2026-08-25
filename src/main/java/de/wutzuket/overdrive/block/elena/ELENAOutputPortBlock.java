package de.wutzuket.overdrive.block.elena;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ELENAOutputPortBlock extends Block implements EntityBlock {
    public ELENAOutputPortBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ELENAOutputPortEntity output) {
                player.openMenu(output, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ELENAOutputPortEntity output)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        FluidTank tank = output.getFluidTank();

        if (stack.is(Items.BUCKET)) {
            if (!level.isClientSide) {
                if (tank.getFluidAmount() >= 1000) {
                    FluidStack drained = tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    if (!drained.isEmpty()) {
                        ItemStack filledBucket = new ItemStack(drained.getFluid().getBucket());
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                        if (!player.getInventory().add(filledBucket)) {
                            player.drop(filledBucket, false);
                        }
                    }
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        var bucketHandler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        if (bucketHandler != null && !stack.is(Items.BUCKET)) {
            FluidStack bucketFluid = bucketHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
            if (!bucketFluid.isEmpty() && tank.isFluidValid(bucketFluid) && tank.fill(bucketFluid, IFluidHandler.FluidAction.SIMULATE) >= 1000) {
                if (!level.isClientSide) {
                    bucketHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    tank.fill(bucketFluid, IFluidHandler.FluidAction.EXECUTE);
                    if (!player.isCreative()) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ELENAOutputPortEntity(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof ELENAOutputPortEntity output) {
                ELENAOutputPortEntity.tick(lvl, pos, st, output);
            }
        };
    }
}
