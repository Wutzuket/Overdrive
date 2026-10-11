package de.wutzuket.overdrive.block.ad;

import de.wutzuket.overdrive.index.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/** Reiner Weiterleitungs-Port: kein eigener Speicher, keine GUI – Pipes landen im Input-Tank des Controllers. */
public class ADInputPortEntity extends BlockEntity {
    private ADControllerEntity cachedController = null;

    public ADInputPortEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.AD_INPUT_PORT.get(), pos, state);
    }

    private @Nullable ADControllerEntity getController() {
        if (level == null) return null;
        if (cachedController == null || cachedController.isRemoved()) {
            cachedController = ADControllerEntity.findNear(level, worldPosition);
        }
        return cachedController;
    }

    public @Nullable FluidTank getTank() {
        ADControllerEntity ctrl = getController();
        return ctrl == null ? null : ctrl.getInputTank();
    }
}
