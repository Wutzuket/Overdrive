package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyOutputPortEntity extends BlockEntity implements IEnergyStorage {
    // Max-Rate-Auslegung: 8 GFE/t passen nicht in int (IEnergyStorage ist int-basiert).
    // Intern long, pro Nachbar und Tick max. Integer.MAX_VALUE -> volle 8 GFE/t brauchen 4x Output-Ports.
    public static final long CAPACITY = 8_000_000_000L;
    private long energy=0;
    public EnergyOutputPortEntity(BlockPos pos, BlockState state){ super(ODBlockEntities.ANNIHILATION_ENERGY_OUTPUT.get(), pos, state); }
    public void tick(Level level, BlockPos pos, BlockState state){
        if(level.isClientSide) return;
        // push energy to neighbors
        for(Direction dir: Direction.values()){
            BlockEntity be=level.getBlockEntity(pos.relative(dir));
            if(be!=null){
                var cap=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, pos.relative(dir), dir.getOpposite());
                if(cap!=null && energy>0){
                    int toSend=(int)Math.min(energy, (long)Integer.MAX_VALUE);
                    int accepted=cap.receiveEnergy(toSend,false);
                    energy-=accepted;
                    if(accepted>0) setChanged();
                }
            }
        }
    }
    public void addEnergy(long amount){ energy=Math.min(CAPACITY, energy+amount); setChanged(); }
    public long getEnergyStoredLong(){ return energy; }
    public long getCapacityLong(){ return CAPACITY; }
    @Override public int receiveEnergy(int maxReceive, boolean simulate){ int a=(int)Math.min((long)maxReceive, CAPACITY-energy); if(!simulate){ energy+=a; setChanged();} return a; }
    @Override public int extractEnergy(int maxExtract, boolean simulate){ int e=(int)Math.min((long)maxExtract, energy); if(!simulate){ energy-=e; setChanged(); } return e; }
    // int-Sicht ist gecappt (Interface-Limit), volle Werte via getEnergyStoredLong()
    @Override public int getEnergyStored(){ return (int)Math.min(energy, (long)Integer.MAX_VALUE); }
    @Override public int getMaxEnergyStored(){ return Integer.MAX_VALUE; }
    @Override public boolean canExtract(){ return true; }
    @Override public boolean canReceive(){ return false; }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r){ super.saveAdditional(tag,r); tag.putLong("energy",energy); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r){ super.loadAdditional(tag,r); energy=tag.contains("energy")?tag.getLong("energy"):0L; }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){ return saveWithoutMetadata(r); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){ return ClientboundBlockEntityDataPacket.create(this); }
}
