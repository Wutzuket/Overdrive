package de.wutzuket.overdrive.block.tungsten;

import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.antimatter.AntimatterTank;
import de.wutzuket.overdrive.block.vacuum.IVacuumNode;
import de.wutzuket.overdrive.block.vacuum.VacuumStructures;
import de.wutzuket.overdrive.index.ODBlockEntities;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.item.AntimatterCellItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class PositronVacuumOutputPortEntity extends BlockEntity implements IVacuumNode, MenuProvider {
    private float vacuum = 0;
    private long vacuumFedTick = 0;
    /** Volumen der Wolfram-Maschine (einmal berechnet, dann gecacht). */
    private int cachedVolume = -1;
    // Positron-Max-Rate: 14.000 mB/Op
    private final AntimatterTank tank = new AntimatterTank(500_000);
    /** Inhalt des frueheren Item-Slots, wird beim ersten Tick ausgegeben. */
    private ItemStack legacyInventory = ItemStack.EMPTY;

    public PositronVacuumOutputPortEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.POSITRON_VACUUM_OUTPUT_PORT.get(), pos, state);
        // Auch Aenderungen durch das Antimatter-Rohrnetz muessen gespeichert und
        // an die Clients geschickt werden, nicht nur die eigenen Zell-Interaktionen.
        tank.setOnChanged(() -> {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        });
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PositronVacuumOutputPortEntity be) {
        IVacuumNode.propagate(be);
        // Der fruehere Item-Slot existiert nicht mehr. Damit nichts verloren geht, wird
        // ein dort gespeicherter Stack beim ersten Tick vor dem Block ausgegeben.
        if (!level.isClientSide && !be.legacyInventory.isEmpty()) {
            ItemStack stack = be.legacyInventory;
            be.legacyInventory = ItemStack.EMPTY;
            Block.popResource(level, pos, stack);
            be.setChanged();
        }
        if (!level.isClientSide) level.sendBlockUpdated(pos, state, state, 2);
    }

    public AntimatterTank getTank() { return tank; }

    /**
     * Fills an empty cell from the tank or empties a full cell into it. Cells are handled by
     * right-clicking the port, so the gui does not need an extra item slot.
     *
     * @return true when the held item is a cell and the interaction was handled
     */
    public boolean handleCellUse(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof AntimatterCellItem cell)) {
            return false;
        }
        // Auf dem Client wird nur der Rechtsklick quittiert, die eigentliche Aenderung
        // passiert auf dem Server und wird ueber das Inventar synchronisiert.
        if (level == null || level.isClientSide) {
            return true;
        }

        int amount = cell.getAmount(held);
        if (amount <= 0) {
            fillCell(player, hand, held, cell);
        } else {
            emptyCell(player, hand, held, cell, amount);
        }
        return true;
    }

    /** Fills one empty cell from the tank. */
    private void fillCell(Player player, InteractionHand hand, ItemStack held, AntimatterCellItem cell) {
        Antimatter tankType = tank.getAntimatterInTank(0);
        int tankAmount = tank.getAmount(0);
        if (tankType == null || tankAmount <= 0) {
            return;
        }
        Antimatter cellType = cell.getType(held);
        if (cellType != null && !cellType.equals(tankType)) {
            return;
        }

        int wanted = Math.min(cell.getCapacity(), tankAmount);
        if (wanted <= 0) {
            return;
        }
        int extracted = tank.extractAntimatter(0, wanted, false);
        if (extracted <= 0) {
            return;
        }

        ItemStack filled = new ItemStack(ODItems.getCellFor(tankType));
        cell.setContent(filled, tankType, extracted);
        replaceOneCell(player, hand, filled);
    }

    /** Empties one full cell into the tank. */
    private void emptyCell(Player player, InteractionHand hand, ItemStack held, AntimatterCellItem cell, int amount) {
        Antimatter cellType = cell.getType(held);
        if (cellType == null) {
            return;
        }
        Antimatter tankType = tank.getAntimatterInTank(0);
        if (tankType != null && !tankType.equals(cellType)) {
            return;
        }
        int space = tank.getCapacity(0) - tank.getAmount(0);
        if (space < amount) {
            return;
        }
        int inserted = tank.insertAntimatter(0, cellType, amount, false);
        if (inserted < amount) {
            return;
        }

        ItemStack empty = new ItemStack(ODItems.EMPTY_ANTIMATTER_CELL.get());
        replaceOneCell(player, hand, empty);
    }

    /**
     * Replaces exactly one cell of the held stack. Empty cells can be stacked, so the rest of the
     * stack stays in the hand and the resulting cell is added to the inventory.
     */
    private void replaceOneCell(Player player, InteractionHand hand, ItemStack result) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getCount() > 1) {
            held.shrink(1);
            player.setItemInHand(hand, held);
            if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }
        } else {
            player.setItemInHand(hand, result);
        }
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override public float getVacuumLevel() { return vacuum; }
    @Override public void setVacuumLevel(float v) { this.vacuum = v; setChanged(); }
    @Override public long getVacuumFedTick() { return vacuumFedTick; }
    @Override public void setVacuumFedTick(long tick) { this.vacuumFedTick = tick; }

    /** Volumen der Wolfram-Maschine in Bloecken (fuer die Vakuum-Pumpe). */
    @Override public int getStructureVolume() {
        if (cachedVolume < 0) {
            Level lvl = getLevel();
            int vol = lvl == null ? 0 : VacuumStructures.tungstenVolume(lvl, getBlockPos());
            // 0 nicht cachen: die Maschine kann spaeter erst fertiggestellt werden.
            cachedVolume = vol > 0 ? vol : -1;
        }
        return Math.max(0, cachedVolume);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overdrive.positron_vacuum_output_port");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new de.wutzuket.overdrive.menu.PositronVacuumOutputPortMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("vacuum", vacuum);
        tag.putInt("structVolume", cachedVolume);
        CompoundTag tankTag = new CompoundTag();
        tank.saveToNBT(tankTag);
        tag.put("tank", tankTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        vacuum = tag.getFloat("vacuum");
        cachedVolume = tag.contains("structVolume") ? tag.getInt("structVolume") : -1;
        if (tag.contains("tank")) tank.loadFromNBT(tag.getCompound("tank"));
        if (tag.contains("Inventory")) {
            ItemStackHandler legacy = new ItemStackHandler(1);
            legacy.deserializeNBT(registries, tag.getCompound("Inventory"));
            legacyInventory = legacy.getStackInSlot(0);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
