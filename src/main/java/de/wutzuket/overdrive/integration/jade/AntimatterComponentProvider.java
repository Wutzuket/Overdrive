package de.wutzuket.overdrive.integration.jade;

import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.block.antimatter.AntimatterPipeBlockEntity;
import de.wutzuket.overdrive.block.antimatter.AntimatterTankEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ProgressStyle;

/**
 * Shows the fill level of an antimatter pipe network or an antimatter tank, matching The One
 * Probe integration.
 */
public class AntimatterComponentProvider implements IComponentProvider<BlockAccessor> {

    public static final AntimatterComponentProvider INSTANCE = new AntimatterComponentProvider();

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath("overdrive", "antimatter");

    private AntimatterComponentProvider() {
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockEntity be = accessor.getBlockEntity();

        if (be instanceof AntimatterPipeBlockEntity pipe) {
            CompoundTag serverData = accessor.getServerData();
            if (serverData.contains(AntimatterServerDataProvider.AMOUNT)) {
                int amount = serverData.getInt(AntimatterServerDataProvider.AMOUNT);
                int capacity = serverData.getInt(AntimatterServerDataProvider.CAPACITY);
                if (amount > 0) {
                    tooltip.add(progress(amount, capacity, pipe.getTier().getColor()));
                }
                return;
            }

            // Fallback when the server does not run Jade: the fill level of the network is
            // synchronised to the client by the pipe itself.
            float fill = Mth.clamp(pipe.getNetworkFillLevel(), 0.0F, 1.0F);
            if (fill > 0.0F && pipe.getLocalType() != null) {
                tooltip.add(progress(fill, Component.literal(Math.round(fill * 100.0F) + "%"),
                        pipe.getTier().getColor()));
            }
            return;
        }

        if (be instanceof AntimatterTankEntity tank) {
            int amount = tank.getAmount(0);
            int capacity = tank.getCapacity(0);
            Antimatter type = tank.getAntimatterInTank(0);
            if (amount > 0 && type != null) {
                tooltip.add(progress(amount, capacity, type.color()));
            }
        }
    }

    private static IElement progress(int amount, int capacity, int color) {
        float fill = capacity <= 0 ? 0.0F : (float) amount / capacity;
        return progress(fill, Component.literal(amount + " / " + capacity + " mB"), color);
    }

    private static IElement progress(float value, Component text, int color) {
        IElementHelper helper = IElementHelper.get();
        ProgressStyle style = helper.progressStyle()
                .color(color)
                .textColor(0xFFFFFF)
                .fitContentX(false);
        return helper.progress(Mth.clamp(value, 0.0F, 1.0F), text, style, BoxStyle.getNestedBox(), true)
                .size(new Vec2(100.0F, 12.0F));
    }
}
