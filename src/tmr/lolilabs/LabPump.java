package tmr.lolilabs;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.fluids.FluidUtil;

public final class LabPump extends Item {

    public LabPump() {
        super(new Properties().m_41487_(1));
    }

    @Override
    public InteractionResult m_6225_(UseOnContext context) {
        var player = context.m_43723_();
        if (player == null) return InteractionResult.PASS;
        if (context.m_43725_().f_46443_) return InteractionResult.SUCCESS;
        var stack = context.m_43722_();
        int radius = stack.m_41784_().m_128451_("LabPumpRadius");
        if (player.m_6144_()) {
            radius = (radius + 1) % 3;
            stack.m_41784_().m_128405_("LabPumpRadius", radius);
            player.m_5661_(Component.m_237110_("message.loli_labs.pump_radius", 2 * radius + 1), true);
            return InteractionResult.CONSUME;
        }
        int pumped = 0;
        var level = context.m_43725_();
        var inventory = player.m_150109_();
        BlockPos center = context.m_8083_();
        for (int x = -radius; x <= radius; x++)
            for (int y = -radius; y <= radius; y++) for (int z = -radius; z <= radius; z++) {
                BlockPos pos = center.m_7918_(x, y, z);
                if (!level.m_7966_(player, pos) || !player.m_36204_(pos, context.m_43719_(), stack)) continue;
                for (int slot = 0; slot < inventory.m_6643_(); slot++) {
                    var container = inventory.m_8020_(slot);
                    if (container.m_41619_() || FluidUtil.getFluidHandler(container).isPresent() == false) continue;
                    var result = FluidUtil.tryPickUpFluid(container, player, level, pos, context.m_43719_());
                    if (result.isSuccess()) {
                        inventory.m_6836_(slot, result.getResult());
                        pumped += 1000;
                        break;
                    }
                }
            }
        player.m_5661_(Component.m_237110_("message.loli_labs.pumped", pumped), true);
        return InteractionResult.CONSUME;
    }
}
