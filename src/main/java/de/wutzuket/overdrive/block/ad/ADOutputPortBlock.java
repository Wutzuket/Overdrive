package de.wutzuket.overdrive.block.ad;

import de.wutzuket.overdrive.antimatter.IAntimatterPort;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Strukturblock ohne GUI: die BE leitet Pipes nur an den Controller-Tank weiter. */
public class ADOutputPortBlock extends Block implements EntityBlock, IAntimatterPort {
    public ADOutputPortBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ADOutputPortEntity(blockPos, blockState);
    }
}
