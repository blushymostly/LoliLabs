package tmr.lolilabs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LabFluids {

    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES,
            LoliLabs.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS,
            LoliLabs.MOD_ID);
    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS = DeferredRegister
            .create(ForgeRegistries.BLOCKS, LoliLabs.MOD_ID);
    public static final Map<String, RegistryObject<ForgeFlowingFluid.Source>> SOURCES = new LinkedHashMap<>();

    public static void add(String name) {
        var type = TYPES.register(name,
                () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.loli_labs." + name)
                        .density(name.startsWith("molten_") ? 3000 : 1000)
                        .viscosity(name.startsWith("molten_") ? 6000 : 1000)
                        .temperature(name.startsWith("molten_") ? 1300 : 300).canConvertToSource(false)) {

                    @Override
                    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
                        consumer.accept(new net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions() {

                            @Override
                            public ResourceLocation getStillTexture() {
                                return new ResourceLocation(LoliLabs.MOD_ID, "block/legacy/fluids/" + name + "_still");
                            }

                            @Override
                            public ResourceLocation getFlowingTexture() {
                                return new ResourceLocation(LoliLabs.MOD_ID, "block/legacy/fluids/" + name + "_flow");
                            }
                        });
                    }
                });
        String bucket = name + "_bucket";
        SOURCES.put(name, FLUIDS.register(name, () -> new ForgeFlowingFluid.Source(properties(name, type, bucket))));
        FLUIDS.register("flowing_" + name, () -> new ForgeFlowingFluid.Flowing(properties(name, type, bucket)));
        BLOCKS.register(name, () -> new LiquidBlock(SOURCES.get(name), BlockBehaviour.Properties.m_60926_(
                ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft", "water")))));
        LabItems.CATALOG.put(bucket, LabItems.ITEMS.register(bucket, () -> new BucketItem(() -> bucketFluid(name),
                new Item.Properties().m_41487_(1)
                        .m_41495_(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "bucket"))))));
    }

    public static Fluid bucketFluid(String name) {
        var nativeFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("mekanism", name));
        return nativeFluid != null && nativeFluid != net.minecraft.world.level.material.Fluids.f_76191_ ? nativeFluid :
                SOURCES.get(name).get();
    }

    private static ForgeFlowingFluid.Properties properties(String name, RegistryObject<? extends FluidType> type,
                                                           String bucket) {
        return new ForgeFlowingFluid.Properties(type, SOURCES.get(name), () -> FLUIDS.getEntries().stream()
                .filter(entry -> entry.getId().m_135815_().equals("flowing_" + name)).findFirst().orElseThrow().get())
                .bucket(() -> LabItems.CATALOG.get(bucket).get()).block(() -> (LiquidBlock) BLOCKS.getEntries().stream()
                        .filter(entry -> entry.getId().m_135815_().equals(name)).findFirst().orElseThrow().get())
                .tickRate(name.startsWith("molten_") ? 30 : 5)
                .levelDecreasePerBlock(name.startsWith("molten_") ? 2 : 1);
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        FLUIDS.register(bus);
        BLOCKS.register(bus);
    }
}
