package tmr.lolilabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import org.zeith.solarflux.block.SolarPanelBlock;
import org.zeith.solarflux.client.screen.SolarPanelScreen;

public final class SparklingClient {

    public static final ResourceLocation PANEL = new ResourceLocation("solarflux", "sp_custom_sparkling");

    @Mod.EventBusSubscriber(modid = LoliLabs.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Models {

        @SubscribeEvent
        public static void register(ModelEvent.RegisterAdditional event) {
            event.register(new ResourceLocation(LoliLabs.MOD_ID, "block/sparkling_textures"));
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void bake(ModelEvent.ModifyBakingResult event) {
            if (ForgeRegistries.BLOCKS.getValue(PANEL) instanceof SolarPanelBlock block) {
                SparklingModel model = new SparklingModel(block);
                event.getModels().put(new ModelResourceLocation(PANEL, ""), model);
                event.getModels().put(new ModelResourceLocation(PANEL, "inventory"), model);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = LoliLabs.MOD_ID, value = Dist.CLIENT)
    public static final class Screens {

        @SubscribeEvent
        public static void open(ScreenEvent.Opening event) {
            if (event.getNewScreen() instanceof SolarPanelScreen screen && !(screen instanceof SparklingScreen) &&
                    PANEL.equals(ForgeRegistries.BLOCKS.getKey(screen.solar.m_58900_().m_60734_()))) {
                event.setNewScreen(new SparklingScreen(screen.m_6262_(),
                        Minecraft.m_91087_().f_91074_.m_150109_(), screen.m_96636_()));
            }
        }
    }
}
