package tmr.lolilabs.utility;

import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;

public final class FortuneBlock extends BaseEntityBlock {

    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = net.minecraft.world.level.block.state.properties.DirectionProperty
            .m_61546_("facing", d -> d != Direction.UP && d != Direction.DOWN);

    public FortuneBlock() {
        super(BlockBehaviour.Properties.m_284310_().m_60913_(5, 20).m_60955_());
        m_49959_(m_49966_().m_61124_(FACING, Direction.NORTH));
    }

    @Override
    protected void m_7926_(StateDefinition.Builder<Block, BlockState> builder) {
        builder.m_61104_(FACING);
    }

    @Override
    public BlockState m_5573_(net.minecraft.world.item.context.BlockPlaceContext c) {
        return m_49966_().m_61124_(FACING, c.m_8125_().m_122424_());
    }

    @Override
    public RenderShape m_7514_(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity m_142194_(BlockPos pos, BlockState state) {
        return new FortuneEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.f_46443_ || type != LabUtility.ENTITY.get() ? null : (w, p, s, be) -> ((FortuneEntity) be).tick();
    }

    @Override
    public InteractionResult m_6227_(BlockState s, Level level, BlockPos pos, Player player, InteractionHand hand,
                                     BlockHitResult hit) {
        if (!level.f_46443_ && player instanceof net.minecraft.server.level.ServerPlayer server)
            brachy.modularui.factory.BlockEntityUIFactory.INSTANCE.open(server, pos);
        return InteractionResult.m_19078_(level.f_46443_);
    }

    @Override
    public void m_6810_(BlockState s, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!s.m_60713_(next.m_60734_()) && level.m_7702_(pos) instanceof FortuneEntity be) be.dropContents();
        super.m_6810_(s, level, pos, next, moving);
    }
}
