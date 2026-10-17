package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.distiller.CryoDistillerEntity;
import de.wutzuket.overdrive.index.ODMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class CryoDistillerMenu extends AbstractContainerMenu {
    private final CryoDistillerEntity be;

    public CryoDistillerMenu(int id, Inventory inv, CryoDistillerEntity be) {
        super(ODMenuTypes.CRYO_DISTILLER_MENU.get(), id);
        this.be = be;
    }

    public CryoDistillerMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(id, inv, (CryoDistillerEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public CryoDistillerEntity getBlockEntity() {
        return be;
    }
}