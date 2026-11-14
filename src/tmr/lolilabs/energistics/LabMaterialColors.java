package tmr.lolilabs.energistics;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Native Loli Labs items rendered with IC2Modern's material icon sets. */
public final class LabMaterialColors {

    public record Material(String id, String material, String form, String iconSet, int primary, int secondary) {

        public int color(int tint) {
            return tint == 0 ? primary : tint == 1 ? secondary : -1;
        }

        public String texture(int layer) {
            return "ic2modern:item/material_sets/" + iconSet + "/" + form +
                    (layer == 1 ? "_secondary" : layer == 2 ? "_overlay" : "");
        }
    }

    public static final Map<String, Material> MATERIALS = load();

    private static Map<String, Material> load() {
        try (var stream = LabMaterialColors.class.getResourceAsStream("/assets/loli_labs/materials.json")) {
            if (stream == null) throw new IllegalStateException("Missing material definitions");
            var result = new LinkedHashMap<String, Material>();
            var entries = com.google.gson.JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonArray();
            for (var entry : entries) {
                var json = entry.getAsJsonObject();
                var material = new Material(json.get("id").getAsString(), json.get("material").getAsString(),
                        json.get("form").getAsString(), json.get("iconSet").getAsString(),
                        Integer.parseInt(json.get("primary").getAsString(), 16),
                        Integer.parseInt(json.get("secondary").getAsString(), 16));
                if (result.put(material.id(), material) != null)
                    throw new IllegalStateException("Duplicate material " + material.id());
            }
            return Collections.unmodifiableMap(result);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Cannot load material definitions", exception);
        }
    }

    private LabMaterialColors() {}
}
