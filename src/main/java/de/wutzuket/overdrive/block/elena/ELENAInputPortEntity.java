package de.wutzuket.overdrive.block.elena;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/** Reiner Weiterleitungs-Port: kein eigener Speicher, keine GUI – Pipes landen im Input-Tank des Controllers. */
public class ELENAInputPortEntity extends BlockEntity {
    private ELENAControllerEntity cachedController = null;

    public ELENAInputPortEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.ELENA_INPUT_PORT.get(), pos, state);
    }

    private @Nullable ELENAControllerEntity getController() {
        if (level == null) return null;
        if (cachedController == null || cachedController.isRemoved()) {
            cachedController = ELENAControllerEntity.findNear(level, worldPosition);
        }
        return cachedController;
    }

    public @Nullable FluidTank getTank() {
        ELENAControllerEntity ctrl = getController();
        return ctrl == null ? null : ctrl.getInputTank();
    }
}
