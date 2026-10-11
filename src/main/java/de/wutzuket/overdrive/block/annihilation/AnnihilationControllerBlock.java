package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.index.ODBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnnihilationControllerBlock extends Block implements EntityBlock {
    public AnnihilationControllerBlock(Properties properties){
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b){ b.add(BlockStateProperties.HORIZONTAL_FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){ return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, c.getHorizontalDirection().getOpposite());}
    @Override protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if(!level.isClientSide){
            BlockEntity be=level.getBlockEntity(pos);
            if(be instanceof AnnihilationControllerEntity ctrl){ player.openMenu(ctrl, pos); return InteractionResult.SUCCESS; }
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Nur Creative darf auto-bauen
        if(!player.isCreative()){
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if(level.isClientSide) return ItemInteractionResult.SUCCESS;
        if(!(stack.getItem() instanceof BlockItem bi)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        Block held = bi.getBlock();
        Block target = null;
        int need = 0;
        boolean isCore = false, isGlass = false;
        if(held == ODBlocks.ANNIHILATION_HEAVY_CASING.get()) target = held;
        else if(held == ODBlocks.REACTOR_CORE_BLOCK.get()){ isCore = true; target = held; }
        else if(held == ODBlocks.CORE_CONTAINMENT_GLASS.get()){ isGlass = true; target = held; }
        else if(held == ODBlocks.CELL_DOCKING_PORT.get()){ target = held; need = 2; }
        else if(held == ODBlocks.ANNIHILATION_VACUUM_PORT.get()){ target = held; need = 1; }
        else if(held == ODBlocks.COOLANT_INPUT_PORT.get()){ target = held; need = 1; }
        else if(held == ODBlocks.STEAM_OUTPUT_PORT.get()){ target = held; need = 1; }
        else if(held == ODBlocks.ANNIHILATION_ENERGY_OUTPUT.get()){ target = held; need = 1; }
        else return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        BlockPos center = AnnihilationControllerEntity.getCenter(pos, state);
        int cx=center.getX(), cy=center.getY(), cz=center.getZ();
        int OUTER=AnnihilationControllerEntity.OUTER;
        int placed=0;
        // für Ports: zähle bereits vorhandene
        int existingPorts = 0;
        if(need>0){
            // Zähle Ports des gleichen Typs
            for(int x=cx-OUTER;x<=cx+OUTER;x++) for(int y=cy-OUTER;y<=cy+OUTER;y++) for(int z=cz-OUTER;z<=cz+OUTER;z++){
                int dx=Math.abs(x-cx), dy=Math.abs(y-cy), dz=Math.abs(z-cz);
                if(!AnnihilationControllerEntity.isTorusWall(dx,dy,dz)) continue;
                Block b=level.getBlockState(new BlockPos(x,y,z)).getBlock();
                if(b==target) existingPorts++;
                if(pos.equals(new BlockPos(x,y,z)) && held==ODBlocks.ANNIHILATION_HEAVY_CASING.get()){
                    // Controller zählt nicht als Heavy
                }
            }
            int missing = need - existingPorts;
            if(missing<=0){
                player.displayClientMessage(Component.literal("Alle "+target.getName().getString()+" bereits vorhanden"), true);
                return ItemInteractionResult.SUCCESS;
            }
            // Fülle fehlende Ports an ersten freien Torus-Wand Positionen
            outer: for(int y=cy-OUTER;y<=cy+OUTER;y++) for(int x=cx-OUTER;x<=cx+OUTER;x++) for(int z=cz-OUTER;z<=cz+OUTER;z++){
                int dx=Math.abs(x-cx), dy=Math.abs(y-cy), dz=Math.abs(z-cz);
                if(!AnnihilationControllerEntity.isTorusWall(dx,dy,dz)) continue;
                BlockPos p=new BlockPos(x,y,z);
                if(p.equals(pos)) continue;
                if(!level.getBlockState(p).isAir()) continue;
                level.setBlock(p, target.defaultBlockState(), 3);
                placed++;
                if(placed>=missing) break outer;
            }
        } else if(isCore){
            for(int x=cx-2;x<=cx+2;x++) for(int y=cy-2;y<=cy+2;y++) for(int z=cz-2;z<=cz+2;z++){
                int dx=Math.abs(x-cx), dy=Math.abs(y-cy), dz=Math.abs(z-cz);
                boolean isShell = dx==2||dy==2||dz==2;
                if(isShell) continue; // Glas schale, nicht Core
                BlockPos p=new BlockPos(x,y,z);
                if(!level.getBlockState(p).isAir()) continue;
                level.setBlock(p, target.defaultBlockState(), 3);
                placed++;
            }
        } else if(isGlass){
            for(int x=cx-2;x<=cx+2;x++) for(int y=cy-2;y<=cy+2;y++) for(int z=cz-2;z<=cz+2;z++){
                int dx=Math.abs(x-cx), dy=Math.abs(y-cy), dz=Math.abs(z-cz);
                boolean isShell = dx==2||dy==2||dz==2;
                if(!isShell) continue;
                BlockPos p=new BlockPos(x,y,z);
                if(!level.getBlockState(p).isAir()) continue;
                level.setBlock(p, target.defaultBlockState(), 3);
                placed++;
            }
        } else {
            // Heavy Casing: alle Torus-Wand Positionen füllen wo Luft ist und wo kein Port benötigt wird
            for(int y=cy-OUTER;y<=cy+OUTER;y++) for(int x=cx-OUTER;x<=cx+OUTER;x++) for(int z=cz-OUTER;z<=cz+OUTER;z++){
                int dx=Math.abs(x-cx), dy=Math.abs(y-cy), dz=Math.abs(z-cz);
                if(!AnnihilationControllerEntity.isTorusWall(dx,dy,dz)) continue;
                BlockPos p=new BlockPos(x,y,z);
                if(p.equals(pos)) continue;
                if(!level.getBlockState(p).isAir()) continue;
                level.setBlock(p, target.defaultBlockState(), 3);
                placed++;
            }
        }
        if(placed>0){
            player.displayClientMessage(Component.literal("Auto-Build: "+placed+"x "+target.getName().getString()+" platziert (Creative)"), true);
        } else {
            player.displayClientMessage(Component.literal("Nichts zu platzieren für "+target.getName().getString()), true);
        }
        return ItemInteractionResult.SUCCESS;
    }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos p, BlockState s){ return new AnnihilationControllerEntity(p,s);}
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type){
        if(type!= ODBlockEntities.ANNIHILATION_CONTROLLER.get()) return null;
        return (l,pos,st,be)->{ if(be instanceof AnnihilationControllerEntity c) c.tick(l,pos,st); };
    }
}
