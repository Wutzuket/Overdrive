package de.wutzuket.overdrive.integration.top;

import de.wutzuket.overdrive.block.vacuum.*;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VacuumPumpProbeInfoProvider implements IProbeInfoProvider {
    @Override
    public ResourceLocation getID() {
        return ResourceLocation.fromNamespaceAndPath("overdrive", "vacuum_pump");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
        Block block = blockState.getBlock();
        boolean isPumpBlock = block instanceof VacuumPumpBlock;
        boolean isRelated = block instanceof PumpCasingBlock
                || block instanceof EnergyInputPortBlock
                || block instanceof VacuumOutputPortBlock
                || block instanceof VacuumPipeBlock;
        if (!isPumpBlock && !isRelated) return;

        BlockEntity be = world.getBlockEntity(data.getPos());

        if (be instanceof IVacuumNode node) {
            int vac = Math.round(node.getVacuumLevel() * 100);
            probeInfo.progress(vac, 100, probeInfo.defaultProgressStyle()
                    .filledColor(0xFF44AAFF)
                    .backgroundColor(0xFF555555)
                    .width(100)
                    .showText(true)
                    .suffix(Component.literal("%")));
        }

        if (be instanceof VacuumPumpBlockEntity pump) {
            IProbeInfo hor = probeInfo.horizontal();
            hor.text(Component.translatable("container.overdrive.vacuum_pump.volume", pump.getVolume()));
            hor.text(Component.literal("  "));
            hor.text(Component.translatable("container.overdrive.vacuum_pump.usage", pump.getEnergyUsage()));
        } else {
            VacuumPumpBlockEntity pump = findPump(world, data.getPos());
            if (pump != null) {
                IProbeInfo hor = probeInfo.horizontal();
                hor.text(Component.translatable("container.overdrive.vacuum_pump.volume", pump.getVolume()));
                hor.text(Component.literal("  "));
                hor.text(Component.translatable("container.overdrive.vacuum_pump.usage", pump.getEnergyUsage()));
            }
        }
    }

    private VacuumPumpBlockEntity findPump(Level world, BlockPos pos) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockEntity be = world.getBlockEntity(pos.offset(dx, dy, dz));
                    if (be instanceof VacuumPumpBlockEntity p) return p;
                }
            }
        }
        return null;
    }
}
