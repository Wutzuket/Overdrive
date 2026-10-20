package de.wutzuket.overdrive.index;

import com.mojang.serialization.Codec;
import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.antimatter.ODAntimatters;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ODDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Overdrive.MODID);

    private static final StreamCodec<io.netty.buffer.ByteBuf, Antimatter> ANTIMATTER_STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(
                    id -> {
                        ResourceLocation parsed = ResourceLocation.tryParse(id);
                        Antimatter found = parsed == null ? null : ODAntimatters.get(parsed);
                        return found != null ? found : ODAntimatters.ANTIPROTONS;
                    },
                    antimatter -> antimatter.id().toString());

    /** Holds the antimatter type that is stored inside an antimatter cell. */
    public static final Supplier<DataComponentType<Antimatter>> ANTIMATTER_TYPE =
            register("antimatter_type", builder -> builder
                    .persistent(Antimatter.CODEC)
                    .networkSynchronized(ANTIMATTER_STREAM_CODEC));

    /** Holds the amount of antimatter units that are stored inside an antimatter cell. */
    public static final Supplier<DataComponentType<Integer>> ANTIMATTER_AMOUNT =
            register("antimatter_amount", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    private static <T> Supplier<DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return DATA_COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
