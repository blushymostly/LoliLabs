package tmr.lolilabs;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LoliLabs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LabMaterialClient {

    @SubscribeEvent
    public static void colors(RegisterColorHandlersEvent.Item event) {
        LabMaterialColors.MATERIALS.forEach((id, material) -> event.register((stack, tint) -> material.color(tint),
                LabItems.CATALOG.get(id).get()));
    }

    /** GT-style EU charge bar for vajra / world breaker (IC2 only registers its own items). */
    @SubscribeEvent
    public static void chargeBars(RegisterItemDecorationsEvent event) {
        for (String id : new String[] { "vajra", "legendary_vajra", "infinity_pickaxe_ultra" }) {
            event.register(LabItems.CATALOG.get(id).get(),
                    (graphics, font, stack, x, y) -> com.ic2ceu.icceu.ElectricChargeRenderer.render(graphics, stack, x,
                            y));
        }
    }

    private LabMaterialClient() {}
}
