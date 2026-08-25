package de.wutzuket.overdrive.index;

import de.wutzuket.overdrive.Overdrive;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ODDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Overdrive.MODID);

    public static final Supplier<DataComponentType<Integer>> ANTIPROTON_AMOUNT =
            register("antiproton_amount", builder -> builder.persistent(net.minecraft.util.ExtraCodecs.POSITIVE_INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT));

    public static final Supplier<DataComponentType<Integer>> POSITRON_AMOUNT =
            register("positron_amount", builder -> builder.persistent(net.minecraft.util.ExtraCodecs.POSITIVE_INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT));

    private static <T> Supplier<DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return DATA_COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
