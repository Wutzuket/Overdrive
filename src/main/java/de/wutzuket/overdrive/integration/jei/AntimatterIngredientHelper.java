package de.wutzuket.overdrive.integration.jei;

import de.wutzuket.overdrive.antimatter.AntimatterStack;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;

/**
 * Supplies the metadata of the antimatter ingredient for JEI: the name, the unique id and the
 * error info. The rendering and the tooltip are done by the
 * {@link AntimatterIngredientRenderer}.
 */
public class AntimatterIngredientHelper implements IIngredientHelper<AntimatterStack> {

    @Override
    public AntimatterIngredientType getIngredientType() {
        return AntimatterIngredientType.INSTANCE;
    }

    @Override
    public String getDisplayName(AntimatterStack ingredient) {
        if (ingredient == null || ingredient.type() == null) {
            return "";
        }
        return ingredient.type().displayName().getString();
    }

    @Override
    public String getUniqueId(AntimatterStack ingredient, UidContext context) {
        if (ingredient == null || ingredient.type() == null) {
            return "empty";
        }
        return ingredient.type().id().toString();
    }

    @Override
    public ResourceLocation getResourceLocation(AntimatterStack ingredient) {
        if (ingredient == null || ingredient.type() == null) {
            return ResourceLocation.fromNamespaceAndPath("overdrive", "antimatter/empty");
        }
        return ResourceLocation.fromNamespaceAndPath("overdrive", "antimatter/" + ingredient.type().id().getPath());
    }

    @Override
    public AntimatterStack copyIngredient(AntimatterStack ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(AntimatterStack ingredient) {
        if (ingredient == null || ingredient.type() == null) {
            return "Invalid antimatter";
        }
        return "";
    }
}
