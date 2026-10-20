package de.wutzuket.overdrive.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public class PositronExtractionRecipeSerializer implements RecipeSerializer<PositronExtractionRecipe> {
    private static final Codec<PositronExtractionRecipe.ResultEntry> RESULT_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(PositronExtractionRecipe.ResultEntry::id),
                    Codec.INT.optionalFieldOf("amount", 0).forGetter(PositronExtractionRecipe.ResultEntry::amount),
                    Codec.INT.optionalFieldOf("count", 0).forGetter(PositronExtractionRecipe.ResultEntry::count)
            ).apply(instance, PositronExtractionRecipe.ResultEntry::new)
    );

    private static final MapCodec<PositronExtractionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("mesh_durability_cost").forGetter(PositronExtractionRecipe::meshDurabilityCost),
                    Codec.INT.fieldOf("energy_cost").forGetter(PositronExtractionRecipe::energyCost),
                    Codec.INT.fieldOf("process_time").forGetter(PositronExtractionRecipe::processTime),
                    RESULT_ENTRY_CODEC.listOf().fieldOf("results").forGetter(PositronExtractionRecipe::resultEntries)
            ).apply(instance, PositronExtractionRecipe::new)
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, PositronExtractionRecipe> STREAM_CODEC =
            StreamCodec.of(
                    PositronExtractionRecipeSerializer::toNetwork,
                    PositronExtractionRecipeSerializer::fromNetwork
            );

    @Override
    public MapCodec<PositronExtractionRecipe> codec() { return CODEC; }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, PositronExtractionRecipe> streamCodec() { return STREAM_CODEC; }

    private static PositronExtractionRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        int meshDurabilityCost = buffer.readInt();
        int energyCost = buffer.readInt();
        int processTime = buffer.readInt();
        de.wutzuket.overdrive.antimatter.AntimatterStack outputAntimatter = de.wutzuket.overdrive.antimatter.AntimatterStack.STREAM_CODEC.decode(buffer);
        ItemStack outputCell = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        return new PositronExtractionRecipe(meshDurabilityCost, energyCost, processTime, outputAntimatter, outputCell);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, PositronExtractionRecipe recipe) {
        buffer.writeInt(recipe.meshDurabilityCost());
        buffer.writeInt(recipe.energyCost());
        buffer.writeInt(recipe.processTime());
        de.wutzuket.overdrive.antimatter.AntimatterStack.STREAM_CODEC.encode(buffer, recipe.outputAntimatter());
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.outputCell());
    }
}
