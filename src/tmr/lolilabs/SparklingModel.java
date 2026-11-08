package tmr.lolilabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import org.joml.Vector3f;
import org.zeith.solarflux.block.SolarPanelBlock;
import org.zeith.solarflux.client.SolarPanelBakedModel;

import java.util.ArrayList;
import java.util.List;

public final class SparklingModel extends SolarPanelBakedModel {

    public SparklingModel(SolarPanelBlock block) {
        super(block);
    }

    private TextureAtlasSprite texture(String face) {
        return Minecraft.m_91087_().m_91258_(InventoryMenu.f_39692_)
                .apply(new ResourceLocation(LoliLabs.MOD_ID, "block/sparkling_panel_" + face));
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, RandomSource random,
                                    ModelData modelData, RenderType renderType) {
        List<BakedQuad> result = new ArrayList<>();
        for (Direction direction : face == null ? Direction.values() : new Direction[] { face }) {
            String surface = direction == Direction.UP ? "top" : direction == Direction.DOWN ? "bottom" : "side";
            result.add(quad(new Vector3f(0, 0, 0), new Vector3f(16, 16, 16),
                    new float[] { 0, 0, 16, 16 }, 0, texture(surface), direction));
        }
        return result;
    }

    @Override
    public boolean m_7539_() {
        return true;
    }

    @Override
    public TextureAtlasSprite t_base() {
        return texture("side");
    }

    @Override
    public TextureAtlasSprite t_top() {
        return texture("top");
    }
}
