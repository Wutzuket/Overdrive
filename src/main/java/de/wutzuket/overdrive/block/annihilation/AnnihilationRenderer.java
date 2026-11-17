package de.wutzuket.overdrive.block.annihilation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class AnnihilationRenderer implements BlockEntityRenderer<AnnihilationControllerEntity> {
    // Plasma-Textur: nahtlose Adern, die mit dem energy_swirl-Shader ueber den Torus gescrollt werden.
    private static final ResourceLocation PLASMA_TEXTURE = ResourceLocation.fromNamespaceAndPath("overdrive", "textures/entity/plasma.png");
    // Aufloesung des Plasma-Netzes: Segmente um den Hauptring x Segmente um das Rohr.
    private static final int PLASMA_MAJOR_SEGMENTS = 72;
    private static final int PLASMA_MINOR_SEGMENTS = 12;
    // Wie oft die Plasma-Textur auf dem Ring bzw. auf dem Rohr wiederholt wird.
    private static final float PLASMA_U_REPEAT = 8f;
    private static final float PLASMA_V_REPEAT = 2f;

    public AnnihilationRenderer(BlockEntityRendererProvider.Context ctx){}
    @Override public boolean shouldRenderOffScreen(@NotNull AnnihilationControllerEntity be){ return true; }
    @Override public @NotNull AABB getRenderBoundingBox(@NotNull AnnihilationControllerEntity be){ return new AABB(be.getBlockPos()).inflate(30); }

    private boolean isValidWall(Block b, BlockPos pos, BlockPos ctrlPos, Level level){
        if(pos.equals(ctrlPos) && b instanceof AnnihilationControllerBlock) return true;
        // Core-Containment-Glas ersetzt das Heavy Casing, aber nur an flachen Wandstellen
        // (4 Seiten auf einer Ebene, gleiche Regel wie in der Strukturpruefung)
        // -> sonst gibt das Hologramm rot aus.
        if(b instanceof CoreContainmentGlassBlock) return AnnihilationControllerEntity.hasFlatWallNeighbors(level, pos);
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
        if(be.getLevel()==null) return;
        // Plasma-Ring immer zeichnen, unabhaengig vom Hologramm.
        renderPlasma(be, pose, buffer, partialTick);
        if(!be.isHologramEnabled()) return;
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
                    } else if(!isValidWall(b, check, ctrl, be.getLevel())){
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

    // ---------------------------------------------------------------------------------------
    // Plasma-Torus: kleinerer Torus aus einem Plasma-Netz im Hohlraum des Reaktor-Torus.
    // ---------------------------------------------------------------------------------------
    private void renderPlasma(AnnihilationControllerEntity be, PoseStack pose, MultiBufferSource buffer, float partialTick){
        if(!be.isValidStructure()) return;
        boolean active=be.isActive();
        // Temperatur -> Farbsaettigung und Sichtbarkeit: bei 0 Grad ist das Plasma unsichtbar,
        // bei 5000 Grad ist die Farbe voll gesaettigt.
        float heat=(float)Math.min(1.0, Math.max(0.0, be.getCoreTemp()/AnnihilationControllerEntity.CORE_TEMP_MAX));
        float load=active? (float)Math.min(1.0, be.getActualBurnRate()/50.0) : 0f;
        // Sichtbarkeit: ohne Hitze kein Plasma.
        float intensity=(active? (0.5f+0.5f*load) : 0.16f)*heat;
        if(intensity<=0.02f) return;

        double time=(be.getLevel().getGameTime()+partialTick)/20.0;
        // Der Pose-Stack ist bereits auf den Controller-Block verschoben -> lokale Koordinaten.
        BlockPos ctrl=be.getBlockPos();
        BlockPos center=AnnihilationControllerEntity.getCenter(ctrl, be.getBlockState());
        double cx=center.getX()-ctrl.getX()+0.5;
        double cy=center.getY()-ctrl.getY()+AnnihilationControllerEntity.PLASMA_HEIGHT;
        double cz=center.getZ()-ctrl.getZ()+0.5;
        // Plasma-Farbe: Hue 305 (Magenta), die Saettigung waechst mit der Temperatur.
        // Kalt ist das Plasma blass, bei 5000 Grad ist es voll gesättigt.
        float[] col=hsvToRgb(305f/360f, heat, 1f);
        float r=col[0], g=col[1], b=col[2];
        // Die Textur wird gescrollt, dadurch fliesst das Plasma ueber den Torus.
        // (Die UV laufen PLASMA_U_REPEAT mal um den Ring, der Offset verschiebt die Adern
        //  deshalb um mehrere Bloecke pro Sekunde.)
        float uOff=(float)((time*0.40)%1.0);
        float vOff=(float)((time*0.25)%1.0);
        VertexConsumer vc=buffer.getBuffer(RenderType.energySwirl(PLASMA_TEXTURE, uOff, vOff));
        // Groesse des Plasmas: das Volumen waechst linear zur Brennrate (mB/t), der
        // Rohr-Radius also quadratisch. Ab 40 B/t (40000 mB/t) ist das Plasma maximal
        // gross und waechst nicht weiter. Die Groesse folgt der eingestellten Brennrate,
        // die Helligkeit dem tatsaechlichen Zustand.
        double burn=be.getBurnRate();
        float tubeR;
        if(active){
            tubeR=(float)(AnnihilationControllerEntity.PLASMA_TUBE_FULL
                    *Math.sqrt(Math.min(burn, AnnihilationControllerEntity.PLASMA_FULL_BURN)
                               /AnnihilationControllerEntity.PLASMA_FULL_BURN));
        } else {
            tubeR=(float)Math.max(AnnihilationControllerEntity.PLASMA_TUBE_IDLE,
                    AnnihilationControllerEntity.PLASMA_TUBE_FULL
                    *Math.sqrt(Math.min(burn, AnnihilationControllerEntity.PLASMA_FULL_BURN)
                               /AnnihilationControllerEntity.PLASMA_FULL_BURN));
        }
        renderPlasmaTorus(vc, pose, cx, cy, cz,
                (float)AnnihilationControllerEntity.PLASMA_MAJOR, tubeR,
                r, g, b, intensity, time);
    }
    /** HSV -> RGB, damit die Saettigung des Plasmafarbs ueber die Temperatur steuert. */
    private static float[] hsvToRgb(float h, float s, float v){
        h=Math.min(1f, Math.max(0f, h))*6f;
        int i=(int)h;
        float f=h-i;
        float p=v*(1f-s), q=v*(1f-f*s), t=v*(1f-(1f-f)*s);
        switch(i%6){
            case 0: return new float[]{v,t,p};
            case 1: return new float[]{q,v,p};
            case 2: return new float[]{p,v,t};
            case 3: return new float[]{p,q,v};
            case 4: return new float[]{t,p,v};
            default: return new float[]{v,p,q};
        }
    }

    /** Zeichnet den Plasma-Torus als Netz aus Quads (RenderType energySwirl nutzt den QUADS-Modus). */
    private static void renderPlasmaTorus(VertexConsumer vc, PoseStack pose, double cx, double cy, double cz,
                                          float majorR, float tubeR, float r, float g, float b, float intensity, double time){
        for(int i=0;i<PLASMA_MAJOR_SEGMENTS;i++){
            double a0=2*Math.PI*i/PLASMA_MAJOR_SEGMENTS;
            double a1=2*Math.PI*(i+1)/PLASMA_MAJOR_SEGMENTS;
            float u0=(float)i/PLASMA_MAJOR_SEGMENTS*PLASMA_U_REPEAT;
            float u1=(float)(i+1)/PLASMA_MAJOR_SEGMENTS*PLASMA_U_REPEAT;
            for(int j=0;j<PLASMA_MINOR_SEGMENTS;j++){
                double b0=2*Math.PI*j/PLASMA_MINOR_SEGMENTS;
                double b1=2*Math.PI*(j+1)/PLASMA_MINOR_SEGMENTS;
                float v0=(float)j/PLASMA_MINOR_SEGMENTS*PLASMA_V_REPEAT;
                float v1=(float)(j+1)/PLASMA_MINOR_SEGMENTS*PLASMA_V_REPEAT;
                // Laufende Helligkeitswelle: das Plasma pulsiert beim Umkreisen.
                double wave=0.5+0.5*Math.sin(a0*3.0-time*2.2);
                float bright=intensity*(float)(0.45+0.55*wave);
                plasmaVertex(vc,pose,cx,cy,cz,a0,b0,majorR,tubeR,r,g,b,bright,u0,v0,time);
                plasmaVertex(vc,pose,cx,cy,cz,a1,b0,majorR,tubeR,r,g,b,bright,u1,v0,time);
                plasmaVertex(vc,pose,cx,cy,cz,a1,b1,majorR,tubeR,r,g,b,bright,u1,v1,time);
                plasmaVertex(vc,pose,cx,cy,cz,a0,b1,majorR,tubeR,r,g,b,bright,u0,v1,time);
            }
        }
    }
    private static void plasmaVertex(VertexConsumer vc, PoseStack pose, double cx, double cy, double cz,
                                     double a, double b, float majorR, float tubeR,
                                     float r, float g, float bl, float bright, float u, float v, double time){
        // Das Rohr wackelt leicht: das Plasma wirbelt wie im Fusionsreaktor.
        double lump=1.0+0.18*Math.sin(a*3.0+time*1.1)+0.10*Math.sin(b*2.0+a*4.0-time*0.8);
        double rad=tubeR*lump;
        double ring=majorR+rad*Math.cos(b);
        double x=cx+ring*Math.cos(a);
        double y=cy+rad*Math.sin(b);
        double z=cz+ring*Math.sin(a);
        // Die Oberseite des Rohrs ist heisser (weisslicher), die Unterseite kuehler.
        // Der Weissanteil bleibt klein, damit die Plasmafarbe saettig bleibt.
        float hot=0.5f+0.5f*(float)Math.sin(b);
        float cr=Math.min(1f, (r+(1f-r)*hot*0.3f)*bright);
        float cg=Math.min(1f, (g+(1f-g)*hot*0.3f)*bright);
        float cb=Math.min(1f, (bl+(1f-bl)*hot*0.3f)*bright);
        double nx=Math.cos(b)*Math.cos(a), ny=Math.sin(b), nz=Math.cos(b)*Math.sin(a);
        vc.addVertex(pose.last(), (float)x, (float)y, (float)z)
                .setColor(cr, cg, cb, 1f)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal((float)nx, (float)ny, (float)nz);
    }

    private int findBuildLayer(AnnihilationControllerEntity be,int cx,int cy,int cz,BlockPos ctrl){
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
                    if(st.isAir() || !isValidWall(b,p,ctrl,be.getLevel())) layerComplete=false;
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
