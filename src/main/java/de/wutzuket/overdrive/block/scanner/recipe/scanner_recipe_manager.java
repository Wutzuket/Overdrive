package de.wutzuket.overdrive.block.scanner.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

public class scanner_recipe_manager extends SimpleJsonResourceReloadListener {
    private static final Direction[] HORIZONTAL_FACINGS = new Direction[] {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
    };

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FOLDER = "recipe";

    public static final scanner_recipe_manager INSTANCE = new scanner_recipe_manager();

    private Map<ResourceLocation, scanner_recipe> recipes = Map.of();

    private scanner_recipe_manager() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, scanner_recipe> loadedRecipes = new LinkedHashMap<>();
        List<Map.Entry<ResourceLocation, JsonElement>> entries = new ArrayList<>(resourceList.entrySet());
        entries.sort(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)));

        for (Map.Entry<ResourceLocation, JsonElement> entry : entries) {
            ResourceLocation id = entry.getKey();
            try {
                JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), "scanner recipe");
                scanner_recipe recipe = scanner_recipe.fromJson(id, json);
                loadedRecipes.put(id, recipe);
            } catch (RuntimeException exception) {
                LOGGER.error("Failed to parse scanner recipe {} from data pack", id, exception);
            }
        }

        this.recipes = Collections.unmodifiableMap(new LinkedHashMap<>(loadedRecipes));
    }

    public scanner_match findMatch(@NotNull Level level, @NotNull BlockPos scannerPos, @NotNull BlockState scannerState) {
        for (scanner_recipe recipe : recipes.values()) {
            for (Direction facing : HORIZONTAL_FACINGS) {
                if (recipe.matches(level, scannerPos, scannerState, facing)) {
                    return new scanner_match(recipe, facing, false);
                }
                if (recipe.acceptMirrored() && recipe.matches(level, scannerPos, scannerState, facing, true)) {
                    return new scanner_match(recipe, facing, true);
                }
            }
        }
        return null;
    }

    public scanner_recipe getRecipe(@NotNull ResourceLocation id) {
        scanner_recipe recipe = recipes.get(id);
        if (recipe == null) {
            throw new IllegalStateException("Unknown scanner recipe: " + id);
        }
        return recipe;
    }

    public Collection<scanner_recipe> getRecipes() {
        return recipes.values();
    }
}
