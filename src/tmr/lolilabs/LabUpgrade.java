package tmr.lolilabs;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class LabUpgrade extends Item {
    private final String key;
    private final int value;
    private final String target;
    public LabUpgrade(String key, int value, String target) { super(new Properties()); this.key = key; this.value = value; this.target = target; }
    @Override public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack upgrade = player.m_21120_(hand);
        ItemStack equipment = player.m_21120_(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        boolean compatible = switch (target) {
            case "sword" -> equipment.m_41720_() instanceof LabSword;
            case "armor" -> equipment.m_41720_() instanceof LabArmor;
            default -> equipment.m_41720_() instanceof LabSword || equipment.m_41720_() instanceof LabArmor
                    || equipment.m_41720_() instanceof LabBattery && !(equipment.m_41720_() instanceof LabEternalGenerator);
        };
        if (!compatible) return InteractionResultHolder.m_19100_(upgrade);
        if (level.f_46443_) return InteractionResultHolder.m_19090_(upgrade);
        var tag = equipment.m_41784_();
        boolean booleanValue = value == 0;
        if (booleanValue ? tag.m_128471_(key) : tag.m_128451_(key) >= value) return InteractionResultHolder.m_19100_(upgrade);
        if (booleanValue) tag.m_128379_(key, true); else tag.m_128405_(key, value);
        if (!player.m_7500_()) upgrade.m_41774_(1);
        player.m_5661_(Component.m_237110_("message.loli_labs.upgrade_installed", upgrade.m_41786_()), true);
        return InteractionResultHolder.m_19090_(upgrade);
    }
    @Override public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) { tooltip.add(Component.m_237115_("tooltip.loli_labs.upgrade")); }
}
