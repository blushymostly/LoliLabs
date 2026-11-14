package tmr.lolilabs.gui;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;

/** Scroll the original 129x5 sparkling strip; the ProgressWidget stencil still controls the stored amount. */
public final class FlowingGauge implements IDrawable {

    private final UITexture texture;

    public FlowingGauge(IDrawable texture) {
        if (!(texture instanceof UITexture ui)) throw new IllegalArgumentException("Flowing gauge requires a texture");
        this.texture = ui;
    }

    public static int phase() {
        return (int) ((System.nanoTime() / 150_000_000L) % 129);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme theme) {
        int start = phase(), cut = Math.round(width * (129 - start) / 129F);
        if (cut > 0) texture.getSubArea(start / 129F, 0, 1, 1).draw(context, x, y, cut, height, theme);
        if (cut < width)
            texture.getSubArea(0, 0, start / 129F, 1).draw(context, x + cut, y, width - cut, height, theme);
    }
}
