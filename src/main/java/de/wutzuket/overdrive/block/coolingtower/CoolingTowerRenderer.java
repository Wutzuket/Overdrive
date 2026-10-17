package de.wutzuket.overdrive.block.coolingtower;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class CoolingTowerRenderer implements BlockEntityRenderer<CoolingTowerControllerEntity> {
    public CoolingTowerRenderer(BlockEntityRendererProvider.Context ctx){}
    @Override public boolean shouldRenderOffScreen(@NotNull CoolingTowerControllerEntity be){ return true; }
    @Override public @NotNull AABB getRenderBoundingBox(@NotNull CoolingTowerControllerEntity be){
        BlockPos center=CoolingTowerControllerEntity.getCenter(be.getBlockPos(), be.getBlockState());
        return new AABB(center).inflate(4,9,4);
    }
    @Override public void render(@NotNull CoolingTowerControllerEntity be, float pt, @NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int light, int overlay){
        if(be.getLevel()==null || !be.isHologramEnabled()) return;
        if(be.isValid()) return; // complete -> hide ghost
        BlockPos ctrl=be.getBlockPos();
        BlockState state=be.getBlockState();
        BlockPos center=CoolingTowerControllerEntity.getCenter(ctrl, state);
        Direction facing=state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int buildY=findBuildLayer(be, ctrl);
        if(buildY==Integer.MAX_VALUE) return;
        int y=buildY;
        int r = (y==0 || y==8) ? 2 : (y>=3 && y<=5 ? 1 : 2);
        // Controller position relative to center (in the wall, facing outward)
        int ctrlX = facing.getStepX() * 2;
        int ctrlZ = facing.getStepZ() * 2;
        
        for(int x=-r;x<=r;x++) for(int z=-r;z<=r;z++){
            // Skip controller position in the wall
            if(y==0 && x==ctrlX && z==ctrlZ) continue;
            boolean isWall=Math.abs(x)==r || Math.abs(z)==r;
            BlockPos p=new BlockPos(center.getX()+x, center.getY()+y, center.getZ()+z);
            var existing=be.getLevel().getBlockState(p);
            // Render coordinates relative to controller (pose stack origin)
            double rx = p.getX() - ctrl.getX();
            double ry = p.getY() - ctrl.getY();
            double rz = p.getZ() - ctrl.getZ();
            if(!isWall){
                // Innenraum muss Luft sein -> fremde Blöcke rot markieren
                if(!existing.isAir()) renderGhostBox(pose,buffer,(float)rx,(float)ry,(float)rz,1.0f,0.0f,0.0f,0.45f);
                continue;
            }
            boolean inSlot=CoolingTowerControllerEntity.isInputSlot(x,y,z);
            boolean outSlot=CoolingTowerControllerEntity.isOutputSlot(x,y,z);
            if(existing.isAir()){
                float rr=0.2f, gg=0.6f, bb=1f, a=0.32f;
                if(inSlot){ rr=1.0f; gg=0.55f; bb=0.1f; }
                else if(outSlot){ rr=0.75f; gg=0.35f; bb=1.0f; }
                else if(y==0 || y==8){ rr=0.0f; gg=0.8f; bb=0.3f; }
                renderGhostBox(pose,buffer,(float)rx,(float)ry,(float)rz,rr,gg,bb,a);
            } else {
                boolean ok;
                if(inSlot) ok=existing.getBlock() instanceof CoolingTowerInputPortBlock;
                else if(outSlot) ok=existing.getBlock() instanceof CoolingTowerOutputPortBlock;
                else ok=existing.getBlock() instanceof CoolingTowerCasingBlock;
                if(!ok) renderGhostBox(pose,buffer,(float)rx,(float)ry,(float)rz,1.0f,0.0f,0.0f,0.45f);
            }
        }
        // also render outer hyperbolic air indicator for middle levels (where outer edge should be air)
        if(buildY>=3 && buildY<=5){
            for(int x=-2;x<=2;x++) for(int z=-2;z<=2;z++){
                if(Math.abs(x)!=2 && Math.abs(z)!=2) continue;
                BlockPos p=new BlockPos(center.getX()+x, center.getY()+y, center.getZ()+z);
                if(!be.getLevel().getBlockState(p).isAir()) continue; // should stay air -> no ghost
                double rx = p.getX() - ctrl.getX();
                double ry = p.getY() - ctrl.getY();
                double rz = p.getZ() - ctrl.getZ();
                renderGhostBox(pose,buffer,(float)rx,(float)ry,(float)rz,0.0f,1.0f,0.0f,0.2f);
            }
        }
    }
    private int findBuildLayer(CoolingTowerControllerEntity be, BlockPos ctrl){
        BlockState state=be.getBlockState();
        BlockPos center=CoolingTowerControllerEntity.getCenter(ctrl, state);
        Direction facing=state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int ctrlX=facing.getStepX()*2;
        int ctrlZ=facing.getStepZ()*2;
        for(int y=0;y<9;y++){
            boolean complete=true;
            int r = (y==0 || y==8) ? 2 : (y>=3 && y<=5 ? 1 : 2);
            for(int x=-r;x<=r;x++) for(int z=-r;z<=r;z++){
                if(y==0 && x==ctrlX && z==ctrlZ) continue;
                boolean isWall=Math.abs(x)==r || Math.abs(z)==r;
                BlockPos p=new BlockPos(center.getX()+x, center.getY()+y, center.getZ()+z);
                var st=be.getLevel().getBlockState(p);
                if(!isWall){
                    if(!st.isAir()) complete=false;
                    continue;
                }
                if(CoolingTowerControllerEntity.isInputSlot(x,y,z)){
                    if(!(st.getBlock() instanceof CoolingTowerInputPortBlock)) complete=false;
                } else if(CoolingTowerControllerEntity.isOutputSlot(x,y,z)){
                    if(!(st.getBlock() instanceof CoolingTowerOutputPortBlock)) complete=false;
                } else {
                    if(st.isAir() || !(st.getBlock() instanceof CoolingTowerCasingBlock)) complete=false;
                }
            }
            if(!complete) return y;
            // second check: middle levels outer frame must be air
            if(y>=3 && y<=5){
                for(int x=-2;x<=2;x++) for(int z=-2;z<=2;z++){
                    if(Math.abs(x)!=2 && Math.abs(z)!=2) continue;
                    BlockPos p=new BlockPos(center.getX()+x, center.getY()+y, center.getZ()+z);
                    if(!be.getLevel().getBlockState(p).isAir()){ complete=false; break; }
                }
                if(!complete) return y;
            }
        }
        return Integer.MAX_VALUE;
    }
    private void renderGhostBox(PoseStack pose, MultiBufferSource buffer,float x,float y,float z,float r,float g,float b,float a){
        float t=0.04f;
        VertexConsumer c=buffer.getBuffer(RenderType.debugFilledBox());
        LevelRenderer.addChainedFilledBoxVertices(pose,c, x,y-t,z-t, x+1,y+t,z+t,r,g,b,a);
        LevelRenderer.addChainedFilledBoxVertices(pose,c, x,y-t,z+1-t, x+1,y+t,z+1+t,r,g,b,a);
        LevelRenderer.addChainedFilledBoxVertices(pose,c, x-t,y,z-t, x+t,y,z+1+t,r,g,b,a);
        LevelRenderer.addChainedFilledBoxVertices(pose,c, x+1-t,y,z-t, x+1+t,y,z+1+t,r,g,b,a);
        LevelRenderer.addChainedFilledBoxVertices(pose,c, x,y+1-t,z-t, x+1,y+1+t,z+t,r,g,b,a);
        LevelRenderer.addChainedFilledBoxVertices(pose,c, x,y+1-t,z+1-t, x+1,y+1+t,z+1+t,r,g,b,a);
        LevelRenderer.addChainedFilledBoxVertices(pose,c, x-t,y+1,z-t, x+t,y+1,z+1+t,r,g,b,a);
        LevelRenderer.addChainedFilledBoxVertices(pose,c, x+1-t,y+1,z-t, x+1+t,y+1,z+1+t,r,g,b,a);
        LevelRenderer.addChainedFilledBoxVertices(pose,c, x-t,y-t,z, x+t,y+1+t,z+t,r,g,b,a);
        LevelRenderer.addChainedFilledBoxVertices(pose,c, x+1-t,y-t,z, x+1+t,y+1+t,z+t,r,g,b,a);
        LevelRenderer.addChainedFilledBoxVertices(pose,c, x-t,y-t,z+1, x+t,y+1+t,z+1+t,r,g,b,a);
        LevelRenderer.addChainedFilledBoxVertices(pose,c, x+1-t,y-t,z+1, x+1+t,y+1+t,z+1+t,r,g,b,a);
    }
}
