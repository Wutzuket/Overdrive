package de.wutzuket.overdrive.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CryoDistillerRecipeSerializer implements RecipeSerializer<CryoDistillerRecipe> {
    /** {@code "catalysator": { "block": "minecraft:blue_ice" }} */
    private static final Codec<ResourceLocation> CATALYST_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("block").forGetter(id -> id)
    ).apply(inst, id -> id));

    private static final MapCodec<CryoDistillerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            FluidStack.CODEC.fieldOf("input_fluid").forGetter(CryoDistillerRecipe::inputFluid),
            CATALYST_CODEC.optionalFieldOf("catalysator").forGetter(r -> Optional.ofNullable(r.catalystBlockId())),
            FluidStack.CODEC.fieldOf("output_fluid").forGetter(CryoDistillerRecipe::outputFluid),
            Codec.INT.fieldOf("energy_per_tick").forGetter(CryoDistillerRecipe::energyPerTick),
            Codec.INT.fieldOf("process_time").forGetter(CryoDistillerRecipe::processTime)
    ).apply(inst, (input, catalyst, output, ept, time) ->
            new CryoDistillerRecipe(input, catalyst.orElse(null), output, ept, time)));

    private static final StreamCodec<RegistryFriendlyByteBuf, CryoDistillerRecipe> STREAM = StreamCodec.of(
            CryoDistillerRecipeSerializer::toNetwork,
            CryoDistillerRecipeSerializer::fromNetwork
    );

    private static CryoDistillerRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        FluidStack in = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
        ResourceLocation catalyst = buf.readBoolean() ? ResourceLocation.STREAM_CODEC.decode(buf) : null;
        FluidStack out = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
        int ept = buf.readInt();
        int time = buf.readInt();
        return new CryoDistillerRecipe(in, catalyst, out, ept, time);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, CryoDistillerRecipe r) {
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, r.inputFluid());
        buf.writeBoolean(r.catalystBlockId() != null);
        if (r.catalystBlockId() != null) {
            ResourceLocation.STREAM_CODEC.encode(buf, r.catalystBlockId());
        }
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, r.outputFluid());
        buf.writeInt(r.energyPerTick());
        buf.writeInt(r.processTime());
    }

    @Override public MapCodec<CryoDistillerRecipe> codec() { return CODEC; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, CryoDistillerRecipe> streamCodec() { return STREAM; }
}