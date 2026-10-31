package tmr.lolilabs;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** Explicit headless validation; no player/world/rendering is simulated. */
@Mod.EventBusSubscriber(modid=LoliLabs.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class MaterialRuntimeChecks {
    private static void require(boolean passed,String message) { if(!passed)throw new IllegalStateException(message); }
    @SubscribeEvent public static void check(GatherDataEvent event) {
        if(!Boolean.getBoolean("loli_labs.verify"))return;
        require(ForgeRegistries.ITEMS.containsKey(new ResourceLocation("solarflux:sp_custom_sparkling")),"Panel not registered");
        require(ForgeRegistries.BLOCKS.containsKey(new ResourceLocation("solarflux:sp_custom_sparkling")),"Panel block not registered");
        int recipes=0;
        for(var entry:LabItems.CATALOG.entrySet()) {
            String id=entry.getKey();
            require(entry.getValue().get().getClass()==Item.class,"Unexpected mechanics in crafting item "+id);
            try(var stream=MaterialRuntimeChecks.class.getResourceAsStream("/data/loli_labs/recipes/"+id+".json")) {
                require(stream!=null,"Missing recipe "+id);
                var json=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
                for(var ingredient:json.getAsJsonArray("ingredients")) {
                    String item=ingredient.getAsJsonObject().get("item").getAsString();
                    require(ForgeRegistries.ITEMS.containsKey(new ResourceLocation(item)),"Unknown ingredient "+item+" in "+id);
                }
                net.minecraft.world.item.crafting.RecipeSerializer.f_44077_.m_6729_(new ResourceLocation(LoliLabs.MOD_ID,id),json);
                recipes++;
            }catch(java.io.IOException error){throw new IllegalStateException(error);}
        }
        require(recipes==315,"Unexpected testing scope");
        System.out.println("LOLI_LABS_RUNTIME_CHECKS_PASSED: 315 plain crafting items and recipes; Sparkling panel block/item registered");
    }
}
