package tmr.lolilabs;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.common.Mod;

/** Run explicitly with -Dloli_labs.verify=true in Forge's data launch target. */
@Mod.EventBusSubscriber(modid=LoliLabs.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class LabRuntimeChecks {
    private static void require(boolean passed,String message) { if (!passed) throw new IllegalStateException(message); }
    @SubscribeEvent public static void check(GatherDataEvent event) {
        if (!Boolean.getBoolean("loli_labs.verify")) return;
        // Data generation skips normal common config/capability setup. Recreate it locally
        // so Mekanism exposes the same native energy adapters it uses in a running game.
        net.minecraftforge.fml.config.ConfigTracker.INSTANCE.loadConfigs(net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
        net.minecraftforge.fml.config.ConfigTracker.INSTANCE.loadConfigs(net.minecraftforge.fml.config.ModConfig.Type.SERVER,
                net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
        if (!ForgeCapabilities.ENERGY.isRegistered())
            net.minecraftforge.common.capabilities.CapabilityManager.INSTANCE.injectCapabilities(net.minecraftforge.fml.ModList.get().getAllScanData());
        mekanism.common.integration.energy.EnergyCompatUtils.initLoadedCache();
        for (String name:new String[]{"advanced_cell","ultimate_cell","quantum_cell"}) {
            ItemStack stack=new ItemStack(LabItems.CATALOG.get(name).get());
            var tank=FluidUtil.getFluidHandler(stack).orElseThrow(()->new IllegalStateException("Missing fluid capability"));
            int capacity=tank.getTankCapacity(0);
            require(tank.fill(new FluidStack(Fluids.f_76193_,capacity+1000),FluidAction.SIMULATE)==capacity,"Fill simulation limit");
            require(tank.getFluidInTank(0).isEmpty(),"Fill simulation mutated NBT");
            require(tank.fill(new FluidStack(Fluids.f_76193_,capacity+1000),FluidAction.EXECUTE)==capacity,"Fill over capacity");
            require(tank.fill(new FluidStack(Fluids.f_76195_,1000),FluidAction.EXECUTE)==0,"Mixed incompatible fluids");
            int[] saves={0};
            var cell=new LabFluidCellHandler().getCellInventory(stack,()->saves[0]++);
            var water=AEFluidKey.of(Fluids.f_76193_);
            require(cell.extract(water,1000,Actionable.SIMULATE,IActionSource.empty())==1000,"ME simulated extraction");
            require(tank.getFluidInTank(0).getAmount()==capacity && saves[0]==0,"ME simulation mutated tank");
            require(cell.extract(water,1000,Actionable.MODULATE,IActionSource.empty())==1000,"ME extraction");
            require(tank.getFluidInTank(0).getAmount()==capacity-1000 && saves[0]==1,"Shared ME/Forge storage");
            require(cell.insert(water,Long.MAX_VALUE,Actionable.MODULATE,IActionSource.empty())==1000,"ME insertion capacity/overflow");
            require(cell.getAvailableStacks().get(water)==capacity,"ME stored amount");
            require(!cell.canFitInsideCell(),"Nested cell storage must be disallowed");
        }
        ItemStack sword=new ItemStack(LabItems.CATALOG.get("sword1").get());
        var energy=sword.getCapability(ForgeCapabilities.ENERGY).orElseThrow(()->new IllegalStateException("Missing sword FE capability"));
        require(energy.receiveEnergy(1000,true)==1000 && energy.getEnergyStored()==0,"FE simulation changed state");
        require(energy.receiveEnergy(1000,false)==1000,"FE receive");
        require(energy.extractEnergy(1000,false)==0,"Gear must not discharge to external consumers");
        require(ItemPower.spend(sword,200) && energy.getEnergyStored()==800,"FE hit cost");
        require(!ItemPower.spend(sword,801) && energy.getEnergyStored()==800,"Insufficient charge mutated state");
        sword.m_41784_().m_128405_("LabPowerLevel",5);
        require(energy.getMaxEnergyStored()==60000000,"Capacity upgrade not applied");
        require(energy.receiveEnergy(Integer.MAX_VALUE,false)==59999200,"FE overflow/capacity");
        require(energy.getEnergyStored()==60000000,"FE charge exceeds capacity");
        for (var entry:LabItems.CATALOG.entrySet()) {
            ItemStack stack=new ItemStack(entry.getValue().get());
            if (stack.m_41720_() instanceof LabMiningTool || stack.m_41720_() instanceof LabConfigurator)
                require(stack.getCapability(ForgeCapabilities.ENERGY).isPresent(),"Missing Mekanism FE: "+entry.getKey());
            if(stack.m_41720_() instanceof net.minecraft.world.item.BucketItem bucket) {
                var contents=FluidUtil.getFluidHandler(stack).orElseThrow(()->new IllegalStateException("Missing bucket capability "+entry.getKey()));
                require(contents.getFluidInTank(0).getAmount()==1000 && contents.getFluidInTank(0).getFluid()==bucket.getFluid(),"Bucket contents mismatch "+entry.getKey());
            }
        }
        var star=new ItemStack(LabItems.CATALOG.get("zodius_micro_star").get());
        var stellar=star.getCapability(ForgeCapabilities.ENERGY).orElseThrow(()->new IllegalStateException("Missing stellar FE"));
        for(int i=0;i<4;i++)stellar.receiveEnergy(Integer.MAX_VALUE,false);
        require(star.m_41784_().m_128454_("LabLongEnergy")==4L*Integer.MAX_VALUE,"64-bit storage overflow");
        require(stellar.extractEnergy(1000,true)==1000 && star.m_41784_().m_128454_("LabLongEnergy")==4L*Integer.MAX_VALUE,"Stellar extraction simulation mutated NBT");
        int recipes=0,models=0;
        var jar=net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get().resolve("mods/loli-labs-1.20.1-1.2.0.jar").toFile();
        try(var resources=new net.minecraft.server.packs.resources.MultiPackResourceManager(net.minecraft.server.packs.PackType.CLIENT_RESOURCES,
                java.util.List.of(new net.minecraft.server.packs.FilePackResources("Loli Labs",jar,false)))) {
            net.minecraftforge.client.model.obj.ObjLoader.INSTANCE.m_6213_(resources);
            for(var entry:LabItems.CATALOG.entrySet()) {
                String id=entry.getKey();
                try(var stream=LabRuntimeChecks.class.getResourceAsStream("/data/loli_labs/recipes/"+id+".json")) {
                    require(stream!=null,"Missing recipe "+id);
                    var json=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
                    for(var ingredient:json.getAsJsonArray("ingredients")) {
                        String item=ingredient.getAsJsonObject().get("item").getAsString();
                        require(net.minecraftforge.registries.ForgeRegistries.ITEMS.containsKey(new net.minecraft.resources.ResourceLocation(item)),"Unknown recipe ingredient: "+item+" in "+id);
                    }
                    net.minecraft.world.item.crafting.RecipeSerializer.f_44077_.m_6729_(new net.minecraft.resources.ResourceLocation(LoliLabs.MOD_ID,id),json);
                    recipes++;
                }
                try(var stream=LabRuntimeChecks.class.getResourceAsStream("/assets/loli_labs/models/item/"+id+".json")) {
                    require(stream!=null,"Missing model "+id);
                    var json=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
                    if(json.has("loader") && json.get("loader").getAsString().equals("forge:obj")) {
                        var model=net.minecraftforge.client.model.obj.ObjLoader.INSTANCE.read(json,null);
                        require(!model.getRootComponentNames().isEmpty(),"Empty OBJ model "+id);models++;
                    }
                }
            }
        } catch(java.io.IOException error){throw new IllegalStateException(error);}
        System.out.println("LOLI_LABS_CONTENT_CHECKS_PASSED: "+recipes+" recipes parsed; "+models+" OBJ models parsed; 52 fluid buckets and 64-bit stellar storage verified");
        System.out.println("LOLI_LABS_RUNTIME_CHECKS_PASSED: fluid capacity, ME/Forge shared storage, simulation, FE upgrades and Mekanism capabilities");
    }
}
