package de.wutzuket.overdrive.block.scanner.recipe;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record scanner_match(@NotNull ResourceLocation id, @NotNull scanner_recipe recipe,
                             @NotNull Direction facing, boolean mirrored) {
}
