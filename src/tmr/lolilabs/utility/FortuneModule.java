package tmr.lolilabs.utility;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

public final class FortuneModule extends Item {

    public final int kind, level;

    public FortuneModule(int kind, int level) {
        super(new Properties().m_41487_(1).m_41497_(Rarity.COMMON));
        this.kind = kind;
        this.level = level;
    }

    @Override
    public boolean m_5812_(ItemStack stack) {
        return kind == 0;
    }

    @Override
    public void m_7373_(ItemStack stack, net.minecraft.world.level.Level world, java.util.List<Component> lines,
                        TooltipFlag flags) {
        super.m_7373_(stack, world, lines, flags);
        if (kind == 0) {
            lines.add(Component.m_237115_("tooltip.loli_labs.fortune.install")
                    .m_130940_(net.minecraft.ChatFormatting.YELLOW)
                    .m_7220_(Component.m_237115_("block.loli_labs.fortune_transformer")
                            .m_130940_(net.minecraft.ChatFormatting.LIGHT_PURPLE)));
            lines.add(Component.m_237115_("tooltip.loli_labs.fortune.ratio")
                    .m_130940_(net.minecraft.ChatFormatting.YELLOW)
                    .m_7220_(Component.m_237110_("tooltip.loli_labs.fortune.ratio_value", level)
                            .m_130940_(net.minecraft.ChatFormatting.WHITE)));
        } else lines.add(Component.m_237110_("tooltip.loli_labs.fortune.module_" + kind, level).m_6881_()
                .m_130940_(net.minecraft.ChatFormatting.GRAY));
    }
}
