package tmr.lolilabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
@Mod.EventBusSubscriber(modid=LoliLabs.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class LabFluidsClient {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(()->LabFluids.FLUIDS.getEntries().forEach(fluid->net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(fluid.get(),net.minecraft.client.renderer.RenderType.m_110466_())));
    }
}
