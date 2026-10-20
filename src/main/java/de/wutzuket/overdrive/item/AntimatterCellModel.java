package de.wutzuket.overdrive.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * The model of an antimatter cell. It has no quads at all, the icon is drawn completely by the
 * {@link AntimatterCellRenderer}. That way a new type of antimatter automatically gets a cell
 * without needing a model file.
 */
public class AntimatterCellModel implements BakedModel {

    public static final AntimatterCellModel INSTANCE = new AntimatterCellModel();

    private AntimatterCellModel() {
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
        return List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(ResourceLocation.withDefaultNamespace("missing"));
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
