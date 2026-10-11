package de.wutzuket.overdrive.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public class AnnihilationRecipeSerializer implements RecipeSerializer<AnnihilationRecipe> {
    private static final MapCodec<AnnihilationRecipe> CODEC = RecordCodecBuilder.mapCodec(inst-> inst.group(
            FluidStack.CODEC.optionalFieldOf("input_antiproton", FluidStack.EMPTY).forGetter(AnnihilationRecipe::inputAntiproton),
            FluidStack.CODEC.optionalFieldOf("input_positron", FluidStack.EMPTY).forGetter(AnnihilationRecipe::inputPositron),
            FluidStack.CODEC.optionalFieldOf("input_coolant", FluidStack.EMPTY).forGetter(AnnihilationRecipe::inputCoolant),
            FluidStack.CODEC.optionalFieldOf("output_fluid", FluidStack.EMPTY).forGetter(AnnihilationRecipe::outputFluid),
            Codec.INT.fieldOf("energy_per_tick").forGetter(AnnihilationRecipe::energyPerTick),
            Codec.INT.fieldOf("temperature_threshold").forGetter(AnnihilationRecipe::temperatureThreshold),
            Codec.INT.fieldOf("process_time").forGetter(AnnihilationRecipe::processTime)
    ).apply(inst, AnnihilationRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, AnnihilationRecipe> STREAM = StreamCodec.of(
            AnnihilationRecipeSerializer::toNetwork, AnnihilationRecipeSerializer::fromNetwork
    );
    @Override public MapCodec<AnnihilationRecipe> codec(){ return CODEC; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, AnnihilationRecipe> streamCodec(){ return STREAM; }
    private static AnnihilationRecipe fromNetwork(RegistryFriendlyByteBuf buf){
        FluidStack anti=FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
        FluidStack pos=FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
        FluidStack in=FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
        FluidStack out=FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
        int e=buf.readInt(); int t=buf.readInt(); int p=buf.readInt();
        return new AnnihilationRecipe(anti,pos,in,out,e,t,p);
    }
    private static void toNetwork(RegistryFriendlyByteBuf buf, AnnihilationRecipe r){
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, r.inputAntiproton());
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, r.inputPositron());
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, r.inputCoolant());
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, r.outputFluid());
        buf.writeInt(r.energyPerTick()); buf.writeInt(r.temperatureThreshold()); buf.writeInt(r.processTime());
    }
}
