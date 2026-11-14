package tmr.lolilabs.magically;

import tmr.lolilabs.LoliLabs;
import tmr.lolilabs.api.ItemCatalog;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

/** LoliMagically 1.7.10 legacy items/blocks (phase 1 stubs). */
public final class LegacyMagically {

    public static final String[] ITEM_IDS = {
            "air_filled_dust",
            "chaos_filled_dust",
            "earth_filled_dust",
            "fire_filled_dust",
            "order_filled_dust",
            "water_filled_dust",
            "dragon_piece",
            "adamantium_scrap",
            "adamantium_ingot",
            "adamantium_arcane_ingot",
            "adamantium_dragon_ingot",
            "adamantium_ethereal_ingot",
            "adamantium_ghost_ingot",
            "star_crystal",
            "star_crystal_aer",
            "star_crystal_aqua",
            "star_crystal_ignis",
            "star_crystal_praecantatio",
            "star_crystal_terra",
            "straw_doll",
            "witch_doll",
            "sister_necromancer_bergamma_doll",
            "sister_necromancer_kella_doll",
            "innos_whole_rune",
            "innos_part_rune_1",
            "innos_part_rune_2",
            "innos_part_rune_3",
            "innos_part_rune_4",
            "innos_part_rune_5",
            "innos_medallion",
            "innos_ancient_artefact",
            "heart_foundation",
            "bat_soul",
            "ghost_soul",
            "soul_shard",
            "health_rune",
            "gaia_relic_base",
            "armor_scroll_upgrade_1",
            "armor_scroll_upgrade_2",
            "armor_scroll_upgrade_3",
            "armor_scroll_upgrade_4",
            "weapon_scroll_upgrade_1",
            "weapon_scroll_upgrade_2",
            "weapon_scroll_upgrade_3",
            "weapon_scroll_upgrade_4",
            "fire_runeball",
            "water_runeball",
            "adonas_scroll",
            "base_upgrade_scroll",
            "infused_air_scroll",
            "durability_rune",
            "time_rune",
            "fire_circle",
            "alchemical_ihor",
            "archimage_talisman",
            "cultist_medallion",
            "demon_notes",
            "emerald_coin",
            "flugel_aura",
            "flugel_killer",
            "flugel_soul_shard",
            "gaya_killer",
            "haos_shard",
            "nether_crystal",
            "sky_crystal",
            "skin_token",
            "stone_tablet",
            "strenght_cube_shard",
            "gaya_shard_1",
            "gaya_shard_2",
            "gaya_shard_3",
            "advanced_chaotic_core",
            "chaotic_flux_capacitor",
            "gaya_ingot_1",
            "gaya_ingot_2",
            "gaya_ingot_3",
            "case_gaya_2",
            "case_gaya_3",
            "case_gaya_4",
            "asmodeus_crystal",
            "beelzebub_crystal",
            "belphegor_crystal",
            "demon_crystal",
            "leviathan_crystal",
            "lucifer_crystal",
            "advanced_elemental_hoe",
            "advanced_elemental_pickaxe",
            "advanced_elemental_shovel",
            "advanced_elemental_sword",
            "advanced_primal_crusher",
            "chaos_sword",
            "manulin_axe",
            "manulin_shovel",
            "manulin_ichor_pickaxe",
            "manulin_ichor_sword",
            "manulin_gem_helmet",
            "manulin_gem_chestplate",
            "manulin_gem_leggings",
            "manulin_gem_boots",
            "sacrificial_dagger_armok",
            "sacrificial_dagger_armok_mask",
            "teleportation_sigil_armok",
            "magical_eye",
            "upgrade_lifestone_generator",
            "research_book",
            "auto_pocket_wardrobe",
            "magical_scroll_give",
            "magical_scroll_clear",
            "awakened_ritual_diviner",
            "research_scroll",
            "disenchantment_scroll",
            "enchantment_scroll",
            "talisman_hidden_riches"
    };
    public static final String[] BLOCK_IDS = {
            "adamantium",
            "adamantium_ethereal",
            "adamantium_ghost",
            "adamantium_arcane",
            "adamantium_dragon"
    };
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            LoliLabs.MOD_ID);
    public static final Map<String, RegistryObject<Block>> BLOCK_CATALOG = new LinkedHashMap<>();

    static {
        for (String id : BLOCK_IDS) {
            BLOCK_CATALOG.put(id,
                    BLOCKS.register(id, () -> new Block(BlockBehaviour.Properties.m_284310_().m_60913_(5F, 12F))));
        }
    }

    public static void registerBlockItems() {
        for (var entry : BLOCK_CATALOG.entrySet()) {
            String id = entry.getKey();
            RegistryObject<Block> block = entry.getValue();
            ItemCatalog.CATALOG.put(id,
                    ItemCatalog.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties())));
        }
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

    public static boolean containsItem(String id) {
        for (String known : ITEM_IDS) if (known.equals(id)) return true;
        return false;
    }

    private LegacyMagically() {}
}
