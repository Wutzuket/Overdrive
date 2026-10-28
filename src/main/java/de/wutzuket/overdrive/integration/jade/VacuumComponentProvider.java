package de.wutzuket.overdrive.integration.jade;

import de.wutzuket.overdrive.block.accelerator.AcceleratorVacuumInputPortBlock;
import de.wutzuket.overdrive.block.ad.ADVacuumPortBlock;
import de.wutzuket.overdrive.block.annihilation.AnnihilationControllerBlock;
import de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity;
import de.wutzuket.overdrive.block.annihilation.AnnihilationVacuumPortBlock;
import de.wutzuket.overdrive.block.annihilation.CoolantInputPortBlock;
import de.wutzuket.overdrive.block.annihilation.SteamOutputPortBlock;
import de.wutzuket.overdrive.block.elena.ELENAVacuumPortBlock;
import de.wutzuket.overdrive.block.tungsten.PositronVacuumOutputPortBlock;
import de.wutzuket.overdrive.block.vacuum.EnergyInputPortBlock;
import de.wutzuket.overdrive.block.vacuum.IVacuumNode;
import de.wutzuket.overdrive.block.vacuum.PumpCasingBlock;
import de.wutzuket.overdrive.block.vacuum.VacuumOutputPortBlock;
import de.wutzuket.overdrive.block.vacuum.VacuumPipeBlock;
import de.wutzuket.overdrive.block.vacuum.VacuumPumpBlock;
import de.wutzuket.overdrive.block.vacuum.VacuumPumpBlockEntity;
import de.wutzuket.overdrive.util.ODFormat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

import java.util.Locale;

/**
 * Shows the vacuum level of the whole vacuum network, just like The One Probe integration.
 * The provider is registered for every block that can be part of a vacuum network.
 */
public class VacuumComponentProvider implements IComponentProvider<BlockAccessor> {

    public static final VacuumComponentProvider INSTANCE = new VacuumComponentProvider();

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath("overdrive", "vacuum");

    private VacuumComponentProvider() {
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        Block block = accessor.getBlock();
        boolean isPumpBlock = block instanceof VacuumPumpBlock;
        boolean isRelated = block instanceof PumpCasingBlock
                || block instanceof EnergyInputPortBlock
                || block instanceof VacuumOutputPortBlock
                || block instanceof VacuumPipeBlock
                || block instanceof AcceleratorVacuumInputPortBlock
                || block instanceof ADVacuumPortBlock
                || block instanceof ELENAVacuumPortBlock
                || block instanceof PositronVacuumOutputPortBlock
                || block instanceof AnnihilationVacuumPortBlock
                || block instanceof AnnihilationControllerBlock
                || block instanceof CoolantInputPortBlock
                || block instanceof SteamOutputPortBlock;
        if (!isPumpBlock && !isRelated) {
            return;
        }

        CompoundTag serverData = accessor.getServerData();
        if (serverData.contains(VacuumServerDataProvider.VACUUM)
                || serverData.contains(VacuumServerDataProvider.PUMP_VOLUME)
                || serverData.getBoolean(VacuumServerDataProvider.CONTROLLER)) {
            appendServerData(tooltip, serverData);
            return;
        }

        // Fallback when the server does not run Jade. Some values are not synchronised to the
        // client, so this can show a stale or empty vacuum level.
        appendClientData(tooltip, accessor);
    }

