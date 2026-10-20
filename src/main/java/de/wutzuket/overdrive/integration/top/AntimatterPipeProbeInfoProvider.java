package de.wutzuket.overdrive.integration.top;

import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.antimatter.AntimatterNetwork;
import de.wutzuket.overdrive.block.antimatter.AntimatterPipeBlockEntity;
import de.wutzuket.overdrive.block.antimatter.AntimatterTankEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Shows the amount of antimatter that is inside a pipe network. Like in Mekanism the information
 * is not only about the pipe that is looked at, but about the whole network it belongs to.
 */
public class AntimatterPipeProbeInfoProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return ResourceLocation.fromNamespaceAndPath("overdrive", "antimatter_pipe");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState,
                             IProbeHitData data) {
        BlockEntity be = world.getBlockEntity(data.getPos());

        if (be instanceof AntimatterPipeBlockEntity pipe) {
            AntimatterNetwork network = pipe.getNetwork();
            if (network == null) {
                return;
            }
            int amount = network.getTotalAmount();
            int capacity = network.getTotalCapacity();
            Antimatter type = network.getType();

            // Wie bei Mekanism wird nur der Balken angezeigt, der beim Ueberfahren den
            // Fuellstand der Matrix nennt.
            if (amount > 0 && type != null) {
                probeInfo.progress(amount, capacity, probeInfo.defaultProgressStyle()
                        .filledColor(pipe.getTier().getColor())
                        .backgroundColor(0xFF555555)
                        .width(100)
                        .showText(true)
                        .suffix(Component.literal(" mB")));
            }
            return;
        }

        if (be instanceof AntimatterTankEntity tank) {
            int amount = tank.getAmount(0);
            int capacity = tank.getCapacity(0);
            Antimatter type = tank.getAntimatterInTank(0);
            if (amount > 0 && type != null) {
                probeInfo.progress(amount, capacity, probeInfo.defaultProgressStyle()
                        .filledColor(type.color())
                        .backgroundColor(0xFF555555)
                        .width(100)
                        .showText(true)
                        .suffix(Component.literal(" mB")));
            }
        }
    }
}
