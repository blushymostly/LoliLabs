package tmr.lolilabs.botania;

import tmr.lolilabs.LoliLabs;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.viewport.GuiContext;

/** Original Magically tooltip frame; content lists only the four supported socket families. */
public final class GreenhouseUpgradeTooltip extends RichTooltip {

    private record Row(ItemStack icon, String name, String description) {}

    private final int socket;
    private GreenhouseUpgrade exact;
    private final UITexture frame = UITexture.builder()
            .location(LoliLabs.MOD_ID, "textures/gui/og/gui_multi_greenhouse_elements.png").imageSize(320, 320)
            .subAreaXYWH(0, 132, 176, 46).adaptable(11).build();

    public GreenhouseUpgradeTooltip(int socket) {
        this.socket = socket;
        addLine(brachy.modularui.api.drawable.Text.str(" "));
        showUpTimer(0);
    }

    public GreenhouseUpgradeTooltip(GreenhouseUpgrade exact) {
        this(GreenhouseUpgrade.slotFor(exact.kind));
        this.exact = exact;
    }

    private java.util.List<Row> rows() {
        var result = new java.util.ArrayList<Row>();
        var kinds = exact != null ? new GreenhouseUpgrade.Kind[] { exact.kind } : socket == 8 ?
                new GreenhouseUpgrade.Kind[] { GreenhouseUpgrade.Kind.EFFICIENCY, GreenhouseUpgrade.Kind.PROCESSING } :
                new GreenhouseUpgrade.Kind[] { GreenhouseUpgrade.kindForSlot(socket) };
        for (var kind : kinds) for (int level = exact == null ? 1 : exact.level; level <=
                (exact == null ? kind.maxLevel : exact.level); level++) {
                    String id = kind.itemId() + (kind.maxLevel > 1 ? "_lvl_" + level : "");
                    var icon = new ItemStack(LabBotaniaBlocks.ITEM_CATALOG.get(id).get());
                    String key = "tooltip.loli_labs.greenhouse.effect." + kind.id;
                    String desc = ((GreenhouseUpgrade) icon.m_41720_()).effectDescription().getString();
                    String name = Component.m_237115_("tooltip.loli_labs.greenhouse.name." + kind.id).getString() +
                            (kind.maxLevel > 1 ? " " + level : "");
                    result.add(new Row(icon, name, desc));
                }
        return result;
    }

    @Override
    public void draw(GuiContext context) {
        draw(context, ItemStack.f_41583_);
    }

    @Override
    public void draw(GuiContext context, ItemStack ignored) {
        render(context, -1, -1);
    }

    public void preview(GuiContext context) {
        render(context, 35, 70);
    }

    private void render(GuiContext context, int fixedX, int fixedY) {
        var rows = rows();
        var font = context.getFont();
        String title = Component.m_237115_(
                exact == null ? "tooltip.loli_labs.greenhouse.available" : "tooltip.loli_labs.greenhouse.item_title")
                .getString();
        int width = font.m_92895_(title) + 32;
        for (var row : rows)
            width = Math.max(width, Math.max(font.m_92895_(row.name), font.m_92895_(row.description)) + 54);
        width = Math.min(width, context.getScreenArea().width - 8);
        int wrap = width - 54, height = 47;
        for (var row : rows)
            height += Math.max(22, 11 + font.m_92923_(Component.m_237113_(row.description), wrap).size() * 10) + 3;
        int x = Math.min(context.getAbsMouseX() + 12, context.getScreenArea().width - width - 4),
                y = Math.max(4, Math.min(context.getAbsMouseY() - 12, context.getScreenArea().height - height - 4));
        if (fixedX >= 0) {
            x = fixedX;
            y = fixedY;
        }
        var pose = context.graphicsPose();
        pose.m_85836_();
        try {
            pose.m_85837_(0, 0, 400);
            frame.draw(context, x, y, width, height, brachy.modularui.theme.WidgetTheme.whiteTextShadow(0, 0,
                    brachy.modularui.api.drawable.IDrawable.NONE));
            var graphics = context.getGraphics();
            graphics.m_280056_(font, title, x + 16, y + 16, 0xffffffff, false);
            int rowY = y + 33;
            for (var row : rows) {
                graphics.m_280480_(row.icon, x + 16, rowY);
                graphics.m_280056_(font, row.name, x + 38, rowY, 0xffffffff, false);
                int descY = rowY + 10;
                var lines = font.m_92923_(Component.m_237113_(row.description), wrap);
                for (var line : lines) {
                    graphics.m_280649_(font, line, x + 38, descY, 0xffaaaaaa, false);
                    descY += 10;
                }
                rowY += Math.max(22, 11 + lines.size() * 10) + 3;
            }
        } finally {
            pose.m_85849_();
        }
    }
}
