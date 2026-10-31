package tmr.editor;

import com.google.gson.*;
import java.util.*;

/** Re-Avaritia 1.4.2 shaped_table / shapeless_table JSON, without executing scripts. */
public final class RecipeModel {
    public static final Gson JSON=new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String SYMBOLS="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!$%&()*+,-./:;<=>?@[]^_{}~";
    public String id="loli_labs:editor/new_recipe",result="";
    public int tier=4,count=1;
    public boolean shapeless,compatible;
    public String nbt="";
    public JsonObject extra=new JsonObject();
    public JsonElement[] cells=new JsonElement[81];
    public int size(){return tier*2+1;}
    public static void validId(String id){if(id==null||!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")||id.length()>240)throw new IllegalArgumentException("Некорректный ID: "+id+". Формат: modid:название (латиница).");}
    public static JsonElement ingredient(String text){
        text=text.trim();if(text.isEmpty())return null;
        if(text.startsWith("{")||text.startsWith("[")){var parsed=JsonParser.parseString(text);validateIngredient(parsed);return parsed;}
        boolean tag=text.startsWith("#");String id=tag?text.substring(1):text;validId(id);var value=new JsonObject();value.addProperty(tag?"tag":"item",id);return value;
    }
    public static void validateIngredient(JsonElement value){
        if(value==null||value.isJsonNull())throw new IllegalArgumentException("Пустой ингредиент.");
        if(value.isJsonArray()){if(value.getAsJsonArray().isEmpty())throw new IllegalArgumentException("Пустой список альтернатив.");for(var v:value.getAsJsonArray())validateIngredient(v);return;}
        if(!value.isJsonObject())throw new IllegalArgumentException("Ингредиент должен быть предметом, тегом или JSON.");
        var o=value.getAsJsonObject();if(o.has("item")){validId(o.get("item").getAsString());if(o.get("item").getAsString().equals("minecraft:air"))throw new IllegalArgumentException("Для пустой клетки используйте ластик, не minecraft:air.");}else if(o.has("tag"))validId(o.get("tag").getAsString());else throw new IllegalArgumentException("Для особого ингредиента сначала выберите предмет/тег. Неподдерживаемый тип: "+o);
    }
    public static String display(JsonElement value){if(value==null)return "";if(value.isJsonArray())return display(value.getAsJsonArray().get(0))+" | …";var o=value.getAsJsonObject();return o.has("item")?o.get("item").getAsString():o.has("tag")?"#"+o.get("tag").getAsString():o.toString();}
    public RecipeModel copy(){var c=new RecipeModel();c.id=id;c.result=result;c.tier=tier;c.count=count;c.shapeless=shapeless;c.compatible=compatible;c.nbt=nbt;c.extra=extra.deepCopy();for(int i=0;i<cells.length;i++)c.cells[i]=cells[i]==null?null:cells[i].deepCopy();return c;}
    public JsonObject recipe(){
        validId(id);validId(result);if(result.equals("minecraft:air"))throw new IllegalArgumentException("Выберите непустой результат.");if(tier<1||tier>4||count<1||count>64)throw new IllegalArgumentException("Уровень: 1–4; количество результата: 1–64.");
        var out=extra.deepCopy();for(String key:List.of("key","pattern","ingredients","compatible"))out.remove(key);
        out.addProperty("type",shapeless?"avaritia:shapeless_table":"avaritia:shaped_table");out.addProperty("tier",tier);
        if(!shapeless)out.addProperty("compatible",compatible);
        var product=new JsonObject();product.addProperty("item",result);if(count!=1)product.addProperty("count",count);if(!nbt.isBlank())product.addProperty("nbt",nbt);out.add("result",product);
        int size=size(),minX=size,minY=size,maxX=-1,maxY=-1;
        for(int y=0;y<size;y++)for(int x=0;x<size;x++)if(cells[y*9+x]!=null){validateIngredient(cells[y*9+x]);minX=Math.min(x,minX);minY=Math.min(y,minY);maxX=Math.max(x,maxX);maxY=Math.max(y,maxY);}
        if(maxX<0)throw new IllegalArgumentException("Добавьте хотя бы один ингредиент в сетку.");
        if(shapeless){var list=new JsonArray();for(int y=0;y<size;y++)for(int x=0;x<size;x++)if(cells[y*9+x]!=null)list.add(cells[y*9+x].deepCopy());out.add("ingredients",list);}
        else{
            var keys=new LinkedHashMap<String,Character>();var key=new JsonObject();var rows=new JsonArray();
            for(int y=minY;y<=maxY;y++){var row=new StringBuilder();for(int x=minX;x<=maxX;x++){var value=cells[y*9+x];if(value==null){row.append(' ');continue;}String canonical=value.toString();var symbol=keys.get(canonical);if(symbol==null){symbol=SYMBOLS.charAt(keys.size());keys.put(canonical,symbol);key.add(symbol.toString(),value.deepCopy());}row.append(symbol);}rows.add(row.toString());}
            out.add("pattern",rows);out.add("key",key);
        }
        return out;
    }
    public static RecipeModel from(String id,JsonObject json){
        validId(id);String type=json.get("type").getAsString();boolean shapeless=type.equals("avaritia:shapeless_table")||type.equals("minecraft:crafting_shapeless");
        if(!shapeless&&!type.equals("avaritia:shaped_table")&&!type.equals("minecraft:crafting_shaped"))throw new IllegalArgumentException("Тип рецепта не поддерживается: "+type);
        var m=new RecipeModel();m.id=id;m.extra=json.deepCopy();m.shapeless=shapeless;
        var product=json.get("result");if(product.isJsonPrimitive())m.result=product.getAsString();else{var o=product.getAsJsonObject();m.result=o.get("item").getAsString();m.count=o.has("count")?o.get("count").getAsInt():1;if(o.has("nbt"))m.nbt=o.get("nbt").isJsonPrimitive()?o.get("nbt").getAsString():o.get("nbt").toString();for(var e:o.entrySet())if(!Set.of("item","count","nbt").contains(e.getKey()))throw new IllegalArgumentException("Неизвестное поле результата: "+e.getKey());}
        int needed=1;
        if(shapeless){var list=json.getAsJsonArray("ingredients");if(list.size()>81)throw new IllegalArgumentException("Больше 81 ингредиента.");int size=list.size()<=9?3:list.size()<=25?5:list.size()<=49?7:9;needed=(size-1)/2;for(int i=0;i<list.size();i++){validateIngredient(list.get(i));m.cells[(i/size)*9+i%size]=list.get(i).deepCopy();}}
        else{var rows=json.getAsJsonArray("pattern");if(rows==null||rows.isEmpty()||rows.size()>9)throw new IllegalArgumentException("Высота рецепта должна быть 1–9.");int width=rows.get(0).getAsString().length();if(width<1||width>9)throw new IllegalArgumentException("Ширина рецепта должна быть 1–9.");needed=(Math.max(width,rows.size())<=3?1:Math.max(width,rows.size())<=5?2:Math.max(width,rows.size())<=7?3:4);var keys=json.getAsJsonObject("key");for(int y=0;y<rows.size();y++){String row=rows.get(y).getAsString();if(row.length()!=width)throw new IllegalArgumentException("Строки pattern имеют разную длину.");for(int x=0;x<width;x++){char symbol=row.charAt(x);if(symbol==' ')continue;var value=keys.get(Character.toString(symbol));if(value==null)throw new IllegalArgumentException("Нет ингредиента для символа "+symbol);validateIngredient(value);m.cells[y*9+x]=value.deepCopy();}}}
        m.tier=json.has("tier")&&json.get("tier").getAsInt()>0?json.get("tier").getAsInt():needed;if(m.tier<needed||m.tier>4)throw new IllegalArgumentException("Размер не соответствует уровню верстака.");
        m.compatible=json.has("compatible")&&json.get("compatible").getAsBoolean();m.recipe();return m;
    }
}
