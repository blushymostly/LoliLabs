package tmr.lolilabs.utility;

import tmr.lolilabs.*;
import tmr.lolilabs.botania.GuiDecoration;

import brachy.modularui.api.drawable.*;
import brachy.modularui.drawable.*;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.screen.*;
import brachy.modularui.value.sync.*;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.slot.*;

public final class FortuneUI {

    private static UITexture atlas(String id) {
        return UITexture.builder().location(LoliLabs.MOD_ID, "textures/gui/utility/" + id + ".png").imageSize(256, 256)
                .build();
    }

    private static IDrawable crop(UITexture image, int x, int y, int w, int h) {
        return image.getSubArea(x / 256F, y / 256F, (x + w) / 256F, (y + h) / 256F);
    }

    public static ModularPanel<?> build(FortuneEntity be, PanelSyncManager sync, UISettings settings) {
        settings.useTheme(com.ic2ceu.icceu.mui.GuiStyle.theme());
        var panel = ModularPanel.defaultPanel("loli_labs_fortune", 212, 246).background(IDrawable.NONE);
        var art = atlas("fortune");
        var common = atlas("elements");
        panel.child(new GuiDecoration().pos(0, 0).size(212, 246).background(crop(art, 0, 0, 212, 246)).decoration());
        panel.child(new GuiDecoration().name("utility_title").pos(39, -12).size(134, 12)
                .background(crop(common, 0, 0, 134, 12)).decoration());
        panel.child(new GuiDecoration().name("utility_logo").pos(67, -26).size(78, 12)
                .background(crop(common, 0, 37, 78, 12)).decoration());
        sync.registerSlotGroup("inputs", 9, 100);
        sync.registerSlotGroup("outputs", 9, 0);
        sync.registerSlotGroup("modules", 1, 200);
        for (int i = 0; i < 54; i++) {
            final int slot = i;
            boolean output = i >= 27;
            int cell = i % 27;
            panel.child(new ItemSlot().name((output ? "output_" : "input_") + cell)
                    .pos(14 + 18 * (cell % 9), (output ? 94 : 14) + 18 * (cell / 9)).size(18).background(IDrawable.NONE)
                    .slot(new ModularSlot(be.inventory, slot).slotGroup(output ? "outputs" : "inputs")
                            .filter(stack -> be.valid(slot, stack)).accessibility(!output, true)));
        }
        for (int i = 54; i < 58; i++) {
            final int slot = i;
            panel.child(new ItemSlot().name("module_" + (i - 54))
                    .pos(i == 54 ? 14 : 180, i == 54 ? 72 : 18 + (i - 55) * 18).size(18).background(IDrawable.NONE)
                    .slot(new ModularSlot(be.inventory, slot).filter(stack -> be.valid(slot, stack))
                            .slotGroup("modules"))
                    .tooltip(t -> t.addLine(Text.lang("tooltip.loli_labs.fortune.slot_" + (slot - 54)))));
        }
        panel.child(SlotGroupWidget.playerInventory(false, (i, slot) -> slot.background(IDrawable.NONE)).name("player")
                .pos(25, 164).disableSortButtons());
        var energy = new LongSyncValue(() -> be.energy);
        var capacity = new LongSyncValue(be::capacity);
        var infinite = new BooleanSyncValue(() -> be.upgrade(3));
        sync.syncValue("energy", energy);
        sync.syncValue("capacity", capacity);
        sync.syncValue("infinite", infinite);
        var spark = UITexture.builder().location(LoliLabs.MOD_ID, "textures/gui/og/gui_multi_greenhouse_common.png")
                .imageSize(256, 256).build();
        panel.child(new ProgressWidget().name("energy_meter").pos(47, 73).size(128, 4)
                .value(new DoubleSyncValue(() -> be.upgrade(3) ? 1 : (double) be.energy / Math.max(1, be.capacity())))
                .texture(IDrawable.NONE, new FlowingGauge(crop(spark, 0, 12, 129, 5)), ProgressDrawable.Direction.RIGHT)
                .tooltipAutoUpdate(true).tooltipDynamic(
                        t -> t.addLine(infinite.getBoolValue() ? Text.lang("tooltip.loli_labs.fortune.infinite") :
                                Text.str(energy.getLongValue() + " / " + capacity.getLongValue() + " EU"))));
        panel.child(new ProgressWidget().name("work_meter").pos(47, 84).size(128, 4)
                .value(new DoubleSyncValue(() -> be.totalWork == 0 ? 0 :
                        Math.min((double) be.progress / LabUtility.TICKS.get(), (double) be.doneWork / be.totalWork)))
                .texture(IDrawable.NONE, new FlowingGauge(crop(spark, 0, 17, 129, 5)), ProgressDrawable.Direction.RIGHT)
                .tooltip(t -> t.addLine(Text.lang("tooltip.loli_labs.fortune.progress"))));
        panel.child(
                new GuiDecoration().name("energy_bolt").pos(38, 73).size(5, 7).background(crop(spark, 134, 15, 5, 7))
                        .setEnabledIf(w -> energy.getLongValue() > 0 || infinite.getBoolValue()).decoration());
        panel.child(LabPowerToggle.create(new BooleanSyncValue(() -> be.enabled, v -> {
            be.enabled = v;
            be.sync();
        }).allowC2S()).name("power").pos(213, 16).size(18, 13));
        if (Boolean.getBoolean("loli_labs.verifyWorld")) {
            class Preview extends brachy.modularui.widget.Widget<Preview> {

                private boolean hooked;

                @Override
                public boolean canHover() {
                    return false;
                }

                @Override
                public void drawForeground(brachy.modularui.screen.viewport.ModularGuiContext context) {
                    if (!hooked) {
                        hooked = true;
                        context.postRenderCallbacks.add(ctx -> {
                            if (tmr.lolilabs.dimensions.DimensionsChecks.tooltipPreview)
                                new brachy.modularui.screen.RichTooltip()
                                        .addFromItem(new net.minecraft.world.item.ItemStack(
                                                LabItems.CATALOG.get("fortune_module_100").get()))
                                        .draw(ctx);
                        });
                    }
                }
            }
            panel.child(new Preview().pos(0, 0).size(1).decoration());
        }
        return panel.excludeAreaInRecipeViewer();
    }
}
