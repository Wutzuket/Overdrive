package de.wutzuket.overdrive.block.scanner.recipe;

import net.minecraft.core.Direction;

public record scanner_match(scanner_recipe recipe, Direction facing, boolean mirrored) {
}
