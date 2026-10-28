package de.wutzuket.overdrive.integration.jade;

import de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity;
import de.wutzuket.overdrive.block.vacuum.IVacuumNode;
import de.wutzuket.overdrive.block.vacuum.VacuumPumpBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Sends the server side vacuum values to the client. The vacuum level of a pipe is only
 * calculated on the server, so the client cannot read it from its own block entity.
 */
public class VacuumServerDataProvider implements IServerDataProvider<BlockAccessor> {

    public static final VacuumServerDataProvider INSTANCE = new VacuumServerDataProvider();

    public static final String VACUUM = "overdrive_vacuum";
    public static final String PUMP_VOLUME = "overdrive_vacuum_volume";
    public static final String PUMP_USAGE = "overdrive_vacuum_usage";

    public static final String CONTROLLER = "overdrive_annihilation_controller";
    public static final String CONTROLLER_VACUUM = "overdrive_annihilation_vacuum";
    public static final String CONTROLLER_TEMP = "overdrive_annihilation_temp";
    public static final String CONTROLLER_ENERGY = "overdrive_annihilation_energy";
    public static final String CONTROLLER_VALID = "overdrive_annihilation_valid";
    public static final String CONTROLLER_ACTIVE = "overdrive_annihilation_active";
    public static final String CONTROLLER_CELLS = "overdrive_annihilation_cells";
    public static final String CONTROLLER_VACUUM_PORTS = "overdrive_annihilation_vacuum_ports";
    public static final String CONTROLLER_COOLANT_PORTS = "overdrive_annihilation_coolant_ports";
    public static final String CONTROLLER_STEAM_PORTS = "overdrive_annihilation_steam_ports";
    public static final String CONTROLLER_ENERGY_PORTS = "overdrive_annihilation_energy_ports";

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath("overdrive", "vacuum_data");

    private VacuumServerDataProvider() {
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (be == null) {
            return;
        }

        if (be instanceof IVacuumNode node) {
            data.putFloat(VACUUM, node.getVacuumLevel());
        }

        if (be instanceof AnnihilationControllerEntity controller) {
            data.putBoolean(CONTROLLER, true);
            data.putFloat(CONTROLLER_VACUUM, controller.getVacuumPercent());
            data.putDouble(CONTROLLER_TEMP, controller.getCoreTemp());
            data.putInt(CONTROLLER_ENERGY, controller.getEnergyStored());
            data.putBoolean(CONTROLLER_VALID, controller.isValidStructure());
            data.putBoolean(CONTROLLER_ACTIVE, controller.isActive());
            data.putInt(CONTROLLER_CELLS, controller.getLastCellPorts());
            data.putInt(CONTROLLER_VACUUM_PORTS, controller.getLastVacuumPorts());
            data.putInt(CONTROLLER_COOLANT_PORTS, controller.getLastCoolantPorts());
            data.putInt(CONTROLLER_STEAM_PORTS, controller.getLastSteamPorts());
            data.putInt(CONTROLLER_ENERGY_PORTS, controller.getLastEnergyPorts());
            return;
        }

        VacuumPumpBlockEntity pump = be instanceof VacuumPumpBlockEntity pumpEntity
                ? pumpEntity
                : findPump(accessor.getLevel(), accessor.getPosition());
        if (pump != null) {
            data.putInt(PUMP_VOLUME, pump.getVolume());
            data.putInt(PUMP_USAGE, pump.getEnergyUsage());
        }
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
