package de.wutzuket.overdrive.item;

import de.wutzuket.overdrive.index.ODDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MagneticCellItem extends Item {
    private final boolean isAntiproton;
    public MagneticCellItem(Properties props, boolean isAntiproton){ super(props.stacksTo(1)); this.isAntiproton=isAntiproton; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag){
        Integer amt = isAntiproton ? stack.get(ODDataComponents.ANTIPROTON_AMOUNT.get()) : stack.get(ODDataComponents.POSITRON_AMOUNT.get());
        int v = amt!=null?amt:0;
        tooltip.add(Component.literal((isAntiproton?"Antiprotons: ":"Positrons: ")+v+" / 1000"));
        if(v==0) tooltip.add(Component.literal("Empty - fill at AD/ELENA Output or Wolfram Target"));
        super.appendHoverText(stack,ctx,tooltip,flag);
    }
    public static ItemStack createFilled(boolean antiproton, int amount){
        ItemStack s=new ItemStack(antiproton? de.wutzuket.overdrive.index.ODItems.MAGNETIC_ANTIPROTON_CELL.get() : de.wutzuket.overdrive.index.ODItems.MAGNETIC_POSITRON_CELL.get());
        s.set(antiproton? ODDataComponents.ANTIPROTON_AMOUNT.get() : ODDataComponents.POSITRON_AMOUNT.get(), amount);
        return s;
    }
}
