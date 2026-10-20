package de.wutzuket.overdrive.integration.top;

import de.wutzuket.overdrive.block.accelerator.AcceleratorVacuumInputPortBlock;
import de.wutzuket.overdrive.util.ODFormat;
import de.wutzuket.overdrive.block.ad.ADVacuumPortBlock;
import de.wutzuket.overdrive.block.annihilation.AnnihilationVacuumPortBlock;
import de.wutzuket.overdrive.block.elena.ELENAVacuumPortBlock;
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
                || block instanceof VacuumPipeBlock
                || block instanceof AcceleratorVacuumInputPortBlock
                || block instanceof ADVacuumPortBlock
                || block instanceof ELENAVacuumPortBlock
                || block instanceof AnnihilationVacuumPortBlock
                || block instanceof de.wutzuket.overdrive.block.annihilation.AnnihilationControllerBlock
                || block instanceof de.wutzuket.overdrive.block.annihilation.CoolantInputPortBlock
                || block instanceof de.wutzuket.overdrive.block.annihilation.SteamOutputPortBlock;
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
        // Annihilation Controller spezielle Anzeige
        if (be instanceof de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity ann) {
            int vac = Math.round(ann.getVacuumPercent());
            probeInfo.progress(vac, 100, probeInfo.defaultProgressStyle()
                    .filledColor(vac>=99?0xFF22CC55:0xFF44AAFF)
                    .backgroundColor(0xFF555555)
                    .width(100)
                    .showText(true)
                    .suffix(Component.literal("% Vakuum")));
            // Limit aus der Reaktor-Konstante, damit es zum GUI-Wert passt.
            probeInfo.text(Component.literal(String.format("Temp: %.0f C / %d C", ann.getCoreTemp(),
                    de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity.CORE_TEMP_MAX)));
            probeInfo.text(Component.literal("Energy: " + ODFormat.formatEnergy(ann.getEnergyStored())));
            if(!ann.isValidStructure()){
                probeInfo.text(Component.literal("Invalid Structure").withStyle(s->s.withColor(0xFF5555)));
                probeInfo.text(Component.literal(String.format("Cell %d/2 Vacuum %d/1 Coolant %d/1 Steam %d/1 Energy %d/1",
                        ann.getLastCellPorts(), ann.getLastVacuumPorts(), ann.getLastCoolantPorts(), ann.getLastSteamPorts(), ann.getLastEnergyPorts())));
            } else {
                probeInfo.text(Component.literal(ann.isActive()?"Annihilating":"Idle").withStyle(s->s.withColor(ann.isActive()?0x22CC55:0xCCAA22)));
            }
            return;
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
