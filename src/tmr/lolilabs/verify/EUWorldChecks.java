package tmr.lolilabs.verify;

import tmr.lolilabs.api.*;
import tmr.lolilabs.dimensions.*;
import tmr.lolilabs.dragonmight.*;
import tmr.lolilabs.energistics.*;
import tmr.lolilabs.energistics.machine.*;
import tmr.lolilabs.magically.*;
import tmr.lolilabs.utility.*;

import com.ic2ceu.icceu.IC2Modern;
import com.ic2ceu.icceu.common.energy.CableBlock;
import com.ic2ceu.icceu.common.energy.CableEntity;
import com.ic2ceu.icceu.common.energy.PacketNetwork;
import com.ic2ceu.icceu.common.machine.CreativeEnergyEntity;
import com.ic2ceu.icceu.common.machine.MachineBlock;
import com.ic2ceu.icceu.common.machine.MachineEntity;

import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder("loli_labs")
@PrefixGameTestTemplate(false)
public final class EUWorldChecks {

    private static void check(boolean pass, String why) {
        if (!pass) throw new IllegalStateException(why);
    }

    private static void wire(net.minecraft.server.level.ServerLevel world, BlockPos pos) {
        world.m_46597_(pos,
                ForgeRegistries.BLOCKS.getValue(new ResourceLocation("ic2modern:glass_fibre_cable")).m_49966_());
        var cable = (CableEntity) world.m_7702_(pos);
        cable.setOpen(Direction.WEST, true);
        cable.setOpen(Direction.EAST, true);
        CableBlock.refreshAround(world, pos);
    }

    @GameTest(m_177046_ = "empty", m_177042_ = 40)
    public static void ic2SolarToMatterThroughCable(GameTestHelper h) {
        var world = h.m_177100_();
        var sourcePos = h.m_177449_(new BlockPos(2, 2, 2));
        var cablePos = sourcePos.m_121945_(Direction.EAST);
        var targetPos = cablePos.m_121945_(Direction.EAST);
        world.m_46597_(sourcePos, IC2Modern.MACHINES.get("quantum_solar_panel").get().m_49966_()
                .m_61124_(MachineBlock.FACING, Direction.WEST));
        world.m_46597_(targetPos, LabMachines.DEFINITIONS.get("matter_solid_t0_x1").block.get().m_49966_());
        wire(world, cablePos);
        var source = (MachineEntity) world.m_7702_(sourcePos);
        var target = (LabMachineEntity) world.m_7702_(targetPos);
        source.energy.restore(30000);
        check(CableBlock.canConnect(world, cablePos, Direction.EAST), "Cable does not recognize EUReceiver");
        PacketNetwork.distribute(source);
        check(target.energy > 0 && source.energy.stored() < 30000, "IC2 -> Loli Labs native EU failed");
        target.energy = 250000;
        target.inventory.setStackInSlot(0,
                new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("thermal:slag"))));
        check(target.produceMatter(), "EU matter operation");
        check(target.energy == 0 && !target.inventory.getStackInSlot(12).m_41619_(), "EU matter conservation");
        System.out.println("LOLI_LABS_EU_WORLD_INPUT_OK cable=true nativeSolar=true slag=true");
        h.m_177412_();
    }

    @GameTest(m_177046_ = "empty", m_177042_ = 40)
    public static void loliSolarToIC2ThroughCable(GameTestHelper h) {
        var world = h.m_177100_();
        var sourcePos = h.m_177449_(new BlockPos(2, 2, 2));
        var cablePos = sourcePos.m_121945_(Direction.EAST);
        var targetPos = cablePos.m_121945_(Direction.EAST);
        world.m_46597_(sourcePos, LabMachines.DEFINITIONS.get("dispersion_solar_panel").block.get().m_49966_()
                .m_61124_(LabMachineBlock.FACING, Direction.WEST));
        world.m_46597_(targetPos, IC2Modern.CREATIVE_ENERGY.get().m_49966_());
        wire(world, cablePos);
        var source = (LabMachineEntity) world.m_7702_(sourcePos);
        var target = (CreativeEnergyEntity) world.m_7702_(targetPos);
        target.source = false;
        target.enabled = true;
        target.setVoltage(8192);
        check(CableBlock.canConnect(world, cablePos, Direction.WEST), "Cable does not recognize EUSource");
        source.serverTick();
        check(source.energy < source.generation(), "Loli Labs -> IC2 native EU failed");
        System.out.println("LOLI_LABS_EU_WORLD_OUTPUT_OK cable=true packets=true");
        h.m_177412_();
    }
}
