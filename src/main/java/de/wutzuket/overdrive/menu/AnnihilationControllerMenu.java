package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.annihilation.AnnihilationControllerEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

public class AnnihilationControllerMenu extends AbstractContainerMenu {
    private final AnnihilationControllerEntity be;
    private final int[] data = new int[29];

    public AnnihilationControllerMenu(int id, Inventory inv, AnnihilationControllerEntity be){
        super(ODMenuTypes.ANNIHILATION_CONTROLLER_MENU.get(), id);
        this.be=be;
        addDataSlots(new ContainerData(){
            @Override public int get(int i){
                if(be==null) return 0;
                return switch(i){
                    // Energie als long über zwei int-Slots (low/high), sonst Cap bei 2.15 GFE (int-max)
                    case 0 -> (int)(be.getEnergyStoredLong() & 0xFFFFFFFFL);
                    case 1 -> (int)(be.getEnergyStoredLong() >>> 32);
                    case 2 -> (int)be.getCoreTemp();
                    case 3 -> (int)(be.getVacuumPercent()*10);
                    case 4 -> be.getCoolantStack().getAmount();
                    case 5 -> be.getCoolantCapacity();
                    case 6 -> be.getSteamStack().getAmount();
                    case 7 -> be.getSteamCapacity();
                    case 8 -> be.isValidStructure()?1:0;
                    case 9 -> be.isActive()?1:0;
                    case 10 -> be.isEnabled()?1:0;
                    case 11 -> (int)Math.min(Integer.MAX_VALUE, Math.round(be.getBurnRate()*10));
                    case 12 -> be.getLastCellPorts();
                    case 13 -> be.getLastVacuumPorts();
                    case 14 -> be.getLastCoolantPorts();
                    case 15 -> be.getLastSteamPorts();
                    case 16 -> be.getLastEnergyPorts();
                    case 17 -> be.getLastHeavy();
                    case 18 -> be.getFuelAntiprotonStack().getAmount();
                    case 19 -> be.getFuelPositronStack().getAmount();
                    case 20 -> be.getIdleCode();
                    case 21 -> fluidId(be.getCoolantStack());
                    case 22 -> fluidId(be.getSteamStack());
                    case 23 -> fluidId(be.getFuelAntiprotonStack());
                    case 24 -> fluidId(be.getFuelPositronStack());
                    case 25 -> (int)(be.getLastGenerationLong() & 0xFFFFFFFFL);
                    case 26 -> be.getFuelAntiprotonCapacity();
                    case 27 -> be.getFuelPositronCapacity();
                    case 28 -> (int)(be.getLastGenerationLong() >>> 32);
                    default -> 0;
                };
            }
            @Override public void set(int i,int v){ if(i>=0 && i<data.length) data[i]=v; }
            @Override public int getCount(){ return data.length; }
        });
    }
    public AnnihilationControllerMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf){
        this(id, inv, (AnnihilationControllerEntity)inv.player.level().getBlockEntity(buf.readBlockPos()));
    }
    @Override public ItemStack quickMoveStack(Player p,int s){ return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player p){ return true; }
    public AnnihilationControllerEntity getBlockEntity(){ return be; }
    // Synced getters for Screen (auf Client aus ContainerData)
    public int getEnergy(){ return (int)Math.min(Integer.MAX_VALUE, getEnergyLong()); }
    public int getMaxEnergy(){ return Integer.MAX_VALUE; }
    public long getEnergyLong(){ return ((long)data[1] << 32) | (data[0] & 0xFFFFFFFFL); }
    public long getMaxEnergyLong(){ return AnnihilationControllerEntity.MAX_ENERGY_STORED; }
    public long getGenerationLong(){ return ((long)data[28] << 32) | (data[25] & 0xFFFFFFFFL); }
    public int getTemp(){ return data[2]; }
    public float getVacuum(){ return data[3]/10f; }
    public int getCoolantAmount(){ return data[4]; }
    public int getCoolantCap(){ return data[5]; }
    public int getSteamAmount(){ return data[6]; }
    public int getSteamCap(){ return data[7]; }
    public boolean isValid(){ return data[8]==1; }
    public boolean isActiveSynced(){ return data[9]==1; }
    public boolean isEnabledSynced(){ return data[10]==1; }
    public double getBurnRateSynced(){ return data[11]/10.0; }
    public int getLastCellSynced(){ return data[12]; }
    public int getLastVacSynced(){ return data[13]; }
    public int getLastCoolSynced(){ return data[14]; }
    public int getLastSteamSynced(){ return data[15]; }
    public int getLastEnergySynced(){ return data[16]; }
    public int getLastHeavySynced(){ return data[17]; }
    public int getFuelAntiprotonSynced(){ return data[18]; }
    public int getFuelPositronSynced(){ return data[19]; }
    public int getIdleCode(){ return data[20]; }
    public int getFuelCap(){ return be != null ? be.getFuelCapacity() : 10000; }
    public int getFuelAntiprotonCap(){ return data[26] > 0 ? data[26] : getFuelCap(); }
    public int getFuelPositronCap(){ return data[27] > 0 ? data[27] : getFuelCap(); }

    private static int fluidId(net.neoforged.neoforge.fluids.FluidStack stack){
        if(stack.isEmpty()) return -1;
        return net.minecraft.core.registries.BuiltInRegistries.FLUID.getId(stack.getFluid());
    }
    private static net.neoforged.neoforge.fluids.FluidStack stackById(int id, int amount){
        if(id < 0 || amount <= 0) return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
        var f = net.minecraft.core.registries.BuiltInRegistries.FLUID.byId(id);
        if(f == null || f == net.minecraft.world.level.material.Fluids.EMPTY) return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
        return new net.neoforged.neoforge.fluids.FluidStack(f, amount);
    }
    public net.neoforged.neoforge.fluids.FluidStack getCoolantFluidStack(){ return stackById(data[21], data[4]); }
    public net.neoforged.neoforge.fluids.FluidStack getSteamFluidStack(){ return stackById(data[22], data[6]); }
    public net.neoforged.neoforge.fluids.FluidStack getFuelAntiprotonFluidStack(){ return stackById(data[23], data[18]); }
    public net.neoforged.neoforge.fluids.FluidStack getFuelPositronFluidStack(){ return stackById(data[24], data[19]); }
    public int getGenerationSynced(){ return data[25]; }
    public static String fluidName(net.neoforged.neoforge.fluids.FluidStack stack, String fallback){
        if(stack.isEmpty()) return fallback;
        return stack.getHoverName().getString();
    }
}
