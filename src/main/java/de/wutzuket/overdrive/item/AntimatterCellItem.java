package de.wutzuket.overdrive.item;

import de.wutzuket.overdrive.antimatter.Antimatter;
import de.wutzuket.overdrive.config.ODConfig;
import de.wutzuket.overdrive.index.ODDataComponents;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * A cell that can hold antimatter. There are no buckets in this mod - the cell is the only way to
 * move antimatter around by hand. The content of a cell is stored in data components so it can be
 * synced to the client and rendered correctly.
 *
 * <p>The icon of the cell is drawn procedurally, the colour comes from the antimatter type. That
 * way a new type of antimatter automatically gets a fitting cell icon.</p>
 */
public class AntimatterCellItem extends Item {

    private final @Nullable Antimatter defaultType;
    private final boolean filledByDefault;

    /**
     * @param properties      the item properties
     * @param defaultType     the antimatter type this cell is made for, {@code null} for the
     *                        universal cell that takes over the type of the tank it is filled from
     * @param filledByDefault true when a fresh cell of this item is already completely filled
     */
    public AntimatterCellItem(Properties properties, @Nullable Antimatter defaultType, boolean filledByDefault) {
        super(properties);
        this.defaultType = defaultType;
        this.filledByDefault = filledByDefault;
    }

    /** @return the maximum amount of antimatter units a cell can hold. */
    public int getCapacity() {
        return Math.max(1, ODConfig.ANTIMATTER_CELL_CAPACITY.get());
    }

    /** @return the type of antimatter this cell holds or {@code null} when it is empty. */
    public @Nullable Antimatter getType(ItemStack stack) {
        Antimatter stored = stack.get(ODDataComponents.ANTIMATTER_TYPE.get());
        if (stored != null) {
            return stored;
        }
        return defaultType;
    }

    /** @return the amount of antimatter units inside the given stack. */
    public int getAmount(ItemStack stack) {
        Integer amount = stack.get(ODDataComponents.ANTIMATTER_AMOUNT.get());
        if (amount == null) {
            return filledByDefault ? getCapacity() : 0;
        }
        return Math.max(0, amount);
    }

    /** @return true when the given stack is a cell. */
    public static boolean isCell(@Nullable ItemStack stack) {
        return stack != null && stack.getItem() instanceof AntimatterCellItem;
    }

    /** Creates a new stack of this cell with the given content. */
    public ItemStack createStack(@Nullable Antimatter type, int amount) {
        ItemStack stack = new ItemStack(this);
        setContent(stack, type, amount);
        return stack;
    }

    /** Writes the given content into the stack. */
    public void setContent(ItemStack stack, @Nullable Antimatter type, int amount) {
        int clamped = Math.max(0, Math.min(amount, getCapacity()));
        if (type == null) {
            stack.remove(ODDataComponents.ANTIMATTER_TYPE.get());
            stack.remove(ODDataComponents.ANTIMATTER_AMOUNT.get());
            return;
        }
        stack.set(ODDataComponents.ANTIMATTER_TYPE.get(), type);
        stack.set(ODDataComponents.ANTIMATTER_AMOUNT.get(), clamped);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getAmount(stack) > 0;
    }

    @Override
    public Component getName(ItemStack stack) {
        Antimatter type = getType(stack);
        if (type == null || getAmount(stack) <= 0) {
            return Component.translatable("item.overdrive.empty_antimatter_cell");
        }
        return Component.translatable("item.overdrive.antimatter_cell.filled", type.displayName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int amount = getAmount(stack);
        Antimatter type = getType(stack);
        if (amount > 0 && type != null) {
            tooltip.add(Component.translatable("tooltip.overdrive.antimatter_cell.content",
                    type.displayName(), amount, getCapacity()));
        } else {
            tooltip.add(Component.translatable("tooltip.overdrive.antimatter_cell.empty"));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return AntimatterCellRenderer.getInstance();
            }
        });
    }
}
