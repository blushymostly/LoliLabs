package tmr.lolilabs.botania;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Real Botania simulation: aimed bursts, binding, collision, lenses, pingback and adjacent-pool input. */
public final class LabManaSpreaderBlockEntity extends vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity {
    public final LabBotaniaTiers.Tier tier;
    public LabManaSpreaderBlockEntity(BlockPos pos,BlockState state){super(pos,state);tier=((LabManaSpreaderBlock)state.m_60734_()).tier;}
    @Override public BlockEntityType<?> m_58903_(){return LabBotaniaBlocks.SPREADER_ENTITY.get();}
    @Override public int getMaxMana(){return ((LabManaSpreaderBlock)m_58900_().m_60734_()).tier.spreaderCapacity;}
    public static void tick(Level level,BlockPos pos,BlockState state,LabManaSpreaderBlockEntity be){commonTick(level,pos,state,be);}
    @Override public void readPacketNBT(CompoundTag tag){super.readPacketNBT(tag);if(!tag.m_128441_("rotationX"))legacyDirection(m_58900_());}
    private void legacyDirection(BlockState state){
        var facing=state.m_61138_(LabManaSpreaderBlock.FACING)?state.m_61143_(LabManaSpreaderBlock.FACING):Direction.NORTH;
        rotationX=switch(facing){case NORTH->270;case SOUTH->90;case EAST->180;default->0;};rotationY=facing==Direction.UP?90:facing==Direction.DOWN?-90:0;
    }
}
