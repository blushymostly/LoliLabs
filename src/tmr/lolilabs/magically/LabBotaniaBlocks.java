package tmr.lolilabs.magically;

import tmr.lolilabs.LoliLabs;
import tmr.lolilabs.api.ItemCatalog;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LabBotaniaBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            LoliLabs.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, LoliLabs.MOD_ID);
    public static final DeferredRegister<net.minecraft.world.entity.EntityType<?>> SPARK_ENTITIES = DeferredRegister
            .create(ForgeRegistries.ENTITY_TYPES, LoliLabs.MOD_ID);
    public static final Map<LabBotaniaTiers.Tier, RegistryObject<net.minecraft.world.entity.EntityType<LabStraySparkEntity>>> SPARK_TYPES = new java.util.EnumMap<>(
            LabBotaniaTiers.Tier.class);
    public static final Map<String, RegistryObject<Block>> BLOCK_CATALOG = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> ITEM_CATALOG = new LinkedHashMap<>();

    public static final RegistryObject<Block> MULTI_GREENHOUSE_CORE = block("multi_greenhouse_core",
            GreenhouseCoreBlock::new);
    public static final RegistryObject<Block> MULTI_GREENHOUSE_BASE = part("multi_greenhouse_base",
            GreenhouseStructure.Role.BASE,
            BlockBehaviour.Properties.m_284310_().m_60913_(5F, 20F));
    public static final RegistryObject<Block> DARK_MANABRICKS = part("dark_manabricks",
            GreenhouseStructure.Role.MANABRICKS,
            BlockBehaviour.Properties.m_284310_().m_60913_(5F, 20F));
    public static final RegistryObject<Block> MANAGLASS = part("managlass", GreenhouseStructure.Role.MANAGLASS,
            BlockBehaviour.Properties.m_284310_().m_60913_(0.3F, 0.3F)
                    .m_60918_(net.minecraft.world.level.block.SoundType.f_56744_).m_60955_()
                    .m_60982_((s, g, p) -> false).m_60991_((s, g, p) -> false));

    public static final RegistryObject<BlockEntityType<GreenhouseBlockEntity>> GREENHOUSE_ENTITY = ENTITIES
            .register("multi_greenhouse", LabBotaniaBlocks::greenhouseType);
    public static final RegistryObject<BlockEntityType<LabManaPoolBlockEntity>> POOL_ENTITY = ENTITIES
            .register("mana_pool", LabBotaniaBlocks::poolType);
    public static final RegistryObject<BlockEntityType<LabManaSpreaderBlockEntity>> SPREADER_ENTITY = ENTITIES
            .register("mana_spreader", LabBotaniaBlocks::spreaderType);

    public static final RegistryObject<Item> CRYSTALLIZED_MANA = item("crystallized_mana");
    public static final RegistryObject<Item> CRYSTALLIZED_MANA_CLUSTER = item("crystallized_mana_cluster");
    public static final RegistryObject<Item> MANULIN_CLOTH = item("manulin_cloth");
    public static final RegistryObject<Item> MANULIN_ICHOR = item("manulin_ichor");
    public static final RegistryObject<Item> MANULIN_NITOR = item("manulin_nitor");

    static {
        for (LabBotaniaTiers.Tier tier : LabBotaniaTiers.Tier.values()) {
            SPARK_TYPES.put(tier,
                    SPARK_ENTITIES.register(tier.sparkId(), () -> net.minecraft.world.entity.EntityType.Builder
                            .<LabStraySparkEntity>m_20704_((type, level) -> new LabStraySparkEntity(type, level, tier),
                                    net.minecraft.world.entity.MobCategory.MISC)
                            .m_20699_(.5F, .5F).m_20702_(8).m_20717_(10)
                            .m_20712_(LoliLabs.MOD_ID + ":" + tier.sparkId())));
            block(tier.poolId(), () -> new LabManaPoolBlock(tier));
            block(tier.spreaderId(), () -> new LabManaSpreaderBlock(tier));
            ITEM_CATALOG.put(tier.sparkId(),
                    ItemCatalog.ITEMS.register(tier.sparkId(), () -> new LabStraySparkItem(tier)));
            ItemCatalog.CATALOG.put(tier.sparkId(), ITEM_CATALOG.get(tier.sparkId()));
        }
        for (GreenhouseUpgrade.Kind kind : GreenhouseUpgrade.Kind.values()) {
            if (kind.maxLevel == 1) {
                var id = kind.itemId();
                ITEM_CATALOG.put(id, ItemCatalog.ITEMS.register(id, () -> new GreenhouseUpgrade(kind, 1)));
                ItemCatalog.CATALOG.put(id, ITEM_CATALOG.get(id));
            } else {
                for (int level = 1; level <= kind.maxLevel; level++) {
                    String id = kind.itemId() + "_lvl_" + level;
                    int lvl = level;
                    ITEM_CATALOG.put(id, ItemCatalog.ITEMS.register(id, () -> new GreenhouseUpgrade(kind, lvl)));
                    ItemCatalog.CATALOG.put(id, ITEM_CATALOG.get(id));
                }
            }
        }
    }

    private static RegistryObject<Block> block(String id, java.util.function.Supplier<Block> supplier) {
        var reg = BLOCKS.register(id, supplier);
        BLOCK_CATALOG.put(id, reg);
        return reg;
    }

    private static RegistryObject<Block> part(String id, GreenhouseStructure.Role role,
                                              BlockBehaviour.Properties props) {
        return block(id, () -> new GreenhousePartBlock(role, props));
    }

    private static RegistryObject<Item> item(String id) {
        var reg = ItemCatalog.ITEMS.register(id, () -> new Item(new Item.Properties()));
        ITEM_CATALOG.put(id, reg);
        ItemCatalog.CATALOG.put(id, reg);
        return reg;
    }

    public static void registerBlockItems() {
        for (var entry : BLOCK_CATALOG.entrySet()) {
            String id = entry.getKey();
            if (ItemCatalog.CATALOG.containsKey(id)) continue;
            var block = entry.getValue();
            RegistryObject<Item> item = ItemCatalog.ITEMS.register(id,
                    () -> new BlockItem(block.get(), new Item.Properties()));
            ItemCatalog.CATALOG.put(id, item);
            ITEM_CATALOG.put(id, item);
        }
    }

    @SuppressWarnings("unchecked")
    private static BlockEntityType<GreenhouseBlockEntity> greenhouseType() {
        return entityType((pos, state) -> new GreenhouseBlockEntity(pos, state),
                MULTI_GREENHOUSE_CORE.get(), MULTI_GREENHOUSE_BASE.get(), DARK_MANABRICKS.get(), MANAGLASS.get());
    }

    @SuppressWarnings("unchecked")
    private static BlockEntityType<LabManaPoolBlockEntity> poolType() {
        var blocks = java.util.Arrays.stream(LabBotaniaTiers.Tier.values())
                .map(t -> BLOCK_CATALOG.get(t.poolId()).get()).toArray(Block[]::new);
        return entityType((pos, state) -> new LabManaPoolBlockEntity(pos, state), blocks);
    }

    @SuppressWarnings("unchecked")
    private static BlockEntityType<LabManaSpreaderBlockEntity> spreaderType() {
        var blocks = java.util.Arrays.stream(LabBotaniaTiers.Tier.values())
                .map(t -> BLOCK_CATALOG.get(t.spreaderId()).get()).toArray(Block[]::new);
        return entityType((pos, state) -> new LabManaSpreaderBlockEntity(pos, state), blocks);
    }

    @SuppressWarnings("unchecked")
    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityType<T> entityType(
                                                                                                                java.util.function.BiFunction<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, T> factory,
                                                                                                                Block... blocks) {
        try {
            Class<?> supplier = Class
                    .forName("net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier");
            Object proxy = java.lang.reflect.Proxy.newProxyInstance(supplier.getClassLoader(),
                    new Class<?>[] { supplier },
                    (p, method, args) -> factory.apply((net.minecraft.core.BlockPos) args[0],
                            (net.minecraft.world.level.block.state.BlockState) args[1]));
            var method = BlockEntityType.Builder.class.getDeclaredMethod("m_155273_", supplier, Block[].class);
            method.setAccessible(true);
            var builder = (BlockEntityType.Builder<T>) method.invoke(null, proxy, blocks);
            return builder.m_58966_(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot register Botania Labs block entity", e);
        }
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        SPARK_ENTITIES.register(bus);
        ENTITIES.register(bus);
    }

    private LabBotaniaBlocks() {}
}
