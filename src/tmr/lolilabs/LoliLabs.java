package tmr.lolilabs;

import net.minecraftforge.fml.common.Mod;

@Mod(LoliLabs.MOD_ID)
public final class LoliLabs {
    public static final String MOD_ID = "loli_labs";

    public LoliLabs() {
        var bus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        LabItems.register(bus);
        LabMachines.register(bus);
        LabConfigMigration.run();
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON,LabMachines.SPEC);
    }
}

