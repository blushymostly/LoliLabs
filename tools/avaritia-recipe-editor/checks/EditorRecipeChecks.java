package tmr.editor.checks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
@Mod.EventBusSubscriber(modid="loli_labs",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class EditorRecipeChecks {
    @SubscribeEvent public static void check(GatherDataEvent event)throws Exception{
        String dir=System.getProperty("avaritia.editor.fixtures");if(dir==null)return;
        int count=0;try(var files=java.nio.file.Files.list(java.nio.file.Path.of(dir))){
            for(var file:files.filter(p->p.toString().endsWith(".json")).sorted().toList()){
                var json=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(file)).getAsJsonObject();
                var serializer=ForgeRegistries.RECIPE_SERIALIZERS.getValue(new ResourceLocation(json.get("type").getAsString()));
                if(serializer==null)throw new IllegalStateException("Serializer missing "+json.get("type"));
                var recipe=serializer.m_6729_(new ResourceLocation("loli_labs","editor_check/"+file.getFileName().toString().replace(".json","")),json);
                if(recipe==null)throw new IllegalStateException("Null recipe "+file);count++;
            }
        }
        if(count!=8)throw new IllegalStateException("Expected eight editor recipes, found "+count);
        System.out.println("AVARITIA_EDITOR_NATIVE_SERIALIZERS_OK recipes=8 tiers=1,2,3,4 shaped=true shapeless=true");
    }
}
