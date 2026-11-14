package tmr.lolilabs;

import tmr.lolilabs.api.ItemCatalog;
import tmr.lolilabs.dimensions.LabDimensions;
import tmr.lolilabs.dragonmight.LegacyDemonic;
import tmr.lolilabs.energistics.LabConfigMigration;
import tmr.lolilabs.energistics.LabMachines;
import tmr.lolilabs.energistics.LabMaterialBlocks;
import tmr.lolilabs.energistics.LabMaterialColors;
import tmr.lolilabs.energistics.LabMiningTool;
import tmr.lolilabs.energistics.LegacyMaterials;
import tmr.lolilabs.magically.LabBotaniaBlocks;
import tmr.lolilabs.magically.LegacyMagically;
import tmr.lolilabs.utility.LabUtility;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/** Domain registration order — mirrors a thin CommonProxy.init pipeline. */
public final class LabBootstrap {

    private LabBootstrap() {}

    public static void init(IEventBus bus) {
        populateCatalog();
        LegacyMagically.register(bus);
        LabBotaniaBlocks.register(bus);
        LabUtility.register(bus);
        LabDimensions.register(bus);
        LabMaterialBlocks.register(bus);
        LabMachines.register(bus);
        ItemCatalog.register(bus);
        LabConfigMigration.run();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LabMachines.SPEC);
    }

    private static void populateCatalog() {
        for (String id : LegacyMaterials.IDS)
            ItemCatalog.CATALOG.put(id, ItemCatalog.ITEMS.register(id, () -> new Item(new Item.Properties())));
        for (String id : LegacyMagically.ITEM_IDS)
            ItemCatalog.CATALOG.put(id, ItemCatalog.ITEMS.register(id, () -> new Item(new Item.Properties())));
        LegacyMagically.registerBlockItems();
        LabBotaniaBlocks.registerBlockItems();
        LabUtility.registerItems();
        LabDimensions.registerItems();
        for (var material : LabMaterialColors.MATERIALS.values()) {
            if (!ItemCatalog.CATALOG.containsKey(material.id())) {
                ItemCatalog.CATALOG.put(material.id(),
                        ItemCatalog.ITEMS.register(material.id(), () -> new Item(new Item.Properties())));
            }
        }
        ItemCatalog.CATALOG.put("vajra", ItemCatalog.ITEMS.register("vajra", () -> new LabMiningTool(false)));
        ItemCatalog.CATALOG.put("legendary_vajra",
                ItemCatalog.ITEMS.register("legendary_vajra", () -> new LabMiningTool(true)));
        ItemCatalog.CATALOG.put("infinity_pickaxe_ultra",
                ItemCatalog.ITEMS.register("infinity_pickaxe_ultra", () -> new LabMiningTool(false)));
        LegacyDemonic.register();
        ItemCatalog.TABS.register("main", () -> CreativeModeTab.builder()
                .m_257941_(Component.m_237115_("itemGroup.loli_labs"))
                .m_257737_(() -> new ItemStack(ItemCatalog.CATALOG.get("sparkling_panel").get()))
                .m_257501_(
                        (parameters, output) -> ItemCatalog.CATALOG.forEach((id, item) -> output.m_246326_(item.get())))
                .m_257652_());
    }
}
