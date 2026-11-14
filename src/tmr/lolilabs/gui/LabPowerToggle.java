package tmr.lolilabs.gui;

import tmr.lolilabs.LoliLabs;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.widgets.ToggleButton;

/** Animated original power switch from the supplied gui_elements atlas. */
public final class LabPowerToggle {

    public static ToggleButton create(BooleanSyncValue value) {
        var button = new ToggleButton().value(value);
        var track = UITexture.builder().location(LoliLabs.MOD_ID, "textures/gui/og/power_track.png").imageSize(18, 13)
                .build();
        var on = UITexture.builder().location(LoliLabs.MOD_ID, "textures/gui/og/power_controls.png").imageSize(256, 256)
                .subAreaXYWH(9, 126, 7, 9).build();
        var off = UITexture.builder().location(LoliLabs.MOD_ID, "textures/gui/og/power_controls.png")
                .imageSize(256, 256).subAreaXYWH(20, 126, 7, 9).build();
        IDrawable drawing = new IDrawable() {

            double position = -1;
            long previous;

            @Override
            public void draw(brachy.modularui.screen.viewport.GuiContext context, int x, int y, int width, int height,
                             brachy.modularui.theme.WidgetTheme theme) {
                long now = System.nanoTime();
                double target = button.isValueSelected() ? 1 : 0;
                if (position < 0) position = target;
                else {
                    double delta = Math.min(.1, (now - previous) / 1_000_000_000D) * 6;
                    position += Math.signum(target - position) * Math.min(Math.abs(target - position), delta);
                }
                previous = now;
                track.draw(context, x, y, width, height, theme);
                (button.isValueSelected() ? on : off).draw(context, x + (int) ((2 + 7 * position) * width / 18),
                        y + 2 * height / 13, 7 * width / 18, 9 * height / 13, theme);
            }
        };
        return button.background(false, drawing).background(true, drawing).tooltipAutoUpdate(true)
                .tooltipDynamic(t -> t.addLine(brachy.modularui.api.drawable.Text
                        .lang(button.isValueSelected() ? "gui.loli_labs.power.on" : "gui.loli_labs.power.off")));
    }
}
