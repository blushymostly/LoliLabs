package tmr.lolilabs;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod(LoliLabs.MOD_ID)
public final class LoliLabs {

    public static final String MOD_ID = "loli_labs";
    private static final Logger LOGGER = LogUtils.getLogger();

    public LoliLabs() {
        var bus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        LabItems.register(bus);
        LabMachines.register(bus);
        LabConfigMigration.run();
        net.minecraftforge.fml.ModLoadingContext.get()
                .registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, LabMachines.SPEC);
        bus.addListener(this::verifyMaterials);
    }

    private void verifyMaterials(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MOD_ID, "rose_gold_ingot"));
            if (item == null || item == net.minecraft.world.item.Items.f_41852_) {
                throw new IllegalStateException(
                        "Loli Labs material missing from registry: " + MOD_ID + ":rose_gold_ingot");
            }
            if (LabItems.CATALOG.containsKey("ingot_rose_gold") ||
                    LabItems.CATALOG.containsKey("reinforced_iridium_iron_plate")) {
                throw new IllegalStateException("Legacy duplicate material ids must not be registered");
            }
            LOGGER.info("LOLI_LABS_MATERIAL_OK {} (catalog={}, creative tab includes rose gold)",
                    ForgeRegistries.ITEMS.getKey(item), LabItems.CATALOG.size());
        });
    }
}
