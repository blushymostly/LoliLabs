package tmr.lolilabs.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Native packets and pool mechanics, retaining our save ID and tier capacities. */
public final class LabManaPoolBlockEntity extends vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity {

    public final LabBotaniaTiers.Tier tier;

    public LabManaPoolBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        tier = ((LabManaPoolBlock) state.m_60734_()).tier;
    }

    @Override
    public BlockEntityType<?> m_58903_() {
        return LabBotaniaBlocks.POOL_ENTITY.get();
    }

    @Override
    public int getMaxMana() {
        return ((LabManaPoolBlock) m_58900_().m_60734_()).tier.poolMana;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LabManaPoolBlockEntity pool) {
        if (level.f_46443_) clientTick(level, pos, state, pool);
        else serverTick(level, pos, state, pool);
    }
}
