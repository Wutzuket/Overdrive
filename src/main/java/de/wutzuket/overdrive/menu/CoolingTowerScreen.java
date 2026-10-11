package de.wutzuket.overdrive.menu;

import de.wutzuket.overdrive.client.gui.GuiStyle;
import de.wutzuket.overdrive.util.ODFormat;
import de.wutzuket.overdrive.client.gui.ButtonHelper;
import de.wutzuket.overdrive.network.ToggleHologramPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class CoolingTowerScreen extends AbstractContainerScreen<CoolingTowerMenu> {
    private static final int WIDTH = 200;
    private static final int HEIGHT = 150;
    private static final int BAR_W = 10;
    private static final int BAR_H = 36;

    private ButtonHelper hologramBtn;
    private ButtonHelper tabStatus, tabStats;
    private int currentTab = 0;

    public CoolingTowerScreen(CoolingTowerMenu m, Inventory inv, Component t){
        super(m,inv,t);
        imageWidth=WIDTH; imageHeight=HEIGHT;
        this.titleLabelY = 4;
        this.inventoryLabelY = HEIGHT - 92;
    }

    @Override protected void init(){
        super.init();
        int x=(width-WIDTH)/2, y=(height-HEIGHT)/2;
        hologramBtn = new ButtonHelper(x+WIDTH-20, y+4, 16, 16, Component.literal("H"), b->{
            var be=menu.getBlockEntity();
            if(be!=null) PacketDistributor.sendToServer(new ToggleHologramPayload(be.getBlockPos()));
        }, GuiStyle.COL_CYAN);
        addRenderableWidget(hologramBtn);
        tabStatus = new ButtonHelper(x+6, y+HEIGHT-20, 44, 14, Component.literal("Status"), b->{ currentTab=0; updateTabVis(); }, GuiStyle.COL_CYAN);
        tabStats = new ButtonHelper(x+52, y+HEIGHT-20, 44, 14, Component.literal("Stats"), b->{ currentTab=1; updateTabVis(); }, GuiStyle.COL_CYAN);
        addRenderableWidget(tabStatus); addRenderableWidget(tabStats);
        updateTabVis();
    }

    private void updateTabVis(){
        if(tabStatus!=null) tabStatus.setMessage(Component.literal((currentTab==0?"[":"")+"Status"+(currentTab==0?"]":"")));
        if(tabStats!=null) tabStats.setMessage(Component.literal((currentTab==1?"[":"")+"Stats"+(currentTab==1?"]":"")));
    }

    private static String fluidName(net.neoforged.neoforge.fluids.FluidStack stack, String fallback){
        if(stack==null || stack.isEmpty()) return fallback;
        return stack.getHoverName().getString();
    }

    @Override protected void renderBg(GuiGraphics g, float p, int mx, int my){
        int x=(width-imageWidth)/2, y=(height-imageHeight)/2;
        GuiStyle.panel(g,x,y,WIDTH,HEIGHT);
        var be=menu.getBlockEntity();
        int barY=y+16;
        if(be!=null){
            GuiStyle.fluidGauge(g, font, be.getInputTank().getFluid(), 8000, x+6, barY, BAR_W, BAR_H, null);
            var out = be.getOutputFluidForDisplay();
            if(!out.isEmpty()) out = out.copyWithAmount(be.getOutputTotalAmount());
            GuiStyle.fluidGauge(g, font, out, be.getOutputTotalCapacity(), x+WIDTH-16, barY, BAR_W, BAR_H, null);
        } else {
            GuiStyle.inset(g, x+6, barY, BAR_W, BAR_H);
            GuiStyle.inset(g, x+WIDTH-16, barY, BAR_W, BAR_H);
        }
        // InnerScreen
        int sx=x+22, sy=y+15, sw=156, sh=56;
        GuiStyle.screenArea(g, sx, sy, sw, sh);
        // Progress bar
        int hx=x+5, hy=y+78, hw=WIDTH-10, hh=7;
        double prog = be!=null ? Math.min(1.0, be.getProgress()/(double)Math.max(1,be.getMaxProgress())) : 0.0;
        GuiStyle.hBar(g, hx, hy, hw, hh, prog, GuiStyle.COL_CYAN);
    }

    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawString(font, title, 8,4, GuiStyle.COL_TEXT,false);
        var be=menu.getBlockEntity();
        int sx=22, sy=15;
        String status = be==null? "?" : !be.isValid()? "§cInvalid" : be.isActive()? "§aCooling" : "§eIdle";
        g.drawString(font, "Status: "+status, sx+4, sy+4, GuiStyle.COL_TEXT,false);
        if(be!=null){
            String detail = !be.isValid()? be.getFailReason().isEmpty()? "Structure" : be.getFailReason() : be.isActive()? "Cooling "+be.getProgress()+"/"+be.getMaxProgress() : "Idle";
            g.drawString(font, detail, sx+4, sy+14, GuiStyle.COL_TEXT_DIM,false);
            String inName = fluidName(be.getInputTank().getFluid(), "Empty");
            g.drawString(font, "In: "+inName, sx+4, sy+24, GuiStyle.COL_CYAN,false);
            g.drawString(font, ODFormat.formatFluidPair(be.getInputTank().getFluidAmount(), 8000), sx+4, sy+34, GuiStyle.COL_TEXT,false);
            String outName = fluidName(be.getOutputFluidForDisplay(), "Empty");
            g.drawString(font, "Out: "+outName, sx+4, sy+44, be.getOutputTotalAmount()>0? GuiStyle.COL_GREEN : GuiStyle.COL_TEXT,false);
        }
        if(be!=null){
            if(currentTab==0){
                String inName = fluidName(be.getInputTank().getFluid(), "Empty");
                g.drawString(font, inName+": "+ODFormat.formatFluidPair(be.getInputTank().getFluidAmount(), 8000), 8,94, GuiStyle.COL_TEXT,false);
                String outName = fluidName(be.getOutputFluidForDisplay(), "Empty");
                g.drawString(font, outName+": "+ODFormat.formatFluidPair(be.getOutputTotalAmount(), be.getOutputTotalCapacity()), 8,104, GuiStyle.COL_TEXT_DIM,false);
            } else {
                drawPortLine(g, "Steam In:"+be.getLastInputPorts()+"/1", be.getLastInputPorts()>=1? GuiStyle.COL_GREEN: GuiStyle.COL_RED, 8,94);
                drawPortLine(g, "Water Out:"+be.getLastOutputPorts()+"/1", be.getLastOutputPorts()>=1? GuiStyle.COL_GREEN: GuiStyle.COL_RED, 100,94);
                if(!be.isValid() && !be.getFailReason().isEmpty()){
                    String posTxt = "";
                    if(be.getFailPos()!=null && be.getBlockPos()!=null){
                        net.minecraft.core.BlockPos rel = be.getFailPos().subtract(be.getBlockPos());
                        posTxt = " ("+rel.getX()+","+rel.getY()+","+rel.getZ()+")";
                    }
                    g.drawString(font, be.getFailReason()+posTxt, 8,104, GuiStyle.COL_RED,false);
                }
            }
        }
    }

    private void drawPortLine(GuiGraphics g, String label, int col, int x,int y){
        g.drawString(font,label,x,y,col,false);
    }

    @Override public void render(GuiGraphics g,int mx,int my,float p){ super.render(g,mx,my,p); renderTooltip(g,mx,my); }

    @Override protected void renderTooltip(GuiGraphics g,int mx,int my){
        int x=(width-WIDTH)/2, y=(height-HEIGHT)/2;
        int barY=y+16;
        var be=menu.getBlockEntity();
        if(be!=null && mx>=x+6 && mx<=x+16 && my>=barY && my<=barY+BAR_H){
            List<Component> l=new ArrayList<>();
            var f=be.getInputTank().getFluid();
            l.add(Component.literal(f.isEmpty()? "Empty" : f.getHoverName().getString()+": "+ODFormat.formatFluidPair(be.getInputTank().getFluidAmount(), 8000)));
            g.renderTooltip(font,l,java.util.Optional.empty(),mx,my); return;
        }
        if(be!=null && mx>=x+WIDTH-16 && mx<=x+WIDTH-6 && my>=barY && my<=barY+BAR_H){
            List<Component> l=new ArrayList<>();
            var f=be.getOutputFluidForDisplay();
            l.add(Component.literal(f.isEmpty()? "Empty" : f.getHoverName().getString()+": "+ODFormat.formatFluidPair(be.getOutputTotalAmount(), be.getOutputTotalCapacity())));
            g.renderTooltip(font,l,java.util.Optional.empty(),mx,my); return;
        }
        super.renderTooltip(g,mx,my);
    }
}
