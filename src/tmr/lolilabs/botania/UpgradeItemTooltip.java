package tmr.lolilabs.botania;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "loli_labs",
                                                      value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class UpgradeItemTooltip {

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void tooltip(net.minecraftforge.client.event.RenderTooltipEvent.Pre event) {
        var upgrade = GreenhouseUpgrade.of(event.getItemStack());
        if (upgrade == null) return;
        event.setCanceled(true);
        var context = new brachy.modularui.screen.viewport.GuiContext(brachy.modularui.api.UIType.NONE);
        context.setGraphics(event.getGraphics());
        context.setOverrideFont(event.getFont());
        context.updateScreenArea(event.getScreenWidth(), event.getScreenHeight());
        context.updateState(event.getX(), event.getY(), 0);
        new GreenhouseUpgradeTooltip(upgrade).draw(context, event.getItemStack());
    }
}
