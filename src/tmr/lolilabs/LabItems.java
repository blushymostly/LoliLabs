package tmr.lolilabs;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LabItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LoliLabs.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.f_279569_,
            LoliLabs.MOD_ID);
    public static final Map<String, RegistryObject<Item>> CATALOG = new LinkedHashMap<>();

    static {
        for (String id : LegacyMaterials.IDS)
            CATALOG.put(id, ITEMS.register(id, () -> new Item(new Item.Properties())));
        for (String id : LegacyMagically.ITEM_IDS)
            CATALOG.put(id, ITEMS.register(id, () -> new Item(new Item.Properties())));
        LegacyMagically.registerBlockItems();
        tmr.lolilabs.botania.LabBotaniaBlocks.registerBlockItems();
        tmr.lolilabs.utility.LabUtility.registerItems();
        tmr.lolilabs.dimensions.LabDimensions.registerItems();
        for (var material : LabMaterialColors.MATERIALS.values()) {
            if (!CATALOG.containsKey(material.id())) {
                CATALOG.put(material.id(), ITEMS.register(material.id(), () -> new Item(new Item.Properties())));
            }
        }
        CATALOG.put("vajra", ITEMS.register("vajra", () -> new LabMiningTool(false)));
        CATALOG.put("legendary_vajra", ITEMS.register("legendary_vajra", () -> new LabMiningTool(true)));
        CATALOG.put("infinity_pickaxe_ultra", ITEMS.register("infinity_pickaxe_ultra", () -> new LabMiningTool(false)));
        LegacyDemonic.register();
        TABS.register("main", () -> CreativeModeTab.builder()
                .m_257941_(Component.m_237115_("itemGroup.loli_labs"))
                .m_257737_(() -> new ItemStack(CATALOG.get("sparkling_panel").get()))
                .m_257501_((parameters, output) -> CATALOG.forEach((id, item) -> output.m_246326_(item.get())))
                .m_257652_());
    }

    public static void register(IEventBus bus) {
        LegacyMagically.register(bus);
        tmr.lolilabs.botania.LabBotaniaBlocks.register(bus);
        tmr.lolilabs.utility.LabUtility.register(bus);
        tmr.lolilabs.dimensions.LabDimensions.register(bus);
        LabMaterialBlocks.register(bus);
        ITEMS.register(bus);
        TABS.register(bus);
    }
}
