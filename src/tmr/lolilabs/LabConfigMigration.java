package tmr.lolilabs;

/** Preserve custom FE balances when upgrading an existing installation to EU. */
public final class LabConfigMigration {

    public static void run() {
        var file = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve("loli_labs-common.toml");
        if (!java.nio.file.Files.exists(file)) return;
        try {
            String old = java.nio.file.Files.readString(file);
            if (old.contains("generationEUPerTick") || !old.contains("generationFEPerTick")) return;
            var pattern = java.util.regex.Pattern
                    .compile("(?m)^(\\s*)(generationFEPerTick|energyFEPerItem)(\\s*=\\s*)([0-9]+)");
            var matcher = pattern.matcher(old);
            var updated = new StringBuffer();
            while (matcher.find()) {
                long value = Long.parseLong(matcher.group(4)) / 4;
                if (matcher.group(2).equals("energyFEPerItem")) value = Math.max(1, value);
                matcher.appendReplacement(updated, java.util.regex.Matcher.quoteReplacement(
                        matcher.group(1) + matcher.group(2).replace("FE", "EU") + matcher.group(3) + value));
            }
            matcher.appendTail(updated);
            var backup = file.resolveSibling("loli_labs-common.toml.before-eu-1.4.0.bak");
            if (!java.nio.file.Files.exists(backup)) java.nio.file.Files.copy(file, backup);
            java.nio.file.Files.writeString(file, updated.toString());
        } catch (java.io.IOException | NumberFormatException error) {
            throw new IllegalStateException("Cannot migrate Loli Labs FE config to EU", error);
        }
    }
}
