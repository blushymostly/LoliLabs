package tmr.checks;

/** Production SRG Minecraft paths with the standard headless Forge data entry point. */
public final class CheckLaunchHandler extends net.minecraftforge.fml.loading.targets.ForgeClientLaunchHandler {
    @Override public String name() { return "lolilabsdata"; }
    @Override public boolean isData() { return true; }
    @Override protected cpw.mods.modlauncher.api.ServiceRunner makeService(String[] args,ModuleLayer layer) {
        return () -> {
            Class<?> main=Class.forName(layer.findModule("minecraft").orElseThrow(),"net.minecraft.data.Main");
            main.getMethod("main",String[].class).invoke(null,(Object)args);
        };
    }
}
