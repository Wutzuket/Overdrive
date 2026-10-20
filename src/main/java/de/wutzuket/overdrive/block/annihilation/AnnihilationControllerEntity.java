package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.block.vacuum.IVacuumNode;
import de.wutzuket.overdrive.config.ODConfig;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.recipe.AnnihilationRecipe;
import de.wutzuket.overdrive.recipe.ODRecipeTypes;
import de.wutzuket.overdrive.recipe.ReactorCoolingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class AnnihilationControllerEntity extends BlockEntity implements MenuProvider {
    public static final int SIZE = 25;
    public static final int OUTER = 12; // 2*12+1=25
    public static final int INNER = 6; // legacy inner hole radius (für Fallback)
    public static final int CORE_RADIUS = 2; // core 5x5x5 = radius 2
    public static final int TORUS_MAJOR = 8; // Hauptradius des Torus
    public static final int TORUS_TUBE = 4;  // Schlauchradius -> Außen 12, Innenloch 4, echte Torus-Geometrie
    public static final int CORE_TEMP_MAX = 5000;
    // Überlast-Gambling: über MELTDOWN_BURN kann der Reaktor jederzeit durchgehen – je weiter darüber,
    // desto wahrscheinlicher (40k bleibt sicher, ~41k knallt im Schnitt nach Minuten). Mit Abweichung,
    // keine exakte Schwelle.
    private static final double MELTDOWN_BURN = 40000;
    private static final double MELTDOWN_CHANCE = 0.01;
    // Max. Temperaturänderung pro Tick: Heizung und Kühlung werden netto verrechnet, damit ein Tick
    // weder übers Gleichgewicht hinausschießt noch ins Sieden stürzt (Sägezahn -> Sättigung -> Meltdown).
    // Gleichgewichte bleiben gleich, echte Überhitzung (leer/trocken/gesättigt) knallt weiter am Cap.
    private static final double MAX_TEMP_CHANGE_PER_TICK = 200;
    private double tickStartTemp = 20;
    private double pendingCooling = 0;
    private boolean meltedThisTick = false;
    private static final int STEAM_PER_TICK = 100; // mB water -> steam
    private static final int ENERGY_PER_ANNIHILATION = 500000; // FE per dose pair

    public static boolean isTorusWall(int dx, int dy, int dz){
        // echter Torus: (sqrt(dx²+dz²)-R)² + dy² = r², Wand bei |dist - r| < 0.9
        double horiz = Math.sqrt((double)dx*dx + (double)dz*dz);
        double distToRing = Math.abs(horiz - TORUS_MAJOR);
        double tubeDist = Math.sqrt(distToRing*distToRing + (double)dy*dy);
        return Math.abs(tubeDist - TORUS_TUBE) < 0.9;
    }
    public static boolean isTorusHollow(int dx, int dy, int dz){
        double horiz = Math.sqrt((double)dx*dx + (double)dz*dz);
        double distToRing = Math.abs(horiz - TORUS_MAJOR);
        double tubeDist = Math.sqrt(distToRing*distToRing + (double)dy*dy);
        return tubeDist < TORUS_TUBE - 0.9;
    }

    private boolean validStructure=false;
    private boolean active=false;
    private boolean hologramEnabled=false;
    private boolean enabled=false; // Mekanism-like start/stop
    private double burnRate=0.1; // abstrakte Rate, siehe ODConfig (min/max)
    private double coreTemp=20;
    // long-Speicher: 8 GFE/t (285714 FE/t x Burn 28000 = 7.999.992.000) passt nicht in int.
    // IEnergyStorage/GUI bleiben int-basiert (gecappt), intern + Ports arbeiten mit long.
    public static final long MAX_ENERGY_STORED = 16_000_000_000_000L; // 16 TFE interner Puffer
    private long energyStored=0;
    private long lastGeneration=0;
    public int getLastGeneration(){ return (int)Math.min(Integer.MAX_VALUE, lastGeneration); }
    public long getLastGenerationLong(){ return lastGeneration; }
    private int tickCounter=0;
    private int meltdownTicks=0;
    private int idleCode=0; // 0=disabled,1=vacuum,2=fuel,3=coolant,4=steam,5=recipe,6=invalid
    public int getIdleCode(){ return idleCode; }
    public boolean isEnabled(){ return enabled; }
    public void setEnabled(boolean e){ enabled=e; setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    public double getBurnRate(){ return burnRate; }
    public void setBurnRate(double r){ if(!Double.isFinite(r)) return; burnRate=Math.max(getMinBurnRate(), r); setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    public double getMinBurnRate(){ return de.wutzuket.overdrive.config.ODConfig.MIN_BURN_RATE.get(); }
    public double getHeatingRate(){ return de.wutzuket.overdrive.config.ODConfig.HEATING_FACTOR.get()*Math.sqrt(burnRate); } // tatsächliche Heizung in K/t
    public double getActualBurnRate(){ return active? burnRate : 0; }
    // für GUI: letzte Struktur-Zählung
    private int lastCellPorts=0, lastVacuumPorts=0, lastCoolantPorts=0, lastSteamPorts=0, lastEnergyPorts=0, lastHeavy=0;
    public int getLastCellPorts(){ return lastCellPorts; }
    public int getLastVacuumPorts(){ return lastVacuumPorts; }
    public int getLastCoolantPorts(){ return lastCoolantPorts; }
    public int getLastSteamPorts(){ return lastSteamPorts; }
    public int getLastEnergyPorts(){ return lastEnergyPorts; }
    public int getLastHeavy(){ return lastHeavy; }

    // cached ports
    private @Nullable BlockPos vacuumPortPos=null;
    private @Nullable BlockPos coolantPortPos=null;
    private @Nullable BlockPos steamPortPos=null;
    private @Nullable BlockPos energyPortPos=null;
    private @Nullable BlockPos cellPortA=null;
    private @Nullable BlockPos cellPortB=null;

    private @Nullable AnnihilationRecipe currentRecipe=null;

    public AnnihilationControllerEntity(BlockPos pos, BlockState state){ super(ODBlockEntities.ANNIHILATION_CONTROLLER.get(), pos, state); }

    public void tick(Level level, BlockPos pos, BlockState state){
        if(level.isClientSide){
            if(validStructure && active && level.getGameTime()%5==0) spawnAmbientParticles(level,pos,state);
            return;
        }
        ServerLevel sl=(ServerLevel)level;
        boolean wasValid=validStructure;
        tickStartTemp=coreTemp;
        pendingCooling=0;
        meltedThisTick=false;
        validStructure=checkStructure(level,pos,state);
        if(validStructure && !wasValid) spawnValidationParticles(sl,pos,state);
        if(!validStructure){
            active=false; idleCode=6; lastGeneration=0;
            coreTemp=Math.max(20, coreTemp-5);
            setChanged(); level.sendBlockUpdated(pos,state,state,3);
            cachePorts(level,pos,state);
            return;
        }
        // find ports if null (auch 2. Cell)
        if(vacuumPortPos==null || coolantPortPos==null || steamPortPos==null || energyPortPos==null || cellPortA==null || cellPortB==null) cachePorts(level,pos,state);

        if(!enabled){
            active=false; idleCode=0; lastGeneration=0;
            coreTemp=Math.max(20, coreTemp-2);
            setChanged(); level.sendBlockUpdated(pos,state,state,3);
            return;
        }
        // vacuum check - 90% reicht zum Starten (99.9% wird durch Pipe-Mittelung selten exakt 100%)
        float vac=getVacuumLevel(level,pos,state);
        if(vac < 0.90f){
            if(active){
                triggerVacuumExplosion(sl,pos,state);
                return;
            } else {
                active=false; idleCode=1; lastGeneration=0;
                setChanged(); level.sendBlockUpdated(pos,state,state,3);
                return;
            }
        }
        if(currentRecipe==null) currentRecipe=findRecipe(level);
        if(currentRecipe==null){
            active=false; idleCode=5; lastGeneration=0;
            coreTemp=Math.max(20, coreTemp-2);
            setChanged(); level.sendBlockUpdated(pos,state,state,3);
            return;
        }
        CellDockingPortEntity portA = cellPortA!=null? (CellDockingPortEntity)level.getBlockEntity(cellPortA): null;
        CellDockingPortEntity portB = cellPortB!=null? (CellDockingPortEntity)level.getBlockEntity(cellPortB): null;
        // Fuel-Check skaliert mit burnRate und Recipe (Divisor per Config, Default: 10 mB * 100 / 20 = 50 mB/Tick je Seite bei Max-Burnrate)
        double fuelDivisor=de.wutzuket.overdrive.config.ODConfig.FUEL_DIVISOR.get();
        int needAnti = currentRecipe.inputAntiproton().isEmpty()?Math.max(1,(int)Math.min(Integer.MAX_VALUE, burnRate/2)):(int)Math.max(1, Math.min(Integer.MAX_VALUE, (long)(currentRecipe.inputAntiproton().amount()*burnRate/fuelDivisor)));
        int needPos = currentRecipe.inputPositron().isEmpty()?Math.max(1,(int)Math.min(Integer.MAX_VALUE, burnRate/2)):(int)Math.max(1, Math.min(Integer.MAX_VALUE, (long)(currentRecipe.inputPositron().amount()*burnRate/fuelDivisor)));
        if(needAnti<1) needAnti=1; if(needPos<1) needPos=1;
        boolean hasBoth = hasFuelForRecipe(portA, portB, needAnti, needPos);
        if(!hasBoth){
            active=false; idleCode=2; lastGeneration=0;
            setChanged(); level.sendBlockUpdated(pos,state,state,3); return;
        }
        // Ports für Kühlung / Energie abrufen (Kühlung läuft ausschließlich über reactor_cooling-Rezepte in handleCooling)
        CoolantInputPortEntity coolant = coolantPortPos!=null? (CoolantInputPortEntity)level.getBlockEntity(coolantPortPos): null;
        SteamOutputPortEntity steam = steamPortPos!=null? (SteamOutputPortEntity)level.getBlockEntity(steamPortPos): null;
        EnergyOutputPortEntity energy = energyPortPos!=null? (EnergyOutputPortEntity)level.getBlockEntity(energyPortPos): null;
        // Cooling separat handhaben (auch wenn nicht aktiv, aber ab 100°C)
        handleCooling(level, pos, state, coolant, steam);
        // Meltdown in handleCooling: Tick abbrechen (kein Reaktivieren/Aufheizen danach).
        if(meltedThisTick){ setChanged(); level.sendBlockUpdated(pos,state,state,3); return; }
        if(!active){ active=true; idleCode=0; setChanged(); level.sendBlockUpdated(pos,state,state,3); }
        tickCounter++;
        // Fuel verbrauchen pro Tick skaliert
        if(!consumeFuelForRecipe(portA, portB, needAnti, needPos)){
            active=false; idleCode=2; lastGeneration=0; setChanged(); level.sendBlockUpdated(pos,state,state,3); return;
        }
        // Heizung wächst wurzelförmig mit der Burnrate (Config heatingFactor * sqrt), die Kühlung (handleCooling)
        // hängt nur vom Temperaturüberschuss ab: Gleichgewicht ~2000 °C bei Burnrate 100 (Wasser),
        // darunter wurzelförmig fallend (z.B. ~1050 °C bei 25, ~290 °C bei 1).
        // Heizung und Kühlung netto in einem Schritt: Kühlung bremst höchstens (Heizung + Marge),
        // Anstieg höchstens +Marge pro Tick. So gibt es kein Sägezahn-Pendeln und kein Overshoot.
        double heat = de.wutzuket.overdrive.config.ODConfig.HEATING_FACTOR.get()*Math.sqrt(burnRate);
        double cool = Math.min(pendingCooling, heat + MAX_TEMP_CHANGE_PER_TICK);
        coreTemp = Math.max(20, Math.min(CORE_TEMP_MAX-1, Math.min(tickStartTemp + heat - cool, tickStartTemp + MAX_TEMP_CHANGE_PER_TICK)));
        long generated = (long)(currentRecipe.energyPerTick() * burnRate);
        lastGeneration = generated;
        // Energie immer in internen Speicher + zusätzlich in Output-Port zum Anzeigen und Ausgeben
        energyStored = Math.min(MAX_ENERGY_STORED, energyStored + generated);
        if(energy!=null) energy.addEnergy(generated);
        spawnAnnihilationParticles(sl,pos,state);
        // Recipe bleibt aktiv, kein Reset alle 100 Ticks nötig (Fuel wird pro Tick verbraucht)
        setChanged(); level.sendBlockUpdated(pos,state,state,2);
    }

    private boolean hasFuelForRecipe(CellDockingPortEntity a, CellDockingPortEntity b, int needAnti, int needPos){
        int totalAnti=0, totalPos=0;
        for(CellDockingPortEntity p: new CellDockingPortEntity[]{a,b}){
            if(p==null) continue;
            if(p.hasAntiprotonFluid()) totalAnti+=p.getTank().getAmount(0);
            if(p.hasPositronFluid()) totalPos+=p.getTank().getAmount(0);
        }
        return totalAnti>=needAnti && totalPos>=needPos;
    }

    private void handleCooling(Level level, BlockPos pos, BlockState state, CoolantInputPortEntity coolant, SteamOutputPortEntity steam){
        if(level.isClientSide) return;
        // Nur ab Boiling Temp kühlen, sonst passiv langsam abkühlen
        if(coreTemp <= 100){
            // unter 100°C nur sehr langsam passiv abkühlen, kein Coolant nötig
            if(coreTemp > 20) coreTemp = Math.max(20, coreTemp - 0.05);
            return;
        }
        // Finde passendes Cooling-Rezept für aktuelles Coolant
        ReactorCoolingRecipe recipe = findCoolingRecipe(level, coolant);
        if(recipe==null){
            // Kein passendes Rezept (leerer Tank oder falsches Fluid) -> keine aktive Kühlung,
            // nur minimale passive Abstrahlung. Ohne Kühlmittel heizt der Reaktor weiter.
            if(coreTemp > 100) coreTemp = Math.max(100, coreTemp - 0.05);
            // trotzdem prüfen ob Überhitzung droht bei hoher BurnRate
            if(coreTemp >= CORE_TEMP_MAX-100 && burnRate>1.0){
                // bei hoher BurnRate ohne gutes Coolant überhitzt es
                coreTemp += 2*Math.sqrt(burnRate);
                if(coreTemp>=CORE_TEMP_MAX) triggerMeltdown((ServerLevel)level, pos, state);
            }
            return;
        }
        // DUMP-Modus leert den Output-Tank vorab (Mekanism-like), damit nach einem Kuehlmittel-Wechsel
        // altes Restfluid (z.B. Steam) den Neustart mit neuem Fluid (z.B. Cryofluid) nicht blockiert.
        if(steam!=null && steam.getDumpMode()==DumpMode.DUMP && !steam.getTank().isEmpty()){
            steam.getTank().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
        }
        // Output-Fluid-Mismatch: im Steam-Tank liegt ein anderes Fluid (z.B. Steam aus Wasserkuehlung),
        // das Rezept wuerde aber ein anderes erzeugen (z.B. Superheated Cryofluid bei Cryofluid-Input).
        // Dann KEINE Kuehlung: kein Coolant verbrauchen, keine Temperatur senken.
        // Waerme staut sich wie bei vollem Tank (idleCode 4 = steam-blockiert).
        if(steam!=null && !steam.getTank().isEmpty()
                && !steam.getTank().getFluid().is(recipe.output().getFluid())){
            idleCode=4;
            if(active) coreTemp += 2*Math.sqrt(burnRate);
            if(coreTemp>=CORE_TEMP_MAX) triggerMeltdown((ServerLevel)level, pos, state);
            return;
        }
        // Berechne wie viel Kühlung nötig ist pro 100K über Boiling.
        // Absichtlich OHNE Burnrate-Faktor: zusammen mit der wurzelförmigen Heizung ergibt das
        // die wurzelförmige Temperaturkurve (mehr Burn => heißer, aber unterlinear).
        double over = coreTemp - recipe.boilingTemp();
        if(over <= 0) return;
        int needMb = (int)Math.ceil(over/100.0 * recipe.mbPer100K());
        if(needMb<1) needMb=1;
        // Verdampfungs-Limit: pro Tick verdampft nur eine maximale mB-Anzahl.
        // Liegt der Bedarf darüber, ist die Kühlung gesättigt -> Überhitzung wie ohne Kühlmittel
        // (Meltdown statt ewigem Parken am Cap). Leiter: Wasser ~340, Cryofluid ~140000 Burn.
        int maxMb = Math.max(1, recipe.maxMbPerTick());
        if(needMb > maxMb){
            if(active) coreTemp += 5*Math.sqrt(burnRate);
            if(coreTemp>=CORE_TEMP_MAX) triggerMeltdown((ServerLevel)level, pos, state);
            return;
        }
        // Teilkühlung: nimm was da ist (Coolant-Menge + Steam-Platz). Reicht es nicht für den
        // vollen Bedarf, kühlt es nur anteilig und die Temp steigt allmählich statt instant zu explodieren.
        // Meltdown gibt es weiter am Cap (z.B. 25k Burn braucht 38 mB Cryo + 38 Platz Steam pro Tick).
        int availCoolant = coolant == null ? 0 : coolant.getTank().getFluidAmount();
        int useMb = Math.min(needMb, availCoolant);
        // Prüfe Steam Platz (DUMP_EXCESS voidet den Überlauf, DUMP wurde weiter oben bereits geleert)
        DumpMode dump = steam != null ? steam.getDumpMode() : DumpMode.IDLE;
        if (steam != null && recipe.steamPerMb() > 0 && useMb > 0) {
            FluidStack probe = recipe.output().copy();
            probe.setAmount(useMb * recipe.steamPerMb());
            int room = steam.getTank().fill(probe, IFluidHandler.FluidAction.SIMULATE);
            if (dump != DumpMode.DUMP_EXCESS) {
                useMb = Math.min(useMb, room / recipe.steamPerMb());
            }
        }
        if (useMb < 1) {
            // gar keine Kühlung möglich (Tank leer oder Steam voll) -> Überhitzung wie bisher
            if (active) coreTemp += 5 * Math.sqrt(burnRate);
            if (coreTemp >= CORE_TEMP_MAX) triggerMeltdown((ServerLevel) level, pos, state);
            return;
        }
        // Verbrauche Coolant und erzeuge Steam, kühle ab
        coolant.getTank().drain(useMb, IFluidHandler.FluidAction.EXECUTE);
        if (steam != null) {
            FluidStack out = recipe.output().copy();
            out.setAmount(useMb * recipe.steamPerMb());
            steam.getTank().fill(out, IFluidHandler.FluidAction.EXECUTE);
        }
        // Kühlung wird am Tick-Ende netto mit der Heizung verrechnet (kein Sturz ins Sieden pro Tick).
        pendingCooling = useMb * recipe.coolingPerMb();
    }
    private ReactorCoolingRecipe findCoolingRecipe(Level level, CoolantInputPortEntity coolant){
        if(coolant==null || coolant.getTank().isEmpty()) return null;
        FluidStack fluid = coolant.getTank().getFluid();
        for(var holder : level.getRecipeManager().getRecipes()){
            if(holder.value() instanceof ReactorCoolingRecipe r && r.getType()==ODRecipeTypes.REACTOR_COOLING.get()){
                if(r.input().getFluid() == fluid.getFluid()) return r;
            }
        }
        return null;
    }

    private boolean consumeFuelForRecipe(CellDockingPortEntity a, CellDockingPortEntity b, int needAnti, int needPos){
        int leftAnti=needAnti, leftPos=needPos;
        for(CellDockingPortEntity p: new CellDockingPortEntity[]{a,b}){
            if(p==null) continue;
            if(leftAnti>0 && p.hasAntiprotonFluid()){
                int take=Math.min(leftAnti, p.getTank().getAmount(0));
                p.getTank().extractAntimatter(0, take, false);
                leftAnti-=take;
            }
            if(leftPos>0 && p.hasPositronFluid()){
                int take=Math.min(leftPos, p.getTank().getAmount(0));
                p.getTank().extractAntimatter(0, take, false);
                leftPos-=take;
            }
        }
        return leftAnti<=0 && leftPos<=0;
    }

    private Fluid getSteamFluid(){
        // Mekanism compatibility: try to resolve mekanism:steam via fluid registry lookup
        // Fallback to water for generic steam tag handling via JEI / recipes
        try {
            var rl = net.minecraft.resources.ResourceLocation.parse("mekanism:steam");
            var opt = net.minecraft.core.registries.BuiltInRegistries.FLUID.getOptional(rl);
            if(opt.isPresent() && opt.get()!=net.minecraft.world.level.material.Fluids.EMPTY) return opt.get();
        } catch(Exception ignored){}
        return net.minecraft.world.level.material.Fluids.WATER;
    }

    private @Nullable AnnihilationRecipe findRecipe(Level level){
        for(RecipeHolder<?> h: level.getRecipeManager().getRecipes()){
            if(h.value() instanceof AnnihilationRecipe r && r.getType()==ODRecipeTypes.ANNIHILATION.get()) return r;
        }
        return null;
    }

    private void cachePorts(Level level, BlockPos pos, BlockState state){
        BlockPos center=getCenter(pos, state);
        int cx=center.getX(), cy=center.getY(), cz=center.getZ();
        // vorherige Ports löschen, damit Entfernen/Hinzufügen korrekt erkannt wird
        vacuumPortPos=null; coolantPortPos=null; steamPortPos=null; energyPortPos=null; cellPortA=null; cellPortB=null;
        int foundCell=0;
        for(int x=cx-OUTER; x<=cx+OUTER; x++) for(int y=cy-OUTER; y<=cy+OUTER; y++) for(int z=cz-OUTER; z<=cz+OUTER; z++){
            BlockPos p=new BlockPos(x,y,z);
            BlockState bs=level.getBlockState(p);
            Block b=bs.getBlock();
            if(b instanceof CellDockingPortBlock){ if(foundCell==0){ cellPortA=p; foundCell=1; } else if(foundCell==1){ cellPortB=p; foundCell=2; } }
            else if(b instanceof AnnihilationVacuumPortBlock && vacuumPortPos==null) vacuumPortPos=p;
            else if(b instanceof CoolantInputPortBlock && coolantPortPos==null) coolantPortPos=p;
            else if(b instanceof SteamOutputPortBlock && steamPortPos==null) steamPortPos=p;
            else if(b instanceof EnergyOutputPortBlock && energyPortPos==null) energyPortPos=p;
        }
    }

    public static BlockPos getCenter(BlockPos controllerPos, BlockState state){
        Direction facing=state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int cx=controllerPos.getX()+facing.getStepX()*OUTER;
        int cy=controllerPos.getY(); // wie Particle Accelerator: Controller mitten in der Wand, zentriert
        int cz=controllerPos.getZ()+facing.getStepZ()*OUTER;
        return new BlockPos(cx,cy,cz);
    }
    private boolean checkStructure(Level level, BlockPos controllerPos, BlockState state){
        BlockPos center=getCenter(controllerPos, state);
        int cx=center.getX(), cy=center.getY(), cz=center.getZ();
        int minX=cx-OUTER, maxX=cx+OUTER, minY=cy-OUTER, maxY=cy+OUTER, minZ=cz-OUTER, maxZ=cz+OUTER;
        int heavyCasings=0, coreBlocks=0, glass=0, cellPorts=0, vacuumPorts=0, coolantPorts=0, steamPorts=0, energyPorts=0;
        int coreInterior=0; // belegte Innenzellen des 5x5x5-Cores (3x3x3 = 27), müssen echte Core-Blöcke sein
        boolean foundController=false;
        // quick controller check
        if(controllerPos.getX()>=minX && controllerPos.getX()<=maxX && controllerPos.getY()>=minY && controllerPos.getY()<=maxY && controllerPos.getZ()>=minZ && controllerPos.getZ()<=maxZ){
            // controller should be on outer wall at y=cy
            int maxDist=Math.max(Math.abs(controllerPos.getX()-cx), Math.abs(controllerPos.getZ()-cz));
            int dy=Math.abs(controllerPos.getY()-cy);
            if(maxDist==OUTER && dy<=OUTER) foundController=true;
        } else {
            // controller outside but facing inward - allow at distance OUTER (in der Wand)
            Direction facing2=state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            if(controllerPos.equals(new BlockPos(cx - facing2.getStepX()*OUTER, cy, cz - facing2.getStepZ()*OUTER))) foundController=true;
            // simplified: if facing correctly, accept controller at edge
            foundController=true;
        }
        // iterate shell
        for(int x=minX; x<=maxX; x++) for(int y=minY; y<=maxY; y++) for(int z=minZ; z<=maxZ; z++){
            BlockPos p=new BlockPos(x,y,z);
            BlockState bs=level.getBlockState(p);
            Block b=bs.getBlock();
            boolean isOuter = x==minX || x==maxX || y==minY || y==maxY || z==minZ || z==maxZ;
            boolean isInnerHole = Math.max(Math.abs(x-cx), Math.max(Math.abs(y-cy), Math.abs(z-cz))) < INNER && Math.max(Math.abs(x-cx), Math.abs(z-cz))>=INNER-2;
            // define donut: outer cube minus inner cube (torus approximated as cubic donut) minus core free
            boolean inDonutWall = false;
            int dx=Math.abs(x-cx), dy=Math.abs(y-cy), dz=Math.abs(z-cz);
            int maxHDxDz=Math.max(dx,dz);
            boolean inOuterCube = dx<=OUTER && dy<=OUTER && dz<=OUTER;
            boolean inInnerCube = dx < INNER && dy < INNER && dz < INNER; // hole quick reject for core area
            boolean inCore = dx<=CORE_RADIUS && dy<=CORE_RADIUS && dz<=CORE_RADIUS && maxHDxDz<=CORE_RADIUS;
            if(!inCore && dx<=OUTER && dz<=OUTER && dy<=OUTER){
                if(isTorusWall(dx, dy, dz)) inDonutWall=true;
            }
            if(inCore){
                if(b instanceof ReactorCoreBlock){
                    coreBlocks++;
                    // Innenzellen (nicht auf der Core-Hülle) zählen separat: ohne gefüllten
                    // Innen-Core darf der Reaktor nicht laufen (kein Coreless-Betrieb)
                    if(dx<CORE_RADIUS && dy<CORE_RADIUS && dz<CORE_RADIUS) coreInterior++;
                }
                else if(b instanceof CoreContainmentGlassBlock) glass++;
                else if(!(b instanceof ReactorCoreBlock) && !(b instanceof CoreContainmentGlassBlock) && !bs.isAir()){
                    // allow core interior to be core blocks or glass
                    if(inCore && (dx==CORE_RADIUS || dy==CORE_RADIUS || dz==CORE_RADIUS)){
                        // expected glass on shell
                        if(!(b instanceof CoreContainmentGlassBlock)) return false;
                    } else {
                        if(!(b instanceof ReactorCoreBlock)) return false;
                    }
                }
                continue;
            }
            if(inDonutWall){
                if(p.equals(controllerPos)){
                    if(!(b instanceof AnnihilationControllerBlock)) return false;
                    foundController=true;
                    continue;
                }
                if(b instanceof AnnihilationHeavyCasingBlock) heavyCasings++;
                else if(b instanceof CellDockingPortBlock) cellPorts++;
                else if(b instanceof AnnihilationVacuumPortBlock) vacuumPorts++;
                else if(b instanceof CoolantInputPortBlock) coolantPorts++;
                else if(b instanceof SteamOutputPortBlock) steamPorts++;
                else if(b instanceof EnergyOutputPortBlock) energyPorts++;
                else return false;
            } else if(!inCore && isTorusHollow(dx,dy,dz)){
                // nur Inneres des Torus-Schlauchs muss Luft sein (Hohlraum), äußeres Volumen egal
                if(!bs.isAir()) return false;
            }
        }
        // für GUI merken
        lastCellPorts=cellPorts; lastVacuumPorts=vacuumPorts; lastCoolantPorts=coolantPorts; lastSteamPorts=steamPorts; lastEnergyPorts=energyPorts; lastHeavy=heavyCasings;
        // we require at least shell counts; allow missing some due to simplified check - just require ports >=1 and controller.
        // Zwingend: alle 27 Innenzellen des Cores müssen ReactorCoreBlock sein (kein Coreless-Betrieb,
        // kein Neustart nach Meltdown ohne Neuaufbau, da Meltdown den Core durch Corium ersetzt).
        return foundController && cellPorts>=2 && vacuumPorts>=1 && coolantPorts>=1 && steamPorts>=1 && energyPorts>=1
                && coreInterior>=27;
    }

    private float getVacuumLevel(Level level, BlockPos pos, BlockState state){
        if(vacuumPortPos==null) return 0;
        BlockEntity be=level.getBlockEntity(vacuumPortPos);
        if(be instanceof IVacuumNode n) return n.getVacuumLevel();
        // fallback scan
        BlockPos center=getCenter(pos, state);
        int cx=center.getX(), cy=center.getY(), cz=center.getZ();
        float best=0;
        for(int x=cx-OUTER; x<=cx+OUTER; x++) for(int y=cy-OUTER; y<=cy+OUTER; y++) for(int z=cz-OUTER; z<=cz+OUTER; z++){
            BlockEntity be2=level.getBlockEntity(new BlockPos(x,y,z));
            if(be2 instanceof IVacuumNode node) best=Math.max(best, node.getVacuumLevel());
        }
        return best;
    }

    private void triggerVacuumExplosion(ServerLevel level, BlockPos pos, BlockState state){
        var mode = de.wutzuket.overdrive.config.ODConfig.REACTOR_FAILURE_MODE.get();
        if(mode == de.wutzuket.overdrive.config.ODConfig.ReactorFailureMode.NONE){
            active=false; validStructure=false; coreTemp=20; setChanged(); level.sendBlockUpdated(pos,state,state,3);
            return;
        }
        BlockPos center=getCenter(pos, state);
        int cx=center.getX(), cy=center.getY(), cz=center.getZ();

        if(mode == de.wutzuket.overdrive.config.ODConfig.ReactorFailureMode.EXPLOSION){
            level.explode(null, cx+0.5, cy+0.5, cz+0.5, 50f, Level.ExplosionInteraction.TNT);
            int r=12;
            for(int x=cx-r; x<=cx+r; x++) for(int y=cy-r; y<=cy+r; y++) for(int z=cz-r; z<=cz+r; z++){
                double d=Math.sqrt((x-cx)*(x-cx)+(y-cy)*(y-cy)+(z-cz)*(z-cz));
                if(d<r) level.setBlock(new BlockPos(x,y,z), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            }
        } else {
            // MELTDOWN
            triggerMeltdown(level, pos, state);
            if(de.wutzuket.overdrive.config.ODConfig.DESTROY_STRUCTURE_ON_MELTDOWN.get()){
                // Also destroy torus structure (keep bottom layer)
                for(int x=cx-OUTER; x<=cx+OUTER; x++) for(int z=cz-OUTER; z<=cz+OUTER; z++){
                    int dx=Math.abs(x-cx), dz=Math.abs(z-cz);
                    if(isTorusWall(dx, 0, dz)){
                        for(int dy=-1; dy<=1; dy++){
                            double chance = dy == -1 ? 0.15 : (dy == 0 ? 0.5 : 0.85);
                            if(level.random.nextDouble() < chance){
                                level.destroyBlock(new BlockPos(x, cy+dy, z), true);
                            }
                        }
                    }
                }
            }
        }
        active=false; validStructure=false; coreTemp=20; setChanged(); level.sendBlockUpdated(pos,state,state,3);
    }
    private void triggerMeltdown(ServerLevel level, BlockPos pos, BlockState state){
        BlockPos center=getCenter(pos, state);        int cx=center.getX(), cy=center.getY(), cz=center.getZ();
        // replace core with corium
        for(int x=cx-CORE_RADIUS; x<=cx+CORE_RADIUS; x++) for(int y=cy-CORE_RADIUS; y<=cy+CORE_RADIUS; y++) for(int z=cz-CORE_RADIUS; z<=cz+CORE_RADIUS; z++){
            level.setBlock(new BlockPos(x,y,z), ODBlocks.CORIUM_BLOCK.get().defaultBlockState(), 3);
        }
        // create pit
        level.explode(null, cx+0.5, cy+0.5, cz+0.5, 20f, Level.ExplosionInteraction.TNT);
        active=false; validStructure=false; coreTemp=20; meltdownTicks=0; enabled=false; meltedThisTick=true; setChanged(); level.sendBlockUpdated(pos,state,state,3);
    }

    private void spawnValidationParticles(ServerLevel level, BlockPos pos, BlockState state){
        BlockPos center=getCenter(pos, state);
        int cx=center.getX(), cy=center.getY(), cz=center.getZ();
        DustParticleOptions opt=new DustParticleOptions(new Vector3f(1f, 0f, 0f), 2.5f);
        // Ganze Torus-Wand abgehen (jeden 2. Block samplen), damit die komplette Struktur aufblitzt.
        for(int x=cx-OUTER; x<=cx+OUTER; x+=2) for(int y=cy-OUTER; y<=cy+OUTER; y+=2) for(int z=cz-OUTER; z<=cz+OUTER; z+=2){
            int dx=Math.abs(x-cx), dy=Math.abs(y-cy), dz=Math.abs(z-cz);
            if(isTorusWall(dx,dy,dz)){
                level.sendParticles(opt, x+0.5, y+0.5, z+0.5, 3, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }
    private void spawnAmbientParticles(Level level, BlockPos pos, BlockState state){
        if(!(level instanceof ServerLevel sl)) return;
        BlockPos center=getCenter(pos, state);
        int cx=center.getX(), cy=center.getY(), cz=center.getZ();
        DustParticleOptions opt=new DustParticleOptions(new Vector3f(0.6f,0.4f,1f),2f);
        for(int i=0;i<8;i++){
            double ang=2*Math.PI*level.random.nextDouble();
            double rx=(OUTER-2)*Math.cos(ang), rz=(OUTER-2)*Math.sin(ang);
            sl.sendParticles(opt, cx+rx+0.5+level.random.nextGaussian()*0.5, cy+level.random.nextInt(OUTER*2)-OUTER+0.5, cz+rz+0.5, 2,0,0,0,0.1);
        }
        // core glow
        DustParticleOptions coreOpt=new DustParticleOptions(new Vector3f(0.3f,0.6f,1f),2f);
        sl.sendParticles(coreOpt, cx+0.5, cy+0.5, cz+0.5, 10, 1,1,1,0.2);
    }
    private void spawnAnnihilationParticles(ServerLevel level, BlockPos pos, BlockState state){
        DustParticleOptions opt=new DustParticleOptions(new Vector3f(1f,0.2f,1f),2f);
        BlockPos center=getCenter(pos, state);
        int cx=center.getX(), cy=center.getY(), cz=center.getZ();
        level.sendParticles(opt, cx+0.5, cy+0.5, cz+0.5, 30, 3,3,3,0.3);
    }

    public boolean isValidStructure(){ return validStructure; }
    public boolean isActive(){ return active; }
    public double getCoreTemp(){ return coreTemp; }
    // int-Sicht (GUI/Probe, gecappt) + long-Sicht (Logik)
    public int getEnergyStored(){ return (int)Math.min(Integer.MAX_VALUE, energyStored); }
    public long getEnergyStoredLong(){ return energyStored; }
    public float getVacuumPercent(){
        if(level==null) return 0;
        return getVacuumLevel(level, worldPosition, getBlockState())*100f;
    }
    public net.neoforged.neoforge.fluids.FluidStack getCoolantStack(){
        if(coolantPortPos!=null && level!=null){
            BlockEntity be=level.getBlockEntity(coolantPortPos);
            if(be instanceof CoolantInputPortEntity p) return p.getTank().getFluid();
        }
        return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
    }
    public int getCoolantCapacity(){
        if(coolantPortPos!=null && level!=null){
            BlockEntity be=level.getBlockEntity(coolantPortPos);
            if(be instanceof CoolantInputPortEntity p) return p.getTank().getCapacity();
        }
        return 300_000_000;
    }
    public net.neoforged.neoforge.fluids.FluidStack getSteamStack(){
        if(steamPortPos!=null && level!=null){
            BlockEntity be=level.getBlockEntity(steamPortPos);
            if(be instanceof SteamOutputPortEntity p) return p.getTank().getFluid();
        }
        return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
    }
    public int getSteamCapacity(){
        if(steamPortPos!=null && level!=null){
            BlockEntity be=level.getBlockEntity(steamPortPos);
            if(be instanceof SteamOutputPortEntity p) return p.getTank().getCapacity();
        }
        return 300_000_000;
    }
    /** @return der Antimatter, der als Antiprotonen in den Cell-Ports liegt. */
    public de.wutzuket.overdrive.antimatter.AntimatterStack getFuelAntiprotonStack(){
        int total=0; de.wutzuket.overdrive.antimatter.Antimatter type=null;
        for(BlockPos p: new BlockPos[]{cellPortA, cellPortB}){
            if(p!=null && level!=null){
                BlockEntity be=level.getBlockEntity(p);
                if(be instanceof CellDockingPortEntity port && port.hasAntiprotonFluid()){
                    total+=port.getTank().getAmount(0);
                    if(type==null) type=port.getTank().getAntimatterInTank(0);
                }
            }
        }
        return new de.wutzuket.overdrive.antimatter.AntimatterStack(type, total);
    }
    /** @return der Antimatter, der als Positronen in den Cell-Ports liegt. */
    public de.wutzuket.overdrive.antimatter.AntimatterStack getFuelPositronStack(){
        int total=0; de.wutzuket.overdrive.antimatter.Antimatter type=null;
        for(BlockPos p: new BlockPos[]{cellPortA, cellPortB}){
            if(p!=null && level!=null){
                BlockEntity be=level.getBlockEntity(p);
                if(be instanceof CellDockingPortEntity port && port.hasPositronFluid()){
                    total+=port.getTank().getAmount(0);
                    if(type==null) type=port.getTank().getAntimatterInTank(0);
                }
            }
        }
        return new de.wutzuket.overdrive.antimatter.AntimatterStack(type, total);
    }
    public int getFuelCapacity(){ return 4_000_000; } // Summe beider CellDockingPorts (nur Fallback)
    // Kapazitaet pro Sorte: Summe der Ports, die leer sind oder bereits dieses Fluid halten.
    // Standard-Setup (Anti links, Posi rechts, je 2M): je 2 KB. Beide Ports leer: je 4 KB.
    public int getFuelAntiprotonCapacity(){
        int cap=0;
        for(BlockPos p: new BlockPos[]{cellPortA, cellPortB}){
            if(p!=null && level!=null){
                BlockEntity be=level.getBlockEntity(p);
                if(be instanceof CellDockingPortEntity port
                        && (port.getTank().getAmount(0) <= 0 || port.hasAntiprotonFluid())) cap+=port.getTank().getCapacity(0);
            }
        }
        return Math.max(1, cap);
    }
    public int getFuelPositronCapacity(){
        int cap=0;
        for(BlockPos p: new BlockPos[]{cellPortA, cellPortB}){
            if(p!=null && level!=null){
                BlockEntity be=level.getBlockEntity(p);
                if(be instanceof CellDockingPortEntity port
                        && (port.getTank().getAmount(0) <= 0 || port.hasPositronFluid())) cap+=port.getTank().getCapacity(0);
            }
        }
        return Math.max(1, cap);
    }
    public boolean isHologramEnabled(){
        if(level!=null && level.isClientSide && Overdrive.isClientHologramEnabled(worldPosition)) return true;
        return hologramEnabled;
    }
    public void toggleHologram(){ hologramEnabled=!hologramEnabled; setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }

    @Override public Component getDisplayName(){ return Component.translatable("container.overdrive.annihilation_controller"); }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player){ return new de.wutzuket.overdrive.menu.AnnihilationControllerMenu(id, inv, this); }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r){
        super.saveAdditional(tag,r);
        tag.putBoolean("valid",validStructure); tag.putBoolean("active",active); tag.putBoolean("holo",hologramEnabled); tag.putBoolean("enabled",enabled);
        tag.putDouble("burnRate",burnRate); tag.putInt("idleCode",idleCode); tag.putDouble("temp",coreTemp); tag.putLong("energy",energyStored); tag.putInt("ticks",tickCounter); tag.putLong("lastGen",lastGeneration);
        tag.putInt("lastCell",lastCellPorts); tag.putInt("lastVac",lastVacuumPorts); tag.putInt("lastCool",lastCoolantPorts); tag.putInt("lastSteam",lastSteamPorts); tag.putInt("lastEnergy",lastEnergyPorts); tag.putInt("lastHeavy",lastHeavy);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r){
        super.loadAdditional(tag,r);
        validStructure=tag.getBoolean("valid"); active=tag.getBoolean("active"); hologramEnabled=tag.getBoolean("holo"); enabled=tag.getBoolean("enabled");
        burnRate=tag.contains("burnRate")?tag.getDouble("burnRate"):0.1; idleCode=tag.getInt("idleCode"); coreTemp=tag.getDouble("temp"); energyStored=tag.contains("energy")?tag.getLong("energy"):0L; tickCounter=tag.getInt("ticks"); lastGeneration=tag.contains("lastGen")?tag.getLong("lastGen"):0L;
        lastCellPorts=tag.getInt("lastCell"); lastVacuumPorts=tag.getInt("lastVac"); lastCoolantPorts=tag.getInt("lastCool"); lastSteamPorts=tag.getInt("lastSteam"); lastEnergyPorts=tag.getInt("lastEnergy"); lastHeavy=tag.getInt("lastHeavy");
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){ return saveWithoutMetadata(r); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){ return ClientboundBlockEntityDataPacket.create(this); }
}
