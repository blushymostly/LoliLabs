package tmr.lolilabs;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.resources.ResourceLocation;

public final class LabItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LoliLabs.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.f_279569_, LoliLabs.MOD_ID);
    public static final Map<String, RegistryObject<Item>> CATALOG = new LinkedHashMap<>();

    static {
        for (String id : LegacyMaterials.IDS) CATALOG.put(id, ITEMS.register(id, () -> new Item(new Item.Properties())));
        CATALOG.put("vajra",ITEMS.register("vajra",()->new LabMiningTool(false)));
        TABS.register("main", () -> CreativeModeTab.builder()
                .m_257941_(Component.m_237115_("itemGroup.loli_labs"))
                .m_257737_(() -> new ItemStack(CATALOG.get("sparkling_panel").get()))
                .m_257501_((parameters, output) -> {
                    CATALOG.values().forEach(item -> output.m_246326_(item.get()));
                }).m_257652_());
    }

    public static void register(IEventBus bus) { ITEMS.register(bus); TABS.register(bus); }
}
