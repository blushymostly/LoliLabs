package tmr.lolilabs.botania;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tmr.lolilabs.LoliLabs;

@Mod.EventBusSubscriber(modid = LoliLabs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LabBotaniaClient {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(LabBotaniaBlocks.MANAGLASS.get(), RenderType.m_110463_());
            for (LabBotaniaTiers.Tier tier : LabBotaniaTiers.Tier.values()) {
                ItemBlockRenderTypes.setRenderLayer(LabBotaniaBlocks.BLOCK_CATALOG.get(tier.poolId()).get(), RenderType.m_110463_());
            }
        });
    }

    @SubscribeEvent public static void renderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(LabBotaniaBlocks.GREENHOUSE_ENTITY.get(),context->new GreenhouseRenderer());
        event.registerBlockEntityRenderer(LabBotaniaBlocks.POOL_ENTITY.get(),LabManaPoolRenderer::new);
        event.registerBlockEntityRenderer(LabBotaniaBlocks.SPREADER_ENTITY.get(),context->new LabManaSpreaderRenderer());
        LabBotaniaBlocks.SPARK_TYPES.values().forEach(type->event.registerEntityRenderer(type.get(),LabStraySparkRenderer::new));
    }
    @Mod.EventBusSubscriber(modid=LoliLabs.MOD_ID,value=Dist.CLIENT)
    public static final class SpreaderHud {
        @SubscribeEvent public static void attach(net.minecraftforge.event.AttachCapabilitiesEvent<net.minecraft.world.level.block.entity.BlockEntity> event){
            if(event.getObject() instanceof LabManaPoolBlockEntity pool)event.addCapability(new net.minecraft.resources.ResourceLocation(LoliLabs.MOD_ID,"pool_hud"),LabBotaniaCapabilities.provider(vazkii.botania.api.BotaniaForgeClientCapabilities.WAND_HUD,new vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity.WandHud(pool)));
            if(event.getObject() instanceof LabManaSpreaderBlockEntity spreader)event.addCapability(new net.minecraft.resources.ResourceLocation(LoliLabs.MOD_ID,"spreader_hud"),LabBotaniaCapabilities.provider(vazkii.botania.api.BotaniaForgeClientCapabilities.WAND_HUD,new vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity.WandHud(spreader)));
        }
    }
    private LabBotaniaClient() {}
}
