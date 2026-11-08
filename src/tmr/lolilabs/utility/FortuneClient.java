package tmr.lolilabs.utility;

import tmr.lolilabs.LoliLabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.vertex.PoseStack;

@Mod.EventBusSubscriber(modid = LoliLabs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FortuneClient implements BlockEntityRenderer<FortuneEntity> {

    public static final ResourceLocation CORE = new ResourceLocation(LoliLabs.MOD_ID, "block/utility/fortune_core");
    public static net.minecraft.client.resources.model.BakedModel coreModel;

    @SubscribeEvent
    public static void baked(ModelEvent.BakingCompleted event) {
        coreModel = event.getModels().get(CORE);
    }

    public static int renderedFrames;
    public static float lastAngle;

    public static float rotation(long ticks, float partial) {
        return -((ticks % 144) + partial) * 2.5F;
    }

    @SubscribeEvent
    public static void models(ModelEvent.RegisterAdditional event) {
        event.register(CORE);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(LabUtility.ENTITY.get(), ctx -> new FortuneClient());
    }

    @Override
    public void m_6922_(FortuneEntity be, float partial, PoseStack pose, MultiBufferSource buffers, int light,
                        int overlay) {
        var mc = Minecraft.m_91087_();
        var model = coreModel;
        if (model == null) return;
        float angle = rotation(be.m_58904_().m_46467_(), partial);
        if (Boolean.getBoolean("loli_labs.verifyWorld")) {
            renderedFrames++;
            lastAngle = angle;
        }
        // Use exterior light, never a full-bright packed light or OBJ ambient emission.
        int block = light & 65535, sky = light >>> 16;
        for (var side : net.minecraft.core.Direction.values()) {
            int sample = LevelRenderer.m_109541_(be.m_58904_(), be.m_58899_().m_121945_(side));
            block = Math.max(block, sample & 65535);
            sky = Math.max(sky, sample >>> 16);
        }
        pose.m_85836_();
        try {
            pose.m_85837_(.5, 0, .5);
            pose.m_252781_(vazkii.botania.common.helper.VecHelper.rotateY(angle));
            pose.m_85837_(-.5, 0, -.5);
            mc.m_91289_().m_110937_().m_111067_(pose.m_85850_(), buffers.m_6299_(RenderType.m_110457_()), be.m_58900_(),
                    model, 1, 1, 1, block | (sky << 16), overlay);
        } finally {
            pose.m_85849_();
        }
    }
}
