package tmr.lolilabs.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class GreenhousePartBlock extends BaseEntityBlock {
    public final GreenhouseStructure.Role role;

    public GreenhousePartBlock(GreenhouseStructure.Role role, BlockBehaviour.Properties properties) {
        super(properties.m_60953_(state->state.m_61143_(GreenhouseCoreBlock.LIT)?15:0));
        this.role = role;
        m_49959_(m_49966_().m_61124_(GreenhouseCoreBlock.FORMED,false).m_61124_(GreenhouseCoreBlock.LIT,false));
    }

    @Override protected void m_7926_(net.minecraft.world.level.block.state.StateDefinition.Builder<Block,BlockState> builder){builder.m_61104_(GreenhouseCoreBlock.FORMED,GreenhouseCoreBlock.LIT);}
    @Override public RenderShape m_7514_(BlockState state) { return state.m_61143_(GreenhouseCoreBlock.FORMED)?RenderShape.INVISIBLE:RenderShape.MODEL; }

    @Override public BlockEntity m_142194_(BlockPos pos, BlockState state) { return new GreenhouseBlockEntity(pos, state); }

    @Override public InteractionResult m_6227_(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.f_46443_ && level.m_7702_(pos) instanceof GreenhouseBlockEntity part && player instanceof net.minecraft.server.level.ServerPlayer server) {
            var core = part.core();
            if (core != null && core.isFormed()) brachy.modularui.factory.BlockEntityUIFactory.INSTANCE.open(server, core.m_58899_());
            else player.m_5661_(net.minecraft.network.chat.Component.m_237115_("gui.loli_labs.greenhouse.incomplete"), true);
        }
        return InteractionResult.m_19078_(level.f_46443_);
    }
}