    private static void appendServerData(ITooltip tooltip, CompoundTag data) {
        if (data.contains(VacuumServerDataProvider.VACUUM)) {
            float vacuum = Mth.clamp(data.getFloat(VacuumServerDataProvider.VACUUM), 0.0F, 1.0F);
            tooltip.add(progress(vacuum, Component.literal(Math.round(vacuum * 100.0F) + "%"), 0xFF44AAFF));
        }

        if (data.getBoolean(VacuumServerDataProvider.CONTROLLER)) {
            float vacuumPercent = data.getFloat(VacuumServerDataProvider.CONTROLLER_VACUUM);
            float vacuum = Mth.clamp(vacuumPercent / 100.0F, 0.0F, 1.0F);
            tooltip.add(progress(vacuum,
                    Component.literal(Math.round(vacuumPercent) + "% Vakuum"),
                    vacuumPercent >= 99.0F ? 0xFF22CC55 : 0xFF44AAFF));
            tooltip.add(Component.literal(String.format(Locale.ROOT, "Temp: %.0f C / %d C",
                    data.getDouble(VacuumServerDataProvider.CONTROLLER_TEMP),
                    AnnihilationControllerEntity.CORE_TEMP_MAX)));
            tooltip.add(Component.literal("Energy: " + ODFormat.formatEnergy(
                    data.getInt(VacuumServerDataProvider.CONTROLLER_ENERGY))));
            if (!data.getBoolean(VacuumServerDataProvider.CONTROLLER_VALID)) {
                tooltip.add(Component.literal("Invalid Structure").withStyle(style -> style.withColor(0xFF5555)));
                tooltip.add(Component.literal(String.format(Locale.ROOT,
                        "Cell %d/2 Vacuum %d/1 Coolant %d/1 Steam %d/1 Energy %d/1",
                        data.getInt(VacuumServerDataProvider.CONTROLLER_CELLS),
                        data.getInt(VacuumServerDataProvider.CONTROLLER_VACUUM_PORTS),
                        data.getInt(VacuumServerDataProvider.CONTROLLER_COOLANT_PORTS),
                        data.getInt(VacuumServerDataProvider.CONTROLLER_STEAM_PORTS),
                        data.getInt(VacuumServerDataProvider.CONTROLLER_ENERGY_PORTS))));
            } else {
                boolean active = data.getBoolean(VacuumServerDataProvider.CONTROLLER_ACTIVE);
                tooltip.add(Component.literal(active ? "Annihilating" : "Idle")
                        .withStyle(style -> style.withColor(active ? 0x22CC55 : 0xCCAA22)));
            }
            return;
        }

        if (data.contains(VacuumServerDataProvider.PUMP_VOLUME)) {
            addPumpInfo(tooltip,
                    data.getInt(VacuumServerDataProvider.PUMP_VOLUME),
                    data.getInt(VacuumServerDataProvider.PUMP_USAGE));
        }
    }

    private static void appendClientData(ITooltip tooltip, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();

        if (be instanceof IVacuumNode node) {
            float vacuum = Mth.clamp(node.getVacuumLevel(), 0.0F, 1.0F);
            tooltip.add(progress(vacuum, Component.literal(Math.round(vacuum * 100.0F) + "%"), 0xFF44AAFF));
        }

        if (be instanceof AnnihilationControllerEntity ann) {
            float vacuumPercent = ann.getVacuumPercent();
            float vacuum = Mth.clamp(vacuumPercent / 100.0F, 0.0F, 1.0F);
            tooltip.add(progress(vacuum,
                    Component.literal(Math.round(vacuumPercent) + "% Vakuum"),
                    vacuumPercent >= 99.0F ? 0xFF22CC55 : 0xFF44AAFF));
            tooltip.add(Component.literal(String.format(Locale.ROOT, "Temp: %.0f C / %d C",
                    ann.getCoreTemp(), AnnihilationControllerEntity.CORE_TEMP_MAX)));
            tooltip.add(Component.literal("Energy: " + ODFormat.formatEnergy(ann.getEnergyStored())));
            if (!ann.isValidStructure()) {
                tooltip.add(Component.literal("Invalid Structure").withStyle(style -> style.withColor(0xFF5555)));
                tooltip.add(Component.literal(String.format(Locale.ROOT,
                        "Cell %d/2 Vacuum %d/1 Coolant %d/1 Steam %d/1 Energy %d/1",
                        ann.getLastCellPorts(), ann.getLastVacuumPorts(), ann.getLastCoolantPorts(),
                        ann.getLastSteamPorts(), ann.getLastEnergyPorts())));
            } else {
                tooltip.add(Component.literal(ann.isActive() ? "Annihilating" : "Idle")
                        .withStyle(style -> style.withColor(ann.isActive() ? 0x22CC55 : 0xCCAA22)));
            }
            return;
        }

        VacuumPumpBlockEntity pump = be instanceof VacuumPumpBlockEntity pumpEntity
                ? pumpEntity
                : findPump(accessor.getLevel(), accessor.getPosition());
        if (pump != null) {
            addPumpInfo(tooltip, pump.getVolume(), pump.getEnergyUsage());
        }
    }

    private static void addPumpInfo(ITooltip tooltip, int volume, int usage) {
        tooltip.add(Component.translatable("container.overdrive.vacuum_pump.volume", volume)
                .append("  ")
                .append(Component.translatable("container.overdrive.vacuum_pump.usage", usage)));
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

    private static VacuumPumpBlockEntity findPump(Level level, BlockPos pos) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockEntity be = level.getBlockEntity(pos.offset(dx, dy, dz));
                    if (be instanceof VacuumPumpBlockEntity pump) {
                        return pump;
                    }
                }
            }
        }
        return null;
    }
}
