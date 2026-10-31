package tmr.lolilabs;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.zeith.solarflux.client.screen.SolarPanelScreen;
import org.zeith.solarflux.container.SolarPanelContainer;

public final class SparklingScreen extends SolarPanelScreen {
    private static final ResourceLocation GUI = new ResourceLocation(LoliLabs.MOD_ID, "textures/gui/gui_solar_panel.png");
    private static final NumberFormat NUMBERS = NumberFormat.getIntegerInstance(Locale.US);

    public SparklingScreen(SolarPanelContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        setSize(212, 212);
        // Preserve the original slot instances and their validation, indices and packets.
        try {
            Field x = Slot.class.getDeclaredField("f_40220_");
            Field y = Slot.class.getDeclaredField("f_40221_");
            x.setAccessible(true);
            y.setAccessible(true);
            for (int i = 0; i < menu.f_38839_.size(); i++) {
                int sx, sy;
                if (i < 27) { sx = 26 + (i % 9) * 18; sy = 128 + (i / 9) * 18; }
                else if (i < 36) { sx = 26 + (i - 27) * 18; sy = 186; }
                else if (i < 41) { sx = 62 + (i - 36) * 18; sy = 92; }
                else { sx = 168; sy = 92; }
                x.setInt(menu.f_38839_.get(i), sx);
                y.setInt(menu.f_38839_.get(i), sy);
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to position Sparkling Solar Panel slots", exception);
        }
    }

    @Override
    protected void renderBackground(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        gfx.m_280163_(GUI, f_97735_, f_97736_, 0, 0, 212, 212, 256, 256);
        // The supplied art has four sockets; draw sockets for all five upgrades and charging.
        gfx.m_280509_(f_97735_ + 60, f_97736_ + 90, f_97735_ + 152, f_97736_ + 110, 0xFF1B1B29);
        for (int i = 36; i < f_97732_.f_38839_.size(); i++) {
            Slot slot = f_97732_.f_38839_.get(i);
            gfx.m_280163_(GUI, f_97735_ + slot.f_40220_ - 1, f_97736_ + slot.f_40221_ - 1,
                    70, 91, 18, 18, 256, 256);
        }
        long stored = data.getLong(0), capacity = data.getLong(8);
        int filled = capacity > 0 ? (int) (127D * Math.min(1D, (double) stored / capacity)) : 0;
        gfx.m_280509_(f_97735_ + 46, f_97736_ + 78, f_97735_ + 46 + filled, f_97736_ + 83, 0xFF64FFF0);
    }

    @Override
    protected boolean renderForeground(GuiGraphics gfx, int mouseX, int mouseY) {
        long stored = data.getLong(0), capacity = data.getLong(8), generation = data.getLong(16);
        var pose = gfx.m_280168_();
        pose.m_85836_();
        pose.m_85837_(26, 32, 0);
        pose.m_85841_(0.75F, 0.75F, 1F);
        String[] lines = {
                I18n.m_118938_("gui.loli_labs.stored", NUMBERS.format(stored)),
                I18n.m_118938_("gui.loli_labs.capacity", NUMBERS.format(capacity)),
                I18n.m_118938_("gui.loli_labs.generation", NUMBERS.format(generation)),
                I18n.m_118938_("gui.loli_labs.sun", Math.round(data.getFloat(32) * 100))
        };
        for (int i = 0; i < lines.length; i++) gfx.m_280056_(f_96547_, lines[i], 0, i * 11, 0xB5FFF4, false);
        pose.m_85849_();
        if (inBounds(f_97735_ + 45, f_97736_ + 77, 129, 7, mouseX, mouseY)) {
            drawMouseOver(NUMBERS.format(stored) + " / " + NUMBERS.format(capacity) + " FE");
        }
        return true;
    }
}

