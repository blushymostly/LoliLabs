package tmr.lolilabs.botania;

/** Botania renders the animation, network color and upgrades; only the base sprite differs. */
public final class LabStraySparkRenderer extends vazkii.botania.client.render.entity.ManaSparkRenderer {

    public LabStraySparkRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected net.minecraft.client.renderer.texture.TextureAtlasSprite getBaseIcon(vazkii.botania.common.entity.ManaSparkEntity entity) {
        if (entity instanceof LabStraySparkEntity spark) {
            return net.minecraft.client.Minecraft.m_91087_()
                    .m_91258_(net.minecraft.world.inventory.InventoryMenu.f_39692_)
                    .apply(new net.minecraft.resources.ResourceLocation("loli_labs",
                            "item/magically/sparks/" + spark.tier.sparkId()));
        }
        return super.getBaseIcon(entity);
    }
}
