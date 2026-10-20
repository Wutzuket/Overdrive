package de.wutzuket.overdrive.item;

import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.block.antimatter.AntimatterTankEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * A creative antimatter tank. Using it on a normal tank turns that tank into a creative one that
 * always has a full amount of its antimatter, like the creative tanks of other mods. There is one
 * creative tank for every type of antimatter.
 */
public class CreativeAntimatterTankItem extends Item {

    private final Antimatter type;

    public CreativeAntimatterTankItem(Properties properties, Antimatter type) {
        super(properties);
        this.type = type;
    }

    /** @return the type of antimatter this tank creates. */
    public Antimatter getType() {
        return type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(de.wutzuket.overdrive.index.ODBlocks.ANTIMATTER_TANK.get())) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof AntimatterTankEntity tank)) {
            return InteractionResult.PASS;
        }
        tank.setCreative(true, type);
        if (!context.getPlayer().isCreative()) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.overdrive.creative_tank").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.overdrive.creative_tank.type",
                type.displayName()).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.overdrive.creative_antimatter_tank", type.displayName());
    }
}
