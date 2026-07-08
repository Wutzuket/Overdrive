package de.wutzuket.overdrive.network;

import de.wutzuket.overdrive.block.accelerator.ParticleAcceleratorControllerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {
    public static void handleToggleHologram(final ToggleHologramPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                Level level = serverPlayer.level();
                BlockPos pos = payload.pos();
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ParticleAcceleratorControllerEntity ctrl) {
                    ctrl.toggleHologram();
                }
            }
        });
    }
}
