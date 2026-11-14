package tmr.lolilabs.utility;

import tmr.lolilabs.LoliLabs;
import tmr.lolilabs.api.ItemCatalog;

import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.*;

public final class LabUtility {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            LoliLabs.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, LoliLabs.MOD_ID);
    public static final RegistryObject<Block> FORTUNE = BLOCKS.register("fortune_transformer", FortuneBlock::new);
    public static final RegistryObject<BlockEntityType<FortuneEntity>> ENTITY = ENTITIES.register("fortune_transformer",
            LabUtility::entityType);
    public static final int[] LEVELS = { 3, 5, 10, 15, 20, 25, 30, 35, 40, 50, 100 };
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.LongValue CAPACITY, COST;
    public static final ForgeConfigSpec.IntValue TICKS;
    static {
        var b = new ForgeConfigSpec.Builder();
        CAPACITY = b.defineInRange("energyCapacityEU", 16777216L, 1L, 1000000000000L);
        COST = b.defineInRange("energyEUPerOre", 2048L, 1L, 100000000L);
        TICKS = b.defineInRange("operationTicks", 20, 1, 1200);
        SPEC = b.build();
    }

    public static void registerItems() {
        ItemCatalog.CATALOG.put("fortune_transformer", ItemCatalog.ITEMS.register("fortune_transformer",
                () -> new BlockItem(FORTUNE.get(), new Item.Properties())));
        for (int level : LEVELS) {
            String id = "fortune_module_" + level;
            ItemCatalog.CATALOG.put(id, ItemCatalog.ITEMS.register(id, () -> new FortuneModule(0, level)));
        }
        for (int i = 1; i <= 3; i++) {
            final int kind = i;
            String id = moduleId(i);
            ItemCatalog.CATALOG.put(id, ItemCatalog.ITEMS.register(id, () -> new FortuneModule(kind, 0)));
        }
    }

    public static String moduleId(int kind) {
        return switch (kind) {
            case 1 -> "fortune_upgrade_smelting";
            case 2 -> "fortune_upgrade_stack";
            default -> "fortune_upgrade_infinite";
        };
    }

    @SuppressWarnings("unchecked")
    private static BlockEntityType<FortuneEntity> entityType() {
        try {
            Class<?> supplier = Class
                    .forName("net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier");
            Object factory = java.lang.reflect.Proxy.newProxyInstance(supplier.getClassLoader(),
                    new Class<?>[] { supplier }, (p, m, a) -> new FortuneEntity((net.minecraft.core.BlockPos) a[0],
                            (net.minecraft.world.level.block.state.BlockState) a[1]));
            var method = BlockEntityType.Builder.class.getDeclaredMethod("m_155273_", supplier, Block[].class);
            method.setAccessible(true);
            return ((BlockEntityType.Builder<FortuneEntity>) method.invoke(null, factory,
                    new Block[] { FORTUNE.get() })).m_58966_(null);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException(error);
        }
    }

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        BLOCKS.register(bus);
        ENTITIES.register(bus);
        net.minecraftforge.fml.ModLoadingContext.get()
                .registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, SPEC, "loli-labs-utility.toml");
    }
}
