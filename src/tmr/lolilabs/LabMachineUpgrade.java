package tmr.lolilabs;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

import mekanism.api.Upgrade;

public final class LabMachineUpgrade extends mekanism.common.item.ItemUpgrade {

    private final int units;

    public LabMachineUpgrade(Upgrade type, int units) {
        super(type, new Properties());
        this.units = Math.min(type.getMax(), units);
    }

    @Override
    public InteractionResult m_6225_(UseOnContext context) {
        var player = context.m_43723_();
        var level = context.m_43725_();
        if (player == null || !player.m_6144_()) return InteractionResult.PASS;
        var tile = level.m_7702_(context.m_8083_());
        if (!(tile instanceof mekanism.common.tile.interfaces.IUpgradeTile machine) || !machine.supportsUpgrades())
            return InteractionResult.PASS;
        var type = getUpgradeType(context.m_43722_());
        var component = machine.getComponent();
        if (!component.supports(type) || component.getUpgrades(type) >= type.getMax()) return InteractionResult.FAIL;
        if (!level.m_7966_(player, context.m_8083_()) ||
                !mekanism.api.security.ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile))
            return InteractionResult.FAIL;
        if (!level.f_46443_ && component.addUpgrades(type, units) > 0) {
            tile.m_6596_();
            if (!player.m_7500_()) context.m_43722_().m_41774_(1);
        }
        return InteractionResult.m_19078_(level.f_46443_);
    }

    @Override
    public void m_7373_(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.Level level,
                        java.util.List<net.minecraft.network.chat.Component> tooltip,
                        net.minecraft.world.item.TooltipFlag flag) {
        super.m_7373_(stack, level, tooltip, flag);
        tooltip.add(net.minecraft.network.chat.Component.m_237110_("tooltip.loli_labs.machine_upgrade", units));
    }
}
