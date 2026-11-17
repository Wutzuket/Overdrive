package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.block.coolingtower.CoolingTowerControllerEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

public class CoolingTowerMenu extends AbstractContainerMenu {
    /** Indizes der synchronisierten Werte. */
    private static final int PROGRESS = 0;
    private static final int MAX_PROGRESS = 1;
    private static final int ACTIVE = 2;
    private static final int VALID = 3;

    private final CoolingTowerControllerEntity be;
    /**
     * Die Werte, die der Server schickt. Nur der Client liest daraus, damit
     * das GUI jeden Tick aktuell ist.
     */
    private final int[] data = new int[4];

    public CoolingTowerMenu(int id, Inventory inv, CoolingTowerControllerEntity be){
        super(de.wutzuket.overdrive.index.ODMenuTypes.COOLING_TOWER_MENU.get(), id);
        this.be=be;

        data[PROGRESS] = be.getProgress();
        data[MAX_PROGRESS] = be.getMaxProgress();
        data[ACTIVE] = be.isActive() ? 1 : 0;
        data[VALID] = be.isValid() ? 1 : 0;

        addDataSlots(new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case PROGRESS -> be.getProgress();
                    case MAX_PROGRESS -> be.getMaxProgress();
                    case ACTIVE -> be.isActive() ? 1 : 0;
                    case VALID -> be.isValid() ? 1 : 0;
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {
                if (index >= 0 && index < data.length) {
                    data[index] = value;
                }
            }
            @Override public int getCount() { return data.length; }
        });
    }
    public CoolingTowerMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf){
        this(id, inv, (CoolingTowerControllerEntity)inv.player.level().getBlockEntity(buf.readBlockPos()));
    }
    @Override public ItemStack quickMoveStack(Player p,int s){ return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player p){ return true; }
    public CoolingTowerControllerEntity getBlockEntity(){ return be; }

    public int getProgress() { return data[PROGRESS]; }
    public int getMaxProgress() { return data[MAX_PROGRESS]; }
    public boolean isActive() { return data[ACTIVE] == 1; }
    public boolean isValid() { return data[VALID] == 1; }
}
