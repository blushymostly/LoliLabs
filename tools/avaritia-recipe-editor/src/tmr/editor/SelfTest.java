package tmr.editor;
import com.google.gson.*;
import java.nio.file.*;

public final class SelfTest {
    private static int checks;
    private static void check(boolean pass,String reason){checks++;if(!pass)throw new AssertionError(reason);}
    public static void run(Path root)throws Exception{
        Files.createDirectories(root);var fixtures=root.resolve("fixtures");Files.createDirectories(fixtures);
        for(int tier=1;tier<=4;tier++)for(boolean shapeless:new boolean[]{false,true}){
            var m=new RecipeModel();m.tier=tier;m.shapeless=shapeless;m.id="loli_labs:editor_test/t"+tier+(shapeless?"_shapeless":"_shaped");m.result="minecraft:diamond";m.count=2;
            int n=m.size();for(int y=0;y<n;y++)for(int x=0;x<n;x++)m.cells[y*9+x]=RecipeModel.ingredient((x+y)%2==0?"minecraft:iron_ingot":"#forge:ingots/gold");
            var json=m.recipe();check(json.get("tier").getAsInt()==tier,"Tier export");var restored=RecipeModel.from(m.id,json);check(restored.recipe().equals(json),"Round-trip "+m.id);
            if(shapeless)check(json.getAsJsonArray("ingredients").size()==n*n,"Shapeless size");else check(json.getAsJsonArray("pattern").size()==n&&json.getAsJsonArray("pattern").get(0).getAsString().length()==n,"Shaped dimensions");
            Files.writeString(fixtures.resolve("t"+tier+(shapeless?"_shapeless":"_shaped")+".json"),RecipeModel.JSON.toJson(json));
        }
        var sparse=new RecipeModel();sparse.result="minecraft:diamond";sparse.cells[4*9+4]=RecipeModel.ingredient("minecraft:stick");check(sparse.recipe().getAsJsonArray("pattern").size()==1,"Trim empty margins");check(sparse.recipe().getAsJsonArray("pattern").get(0).getAsString().length()==1,"Trim width");
        sparse.nbt="{display:{Name:'{\"text\":\"Test\"}'}}";check(RecipeModel.from(sparse.id,sparse.recipe()).nbt.equals(sparse.nbt),"Result NBT");
        sparse.cells[4*9+4]=RecipeModel.ingredient("[{\"item\":\"minecraft:iron_ingot\"},{\"tag\":\"forge:ingots/gold\"}]");check(RecipeModel.from(sparse.id,sparse.recipe()).recipe().equals(sparse.recipe()),"Ingredient alternatives");
        var unique=new RecipeModel();unique.result="minecraft:diamond";for(int i=0;i<81;i++)unique.cells[i]=RecipeModel.ingredient("minecraft:test_"+i);check(unique.recipe().getAsJsonObject("key").size()==81,"81 distinct symbols");
        boolean invalid=false;try{RecipeModel.ingredient("minecraft:bad';alert(1)");}catch(IllegalArgumentException e){invalid=true;}check(invalid,"Reject invalid IDs");
        invalid=false;try{new RecipeModel().recipe();}catch(IllegalArgumentException e){invalid=true;}check(invalid,"Empty recipe rejected");
        var instance=root.resolve("test-instance");Files.createDirectories(instance.resolve("minecraft/mods"));var store=new RecipeStore(instance);store.recipes.put(sparse.id,sparse.recipe());store.save();var again=new RecipeStore(instance);check(again.recipes.equals(store.recipes),"Project persistence");
        String js=Files.readString(store.script);check(js.contains("event.remove({id:")&&js.contains("event.custom(")&&js.contains(").id("),"KubeJS replacement");
        store.recipes.clear();store.save();check(!Files.readString(store.script).contains("event.custom("),"Deleted override removed");
        invalid=false;try{again.save();}catch(java.io.IOException e){invalid=true;}check(invalid,"Concurrent overwrite protected");
        try(var paths=Files.walk(store.project.getParent().resolve("backups"))){check(paths.anyMatch(p->p.getFileName().toString().equals("avaritia_editor.js")),"Backup generated script");}
        Files.writeString(store.script,"// hand-written file\n");invalid=false;try{store.save();}catch(java.io.IOException e){invalid=true;}check(invalid,"Unmanaged script protected");
        Files.writeString(root.resolve("fixture-id-list.txt"),Files.list(fixtures).map(p->p.toAbsolutePath().toString()).sorted().reduce("",(a,b)->a+b+"\n"));
        System.out.println("AVARITIA_EDITOR_SELF_TEST_OK checks="+checks+" fixtures=8");
    }
    public static void catalog(Path instance)throws Exception{
        var catalog=new Catalog(instance);check(catalog.items.size()>100,"Item catalog");check(catalog.icon("minecraft:diamond")!=null,"Vanilla icon");check(catalog.icon("loli_labs:sparkling_panel")!=null,"Loli Labs icon");
        int count=0;for(var recipe:catalog.recipes)if(recipe.json().get("type").getAsString().startsWith("avaritia:")){try{RecipeModel.from(recipe.id(),recipe.json()).recipe();count++;}catch(Exception e){throw new AssertionError("Cannot import "+recipe.id(),e);}}
        check(count>0,"Avaritia source recipes");System.out.println("AVARITIA_EDITOR_CATALOG_OK items="+catalog.items.size()+" tags="+catalog.tags.size()+" sourceRecipes="+catalog.recipes.size()+" avaritiaRoundTrips="+count);
    }
}
