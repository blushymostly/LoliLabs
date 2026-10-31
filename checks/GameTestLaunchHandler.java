package tmr.checks;

/** Isolated GameTest server using the installed SRG client bundle's server classes. */
public final class GameTestLaunchHandler extends net.minecraftforge.fml.loading.targets.ForgeClientLaunchHandler {
    @Override public String name() { return "lolilabsgametest"; }
    @Override public boolean isProduction() { return false; }
    @Override public net.minecraftforge.api.distmarker.Dist getDist() { return net.minecraftforge.api.distmarker.Dist.DEDICATED_SERVER; }
    @Override protected cpw.mods.modlauncher.api.ServiceRunner makeService(String[] args, ModuleLayer layer) {
        return () -> {
            Class<?> main = Class.forName(layer.findModule("minecraft").orElseThrow(), "net.minecraft.server.Main");
            main.getMethod("main", String[].class).invoke(null, (Object) args);
        };
    }
}

