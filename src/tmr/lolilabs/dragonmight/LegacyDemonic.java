package tmr.lolilabs.dragonmight;

import tmr.lolilabs.api.ItemCatalog;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public final class LegacyDemonic {

    private LegacyDemonic() {}

    public static void register() {
        ItemCatalog.CATALOG.put("demonic_infinity_helmet", ItemCatalog.ITEMS.register("demonic_infinity_helmet",
                () -> new LabPlainArmor(ArmorItem.Type.HELMET, "demonic_infinity")));
        ItemCatalog.CATALOG.put("demonic_infinity_chestplate", ItemCatalog.ITEMS.register("demonic_infinity_chestplate",
                () -> new LabPlainArmor(ArmorItem.Type.CHESTPLATE, "demonic_infinity")));
        ItemCatalog.CATALOG.put("demonic_infinity_leggings", ItemCatalog.ITEMS.register("demonic_infinity_leggings",
                () -> new LabPlainArmor(ArmorItem.Type.LEGGINGS, "demonic_infinity")));
        ItemCatalog.CATALOG.put("demonic_infinity_boots", ItemCatalog.ITEMS.register("demonic_infinity_boots",
                () -> new LabPlainArmor(ArmorItem.Type.BOOTS, "demonic_infinity")));
        ItemCatalog.CATALOG.put("demonic_infinity_sword", ItemCatalog.ITEMS.register("demonic_infinity_sword",
                () -> new SwordItem(Tiers.NETHERITE, 20, -2.4F, new Item.Properties().m_41486_())));
    }
}
