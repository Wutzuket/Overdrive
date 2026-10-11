package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.coolingtower.CoolingTowerControllerEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class CoolingTowerMenu extends AbstractContainerMenu {
    private final CoolingTowerControllerEntity be;
    public CoolingTowerMenu(int id, Inventory inv, CoolingTowerControllerEntity be){
        super(de.wutzuket.overdrive.index.ODMenuTypes.COOLING_TOWER_MENU.get(), id);
        this.be=be;
    }
    public CoolingTowerMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf){
        this(id, inv, (CoolingTowerControllerEntity)inv.player.level().getBlockEntity(buf.readBlockPos()));
    }
    @Override public ItemStack quickMoveStack(Player p,int s){ return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player p){ return true; }
    public CoolingTowerControllerEntity getBlockEntity(){ return be; }
}
