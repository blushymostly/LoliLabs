package tmr.lolilabs.botania;

import tmr.lolilabs.LoliLabs;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** Uses the original Bedrock geometry, UVs, textures and animation clips. */
public final class GreenhouseRenderer implements BlockEntityRenderer<GreenhouseBlockEntity> {

    private final GeoBlockRenderer<GreenhouseBlockEntity> delegate = new GeoBlockRenderer<>(new GeoModel<>() {

        public ResourceLocation getModelResource(GreenhouseBlockEntity be) {
            return resource("geo/block/multi_greenhouse/multi_greenhouse.geo.json");
        }

        public ResourceLocation getTextureResource(GreenhouseBlockEntity be) {
            return resource(
                    "textures/models/block/multi_greenhouse/multi_greenhouse" + (be.enabled ? "_active" : "") + ".png");
        }

        public ResourceLocation getAnimationResource(GreenhouseBlockEntity be) {
            return resource("animations/block/multi_greenhouse.animation.json");
        }
    });

    public void verifyAssets(GreenhouseBlockEntity be) {
        var model = delegate.getGeoModel();
        if (model.getBakedModel(model.getModelResource(be)) == null ||
                model.getAnimation(be, "animation.model.loop") == null)
            throw new IllegalStateException("OG greenhouse model/animation not baked");
    }

    public static int exteriorLight(GreenhouseBlockEntity be, int fallback) {
        if (be.labLevel() == null) return fallback;
        int block = fallback & 65535, sky = fallback >>> 16;
        int[][] offsets = { { 0, 2, 0 }, { -2, 0, 0 }, { 2, 0, 0 }, { 0, 0, -2 }, { 0, 0, 2 } };
        for (var offset : offsets) {
            int sample = net.minecraft.client.renderer.LevelRenderer.m_109541_(be.labLevel(),
                    be.labPos().m_7918_(offset[0], offset[1], offset[2]));
            block = Math.max(block, sample & 65535);
            sky = Math.max(sky, sample >>> 16);
        }
        return block | (sky << 16);
    }

    private static ResourceLocation resource(String path) {
        return new ResourceLocation(LoliLabs.MOD_ID, path);
    }

    @Override
    public void m_6922_(GreenhouseBlockEntity be, float partial, com.mojang.blaze3d.vertex.PoseStack pose,
                        net.minecraft.client.renderer.MultiBufferSource buffers, int light, int overlay) {
        if (!be.isFormed()) return;
        pose.m_85836_();
        try {
            pose.m_85837_(0, -1, 0);
            delegate.m_6922_(be, partial, pose, buffers, exteriorLight(be, light), overlay);
            renderPorts(pose, buffers, exteriorLight(be, light), overlay);
            renderFlowers(be, pose, buffers, exteriorLight(be, light), overlay);
        } finally {
            pose.m_85849_();
        }
    }

    public static int visibleFlowers(GreenhouseBlockEntity be) {
        int count = 0;
        for (int i = 0; i < 8; i++)
            if (be.inventory.getStackInSlot(i).m_41720_() instanceof net.minecraft.world.item.BlockItem) count++;
        return count;
    }

    private static void renderFlowers(GreenhouseBlockEntity be, com.mojang.blaze3d.vertex.PoseStack pose,
                                      net.minecraft.client.renderer.MultiBufferSource buffers, int light, int overlay) {
        int[][] positions = { { -1, -1 }, { 0, -1 }, { 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 } };
        for (int i = 0; i < 8; i++) {
            var stack = be.inventory.getStackInSlot(i);
            if (!(stack.m_41720_() instanceof net.minecraft.world.item.BlockItem item)) continue;
            pose.m_85836_();
            try {
                pose.m_85837_(positions[i][0], 1.001, positions[i][1]);
                net.minecraft.client.Minecraft.m_91087_().m_91289_().m_110912_(item.m_40614_().m_49966_(), pose,
                        buffers, light, overlay);
            } finally {
                pose.m_85849_();
            }
        }
    }

    /** The OG center opening is completed by a separate 12x12 slot plane on each side. */
    private static void renderPorts(com.mojang.blaze3d.vertex.PoseStack pose,
                                    net.minecraft.client.renderer.MultiBufferSource buffers, int light, int overlay) {
        var out = buffers.m_6299_(net.minecraft.client.renderer.RenderType
                .m_110458_(resource("textures/models/block/multi_greenhouse/multi_greenhouse_slot_empty.png")));
        pose.m_85836_();
        try {
            pose.m_85837_(.5, 0, .5);
            for (var face : new net.minecraft.core.Direction[] { net.minecraft.core.Direction.NORTH,
                    net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.WEST,
                    net.minecraft.core.Direction.EAST }) {
                float nx = face.m_122429_(), nz = face.m_122431_(), tx = nz * .375F, tz = -nx * .375F, cx = nx * 1.501F,
                        cz = nz * 1.501F;
                portVertex(pose, out, cx - tx, .125F, cz - tz, 0, .375F, nx, nz, light, overlay);
                portVertex(pose, out, cx + tx, .125F, cz + tz, .375F, .375F, nx, nz, light, overlay);
                portVertex(pose, out, cx + tx, .875F, cz + tz, .375F, 0, nx, nz, light, overlay);
                portVertex(pose, out, cx - tx, .875F, cz - tz, 0, 0, nx, nz, light, overlay);
            }
        } finally {
            pose.m_85849_();
        }
    }

    private static void portVertex(com.mojang.blaze3d.vertex.PoseStack pose,
                                   com.mojang.blaze3d.vertex.VertexConsumer out, float x, float y, float z, float u,
                                   float v, float nx, float nz, int light, int overlay) {
        out.m_252986_(pose.m_85850_().m_252922_(), x, y, z).m_6122_(255, 255, 255, 255).m_7421_(u, v).m_86008_(overlay)
                .m_85969_(light)
                .m_252939_(pose.m_85850_().m_252943_(), nx, 0, nz).m_5752_();
    }

    @Override
    public boolean m_5932_(GreenhouseBlockEntity be) {
        return true;
    }
}
