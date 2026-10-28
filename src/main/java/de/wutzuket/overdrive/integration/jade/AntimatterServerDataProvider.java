package de.wutzuket.overdrive.integration.jade;

import de.wutzuket.overdrive.antimatter.AntimatterNetwork;
import de.wutzuket.overdrive.block.antimatter.AntimatterPipeBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Sends the totals of an antimatter pipe network to the client. The {@link AntimatterNetwork} is
 * server side only, so the client cannot read the amount and capacity by itself.
 */
public class AntimatterServerDataProvider implements IServerDataProvider<BlockAccessor> {

    public static final AntimatterServerDataProvider INSTANCE = new AntimatterServerDataProvider();

    public static final String AMOUNT = "overdrive_antimatter_amount";
    public static final String CAPACITY = "overdrive_antimatter_capacity";

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath("overdrive", "antimatter_network_data");

    private AntimatterServerDataProvider() {
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof AntimatterPipeBlockEntity pipe)) {
            return;
        }
        AntimatterNetwork network = pipe.getNetwork();
        if (network == null) {
            return;
        }
        data.putInt(AMOUNT, network.getTotalAmount());
        data.putInt(CAPACITY, network.getTotalCapacity());
    }
}
