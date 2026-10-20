package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.antimatter.AntimatterStack;
import mezz.jei.api.ingredients.IIngredientType;

import java.util.Optional;

/**
 * JEI ingredient type for the antimatter. It makes the antimatter show up in the recipes like a
 * fluid does: with a gauge, the amount and a tooltip.
 */
public class AntimatterIngredientType implements IIngredientType<AntimatterStack> {

    public static final AntimatterIngredientType INSTANCE = new AntimatterIngredientType();

    @Override
    public Class<? extends AntimatterStack> getIngredientClass() {
        return AntimatterStack.class;
    }

    @Override
    public String getUid() {
        return "overdrive_antimatter";
    }

    @Override
    public Optional<AntimatterStack> castIngredient(Object ingredient) {
        return ingredient instanceof AntimatterStack stack ? Optional.of(stack) : Optional.empty();
    }
}
