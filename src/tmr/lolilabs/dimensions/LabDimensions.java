package tmr.lolilabs.dimensions;

import tmr.lolilabs.LoliLabs;
import tmr.lolilabs.api.ItemCatalog;

import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.*;

import com.google.gson.*;

public final class LabDimensions {

    public record Definition(String id, String ogId, String kind, String original, String sha256, String ru, String en,
                             boolean glint, boolean reused, String drop, String base, String primary, String secondary,
                             boolean nether) {}

    public static final java.util.List<Definition> DEFINITIONS = load();
    public static final java.util.Map<String, RegistryObject<Block>> BLOCK_CATALOG = new java.util.LinkedHashMap<>();
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            LoliLabs.MOD_ID);

    private static java.util.List<Definition> load() {
        try (var in = LabDimensions.class.getResourceAsStream("/assets/loli_labs/dimensions.json")) {
            return java.util.List
                    .of(new Gson().fromJson(new java.io.InputStreamReader(java.util.Objects.requireNonNull(in),
                            java.nio.charset.StandardCharsets.UTF_8), Definition[].class));
        } catch (java.io.IOException error) {
            throw new IllegalStateException(error);
        }
    }

    public static int addedCount() {
        return (int) DEFINITIONS.stream().filter(d -> !d.reused()).count();
    }

    public static boolean contains(String id) {
        return DEFINITIONS.stream().anyMatch(d -> d.id().equals(id));
    }

    public static void registerItems() {
        for (var def : DEFINITIONS) {
            if (def.reused()) {
                if (!ItemCatalog.CATALOG.containsKey(def.id()))
                    throw new IllegalStateException("Missing reused material " + def.id());
                continue;
            }
            if (!def.kind().equals("item")) {
                var block = BLOCKS.register(def.id(),
                        () -> def.kind().equals("ore") ?
                                new com.ic2ceu.icceu.material.MaterialOreBlock(Integer.parseInt(def.primary(), 16),
                                        Integer.parseInt(def.secondary(), 16), def.nether()) :
                                new Block(BlockBehaviour.Properties.m_284310_().m_60913_(5, 30).m_60999_()));
                BLOCK_CATALOG.put(def.id(), block);
                ItemCatalog.CATALOG.put(def.id(),
                        ItemCatalog.ITEMS.register(def.id(), () -> new BlockItem(block.get(), new Item.Properties())));
            } else ItemCatalog.CATALOG.put(def.id(), ItemCatalog.ITEMS.register(def.id(),
                    () -> def.glint() ? new EnchantedMaterial() : new Item(new Item.Properties())));
        }
    }

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        BLOCKS.register(bus);
    }

    public static final class EnchantedMaterial extends Item {

        public EnchantedMaterial() {
            super(new Properties());
        }

        @Override
        public boolean m_5812_(ItemStack stack) {
            return true;
        }
    }
}
