package de.wutzuket.overdrive.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Implemented by every machine that plays a continuous sound while it is
 * running.
 *
 * The sound is started and stopped by the client, so the machine only has to
 * report whether it is currently doing work. The state has to be part of the
 * block entity data, because the client reads it from its own copy of the
 * block entity.
 */
public interface IMachineSound {

    /** The sound that is played while the machine is running. */
    SoundEvent getMachineSound();

    /** True while the machine is doing work. */
    boolean isMachineRunning();

    /**
     * The positions at which the sound of the machine is played.
     *
     * A multiblock spreads its sound over the whole structure, so that the
     * sound comes from the machine and not only from its controller.
     *
     * @param controllerPos the position of the controller
     * @param state the block state of the controller, it holds the facing
     */
    default List<BlockPos> getSoundPositions(BlockPos controllerPos, BlockState state) {
        return List.of(controllerPos);
    }
}
