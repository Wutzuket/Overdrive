package de.wutzuket.overdrive.block.accelerator;

import de.wutzuket.overdrive.fluid.ODFluids;
import de.wutzuket.overdrive.fluid.ODItemsFluid;
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

public class IridiumTargetBlock extends Block implements EntityBlock {
    public IridiumTargetBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof IridiumTargetEntity target) {
                player.openMenu(target, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IridiumTargetEntity target)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        FluidTank tank = target.getTank();

        // Leerer Eimer: 1000 mB abfuellen
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

        // Liquid-Iridium-Eimer: einfuellen
        if (stack.is(ODItemsFluid.LIQUID_IRIDIUM_BUCKET.get())) {
            if (!level.isClientSide) {
                FluidStack bucketFluid = new FluidStack(ODFluids.LIQUID_IRIDIUM_SOURCE.get(), 1000);
                if (tank.isFluidValid(bucketFluid) && tank.fill(bucketFluid, IFluidHandler.FluidAction.SIMULATE) >= 1000) {
                    tank.fill(bucketFluid, IFluidHandler.FluidAction.EXECUTE);
                    if (!player.isCreative()) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    }
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new IridiumTargetEntity(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof IridiumTargetEntity target) {
                IridiumTargetEntity.tick(lvl, pos, st, target);
            }
        };
    }
}
