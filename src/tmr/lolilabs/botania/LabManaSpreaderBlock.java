package tmr.lolilabs.botania;

import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;

/** Native Botania interactions, lens insertion, redstone control and placement rotation. */
public final class LabManaSpreaderBlock extends vazkii.botania.common.block.mana.ManaSpreaderBlock {

    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = net.minecraft.world.level.block.state.properties.BlockStateProperties.f_61372_;
    public final LabBotaniaTiers.Tier tier;

    public LabManaSpreaderBlock(LabBotaniaTiers.Tier tier) {
        super(Variant.GAIA, BlockBehaviour.Properties.m_284310_().m_60913_(2F, 10F).m_60955_());
        this.tier = tier;
    }

    @Override
    public BlockEntity m_142194_(BlockPos pos, BlockState state) {
        return new LabManaSpreaderBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return type == LabBotaniaBlocks.SPREADER_ENTITY.get() ? (world, pos, current, be) -> LabManaSpreaderBlockEntity
                .tick(world, pos, current, (LabManaSpreaderBlockEntity) be) : null;
    }
}
