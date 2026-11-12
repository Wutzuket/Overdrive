package de.wutzuket.overdrive.integration.guideme;

import guideme.Guide;
import guideme.GuideItemSettings;
import guideme.Guides;
import guideme.scene.element.SceneElementTagCompiler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Registriert das Handbuch von Overdrive bei GuideME.
 *
 * <p>This class references the GuideME API and may therefore only be loaded
 * when GuideME is installed. Use {@link GuideMeCompat#isLoaded()} as guard.
 *
 * <p>Das Handbuch wird - wie bei Modern Industrialization - im Code
 * registriert, damit eigene Tags wie {@code <PortSlots />} moeglich sind.
 * Ein datengesteuertes Guide (ueber {@code guideme_guides/guide.json}) kann
 * keine eigenen Tags erweitern und wuerde das im Code registrierte Guide
 * ohnehin ueberschreiben.
 */
public final class GuideMeGuide {

    /** Id of the Overdrive handbook. The pages live in assets/overdrive/guides/overdrive/guide. */
    public static final ResourceLocation GUIDE_ID = ResourceLocation.fromNamespaceAndPath("overdrive", "guide");

    private static boolean registered;

    private GuideMeGuide() {
    }

    /**
     * Registriert das Handbuch. Muss auf der Client-Seite aufgerufen werden.
     */
    public static void registerGuide() {
        if (registered) {
            return;
        }
        registered = true;

        Guide.builder(GUIDE_ID)
                .defaultLanguage("en_us")
                .itemSettings(new GuideItemSettings(
                        Optional.of(Component.translatable("item.overdrive.guide")),
                        List.of(
                                Component.translatable("tooltip.overdrive.guide.hint"),
                                Component.translatable("tooltip.overdrive.guide.hotkey")
                                        .withStyle(ChatFormatting.DARK_GRAY)),
                        Optional.of(ResourceLocation.fromNamespaceAndPath("overdrive", "item/guide"))))
                .extension(SceneElementTagCompiler.EXTENSION_POINT, new PortSlotsElementCompiler())
                .build();
    }

    /**
     * @return a guide item that opens the Overdrive handbook when it is used
     */
    public static ItemStack createGuideItem() {
        return Guides.createGuideItem(GUIDE_ID);
    }
}
