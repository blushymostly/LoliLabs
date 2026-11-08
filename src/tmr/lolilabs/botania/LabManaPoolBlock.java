package tmr.lolilabs.botania;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;

/** Native pool collision, infusion, dye, wand controls and item charging. */
public final class LabManaPoolBlock extends vazkii.botania.common.block.mana.ManaPoolBlock {
    public final LabBotaniaTiers.Tier tier;
    public LabManaPoolBlock(LabBotaniaTiers.Tier tier){super(Variant.DEFAULT,BlockBehaviour.Properties.m_284310_().m_60913_(2F,10F).m_60955_());this.tier=tier;}
    @Override public BlockEntity m_142194_(BlockPos pos,BlockState state){return new LabManaPoolBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level,BlockState state,BlockEntityType<T> type){return type==LabBotaniaBlocks.POOL_ENTITY.get()?(world,pos,current,be)->LabManaPoolBlockEntity.tick(world,pos,current,(LabManaPoolBlockEntity)be):null;}
}
