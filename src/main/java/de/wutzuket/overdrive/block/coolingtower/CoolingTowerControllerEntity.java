package de.wutzuket.overdrive.block.coolingtower;

import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.recipe.CoolingTowerRecipe;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class CoolingTowerControllerEntity extends BlockEntity implements MenuProvider {
    public static final int W=5, H=9;
    // Controller sits in the base wall (radius 2), facing inward. Center is 2 blocks in front of controller.
    // Port slots relative to CENTER: Input at (2, 1, 0), Output at (-2, 7, 0) in center coordinates
    public static final int INPUT_DX=2, INPUT_DY=1, INPUT_DZ=0;
    public static final int OUTPUT_DX=-2, OUTPUT_DY=7, OUTPUT_DZ=0;
    public static boolean isInputSlot(int x,int y,int z){ return x==INPUT_DX && y==INPUT_DY && z==INPUT_DZ; }
    public static boolean isOutputSlot(int x,int y,int z){ return x==OUTPUT_DX && y==OUTPUT_DY && z==OUTPUT_DZ; }

    public static BlockPos getCenter(BlockPos controllerPos, BlockState state){
        Direction facing=state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        // Controller faces outward (away from center), so center is opposite direction
        return controllerPos.relative(facing.getOpposite(), 2);
    }
    private final FluidTank inputTank = new FluidTank(8000){
        @Override protected void onContentsChanged(){ setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    };
    private final FluidTank outputTank = new FluidTank(8000){
        @Override protected void onContentsChanged(){ setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    };
    private boolean valid=false;
    private boolean active=false;
    private boolean hologramEnabled=false;
    private int progress=0;
    private int lastProcessTime=40;
    private @Nullable CoolingTowerRecipe recipe=null;
    public int getProgress(){ return progress; }
    public int getMaxProgress(){ return Math.max(1,lastProcessTime); }
    private @Nullable BlockPos inputPortPos=null;
    private @Nullable BlockPos outputPortPos=null;
    private int lastInputPorts=0, lastOutputPorts=0;
    public int getLastInputPorts(){ return lastInputPorts; }
    public int getLastOutputPorts(){ return lastOutputPorts; }
    // Fehlergrund für GUI: 0=keiner, 1=Controller fehlt, 2=falscher Wandblock, 3=Innenraum muss Luft sein, 4=Außenkante muss Luft sein, 5=Ports fehlen
    private int failCode=0;
    private @Nullable BlockPos failPos=null;
    public int getFailCode(){ return failCode; }
    public @Nullable BlockPos getFailPos(){ return failPos; }
    public String getFailReason(){
        return switch(failCode){
            case 1 -> "Controller missing at base";
            case 2 -> "Wrong wall block";
            case 3 -> "Interior must be air";
            case 4 -> "Outer edge must be air (y+3..5)";
            case 5 -> "Ports missing";
            default -> "";
        };
    }

    public CoolingTowerControllerEntity(BlockPos pos, BlockState state){ super(ODBlockEntities.COOLING_TOWER_CONTROLLER.get(), pos, state); }

    public void tick(Level level, BlockPos pos, BlockState state){
        if(level.isClientSide){
            if(valid && active && level.random.nextFloat()<0.3f){
                BlockPos center = getCenter(pos, state);
                double x=center.getX()+0.5, y=pos.getY()+H+0.5, z=center.getZ()+0.5;
                level.addParticle(ParticleTypes.CLOUD, x+level.random.nextGaussian()*0.5, y, z+level.random.nextGaussian()*0.5, 0,0.05,0);
                level.addParticle(ParticleTypes.CLOUD, x+level.random.nextGaussian()*0.5, y+0.5, z+level.random.nextGaussian()*0.5, 0,0.05,0);
                level.addParticle(ParticleTypes.CLOUD, x+level.random.nextGaussian()*0.5, y+1.0, z+level.random.nextGaussian()*0.5, 0,0.05,0);
            }
            return;
        }
        boolean wasValid=valid;
        valid=checkStructure(level,pos);
        if(valid && !wasValid){
            if(level instanceof ServerLevel sl){
                // Ganze Turm-Schale abgehen (gleiche Geometrie wie checkStructure).
                DustParticleOptions opt=new DustParticleOptions(new Vector3f(1f, 0f, 0f), 2.5f);
                BlockPos center = getCenter(pos, state);
                int cx=center.getX(), cy=center.getY(), cz=center.getZ();
                for(int y=0; y<H; y++){
                    int r = (y==0 || y==H-1) ? 2 : (y>=3 && y<=5 ? 1 : 2);
                    for(int x=-r; x<=r; x+=1) for(int z=-r; z<=r; z+=1){
                        if(Math.abs(x)!=r && Math.abs(z)!=r) continue;
                        BlockPos pp=new BlockPos(cx+x, cy+y, cz+z);
                        sl.sendParticles(opt, pp.getX()+0.5, pp.getY()+0.5, pp.getZ()+0.5, 3, 0.2, 0.2, 0.2, 0.05);
                    }
                }
            }
            setChanged();
        }
        if(!valid){ active=false; progress=0; recipe=null; inputPortPos=null; outputPortPos=null; setChanged(); level.sendBlockUpdated(pos,state,state,3); return; }
        if(inputPortPos==null || outputPortPos==null) cachePorts(level,pos);
        transferWithPorts(level);
        // Gecachtes Rezept muss zu den aktuellen Tankinhalten passen, sonst neu suchen
        // (verhindert, dass bei Fluidwechsel das alte Rezept mit falschem Inhalt weiterläuft
        //  und fremdes Fluid in den Output schreibt)
        if(recipe!=null && !recipeMatchesTanks(recipe)){ recipe=null; progress=0; }
        if(recipe==null) recipe=findRecipe(level);
        if(recipe==null || inputTank.getFluidAmount() < recipe.inputFluid().getAmount()){
            active=false; progress=0; setChanged(); level.sendBlockUpdated(pos,state,state,3); return;
        }
        // Max-Fortschritt pro Tick aus dem AKTUELLEN Rezept nehmen (Rezept kann je nach Fluid wechseln)
        if(lastProcessTime!=recipe.processTime()){ lastProcessTime=recipe.processTime(); setChanged(); }
        // Output enthält ein Fremdfluid (nicht der Rezept-Output zum Input): Rezept darf nicht laufen
        if(hasMismatchedOutput(recipe)){
            active=false; progress=0; recipe=null; setChanged(); level.sendBlockUpdated(pos,state,state,3); return;
        }
        // check output space
        FluidStack out=recipe.outputFluid();
        if(outputTank.fill(out.copy(), IFluidHandler.FluidAction.SIMULATE) < out.getAmount()){
            active=false; setChanged(); level.sendBlockUpdated(pos,state,state,3); return;
        }
        if(!active){ active=true; progress=0; setChanged(); level.sendBlockUpdated(pos,state,state,3); }
        progress++;
        if(progress>=recipe.processTime()){
            inputTank.drain(recipe.inputFluid().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            outputTank.fill(out.copy(), IFluidHandler.FluidAction.EXECUTE);
            progress=0; recipe=null;
            if(level instanceof ServerLevel sl) {
                BlockPos center = getCenter(pos, state);
                sl.sendParticles(ParticleTypes.CLOUD, center.getX()+0.5, pos.getY()+H+0.5, center.getZ()+0.5, 10, 0.5,0.2,0.5,0.02);
            }
            setChanged(); level.sendBlockUpdated(pos,state,state,3);
        } else {
            setChanged(); level.sendBlockUpdated(pos,state,state,2);
        }
    }

    private void cachePorts(Level level, BlockPos ctrl){
        inputPortPos=null; outputPortPos=null;
        BlockPos center = getCenter(ctrl, level.getBlockState(ctrl));
        int cx=center.getX(), cy=center.getY(), cz=center.getZ();
        for(int y=0; y<H; y++){
            int r = (y==0 || y==H-1) ? 2 : (y>=3 && y<=5 ? 1 : 2);
            for(int x=-r; x<=r; x++) for(int z=-r; z<=r; z++){
                boolean isWall = Math.abs(x)==r || Math.abs(z)==r;
                if(!isWall) continue;
                BlockPos p=new BlockPos(cx+x, cy+y, cz+z);
                Block b=level.getBlockState(p).getBlock();
                if(b instanceof CoolingTowerInputPortBlock && inputPortPos==null) inputPortPos=p.immutable();
                else if(b instanceof CoolingTowerOutputPortBlock && outputPortPos==null) outputPortPos=p.immutable();
            }
        }
    }

    private void transferWithPorts(Level level){
        if(inputPortPos!=null){
            BlockEntity be=level.getBlockEntity(inputPortPos);
            if(be instanceof CoolingTowerInputPortEntity port){
                FluidStack avail=port.getTank().getFluid();
                if(!avail.isEmpty()){
                    int canFill=inputTank.fill(avail.copy(), IFluidHandler.FluidAction.SIMULATE);
                    if(canFill>0){
                        FluidStack drained=port.getTank().drain(canFill, IFluidHandler.FluidAction.EXECUTE);
                        inputTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            } else inputPortPos=null;
        }
        if(outputPortPos!=null){
            BlockEntity be=level.getBlockEntity(outputPortPos);
            if(be instanceof CoolingTowerOutputPortEntity port){
                FluidStack avail=outputTank.getFluid();
                if(!avail.isEmpty()){
                    int canFill=port.getTank().fill(avail.copy(), IFluidHandler.FluidAction.SIMULATE);
                    if(canFill>0){
                        FluidStack drained=outputTank.drain(canFill, IFluidHandler.FluidAction.EXECUTE);
                        port.getTank().fill(drained, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            } else outputPortPos=null;
        }
    }

    private boolean checkStructure(Level level, BlockPos ctrl){
        failCode=0; failPos=null;
        BlockPos center = getCenter(ctrl, level.getBlockState(ctrl));
        int cx=center.getX(), cy=center.getY(), cz=center.getZ();

        // Ports an den festen Slots (relativ zum Center)
        BlockPos inSlot=new BlockPos(cx+INPUT_DX, cy+INPUT_DY, cz+INPUT_DZ);
        BlockPos outSlot=new BlockPos(cx+OUTPUT_DX, cy+OUTPUT_DY, cz+OUTPUT_DZ);
        if(level.getBlockState(inSlot).getBlock() instanceof CoolingTowerInputPortBlock){ lastInputPorts=1; inputPortPos=inSlot.immutable(); }
        else { lastInputPorts=0; inputPortPos=null; }
        if(level.getBlockState(outSlot).getBlock() instanceof CoolingTowerOutputPortBlock){ lastOutputPorts=1; outputPortPos=outSlot.immutable(); }
        else { lastOutputPorts=0; outputPortPos=null; }

        // Controller must be at base wall (y=cy, radius 2 from center) - facing outward from center
        Direction facing=level.getBlockState(ctrl).getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos expectedCtrl=new BlockPos(cx + facing.getStepX()*2, cy, cz + facing.getStepZ()*2);
        if(!ctrl.equals(expectedCtrl)){
            failCode=1; failPos=ctrl.immutable(); return false;
        }
        if(!(level.getBlockState(ctrl).getBlock() instanceof CoolingTowerControllerBlock)){
            failCode=1; failPos=ctrl.immutable(); return false;
        }

        // structure 5x5x9 with hyperbolic waist (middle narrower 3x3), centered at center
        for(int y=0; y<H; y++){
            int r = (y==0 || y==H-1) ? 2 : (y>=3 && y<=5 ? 1 : 2);
            for(int x=-r; x<=r; x++) for(int z=-r; z<=r; z++){
                boolean isWall = Math.abs(x)==r || Math.abs(z)==r;
                BlockPos pp=new BlockPos(cx+x, cy+y, cz+z);
                Block b=level.getBlockState(pp).getBlock();
                if(pp.equals(ctrl)){
                    // Controller position in wall - already checked above
                    continue;
                } else if(isWall){
                    if(isInputSlot(x,y,z)){
                        if(!(b instanceof CoolingTowerInputPortBlock)){ failCode=2; failPos=pp.immutable(); return false; }
                    } else if(isOutputSlot(x,y,z)){
                        if(!(b instanceof CoolingTowerOutputPortBlock)){ failCode=2; failPos=pp.immutable(); return false; }
                    } else {
                        if(b instanceof CoolingTowerInputPortBlock || b instanceof CoolingTowerOutputPortBlock){ failCode=2; failPos=pp.immutable(); return false; }
                        if(!(b instanceof CoolingTowerCasingBlock)){ failCode=2; failPos=pp.immutable(); return false; }
                    }
                } else {
                    // interior must be air
                    if(!level.getBlockState(pp).isAir()){ failCode=3; failPos=pp.immutable(); return false; }
                }
            }
        }
        // allow outer frame for 5x5: check full 5x5 shell
        for(int y=0; y<H; y++){
            for(int x=-2; x<=2; x++) for(int z=-2; z<=2; z++){
                int dx=Math.abs(x), dz=Math.abs(z);
                boolean isEdge = dx==2 || dz==2;
                if(!isEdge) continue;
                // middle levels (3-5) should be air at outer edge (hyperbolic)
                if(y>=3 && y<=5){
                    BlockPos pp=new BlockPos(cx+x, cy+y, cz+z);
                    if(!level.getBlockState(pp).isAir()){ failCode=4; failPos=pp.immutable(); return false; }
                }
            }
        }
        if(lastInputPorts<1 || lastOutputPorts<1){ failCode=5; failPos=null; return false; }
        return true;
    }

    private boolean recipeMatchesTanks(CoolingTowerRecipe r){
        FluidStack in=inputTank.getFluid();
        if(in.isEmpty() || !in.is(r.inputFluid().getFluid())) return false;
        if(hasMismatchedOutput(r)) return false;
        return true;
    }

    // True, wenn im Controller-Outputtank ein Fluid liegt,
    // das nicht dem Rezept-Output zum aktuellen Input entspricht.
    // In dem Fall darf das Rezept nicht laufen (weder anlaufen noch weiterlaufen).
    // (Nur der Controller-Tank zählt: Der Port-Tank ist ein separates Lager
    //  und darf Reste eines früheren Rezepts enthalten, ohne Neuanlauf zu blockieren.)
    private boolean hasMismatchedOutput(CoolingTowerRecipe r){
        FluidStack out=outputTank.getFluid();
        if(!out.isEmpty() && !out.is(r.outputFluid().getFluid())) return true;
        return false;
    }

    private @Nullable CoolingTowerRecipe findRecipe(Level level){
        if(inputTank.isEmpty()) return null;
        FluidStack in=inputTank.getFluid();
        for(RecipeHolder<?> h: level.getRecipeManager().getRecipes()){
            if(h.value() instanceof CoolingTowerRecipe r && r.getType()==ODRecipeTypes.COOLING_TOWER.get()){
                if(in.is(r.inputFluid().getFluid()) && in.getAmount()>=r.inputFluid().getAmount()){
                    // Output mit Fremdfluid belegt -> dieses Rezept darf nicht anlaufen
                    if(hasMismatchedOutput(r)) continue;
                    return r;
                }
            }
        }
        // fallback: steam -> water
        if(in.getFluid()==net.minecraft.world.level.material.Fluids.WATER) return null;
        // generic fallback recipe if no datapack: steam to water
        return null;
    }

    public FluidTank getInputTank(){ return inputTank; }
    public FluidTank getOutputTank(){ return outputTank; }
    private @Nullable CoolingTowerOutputPortEntity getOutputPort(){
        if(outputPortPos!=null && level!=null){
            BlockEntity be=level.getBlockEntity(outputPortPos);
            if(be instanceof CoolingTowerOutputPortEntity port) return port;
        }
        return null;
    }
    // Kombinierte Output-Menge fürs GUI: Controller-Tank + Output-Port (Transfer schiebt pro Tick um)
    public int getOutputTotalAmount(){
        int amt=outputTank.getFluidAmount();
        CoolingTowerOutputPortEntity port=getOutputPort();
        if(port!=null) amt+=port.getTank().getFluidAmount();
        return amt;
    }
    public int getOutputTotalCapacity(){
        int cap=outputTank.getCapacity();
        CoolingTowerOutputPortEntity port=getOutputPort();
        if(port!=null) cap+=port.getTank().getCapacity();
        return cap;
    }
    public FluidStack getOutputFluidForDisplay(){
        if(!outputTank.isEmpty()) return outputTank.getFluid();
        CoolingTowerOutputPortEntity port=getOutputPort();
        if(port!=null && !port.getTank().isEmpty()) return port.getTank().getFluid();
        return FluidStack.EMPTY;
    }
    public boolean isValid(){ return valid; }
    public boolean isActive(){ return active; }
    public boolean isHologramEnabled(){
        if(level!=null && level.isClientSide && de.wutzuket.overdrive.Overdrive.isClientHologramEnabled(worldPosition)) return true;
        return hologramEnabled;
    }
    public void toggleHologram(){ hologramEnabled=!hologramEnabled; setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }

    @Override public Component getDisplayName(){ return Component.translatable("container.overdrive.cooling_tower"); }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player){ return new de.wutzuket.overdrive.menu.CoolingTowerMenu(id, inv, this); }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r){
        super.saveAdditional(tag,r);
        tag.putBoolean("valid",valid); tag.putBoolean("active",active); tag.putBoolean("holo",hologramEnabled); tag.putInt("progress",progress); tag.putInt("failCode",failCode); tag.putInt("lastTime",lastProcessTime);
        if(failPos!=null) tag.putLong("failPos",failPos.asLong());
        tag.putInt("inPorts",lastInputPorts); tag.putInt("outPorts",lastOutputPorts);
        if(inputPortPos!=null) tag.putLong("inPort",inputPortPos.asLong());
        if(outputPortPos!=null) tag.putLong("outPort",outputPortPos.asLong());
        tag.put("in", inputTank.writeToNBT(r,new CompoundTag()));
        tag.put("out", outputTank.writeToNBT(r,new CompoundTag()));
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r){
        super.loadAdditional(tag,r);
        valid=tag.getBoolean("valid"); active=tag.getBoolean("active"); hologramEnabled=tag.getBoolean("holo"); progress=tag.getInt("progress"); failCode=tag.getInt("failCode");
        if(tag.contains("lastTime")) lastProcessTime=Math.max(1,tag.getInt("lastTime"));
        failPos=tag.contains("failPos")?BlockPos.of(tag.getLong("failPos")):null;
        lastInputPorts=tag.getInt("inPorts"); lastOutputPorts=tag.getInt("outPorts");
        inputPortPos=tag.contains("inPort")?BlockPos.of(tag.getLong("inPort")):null;
        outputPortPos=tag.contains("outPort")?BlockPos.of(tag.getLong("outPort")):null;
        if(tag.contains("in")) inputTank.readFromNBT(r, tag.getCompound("in"));
        if(tag.contains("out")) outputTank.readFromNBT(r, tag.getCompound("out"));
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){ return saveWithoutMetadata(r); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){ return ClientboundBlockEntityDataPacket.create(this); }
}
