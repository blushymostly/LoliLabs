package tmr.lolilabs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LoliLabs.MOD_ID)
public final class LabMiningEvents {
    private static final ThreadLocal<Boolean> BREAKING = ThreadLocal.withInitial(() -> false);
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void mined(BlockEvent.BreakEvent event) {
        if (BREAKING.get() || !(event.getPlayer() instanceof ServerPlayer player) || player.m_7500_()) return;
        var stack = player.m_21205_();
        if (!(stack.m_41720_() instanceof LabMiningTool tool) || !tool.area || !stack.m_41784_().m_128471_("LabAreaMining")) return;
        var hit = player.m_19907_(6D, 1F, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.m_6662_() != HitResult.Type.BLOCK) return;
        var axis = blockHit.m_82434_().m_122434_();
        BlockPos center = event.getPos();
        float hardness = event.getState().m_60800_(event.getLevel(), center);
        if (hardness < 0) return;
        BREAKING.set(true);
        try {
            for (int a = -1; a <= 1; a++) for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) continue;
                BlockPos pos = switch (axis) {
                    case X -> center.m_7918_(0, a, b);
                    case Y -> center.m_7918_(a, 0, b);
                    case Z -> center.m_7918_(a, b, 0);
                };
                var level = player.m_9236_();
                var state = level.m_8055_(pos);
                float nextHardness = state.m_60800_(level, pos);
                if (state.m_60795_() || nextHardness < 0 || nextHardness > Math.max(1, hardness * 2)) continue;
                var energy = stack.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY).orElse(null);
                if (energy == null || energy.getEnergyStored() < 1000) break;
                // Server game mode runs normal protection events and native tool energy/drops for every block.
                if (level.m_7966_(player, pos)) player.f_8941_.m_9280_(pos);
            }
        } finally { BREAKING.set(false); }
    }
}
