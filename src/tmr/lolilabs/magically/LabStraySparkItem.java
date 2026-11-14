package tmr.lolilabs.magically;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;

public final class LabStraySparkItem extends Item {

    public final LabBotaniaTiers.Tier tier;

    public LabStraySparkItem(LabBotaniaTiers.Tier tier) {
        super(new Item.Properties());
        this.tier = tier;
    }

    @Override
    public InteractionResult m_6225_(UseOnContext ctx) {
        var level = ctx.m_43725_();
        var pos = ctx.m_8083_();
        var stack = ctx.m_43722_();
        var attached = vazkii.botania.xplat.XplatAbstractions.INSTANCE.findSparkAttachable(level, pos,
                level.m_8055_(pos), level.m_7702_(pos), net.minecraft.core.Direction.UP);
        if (attached == null || !attached.canAttachSpark(stack) || attached.getAttachedSpark() != null)
            return InteractionResult.PASS;
        if (!level.f_46443_) {
            var spark = LabBotaniaBlocks.SPARK_TYPES.get(tier).get().m_20615_(level);
            if (spark == null) return InteractionResult.FAIL;
            spark.m_6034_(pos.m_123341_() + .5, pos.m_123342_() + 1.25, pos.m_123343_() + .5);
            var player = ctx.m_43723_();
            var other = player == null ? ItemStack.f_41583_ :
                    player.m_21120_(ctx.m_43724_() == net.minecraft.world.InteractionHand.MAIN_HAND ?
                            net.minecraft.world.InteractionHand.OFF_HAND :
                            net.minecraft.world.InteractionHand.MAIN_HAND);
            if (other.m_41720_() instanceof DyeItem dye) spark.setNetwork(dye.m_41089_());
            if (!level.m_7967_(spark)) return InteractionResult.FAIL;
            attached.attachSpark(spark);
            if (player == null || !player.m_7500_()) {
                stack.m_41774_(1);
                if (other.m_41720_() instanceof DyeItem) other.m_41774_(1);
            }
        }
        return InteractionResult.m_19078_(level.f_46443_);
    }
}
