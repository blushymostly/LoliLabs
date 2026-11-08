package tmr.lolilabs.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;

public final class GreenhouseCoreBlock extends BaseEntityBlock {

    public static final net.minecraft.world.level.block.state.properties.BooleanProperty FORMED = net.minecraft.world.level.block.state.properties.BooleanProperty
            .m_61465_("formed");
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty LIT = net.minecraft.world.level.block.state.properties.BooleanProperty
            .m_61465_("lit");
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = com.ic2ceu.icceu.MachineBlock.FACING;

    public GreenhouseCoreBlock() {
        super(BlockBehaviour.Properties.m_284310_().m_60913_(5F, 20F).m_60953_(state -> state.m_61143_(LIT) ? 15 : 0));
        m_49959_(m_49966_().m_61124_(FACING, net.minecraft.core.Direction.NORTH).m_61124_(FORMED, false).m_61124_(LIT,
                false));
    }

    @Override
    protected void m_7926_(StateDefinition.Builder<Block, BlockState> builder) {
        builder.m_61104_(FACING, FORMED, LIT);
    }

    @Override
    public BlockState m_5573_(net.minecraft.world.item.context.BlockPlaceContext context) {
        return m_49966_().m_61124_(FACING, context.m_8125_().m_122424_());
    }

    @Override
    public RenderShape m_7514_(BlockState state) {
        return state.m_61143_(GreenhouseCoreBlock.FORMED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public BlockEntity m_142194_(BlockPos pos, BlockState state) {
        return new GreenhouseBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.f_46443_ ? null :
                m_152132_(type, LabBotaniaBlocks.GREENHOUSE_ENTITY.get(), GreenhouseBlockEntity::tick);
    }

    @Override
    public InteractionResult m_6227_(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                     BlockHitResult hit) {
        if (!level.f_46443_ && level.m_7702_(pos) instanceof GreenhouseBlockEntity be &&
                player instanceof net.minecraft.server.level.ServerPlayer server) {
            if (be.isFormed()) brachy.modularui.factory.BlockEntityUIFactory.INSTANCE.open(server, pos);
            else player.m_5661_(net.minecraft.network.chat.Component.m_237115_("gui.loli_labs.greenhouse.incomplete"),
                    true);
        }
        return InteractionResult.m_19078_(level.f_46443_);
    }

    @Override
    public void m_6810_(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.m_60713_(next.m_60734_()) && level.m_7702_(pos) instanceof GreenhouseBlockEntity be) {
            be.updateFormedBlocks(false);
            for (int i = 0; i < GreenhouseBlockEntity.SLOTS; i++) {
                Block.m_49840_(level, pos, be.inventory.getStackInSlot(i));
                be.inventory.setStackInSlot(i, net.minecraft.world.item.ItemStack.f_41583_);
            }
        }
        super.m_6810_(state, level, pos, next, moving);
    }
}
