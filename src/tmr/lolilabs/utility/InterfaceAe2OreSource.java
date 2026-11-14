package tmr.lolilabs.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

/** Prod adapter: block interfaces and cable-part interfaces on every side. */
public final class InterfaceAe2OreSource implements Ae2OreSource {

    private final Level level;
    private final BlockPos pos;

    public InterfaceAe2OreSource(Level level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    public static appeng.helpers.InterfaceLogicHost interfaceAt(BlockEntity be, Direction face) {
        if (be instanceof appeng.helpers.InterfaceLogicHost host) return host;
        if (be instanceof appeng.api.parts.IPartHost parts &&
                parts.getPart(face) instanceof appeng.helpers.InterfaceLogicHost host)
            return host;
        return null;
    }

    @Override
    public void pull(IItemHandler inventory) {
        if (level == null) return;
        for (Direction side : Direction.values()) {
            var host = interfaceAt(level.m_7702_(pos.m_121945_(side)), side.m_122424_());
            if (host == null) continue;
            var stock = host.getInterfaceLogic().getStorage();
            for (int s = 0; s < stock.size(); s++) {
                var stored = stock.getStack(s);
                if (stored == null || !(stored.what() instanceof appeng.api.stacks.AEItemKey key)) continue;
                var offered = key.toStack((int) Math.min(64, stored.amount()));
                if (!FortuneEntity.ore(offered)) continue;
                var left = offered.m_41777_();
                for (int i = 0; i < 27 && !left.m_41619_(); i++) left = inventory.insertItem(i, left, true);
                int count = offered.m_41613_() - left.m_41613_();
                if (count == 0) continue;
                int taken = (int) stock.extract(s, key, count, appeng.api.config.Actionable.MODULATE);
                var extracted = key.toStack(taken);
                for (int i = 0; i < 27 && !extracted.m_41619_(); i++)
                    extracted = inventory.insertItem(i, extracted, false);
                if (!extracted.m_41619_()) {
                    long returned = stock.insert(s, key, extracted.m_41613_(), appeng.api.config.Actionable.MODULATE);
                    extracted.m_41774_((int) returned);
                    if (!extracted.m_41619_()) Block.m_49840_(level, pos, extracted);
                }
            }
        }
    }
}
