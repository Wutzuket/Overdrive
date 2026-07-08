package de.wutzuket.overdrive.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class CollisionRecipeSerializer implements RecipeSerializer<CollisionRecipe> {
    private static final MapCodec<CollisionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("input_target").forGetter(CollisionRecipe::inputTarget),
                    Codec.INT.fieldOf("energy_cost").forGetter(CollisionRecipe::energyCost),
                    Codec.INT.fieldOf("process_time").forGetter(CollisionRecipe::processTime),
                    Codec.list(FluidStack.CODEC).fieldOf("results").forGetter(r -> List.of(r.outputFluid()))
            ).apply(instance, (input, energy, time, results) ->
                    new CollisionRecipe(input, energy, time, results.isEmpty() ? FluidStack.EMPTY : results.getFirst()))
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, CollisionRecipe> STREAM_CODEC =
            StreamCodec.of(
                    CollisionRecipeSerializer::toNetwork,
                    CollisionRecipeSerializer::fromNetwork
            );

    @Override
    public MapCodec<CollisionRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CollisionRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static CollisionRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        Ingredient inputTarget = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        int energyCost = buffer.readInt();
        int processTime = buffer.readInt();
        FluidStack outputFluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        return new CollisionRecipe(inputTarget, energyCost, processTime, outputFluid);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, CollisionRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.inputTarget());
        buffer.writeInt(recipe.energyCost());
        buffer.writeInt(recipe.processTime());
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.outputFluid());
    }
}
