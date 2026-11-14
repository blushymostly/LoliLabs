package tmr.lolilabs;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.gson.*;

/** Checks the actual native serializers, not KubeJS-shaped stubs. */
public final class CanonicalRecipeChecks {

    private static final java.util.Set<String> IDS = new java.util.HashSet<>();

    private static JsonObject read(String path) {
        try (var in = CanonicalRecipeChecks.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Missing resource " + path);
            return JsonParser.parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
                    .getAsJsonObject();
        } catch (java.io.IOException error) {
            throw new IllegalStateException(error);
        }
    }

    private static final JsonObject MANIFEST = read("/assets/loli_labs/canonical-recipes.json");
    static {
        for (var entry : MANIFEST.getAsJsonArray("recipes")) IDS.add(entry.getAsJsonObject().get("id").getAsString());
    }

    public static boolean contains(String id) {
        return IDS.contains(id);
    }

    private static void items(JsonElement node) {
        if (node.isJsonObject()) for (var e : node.getAsJsonObject().entrySet()) {
            if (e.getKey().equals("item") && e.getValue().isJsonPrimitive() &&
                    !ForgeRegistries.ITEMS.containsKey(new ResourceLocation(e.getValue().getAsString())))
                throw new IllegalStateException("Unknown canonical ingredient/output " + e.getValue());
            items(e.getValue());
        }
        else if (node.isJsonArray()) for (var value : node.getAsJsonArray()) items(value);
    }

    public static void runtime() {
        int count = 0;
        for (var value : MANIFEST.getAsJsonArray("recipes")) {
            var entry = value.getAsJsonObject();
            var id = new ResourceLocation(entry.get("id").getAsString());
            var json = read("/data/" + id.m_135827_() + "/recipes/" + id.m_135815_() + ".json");
            var plain = json.deepCopy();
            plain.remove("conditions");
            if (!plain.equals(entry.getAsJsonObject("json")))
                throw new IllegalStateException("Canonical payload changed " + id);
            items(plain);
            var serializer = ForgeRegistries.RECIPE_SERIALIZERS
                    .getValue(new ResourceLocation(json.get("type").getAsString()));
            if (serializer == null || serializer.m_6729_(id, json) == null)
                throw new IllegalStateException("Canonical recipe failed native serializer " + id);
            count++;
        }
        if (count != 50) throw new IllegalStateException("Canonical recipe count " + count);
        System.out.println("LOLI_LABS_CANONICAL_RECIPES_OK count=" + count +
                " native serializers; exact KubeJS payloads; all item references registered");
    }
}
