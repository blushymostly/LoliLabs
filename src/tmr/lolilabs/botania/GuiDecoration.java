package tmr.lolilabs.botania;

/** Painted GUI art is never a mouse target, regardless of child ordering. */
public final class GuiDecoration extends brachy.modularui.widget.Widget<GuiDecoration> {
    @Override public boolean canHover(){return false;}
    @Override public boolean canClickThrough(){return true;}
    @Override public boolean canHoverThrough(){return true;}
}
