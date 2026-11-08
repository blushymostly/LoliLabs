package tmr.lolilabs;

import java.util.Collections;
import java.util.Map;

/** Deprecated: singularities live in loliavaritia datapack. Kept as empty stub for migration. */
public final class EditorSingularities {
    public record Definition(String id, String displayName, int underlay, int overlay) {}
    public static final Map<String, Definition> DEFINITIONS = Collections.emptyMap();
    public static void registerItems() {}
    public static void verify() {}
    private EditorSingularities() {}
}
