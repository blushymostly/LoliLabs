package tmr.lolilabs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;

public final class LabMachineBlock extends BaseEntityBlock {
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING=com.ic2ceu.icceu.MachineBlock.FACING;
    public static net.minecraft.core.Direction rotationFacing(int kind,net.minecraft.core.Direction selected){return kind==2?selected:selected.m_122424_();}
    public final LabMachines.Definition definition;
    public LabMachineBlock(LabMachines.Definition definition,BlockBehaviour.Properties properties){super(properties);this.definition=definition;m_49959_(m_49966_().m_61124_(FACING,net.minecraft.core.Direction.NORTH));}
    @Override protected void m_7926_(StateDefinition.Builder<Block,BlockState> builder){builder.m_61104_(FACING);}
    @Override public BlockState m_5573_(net.minecraft.world.item.context.BlockPlaceContext context){var front=context.m_8125_().m_122424_();if(context.m_43723_()!=null&&context.m_43723_().m_6144_()&&context.m_43725_().m_8055_(context.m_8083_().m_121945_(context.m_43719_().m_122424_())).m_60734_() instanceof com.ic2ceu.icceu.CableBlock)front=context.m_43719_();return m_49966_().m_61124_(FACING,front);}
    @Override public RenderShape m_7514_(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity m_142194_(BlockPos pos,BlockState state){return new LabMachineEntity(pos,state);}
    @Override public MenuProvider m_7246_(BlockState state,Level level,BlockPos pos){return level.m_7702_(pos) instanceof LabMachineEntity machine?machine:null;}
    @Override public InteractionResult m_6227_(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        var held=player.m_21120_(hand);var context=new net.minecraft.world.item.context.UseOnContext(player,hand,hit);
        if(held.m_41720_() instanceof com.ic2ceu.icceu.UpgradeItem upgrade&&upgrade.directional())return upgrade.m_6225_(context);
        if(held.m_41720_() instanceof com.ic2ceu.icceu.MachineTool tool){
            if(!level.m_7966_(player,pos))return InteractionResult.FAIL;
            if(tool.electric()&&tool.charge(held)<tool.energyUse()&&!player.m_7500_())return InteractionResult.FAIL;
            var location=hit.m_82450_();var selected=com.ic2ceu.icceu.ToolGrid.side(hit.m_82434_(),location.f_82479_-pos.m_123341_(),location.f_82480_-pos.m_123342_(),location.f_82481_-pos.m_123343_());
            if(tool.wrench()&&player.m_6144_()){
                var front=rotationFacing(definition.kind,selected);if(state.m_61143_(FACING)==front)return InteractionResult.FAIL;
                if(!level.f_46443_){level.m_46597_(pos,state.m_61124_(FACING,front));com.ic2ceu.icceu.CableBlock.refreshAround(level,pos);com.ic2ceu.icceu.tool.ToolHelper.onActionDone(player,held,level,location);}
                return InteractionResult.m_19078_(level.f_46443_);
            }
            if(level.m_7702_(pos) instanceof LabMachineEntity machine&&(tool.definition.name().equals("mallet")||tool.definition.name().equals("hammer"))){
                if(!level.f_46443_){if(tool.definition.name().equals("mallet"))machine.enabled=!machine.enabled;else machine.muted=!machine.muted;machine.syncSettings();com.ic2ceu.icceu.tool.ToolHelper.onActionDone(player,held,level,location);}
                return InteractionResult.m_19078_(level.f_46443_);
            }
            var result=tool.m_6225_(context);if(result!=InteractionResult.PASS)return result;
            if(tool.wrench()||tool.definition.behaviors().stream().anyMatch(b->!b.shouldOpenUIAfterUse(context)))return InteractionResult.PASS;
        }
        if(!level.f_46443_ && level.m_7702_(pos) instanceof LabMachineEntity machine && player instanceof net.minecraft.server.level.ServerPlayer server){machine.beforeOpen(server);brachy.modularui.factory.BlockEntityUIFactory.INSTANCE.open(server,pos);}
        return InteractionResult.m_19078_(level.f_46443_);
    }
    @Override public void m_6807_(BlockState state,Level level,BlockPos pos,BlockState old,boolean moving){super.m_6807_(state,level,pos,old,moving);if(!level.f_46443_)com.ic2ceu.icceu.CableBlock.refreshAround(level,pos);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level,BlockState state,BlockEntityType<T> type){
        return level.f_46443_?null:m_152132_(type,LabMachines.ENTITY.get(),LabMachineEntity::tick);
    }
}
