package de.wutzuket.overdrive.network;

import de.wutzuket.overdrive.menu.HologramMenu;
import de.wutzuket.overdrive.menu.HologramScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleHologramMenuState(final HologramMenuStatePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof HologramScreen screen) {
                screen.applyServerState(payload);
            } else if (minecraft.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> genericScreen
                    && genericScreen.getMenu() instanceof HologramMenu hologramMenu) {
                hologramMenu.applyClientSync(payload.availableFiles(), payload.selectedFile(),
                        payload.offsetX(), payload.offsetY(), payload.offsetZ(),
                        payload.scale(), payload.rotationX(), payload.rotationY(), payload.rotationZ(),
                        payload.spin());
            }
        });
    }
}
