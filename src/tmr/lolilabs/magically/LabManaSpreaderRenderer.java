package tmr.lolilabs.magically;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.ItemDisplayContext;

import com.mojang.blaze3d.vertex.PoseStack;
import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.common.helper.VecHelper;

/** OG shell and core follow the native Botania firing angles, including arbitrary wand bindings. */
public final class LabManaSpreaderRenderer implements BlockEntityRenderer<LabManaSpreaderBlockEntity> {

    @Override
    public void m_6922_(LabManaSpreaderBlockEntity be, float partial, PoseStack pose, MultiBufferSource buffers,
                        int light, int overlay) {
        var mc = Minecraft.m_91087_();
        var state = be.m_58900_();
        var out = buffers.m_6299_(RenderType.m_110457_());
        pose.m_85836_();
        try {
            pose.m_85837_(.5, .5, .5);
            pose.m_252781_(VecHelper.rotateY(be.rotationX + 90).mul(VecHelper.rotateX(be.rotationY)));
            pose.m_85837_(-.5, -.5, -.5);
            mc.m_91289_().m_110937_().m_111067_(pose.m_85850_(), out, state, mc.m_91289_().m_110910_(state), 1, 1, 1,
                    light, overlay);
            var lens = be.getItemHandler().m_8020_(0);
            if (!lens.m_41619_()) {
                pose.m_85836_();
                try {
                    pose.m_85837_(.5, .5, .094);
                    pose.m_252781_(VecHelper.rotateZ(180));
                    pose.m_252781_(VecHelper.rotateX(180));
                    pose.m_85841_(.997F, .997F, 1);
                    mc.m_91291_().m_269128_(lens, ItemDisplayContext.NONE, light, overlay, pose, buffers, be.m_58904_(),
                            0);
                } finally {
                    pose.m_85849_();
                }
            }
            if (be.paddingColor != null) {
                pose.m_85836_();
                try {
                    pose.m_85837_(.5, .5, .5);
                    pose.m_252781_(VecHelper.rotateX(-90));
                    pose.m_252781_(VecHelper.rotateY(180));
                    pose.m_85837_(-.5, -.5, -.5);
                    mc.m_91289_().m_110937_().m_111067_(pose.m_85850_(), out, state,
                            MiscellaneousModels.INSTANCE.spreaderPaddings.get(be.paddingColor), 1, 1, 1, light,
                            overlay);
                } finally {
                    pose.m_85849_();
                }
            }
        } finally {
            pose.m_85849_();
        }
        if (state.m_61143_(vazkii.botania.api.state.BotaniaStateProperties.HAS_SCAFFOLDING))
            mc.m_91289_().m_110937_().m_111067_(pose.m_85850_(), out, state,
                    MiscellaneousModels.INSTANCE.gaiaSpreaderScaffolding, 1, 1, 1, light, overlay);
    }
}
