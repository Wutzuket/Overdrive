package de.wutzuket.overdrive.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.wutzuket.overdrive.antimatter.AntimatterStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public class CollisionRecipeSerializer implements RecipeSerializer<CollisionRecipe> {
    private static final MapCodec<CollisionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    FluidStack.CODEC.fieldOf("input_fluid").forGetter(CollisionRecipe::inputFluid),
                    Codec.INT.fieldOf("energy_cost").forGetter(CollisionRecipe::energyCost),
                    Codec.INT.fieldOf("process_time").forGetter(CollisionRecipe::processTime),
                    AntimatterStack.MAP_CODEC.fieldOf("output_antimatter").forGetter(CollisionRecipe::outputAntimatter)
            ).apply(instance, CollisionRecipe::new)
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
        FluidStack inputFluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        int energyCost = buffer.readInt();
        int processTime = buffer.readInt();
        AntimatterStack outputAntimatter = AntimatterStack.STREAM_CODEC.decode(buffer);
        return new CollisionRecipe(inputFluid, energyCost, processTime, outputAntimatter);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, CollisionRecipe recipe) {
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.inputFluid());
        buffer.writeInt(recipe.energyCost());
        buffer.writeInt(recipe.processTime());
        AntimatterStack.STREAM_CODEC.encode(buffer, recipe.outputAntimatter());
    }
}
