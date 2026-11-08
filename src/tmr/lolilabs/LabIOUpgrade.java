package tmr.lolilabs;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;

/** Adapts directional IC2 cards to native Mekanism side configuration. */
public final class LabIOUpgrade extends Item {

    private final boolean output;
    private final TransmissionType type;

    public LabIOUpgrade(boolean output, TransmissionType type) {
        super(new Properties());
        this.output = output;
        this.type = type;
    }

    @Override
    public InteractionResult m_6225_(UseOnContext context) {
        var level = context.m_43725_();
        var player = context.m_43723_();
        var pos = context.m_8083_();
        var tile = level.m_7702_(pos);
        if (player == null || !(tile instanceof ISideConfiguration machine) || !machine.getConfig().supports(type))
            return InteractionResult.PASS;
        if (!level.m_7966_(player, pos) ||
                !mekanism.api.security.ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile))
            return InteractionResult.FAIL;
        var config = machine.getConfig().getConfig(type);
        var side = mekanism.api.RelativeSide.fromDirections(machine.getDirection(), context.m_43719_());
        var data = output ? DataType.OUTPUT : DataType.INPUT;
        if (!config.isSideEnabled(side) || !config.getSupportedDataTypes().contains(data))
            return InteractionResult.FAIL;
        if (level.f_46443_) return InteractionResult.SUCCESS;
        config.setDataType(data, side);
        if (output && config.canEject()) config.setEjecting(true);
        machine.getConfig().sideChanged(type, side);
        tile.m_6596_();
        if (!player.m_7500_()) context.m_43722_().m_41774_(1);
        return InteractionResult.CONSUME;
    }
}
