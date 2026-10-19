package de.wutzuket.overdrive.block.coalgenerator;

import de.wutzuket.overdrive.config.ODConfig;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.menu.CoalGeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class CoalGeneratorEntity extends BlockEntity implements IEnergyStorage, MenuProvider {
    private final ItemStackHandler fuelHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return getBurnTime(stack) > 0;
        }
    };

    private int energy = 0;
    private int burnTime = 0;
    private int currentBurnTime = 0;

    public CoalGeneratorEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.COAL_GENERATOR_BLOCK_ENTITY.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        boolean changed = false;
        boolean active = isActive();

        if (burnTime > 0) {
            int maxEnergy = getMaxEnergyStored();
            if (energy < maxEnergy) {
                int toAdd = Math.min(ODConfig.COAL_GENERATOR_FE_PER_TICK.get(), maxEnergy - energy);
                energy += toAdd;
                burnTime--;
                changed = true;
            }
        } else {
            ItemStack fuel = fuelHandler.getStackInSlot(0);
            if (!fuel.isEmpty()) {
                int burn = getBurnTime(fuel);
                if (burn > 0) {
                    currentBurnTime = burn;
                    burnTime = burn;
                    // Rest-Item (z.B. leerer Eimer aus einem Lava-Eimer) zurueckgeben
                    ItemStack remaining = fuel.getCraftingRemainingItem();
                    fuel.shrink(1);
                    if (!remaining.isEmpty() && fuel.isEmpty()) {
                        fuelHandler.setStackInSlot(0, remaining.copy());
                    }
                    changed = true;
                }
            }
        }

        if (active != isActive()) changed = true;

        // Energie automatisch an angrenzende Bloecke weitergeben (Pipes, Ports, ...)
        if (ODConfig.COAL_GENERATOR_AUTO_PUSH.get()) {
            int pushed = pushEnergyToNeighbors(level, pos, ODConfig.COAL_GENERATOR_FE_PER_TICK.get());
            if (pushed > 0) changed = true;
        }

        if (changed) {
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    /**
     * Schiebt Energie an alle Nachbarbloecke, die eine EnergyStorage-Capability mit
     * canReceive()==true besitzen (z.B. Mekanism-Energy-Cables, Energy-Ports).
     *
     * @return tatsaechlich uebertragene Energie in FE
     */
    private int pushEnergyToNeighbors(Level level, BlockPos pos, int maxPush) {
        if (maxPush <= 0 || energy <= 0) return 0;

        int pushed = 0;
        for (Direction dir : Direction.values()) {
            if (pushed >= maxPush) break;

            // Die Seite des Nachbarn, die zu uns zeigt
            IEnergyStorage storage = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    pos.relative(dir),
                    dir.getOpposite()
            );
            if (storage == null || !storage.canReceive()) continue;

            int available = Math.min(maxPush - pushed, energy);
            if (available <= 0) break;

            int accepted = storage.receiveEnergy(available, false);
            if (accepted > 0) {
                energy -= accepted;
                pushed += accepted;
                setChanged();
            }
        }
        return pushed;
    }

    /** Hat der Generator gerade einen Abnehmer fuer Energie? (fuer die GUI-Anzeige) */
    public boolean hasEnergyReceiver(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) return false;
        for (Direction dir : Direction.values()) {
            IEnergyStorage storage = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    pos.relative(dir),
                    dir.getOpposite()
            );
            if (storage != null && storage.canReceive()) return true;
        }
        return false;
    }

    public boolean isActive() {
        return burnTime > 0;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getCurrentBurnTime() {
        return currentBurnTime;
    }

    public ItemStack getFuelStack() {
        return fuelHandler.getStackInSlot(0);
    }

    public IItemHandler getFuelHandler() {
        return fuelHandler;
    }

    public IItemHandler getFuelWrapper() {
        return fuelHandler;
    }

    /**
     * Brenndauer exakt wie im Vanilla-Ofen (RecipeType.SMELTING = ungekuerzt).
     * Nutzt NeoForge-Data-Map + FurnaceFuelBurnTimeEvent, damit auch Mod-Brennstoffe,
     * Datapack-Werte und selbst registrierte Fuels funktionieren.
     * Zusaetzlich koennen ueber die Config eigene Brennstoffe definiert werden.
     * 0 = kein Brennstoff.
     */
    public static int getBurnTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        // 1) Eigene Brennstoffe aus der Config (itemid=burntime)
        int custom = getCustomBurnTime(stack);
        if (custom > 0) return custom;

        // 2) Vanilla-/Mod-Brennstoffe (Data-Map + FurnaceFuelBurnTimeEvent)
        try {
            return Math.max(0, stack.getBurnTime(RecipeType.SMELTING));
        } catch (Exception e) {
            return 0;
        }
    }

    /** Brenndauer aus der Config-Liste "extraFuels" (itemid=burntime). */
    private static int getCustomBurnTime(ItemStack stack) {
        try {
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            for (String entry : ODConfig.COAL_GENERATOR_EXTRA_FUELS.get()) {
                int idx = entry.indexOf('=');
                if (idx <= 0) continue;
                if (!entry.substring(0, idx).trim().equals(id)) continue;
                try {
                    return Math.max(0, Integer.parseInt(entry.substring(idx + 1).trim()));
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    /** Ist der Stack ueberhaupt ein Brennstoff? */
    public static boolean isFuel(ItemStack stack) {
        return getBurnTime(stack) > 0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overdrive.coal_generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new CoalGeneratorMenu(id, inv, this);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = Math.min(maxExtract, energy);
        if (extracted <= 0) return 0;
        if (!simulate) {
            energy -= extracted;
            setChanged();
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return ODConfig.COAL_GENERATOR_MAX_ENERGY.get().intValue();
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putInt("burnTime", burnTime);
        tag.putInt("currentBurnTime", currentBurnTime);
        tag.put("fuel", fuelHandler.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
        burnTime = tag.getInt("burnTime");
        currentBurnTime = tag.getInt("currentBurnTime");
        fuelHandler.deserializeNBT(registries, tag.getCompound("fuel"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
