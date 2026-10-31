package tmr.lolilabs;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.zeith.solarflux.block.SolarPanelBlock;
import org.zeith.solarflux.panels.SolarPanel;

public final class SparklingBlock extends SolarPanelBlock {
    public SparklingBlock(SolarPanel panel, ResourceLocation id) { super(panel, id); }

    @Override
    public VoxelShape m_5940_(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.m_83144_();
    }

    @Override
    public VoxelShape m_5939_(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.m_83144_();
    }

    @Override
    public VoxelShape recalcShape(BlockGetter level, BlockPos pos) {
        return Shapes.m_83144_();
    }
}
