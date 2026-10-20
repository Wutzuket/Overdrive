package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.accelerator.ParticleAcceleratorControllerEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class AcceleratorControllerMenu extends AbstractContainerMenu {
    private final ParticleAcceleratorControllerEntity blockEntity;
    private final int[] data = new int[19];

    public AcceleratorControllerMenu(int containerId, Inventory inventory, ParticleAcceleratorControllerEntity blockEntity) {
        super(ODMenuTypes.ACCELERATOR_CONTROLLER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        data[0] = (int) (blockEntity.getEnergyStoredLong() & 0xFFFFFFFFL);
        data[1] = (int) (blockEntity.getEnergyStoredLong() >>> 32);
        data[2] = blockEntity.isValidStructure() ? 1 : 0;
        data[3] = blockEntity.isActive() ? 1 : 0;
        data[4] = blockEntity.getProgressTicks();
        data[5] = blockEntity.isHologramEnabled() ? 1 : 0;
        data[6] = (int) (blockEntity.getVacuumLevel() * 1000);
        data[7] = blockEntity.getEnergyRate();
        data[8] = blockEntity.getTargetIridiumAmount();
        data[9] = blockEntity.getTargetIridiumCapacity();
        data[10] = BuiltInRegistries.FLUID.getId(blockEntity.getTargetIridiumFluid().getFluid());
        data[11] = blockEntity.getChargeScaled();
        data[12] = blockEntity.getChargeTicksElapsed();
        data[13] = blockEntity.getChargeTicksTotal();
        data[14] = blockEntity.getOutputAntimatterAmountForDisplay();
        data[15] = blockEntity.getOutputAntimatterCapacityForDisplay();
        data[16] = blockEntity.getOutputAntimatterForDisplay() == null ? 0 : blockEntity.getOutputAntimatterForDisplay().color();
        data[17] = typeIndex(blockEntity.getOutputAntimatterForDisplay());
        // Produktion in mB/t, x100 als Festkomma (Brokerrechnung).
        data[18] = (int) Math.round(blockEntity.getProductionPerTick() * 100);
        addDataSlots(new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int) (blockEntity.getEnergyStoredLong() & 0xFFFFFFFFL);
                    case 1 -> (int) (blockEntity.getEnergyStoredLong() >>> 32);
                    case 2 -> blockEntity.isValidStructure() ? 1 : 0;
                    case 3 -> blockEntity.isActive() ? 1 : 0;
                    case 4 -> blockEntity.getProgressTicks();
                    case 5 -> blockEntity.isHologramEnabled() ? 1 : 0;
                    case 6 -> (int) (blockEntity.getVacuumLevel() * 1000);
                    case 7 -> blockEntity.getEnergyRate();
                    case 8 -> blockEntity.getTargetIridiumAmount();
                    case 9 -> blockEntity.getTargetIridiumCapacity();
                    case 10 -> BuiltInRegistries.FLUID.getId(blockEntity.getTargetIridiumFluid().getFluid());
                    case 11 -> blockEntity.getChargeScaled();
                    case 12 -> blockEntity.getChargeTicksElapsed();
                    case 13 -> blockEntity.getChargeTicksTotal();
                    case 14 -> blockEntity.getOutputAntimatterAmountForDisplay();
                    case 15 -> blockEntity.getOutputAntimatterCapacityForDisplay();
                    case 16 -> blockEntity.getOutputAntimatterForDisplay() == null ? 0 : blockEntity.getOutputAntimatterForDisplay().color();
                    case 17 -> typeIndex(blockEntity.getOutputAntimatterForDisplay());
                    case 18 -> (int) Math.round(blockEntity.getProductionPerTick() * 100);
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < data.length) {
                    data[index] = value;
                }
            }

            @Override
            public int getCount() {
                return data.length;
            }
        });
    }

    public AcceleratorControllerMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, (ParticleAcceleratorControllerEntity) inventory.player.level()
                .getBlockEntity(buffer.readBlockPos()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public ParticleAcceleratorControllerEntity getBlockEntity() {
        return blockEntity;
    }

    public long getEnergyLong() {
        return ((long) data[1] << 32) | (data[0] & 0xFFFFFFFFL);
    }

    public long getMaxEnergyLong() {
        return ParticleAcceleratorControllerEntity.MAX_ENERGY_STORED;
    }

    public int getMaxRate() {
        return ParticleAcceleratorControllerEntity.MAX_ENERGY_RATE;
    }

    public int getEnergyRateSynced() {
        return data[7];
    }

    public int getIridiumInputSynced() {
        return data[8];
    }

    public int getIridiumInputCap() {
        return data[9];
    }

    public Fluid getIridiumFluid() {
        return BuiltInRegistries.FLUID.byId(data[10]);
    }

    public boolean isValidStructure() {
        return data[2] == 1;
    }

    public boolean isActive() {
        return data[3] == 1;
    }

    public int getProgressTicks() {
        return data[4];
    }

    public int getChargeScaled() {
        return Math.min(200, Math.max(0, data[11]));
    }

    public int getChargeTicksElapsed() {
        return Math.max(0, data[12]);
    }

    public int getChargeTicksTotal() {
        return Math.max(1, data[13]);
    }

    /** Produktion der Maschine in mB pro Tick (z.B. 20000 -> "20 B/t"). */
    public double getProductionPerTickSynced() {
        return data[18] / 100.0;
    }

    public boolean isHologramEnabled() {
        return data[5] == 1;
    }

    public int getEnergyScaled(int max) {
        long maxEnergy = getMaxEnergyLong();
        if (maxEnergy <= 0) return 0;
        return (int) (getEnergyLong() * max / maxEnergy);
    }

    public int getVacuumLevel() {
        return data[6];
    }

    public int getVacuumScaled(int max) {
        return getVacuumLevel() * max / 1000;
    }

    public int getFluidAmountSynced() {
        return data[14];
    }

    public int getFluidCapacitySynced() {
        return data[15];
    }

    /** @return die Farbe des Antimatters im Output (0 wenn leer). */
    /** @return the position of the antimatter type in the list of the types, -1 when there is none. */
    private static int typeIndex(de.wutzuket.overdrive.antimatter.Antimatter type) {
        if (type == null) {
            return -1;
        }
        return de.wutzuket.overdrive.antimatter.ODAntimatters.getTypes().indexOf(type);
    }

    /** @return the antimatter that the accelerator currently outputs. */
    public de.wutzuket.overdrive.antimatter.Antimatter getOutputAntimatterType() {
        int index = data[17];
        var types = de.wutzuket.overdrive.antimatter.ODAntimatters.getTypes();
        if (index < 0 || index >= types.size()) {
            return null;
        }
        return types.get(index);
    }

    public int getOutputAntimatterColor() {
        return data[16];
    }

    /** @return die Menge des Antimatters im Output. */
    public int getOutputAmountSynced() {
        return data[14];
    }

    public Fluid getOutputFluid() {
        return BuiltInRegistries.FLUID.byId(data[16]);
    }
}
