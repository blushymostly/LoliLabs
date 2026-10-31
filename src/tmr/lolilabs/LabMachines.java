package tmr.lolilabs;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.*;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;

public final class LabMachines {
    public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,LoliLabs.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,LoliLabs.MOD_ID);
    public static final Map<String,Definition> DEFINITIONS=new LinkedHashMap<>();
    public static final ForgeConfigSpec.Builder CONFIG=new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final RegistryObject<BlockEntityType<LabMachineEntity>> ENTITY;
    public static final class Definition {
        public final String id,matter; public final int kind,tier,batch;
        public final RegistryObject<Block> block;
        public final ForgeConfigSpec.LongValue generation,cost,voltage;
        public final ForgeConfigSpec.IntValue packets;
        Definition(String id,int kind,long rate,String matter,int tier,int batch){
            this.id=id;this.kind=kind;this.matter=matter;this.tier=tier;this.batch=batch;
            CONFIG.push(id);
            generation=CONFIG.defineInRange("generationEUPerTick",rate/4,0L,1_000_000_000_000L);
            cost=CONFIG.defineInRange("energyEUPerItem",kind==2?250_000L*(1L<<(2*tier)):1L,1L,1_000_000_000_000L);
            voltage=CONFIG.defineInRange("outputVoltageEU",8192L,1L,1_000_000_000_000L);
            packets=CONFIG.defineInRange("outputPacketsPerTick",16,1,256);
            CONFIG.pop();
            block=BLOCKS.register(id,()->new LabMachineBlock(this,BlockBehaviour.Properties.m_284310_().m_60913_(5F,20F)));
            LabItems.CATALOG.put(id,LabItems.ITEMS.register(id,()->new LabMachineItem(block.get(),new Item.Properties(),this)));
        }
        public long rate(){return generation.get();}
        public long cycleCost(){return Math.multiplyExact(cost.get(),batch);}
    }
    static {
        MachineDefinitions.register();
        SPEC=CONFIG.build();
        ENTITY=ENTITIES.register("machine",LabMachines::entityType);
    }
    static void add(String id,int kind,long rate,String matter,int tier,int batch){DEFINITIONS.put(id,new Definition(id,kind,rate,matter,tier,batch));}
    @SuppressWarnings("unchecked") private static BlockEntityType<LabMachineEntity> entityType(){
        // The installed SRG compile jar predates Forge's access transformer for this
        // supplier interface. Reflection bridges that compile-only visibility difference.
        try{
            Class<?> supplier=Class.forName("net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier");
            Object factory=java.lang.reflect.Proxy.newProxyInstance(supplier.getClassLoader(),new Class<?>[]{supplier},
                (proxy,method,args)->new LabMachineEntity((net.minecraft.core.BlockPos)args[0],(net.minecraft.world.level.block.state.BlockState)args[1]));
            var method=BlockEntityType.Builder.class.getDeclaredMethod("m_155273_",supplier,Block[].class);method.setAccessible(true);
            var builder=(BlockEntityType.Builder<LabMachineEntity>)method.invoke(null,factory,DEFINITIONS.values().stream().map(d->d.block.get()).toArray(Block[]::new));
            return builder.m_58966_(null);
        }catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot register Loli Labs machine entity",error);}
    }
    public static void register(IEventBus bus){BLOCKS.register(bus);ENTITIES.register(bus);}
    public static long addSafe(long a,long b){return b>Long.MAX_VALUE-a?Long.MAX_VALUE:a+b;}
    public static Definition panel(net.minecraft.world.item.ItemStack stack){
        if(stack.m_41720_() instanceof BlockItem item && item.m_40614_() instanceof LabMachineBlock b && b.definition.kind==0)return b.definition;
        return null;
    }
}
