package de.wutzuket.overdrive.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * Rezept fuer den Cryo Distiller (1x1x1).
 * Nimmt eine Eingabefluessigkeit, verbraucht Energie pro Tick und liefert eine Ausgabefluessigkeit.
 * Der optionale "catalysator" (JSON: {@code "catalysator": { "block": "minecraft:blue_ice" }})
 * muss als Block direkt unter der Maschine liegen, damit sie laeuft.
 */
public class CryoDistillerRecipe implements Recipe<RecipeInput> {
    private final FluidStack inputFluid;
    private final @Nullable ResourceLocation catalystBlockId;
    private final FluidStack outputFluid;
    private final int energyPerTick;
    private final int processTime;

    public CryoDistillerRecipe(FluidStack inputFluid, @Nullable ResourceLocation catalystBlockId,
                               FluidStack outputFluid, int energyPerTick, int processTime) {
        this.inputFluid = inputFluid;
        this.catalystBlockId = catalystBlockId;
        this.outputFluid = outputFluid;
        this.energyPerTick = energyPerTick;
        this.processTime = processTime;
    }

    @Override public boolean matches(RecipeInput input, Level level) { return true; }
    @Override public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public RecipeSerializer<?> getSerializer() { return ODRecipeSerializers.CRYO_DISTILLER_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ODRecipeTypes.CRYO_DISTILLER.get(); }

    public FluidStack inputFluid() { return inputFluid; }
    public FluidStack outputFluid() { return outputFluid; }
    public int energyPerTick() { return energyPerTick; }
    public int processTime() { return processTime; }

    /** Block-ID des noetigen Katalysators, oder null wenn keiner gebraucht wird. */
    public @Nullable ResourceLocation catalystBlockId() { return catalystBlockId; }

    /** true, wenn dieses Rezept einen Katalysator-Block verlangt. */
    public boolean needsCatalyst() { return catalystBlockId != null; }

    /** Gesamtkosten des Vorgangs: pro Tick * Dauer. */
    public long totalEnergy() { return (long) energyPerTick * (long) Math.max(1, processTime); }
}