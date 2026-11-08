package tmr.lolilabs.jade;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

/** Shared Jade chrome for Loli Labs machines. */
public final class LabJadeBars {

    /** Same red as IC2/GT workable progress. */
    public static final int PROGRESS_COLOR = 0xFFBB1C28;

    private LabJadeBars() {}

    public static BoxStyle box() {
        return Util.m_137469_(BoxStyle.DEFAULT, style -> style.borderColor = 0xFF555555);
    }

    /** Energy fill using the in-GUI flowing spark strip. */
    public static void energy(ITooltip tooltip, float fill, Component text) {
        IElementHelper helper = tooltip.getElementHelper();
        tooltip.add(helper.progress(
                fill,
                text,
                helper.progressStyle().overlay(FlowingEnergyElement.eu()).textColor(-1),
                box(),
                true));
    }

    /** Secondary flowing meter (mana / fortune work). */
    public static void flowing(ITooltip tooltip, float fill, Component text, boolean manaStrip) {
        IElementHelper helper = tooltip.getElementHelper();
        tooltip.add(helper.progress(
                fill,
                text,
                helper.progressStyle()
                        .overlay(manaStrip ? FlowingEnergyElement.mana() : FlowingEnergyElement.eu())
                        .textColor(-1),
                box(),
                true));
    }

    public static void work(ITooltip tooltip, float fill, Component text) {
        IElementHelper helper = tooltip.getElementHelper();
        tooltip.add(helper.progress(
                fill,
                text,
                helper.progressStyle().color(PROGRESS_COLOR).textColor(-1),
                box(),
                true));
    }
}
