package de.wutzuket.overdrive.block.annihilation;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EnergyOutputPortBlock extends Block implements EntityBlock {
    public EnergyOutputPortBlock(Properties p){ super(p); }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state){ return new EnergyOutputPortEntity(pos,state); }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type){
        if(type!=ODBlockEntities.ANNIHILATION_ENERGY_OUTPUT.get()) return null;
        return (l,po,st,be)-> { if(be instanceof EnergyOutputPortEntity e) e.tick(l,po,st); };
    }
}
