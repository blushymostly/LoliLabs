package tmr.editor;
import com.google.gson.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;

/** Only edits the editor-owned project and one dedicated KubeJS file. */
public final class RecipeStore {
    public final Path game,project,script;
    public final TreeMap<String,JsonObject> recipes=new TreeMap<>();
    private byte[] expectedProject;
    public RecipeStore(Path instance)throws IOException{
        game=instance.resolve("minecraft").toAbsolutePath().normalize();
        if(!Files.isDirectory(game.resolve("mods")))throw new IOException("Не найдена папка minecraft/mods: "+game);
        project=game.resolve("kubejs/avaritia-editor/recipes.json");script=game.resolve("kubejs/server_scripts/avaritia_editor.js");
        if(Files.exists(project)){expectedProject=Files.readAllBytes(project);var json=JsonParser.parseString(new String(expectedProject,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();if(json.get("schema").getAsInt()!=1)throw new IOException("Неизвестная версия проекта.");for(var entry:json.getAsJsonObject("recipes").entrySet())recipes.put(entry.getKey(),RecipeModel.from(entry.getKey(),entry.getValue().getAsJsonObject()).recipe());}
    }
    public static String javascript(Map<String,JsonObject> recipes){
        var out=new StringBuilder("// Managed by Avaritia Recipe Editor. Edit recipes.json through the editor.\nServerEvents.recipes(event => {\n");
        for(var entry:new TreeMap<>(recipes).entrySet()){
            RecipeModel.from(entry.getKey(),entry.getValue());String id=RecipeModel.JSON.toJson(entry.getKey());
            out.append("  event.remove({id: ").append(id).append("});\n  event.custom(").append(RecipeModel.JSON.toJson(entry.getValue())).append(").id(").append(id).append(");\n");
        }
        return out.append("});\n").toString();
    }
    public void save()throws IOException{
        byte[] current=Files.exists(project)?Files.readAllBytes(project):null;
        if(!Arrays.equals(current,expectedProject))throw new IOException("Проект изменён в другом окне. Перезапустите редактор перед сохранением, чтобы не затереть изменения.");
        String js=javascript(recipes);var data=new JsonObject();data.addProperty("schema",1);var entries=new JsonObject();recipes.forEach(entries::add);data.add("recipes",entries);
        Files.createDirectories(project.getParent());Files.createDirectories(script.getParent());
        byte[] oldScript=Files.exists(script)?Files.readAllBytes(script):null;
        if(oldScript!=null&&!new String(oldScript,java.nio.charset.StandardCharsets.UTF_8).startsWith("// Managed by Avaritia Recipe Editor."))throw new IOException("Файл avaritia_editor.js создан другой программой. Переименуйте его перед сохранением.");
        String stamp=DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").format(LocalDateTime.now())+"-"+UUID.randomUUID().toString().substring(0,6);
        var backup=project.getParent().resolve("backups").resolve(stamp);Files.createDirectories(backup);
        if(Files.exists(project))Files.copy(project,backup.resolve("recipes.json"));if(oldScript!=null)Files.write(backup.resolve("avaritia_editor.js"),oldScript);
        try{atomic(script,js);atomic(project,RecipeModel.JSON.toJson(data)+"\n");expectedProject=Files.readAllBytes(project);}catch(IOException error){if(oldScript!=null)atomic(script,new String(oldScript,java.nio.charset.StandardCharsets.UTF_8));else Files.deleteIfExists(script);throw error;}
    }
    private static void atomic(Path path,String text)throws IOException{
        var temporary=Files.createTempFile(path.getParent(),".avaritia-editor-",".tmp");
        try{Files.writeString(temporary,text);try{Files.move(temporary,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temporary,path,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(temporary);}
    }
}
