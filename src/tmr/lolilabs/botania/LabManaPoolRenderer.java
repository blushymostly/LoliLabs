package tmr.lolilabs.botania;

/** The shell remains the original baked model; Botania draws its synchronized mana surface. */
public final class LabManaPoolRenderer implements
                                       net.minecraft.client.renderer.blockentity.BlockEntityRenderer<LabManaPoolBlockEntity> {

    private final vazkii.botania.client.render.block_entity.ManaPoolBlockEntityRenderer delegate;

    public LabManaPoolRenderer(net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context context) {
        delegate = new vazkii.botania.client.render.block_entity.ManaPoolBlockEntityRenderer(context);
    }

    @Override
    public void m_6922_(LabManaPoolBlockEntity be, float partial, com.mojang.blaze3d.vertex.PoseStack pose,
                        net.minecraft.client.renderer.MultiBufferSource buffers, int light, int overlay) {
        delegate.m_6922_(be, partial, pose, buffers, light, overlay);
    }
}
