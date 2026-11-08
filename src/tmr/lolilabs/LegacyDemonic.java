package tmr.lolilabs;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public final class LegacyDemonic {

    private LegacyDemonic() {}

    public static void register() {
        LabItems.CATALOG.put("demonic_infinity_helmet", LabItems.ITEMS.register("demonic_infinity_helmet",
                () -> new LabPlainArmor(ArmorItem.Type.HELMET, "demonic_infinity")));
        LabItems.CATALOG.put("demonic_infinity_chestplate", LabItems.ITEMS.register("demonic_infinity_chestplate",
                () -> new LabPlainArmor(ArmorItem.Type.CHESTPLATE, "demonic_infinity")));
        LabItems.CATALOG.put("demonic_infinity_leggings", LabItems.ITEMS.register("demonic_infinity_leggings",
                () -> new LabPlainArmor(ArmorItem.Type.LEGGINGS, "demonic_infinity")));
        LabItems.CATALOG.put("demonic_infinity_boots", LabItems.ITEMS.register("demonic_infinity_boots",
                () -> new LabPlainArmor(ArmorItem.Type.BOOTS, "demonic_infinity")));
        LabItems.CATALOG.put("demonic_infinity_sword", LabItems.ITEMS.register("demonic_infinity_sword",
                () -> new SwordItem(Tiers.NETHERITE, 20, -2.4F, new Item.Properties().m_41486_())));
    }
}
