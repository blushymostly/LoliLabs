package tmr.lolilabs.magically;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.UITexture;

import java.util.*;
import java.util.function.IntSupplier;

/** Client-only activation wave following the interior channel from each upgrade socket. */
public final class GreenhouseGauge implements IDrawable {

    private static final int[][] PIXELS = load();

    private static int[][] load() {
        try (var input = GreenhouseGauge.class.getResourceAsStream("/assets/loli_labs/greenhouse-gauge-paths.json")) {
            var json = com.google.gson.JsonParser
                    .parseReader(new java.io.InputStreamReader(Objects.requireNonNull(input),
                            java.nio.charset.StandardCharsets.UTF_8))
                    .getAsJsonArray();
            int[][] result = new int[json.size()][6];
            for (int i = 0; i < result.length; i++)
                for (int j = 0; j < 6; j++) result[i][j] = json.get(i).getAsJsonArray().get(j).getAsInt();
            return result;
        } catch (java.io.IOException error) {
            throw new IllegalStateException(error);
        }
    }

    public static java.util.List<int[]> pixels() {
        return java.util.Arrays.asList(PIXELS);
    }

    private final IntSupplier installed;
    private final UITexture texture;
    private final java.util.function.IntToLongFunction activated;
    private final java.util.function.LongSupplier worldTime;
    private final double[] distances = { -1, -1, -1, -1 };

    public GreenhouseGauge(IntSupplier installed, java.util.function.IntToLongFunction activated,
                           java.util.function.LongSupplier worldTime, UITexture texture) {
        this.installed = installed;
        this.activated = activated;
        this.worldTime = worldTime;
        this.texture = texture;
    }

    public double distance(int socket) {
        return distances[socket];
    }

    public static boolean reached(int[] pixel, int socket, double distance) {
        return pixel[socket + 2] >= 0 && pixel[socket + 2] <= distance;
    }

    public static boolean lit(int[] pixel, int flags, double[] distances) {
        if (pixel[1] < 37) {
            int side = pixel[0] < 122 ? 0 : 2;
            return ((flags >> side) & 3) == 3 && reached(pixel, side, distances[side]) &&
                    reached(pixel, side + 1, distances[side + 1]);
        }
        for (int i = 0; i < 4; i++) if ((flags & (1 << i)) != 0 && reached(pixel, i, distances[i])) return true;
        return false;
    }

    private void paintRun(brachy.modularui.screen.viewport.GuiContext context, int x, int y, int sourceX, int sourceY,
                          int width, brachy.modularui.theme.WidgetTheme theme) {
        int phase = tmr.lolilabs.gui.FlowingGauge.phase(), start = Math.floorMod(sourceX + phase, 129),
                row = Math.floorMod(sourceY, 5), drawn = 0;
        while (drawn < width) {
            int count = Math.min(width - drawn, 129 - start);
            texture.getSubArea(start / 129F, row / 5F, (start + count) / 129F, (row + 1) / 5F).draw(context, x + drawn,
                    y, count, 1, theme);
            drawn += count;
            start = 0;
        }
    }

    @Override
    public void draw(brachy.modularui.screen.viewport.GuiContext context, int x, int y, int width, int height,
                     brachy.modularui.theme.WidgetTheme theme) {
        int flags = installed.getAsInt();
        for (int i = 0; i < 4; i++) {
            long installedAt = activated.applyAsLong(i);
            distances[i] = (flags & (1 << i)) == 0 ? -1 : installedAt == Long.MIN_VALUE ? 500 : Math.max(0,
                    Math.min(500, (worldTime.getAsLong() + context.getRenderPartialTicks() - installedAt) * 5));
        }
        // Coalesce horizontal spans: the animation paints solid interiors and leaves beveled borders untouched.
        int runX = -1, runY = -1, runWidth = 0;
        for (var pixel : PIXELS) {
            boolean on = lit(pixel, flags, distances);
            if (on && pixel[1] == runY && pixel[0] == runX + runWidth) {
                runWidth++;
                continue;
            }
            if (runWidth > 0) paintRun(context, x + runX - 33, y + runY - 30, runX, runY, runWidth, theme);
            if (on) {
                runX = pixel[0];
                runY = pixel[1];
                runWidth = 1;
            } else runWidth = 0;
        }
        if (runWidth > 0) paintRun(context, x + runX - 33, y + runY - 30, runX, runY, runWidth, theme);
    }
}
