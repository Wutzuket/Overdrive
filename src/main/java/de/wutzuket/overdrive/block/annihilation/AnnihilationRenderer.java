package de.wutzuket.overdrive.block.annihilation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class AnnihilationRenderer implements BlockEntityRenderer<AnnihilationControllerEntity> {
    public AnnihilationRenderer(BlockEntityRendererProvider.Context ctx){}
    @Override public boolean shouldRenderOffScreen(@NotNull AnnihilationControllerEntity be){ return true; }
    @Override public @NotNull AABB getRenderBoundingBox(@NotNull AnnihilationControllerEntity be){ return new AABB(be.getBlockPos()).inflate(30); }

    private boolean isValidWall(Block b, BlockPos pos, BlockPos ctrlPos){
        if(pos.equals(ctrlPos) && b instanceof AnnihilationControllerBlock) return true;
        return b instanceof AnnihilationHeavyCasingBlock
                || b instanceof CellDockingPortBlock
                || b instanceof AnnihilationVacuumPortBlock
                || b instanceof CoolantInputPortBlock
                || b instanceof SteamOutputPortBlock
                || b instanceof EnergyOutputPortBlock;
    }
    private boolean isValidCore(Block b, int dx,int dy,int dz){
        boolean isShell = dx==2 || dy==2 || dz==2;
        if(isShell) return b instanceof CoreContainmentGlassBlock;
        return b instanceof ReactorCoreBlock;
    }

    @Override public void render(@NotNull AnnihilationControllerEntity be, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int light, int overlay){
        if(be.getLevel()==null || !be.isHologramEnabled()) return;
        BlockState state=be.getBlockState();
        Direction facing=state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos ctrl=be.getBlockPos();
        BlockPos center=AnnihilationControllerEntity.getCenter(ctrl, state);
        int cx=center.getX(), cy=center.getY(), cz=center.getZ();
        int OUTER=AnnihilationControllerEntity.OUTER;
        int buildY=findBuildLayer(be, cx, cy, cz, ctrl);
        if(buildY==Integer.MAX_VALUE) return;
        for(int x=cx-OUTER; x<=cx+OUTER; x++){
            for(int z=cz-OUTER; z<=cz+OUTER; z++){
                int dx=Math.abs(x-cx), dz=Math.abs(z-cz);
                int y=buildY;
                int dy=Math.abs(y-cy);
                boolean inCore = dx<=2 && dy<=2 && dz<=2;
                BlockPos check=new BlockPos(x,y,z);
                BlockState existing=be.getLevel().getBlockState(check);
                Block b=existing.getBlock();
                if(inCore){
                    if(existing.isAir()){
                        float r,g,bb; boolean isShell = dx==2 || dy==2 || dz==2;
                        if(isShell){ r=0.2f; g=0.9f; bb=0.9f; } else { r=1f; g=0.6f; bb=0.2f; }
                        double rx=x-ctrl.getX(), ry=y-ctrl.getY(), rz=z-ctrl.getZ();
                        renderGhostBox(pose,buffer,(float)rx,(float)ry,(float)rz,r,g,bb,0.35f);
                    } else if(!isValidCore(b,dx,dy,dz)){
                        double rx=x-ctrl.getX(), ry=y-ctrl.getY(), rz=z-ctrl.getZ();
                        renderGhostBox(pose,buffer,(float)rx,(float)ry,(float)rz,1f,0f,0f,0.45f);
                    }
                    continue;
                }
                boolean isWall = AnnihilationControllerEntity.isTorusWall(dx, dy, dz);
                boolean isHollow = AnnihilationControllerEntity.isTorusHollow(dx, dy, dz);
                if(isWall){
                    if(existing.isAir()){
                        double rx=x-ctrl.getX(), ry=y-ctrl.getY(), rz=z-ctrl.getZ();
                        renderGhostBox(pose,buffer,(float)rx,(float)ry,(float)rz,0.6f,0.2f,1f,0.35f);
                    } else if(!isValidWall(b, check, ctrl)){
                        double rx=x-ctrl.getX(), ry=y-ctrl.getY(), rz=z-ctrl.getZ();
                        renderGhostBox(pose,buffer,(float)rx,(float)ry,(float)rz,1f,0f,0f,0.45f);
                    }
                } else if(isHollow){
                    // nur Inneres des Torus-Schlauchs muss Luft sein, äußeres Volumen egal
                    if(!existing.isAir()){
                        double rx=x-ctrl.getX(), ry=y-ctrl.getY(), rz=z-ctrl.getZ();
                        renderGhostBox(pose,buffer,(float)rx,(float)ry,(float)rz,1f,0f,0f,0.45f);
                    }
                }
            }
        }
    }
    private int findBuildLayer(AnnihilationControllerEntity be,int cx,int cy,int cz, BlockPos ctrl){
        int OUTER=AnnihilationControllerEntity.OUTER;
        for(int y=cy-OUTER; y<=cy+OUTER; y++){
            boolean layerComplete=true;
            for(int x=cx-OUTER; x<=cx+OUTER; x++) for(int z=cz-OUTER; z<=cz+OUTER; z++){
                int dx=Math.abs(x-cx), dy=Math.abs(y-cy), dz=Math.abs(z-cz);
                boolean inCore = dx<=2 && dy<=2 && dz<=2;
                BlockPos p=new BlockPos(x,y,z);
                BlockState st=be.getLevel().getBlockState(p);
                Block b=st.getBlock();
                if(inCore){
                    if(st.isAir() || !isValidCore(b,dx,dy,dz)) layerComplete=false;
                    continue;
                }
                boolean isWall = AnnihilationControllerEntity.isTorusWall(dx, dy, dz);
                boolean isHollow = AnnihilationControllerEntity.isTorusHollow(dx, dy, dz);
                if(isWall){
                    if(st.isAir() || !isValidWall(b,p,ctrl)) layerComplete=false;
                } else if(isHollow){
                    if(!st.isAir()) layerComplete=false;
                }
                if(!layerComplete) break;
            }
            if(!layerComplete) return y;
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
