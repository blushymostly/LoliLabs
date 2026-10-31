package tmr.editor;
import com.google.gson.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.zip.*;
import java.io.*;

public final class Catalog {
    public record Source(Path jar,String entry){byte[] bytes()throws IOException{try(var zip=new ZipFile(jar.toFile());var in=zip.getInputStream(zip.getEntry(entry))){return in.readAllBytes();}}}
    public record Entry(String id,String name){@Override public String toString(){return name+"  ·  "+id;}}
    public record Recipe(String id,JsonObject json,String source){@Override public String toString(){return id;}}
    public final List<Entry> items=new ArrayList<>(),tags=new ArrayList<>();
    public final List<Recipe> recipes=new ArrayList<>();
    public final Map<String,String> names=new HashMap<>();
    private final Map<String,JsonObject> models=new HashMap<>();
    private final Map<String,Source> textures=new HashMap<>();
    private final Map<String,ImageIcon> icons=new HashMap<>();
    private final Set<String> itemIds=new TreeSet<>(),tagIds=new TreeSet<>();
    private final Map<String,String> english=new HashMap<>(),russian=new HashMap<>();
    public Catalog(Path instance)throws IOException{
        var mods=instance.resolve("minecraft/mods");var jars=new ArrayList<Path>();
        Path prism=instance.getParent().getParent(),vanilla=prism.resolve("libraries/net/minecraft/client/1.20.1-20230612.114412/client-1.20.1-20230612.114412-extra.jar");if(Files.exists(vanilla))jars.add(vanilla);
        try(var stream=Files.list(mods)){jars.addAll(stream.filter(p->p.toString().endsWith(".jar")).sorted().toList());}
        var recipeMap=new TreeMap<String,Recipe>();
        for(Path jar:jars)try(var zip=new ZipFile(jar.toFile())){
            var entries=zip.entries();while(entries.hasMoreElements()){
                var entry=entries.nextElement();String name=entry.getName();String[] p=name.split("/",4);
                if(p.length<4||entry.isDirectory())continue;String namespace=p[1],rest=p[3];
                if(p[0].equals("assets")&&p[2].equals("models")&&rest.endsWith(".json")){
                    var json=read(zip,entry);if(json==null)continue;String model=rest.substring(0,rest.length()-5);models.put(namespace+":"+model,json);
                    if(model.startsWith("item/"))itemIds.add(namespace+":"+model.substring(5));
                }else if(p[0].equals("assets")&&p[2].equals("textures")&&rest.endsWith(".png"))textures.put(namespace+":"+rest.substring(0,rest.length()-4),new Source(jar,name));
                else if(p[0].equals("assets")&&p[2].equals("lang")&&(rest.equals("ru_ru.json")||rest.equals("en_us.json"))){var json=read(zip,entry);if(json!=null)json.entrySet().forEach(e->{if(e.getValue().isJsonPrimitive())(rest.equals("ru_ru.json")?russian:english).put(e.getKey(),e.getValue().getAsString());});}
                else if(p[0].equals("data")&&p[2].equals("tags")&&rest.startsWith("items/")&&rest.endsWith(".json"))tagIds.add("#"+namespace+":"+rest.substring(6,rest.length()-5));
                else if(p[0].equals("data")&&p[2].equals("recipes")&&rest.endsWith(".json")){var json=read(zip,entry);if(json!=null&&json.has("type")&&Set.of("avaritia:shaped_table","avaritia:shapeless_table","minecraft:crafting_shaped","minecraft:crafting_shapeless").contains(json.get("type").getAsString())){String id=namespace+":"+rest.substring(0,rest.length()-5);recipeMap.put(id,new Recipe(id,json,jar.getFileName().toString()));}}
            }
        }catch(ZipException ignored){}
        for(String id:itemIds){String key=id.replace(':','.');String label=russian.getOrDefault("item."+key,russian.getOrDefault("block."+key,english.getOrDefault("item."+key,english.getOrDefault("block."+key,id))));names.put(id,label);items.add(new Entry(id,label));}
        for(String id:tagIds)tags.add(new Entry(id,id));recipes.addAll(recipeMap.values());
    }
    private static JsonObject read(ZipFile zip,ZipEntry entry){if(entry.getSize()>2_000_000)return null;try(var in=zip.getInputStream(entry);var reader=new InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)){var value=JsonParser.parseReader(reader);return value.isJsonObject()?value.getAsJsonObject():null;}catch(Exception e){return null;}}
    public String name(String id){return names.getOrDefault(id,id);}
    public ImageIcon icon(String id){
        if(icons.containsKey(id))return icons.get(id);
        ImageIcon icon=null;
        try{String texture=texture(id.contains(":")?id.split(":",2)[0]+":item/"+id.split(":",2)[1]:id,new HashSet<>());var source=textures.get(texture);if(source!=null){BufferedImage original=ImageIO.read(new ByteArrayInputStream(source.bytes()));if(original!=null){int size=Math.min(original.getWidth(),original.getHeight());var image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);var g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);g.drawImage(original,0,0,32,32,0,0,size,size,null);g.dispose();icon=new ImageIcon(image);}}}catch(Exception ignored){}
        icons.put(id,icon);return icon;
    }
    private String texture(String model,Set<String> seen){
        if(!seen.add(model)||seen.size()>24)return "";var json=models.get(model);if(json==null)return "";
        if(json.has("textures")){var t=json.getAsJsonObject("textures");for(String key:List.of("layer0","particle","side","all","top","front"))if(t.has(key)){String value=t.get(key).getAsString();var visited=new HashSet<String>();while(value.startsWith("#")&&visited.add(value)&&t.has(value.substring(1)))value=t.get(value.substring(1)).getAsString();if(!value.startsWith("#"))return value.contains(":")?value:"minecraft:"+value;}}
        if(json.has("parent")){String parent=json.get("parent").getAsString();return texture(parent.contains(":")?parent:"minecraft:"+parent,seen);}return "";
    }
}
