package tmr.lolilabs;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

/** Deprecated: singularities live in loliavaritia datapack. */
@Mod.EventBusSubscriber(modid = LoliLabs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EditorSingularitiesClient {
    public static void verify(Minecraft mc) {
        System.out.println("LOLI_LABS_NATIVE_SINGULARITIES_CLIENT_OK count=0 (moved to loliavaritia)");
    }
    private EditorSingularitiesClient() {}
}
