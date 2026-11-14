package tmr.lolilabs;

import tmr.lolilabs.machine.MachineBehaviour;
import tmr.lolilabs.machine.MatterBehaviour;
import tmr.lolilabs.machine.SolarBehaviour;
import tmr.lolilabs.machine.UnifierBehaviour;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LabMachines {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            LoliLabs.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, LoliLabs.MOD_ID);
    public static final Map<String, Definition> DEFINITIONS = new LinkedHashMap<>();
    public static final ForgeConfigSpec.Builder CONFIG = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final RegistryObject<BlockEntityType<LabMachineEntity>> ENTITY;

    public abstract static class Definition {

        public final String id, matter;
        public final int tier, batch;
        public final RegistryObject<Block> block;
        public final ForgeConfigSpec.LongValue generation, cost, voltage;
        public final ForgeConfigSpec.IntValue packets;

        Definition(String id, long rate, String matter, int tier, int batch, long defaultCost) {
            this.id = id;
            this.matter = matter;
            this.tier = tier;
            this.batch = batch;
            CONFIG.push(id);
            generation = CONFIG.defineInRange("generationEUPerTick", rate / 4, 0L, 1_000_000_000_000L);
            cost = CONFIG.defineInRange("energyEUPerItem", defaultCost, 1L, 1_000_000_000_000L);
            voltage = CONFIG.defineInRange("outputVoltageEU", 8192L, 1L, 1_000_000_000_000L);
            packets = CONFIG.defineInRange("outputPacketsPerTick", 16, 1, 256);
            CONFIG.pop();
            var props = BlockBehaviour.Properties.m_284310_().m_60913_(5F, 20F);
            if (kindCode() <= 1)
                props = props.m_60953_(state -> state.m_61143_(LabMachineBlock.ACTIVE) ? 15 : 0);
            final var registered = props;
            block = BLOCKS.register(id, () -> new LabMachineBlock(this, registered));
            LabItems.CATALOG.put(id,
                    LabItems.ITEMS.register(id, () -> new LabMachineItem(block.get(), new Item.Properties(), this)));
        }

        public long rate() {
            return generation.get();
        }

        public long cycleCost() {
            return Math.multiplyExact(cost.get(), batch);
        }

        /** Legacy UI / container code still syncs solar=0, unifier=1, matter=2. */
        public abstract int kindCode();

        public abstract MachineBehaviour createBehaviour();
    }

    public static final class SolarPanelDef extends Definition {

        SolarPanelDef(String id, long rate) {
            super(id, rate, "", 0, 1, 1L);
        }

        @Override
        public int kindCode() {
            return 0;
        }

        @Override
        public MachineBehaviour createBehaviour() {
            return new SolarBehaviour();
        }
    }

    public static final class UnifierDef extends Definition {

        UnifierDef(String id) {
            super(id, 0L, "", 0, 1, 1L);
        }

        @Override
        public int kindCode() {
            return 1;
        }

        @Override
        public MachineBehaviour createBehaviour() {
            return new UnifierBehaviour();
        }
    }

    public static final class MatterSolidDef extends Definition {

        MatterSolidDef(String id, String matter, int tier, int batch) {
            super(id, 0L, matter, tier, batch, 250_000L * (1L << (2 * tier)));
        }

        @Override
        public int kindCode() {
            return 2;
        }

        @Override
        public MachineBehaviour createBehaviour() {
            return new MatterBehaviour();
        }
    }

    static {
        MachineDefinitions.register();
        SPEC = CONFIG.build();
        ENTITY = ENTITIES.register("machine", LabMachines::entityType);
    }

    static void solar(String id, long rate) {
        DEFINITIONS.put(id, new SolarPanelDef(id, rate));
    }

    static void unifier(String id) {
        DEFINITIONS.put(id, new UnifierDef(id));
    }

    static void matter(String id, String matter, int tier, int batch) {
        DEFINITIONS.put(id, new MatterSolidDef(id, matter, tier, batch));
    }

    @SuppressWarnings("unchecked")
    private static BlockEntityType<LabMachineEntity> entityType() {
        // The installed SRG compile jar predates Forge's access transformer for this
        // supplier interface. Reflection bridges that compile-only visibility difference.
        try {
            Class<?> supplier = Class
                    .forName("net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier");
            Object factory = java.lang.reflect.Proxy.newProxyInstance(supplier.getClassLoader(),
                    new Class<?>[] { supplier },
                    (proxy, method, args) -> new LabMachineEntity((net.minecraft.core.BlockPos) args[0],
                            (net.minecraft.world.level.block.state.BlockState) args[1]));
            var method = BlockEntityType.Builder.class.getDeclaredMethod("m_155273_", supplier, Block[].class);
            method.setAccessible(true);
            var builder = (BlockEntityType.Builder<LabMachineEntity>) method.invoke(null, factory,
                    DEFINITIONS.values().stream().map(d -> d.block.get()).toArray(Block[]::new));
            return builder.m_58966_(null);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Cannot register Loli Labs machine entity", error);
        }
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ENTITIES.register(bus);
    }

    public static long addSafe(long a, long b) {
        return b > Long.MAX_VALUE - a ? Long.MAX_VALUE : a + b;
    }

    public static Definition panel(net.minecraft.world.item.ItemStack stack) {
        if (stack.m_41720_() instanceof BlockItem item && item.m_40614_() instanceof LabMachineBlock b &&
                b.definition instanceof SolarPanelDef)
            return b.definition;
        return null;
    }
}
