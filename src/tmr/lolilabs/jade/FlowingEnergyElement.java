package tmr.lolilabs.jade;

import tmr.lolilabs.FlowingGauge;
import tmr.lolilabs.LoliLabs;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

import snownee.jade.api.ui.Element;

/**
 * Jade overlay that scrolls the same 129×5 sparkling strip as {@link FlowingGauge}
 * in machine ModularUI meters.
 */
public final class FlowingEnergyElement extends Element {

    public static final ResourceLocation ATLAS = new ResourceLocation(LoliLabs.MOD_ID,
            "textures/gui/og/gui_multi_greenhouse_common.png");
    /** EU meter row in the shared OG atlas (matches LabModularUI / Greenhouse / Fortune). */
    public static final int EU_V = 12;
    /** Mana / work-meter row. */
    public static final int MANA_V = 17;
    private static final int STRIP = 129;
    private static final int STRIP_H = 5;

    private final int atlasV;

    public FlowingEnergyElement(int atlasV) {
        this.atlasV = atlasV;
    }

    public static FlowingEnergyElement eu() {
        return new FlowingEnergyElement(EU_V);
    }

    public static FlowingEnergyElement mana() {
        return new FlowingEnergyElement(MANA_V);
    }

    @Override
    public Vec2 getSize() {
        return size != null ? size : Vec2.f_82462_;
    }

    @Override
    public void render(GuiGraphics graphics, float x, float y, float w, float h) {
        int width = Math.max(0, Math.round(w > 0 ? w : (size != null ? size.f_82470_ : 0)));
        int height = Math.max(1, Math.round(h > 0 ? h : (size != null ? size.f_82471_ : STRIP_H)));
        if (width <= 0) return;
        int start = FlowingGauge.phase();
        int cut = Math.round(width * (STRIP - start) / (float) STRIP);
        int ix = Math.round(x);
        int iy = Math.round(y);
        if (cut > 0) {
            graphics.m_280411_(ATLAS, ix, iy, cut, height, start, atlasV, STRIP - start, STRIP_H, 256, 256);
        }
        if (cut < width) {
            graphics.m_280411_(ATLAS, ix + cut, iy, width - cut, height, 0, atlasV, start, STRIP_H, 256, 256);
        }
    }
}
