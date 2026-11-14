package tmr.lolilabs.machine;

import tmr.lolilabs.LabMachineBlock;
import tmr.lolilabs.LabMachineEntity;
import tmr.lolilabs.LabMachines;

import com.ic2ceu.icceu.api.item.ChargeableItem;
import com.ic2ceu.icceu.common.energy.PacketNetwork;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

/** Shared EU output for solar panels and the unifier. */
abstract class GeneratorBehaviour implements MachineBehaviour {

    /** Slots exposed to automation. */
    abstract int automationSlots();

    /** Slots the player can fill and the machine will charge. */
    abstract int useSlots();

    @Override
    public void serverTick(LabMachineEntity machine) {
        if (machine.euLevel() != null) {
            var state = machine.m_58900_();
            boolean lit = workingLight(machine);
            if (state.m_61143_(LabMachineBlock.ACTIVE) != lit)
                machine.euLevel().m_7731_(machine.euPos(), state.m_61124_(LabMachineBlock.ACTIVE, lit), 3);
        }
        if (!machine.enabled) return;
        machine.energy = Math.min(machine.capacity(), LabMachines.addSafe(machine.energy, generation(machine)));
        for (int i = 0; i < useSlots() && machine.energy > 0; i++) machine.chargeItem(i);
        machine.prepareEU();
        PacketNetwork.distribute(machine);
    }

    @Override
    public long capacity(LabMachineEntity machine) {
        return Math.max(1_000_000, Math.min((Long.MAX_VALUE / 40) / 15625, generation(machine)) * 15625);
    }

    @Override
    public long outputVoltage(LabMachineEntity machine) {
        return machine.definition.voltage.get();
    }

    @Override
    public boolean emitsEnergy(LabMachineEntity machine, Direction side) {
        return machine.enabled && side != Direction.UP;
    }

    @Override
    public boolean acceptsEnergy(LabMachineEntity machine, Direction side) {
        return false;
    }

    @Override
    public boolean connectsEU(LabMachineEntity machine, Direction side) {
        return side != Direction.UP;
    }

    @Override
    public boolean tracksActive() {
        return true;
    }

    @Override
    public IItemHandler accessibleInventory(LabMachineEntity machine) {
        return new RangedWrapper(machine.inventory, 0, automationSlots());
    }

    @Override
    public int slotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean validSlot(LabMachineEntity machine, int slot, ItemStack stack) {
        return slot < useSlots() && accepts(machine, stack);
    }

    @Override
    public boolean accepts(LabMachineEntity machine, ItemStack stack) {
        return stack.m_41720_() instanceof ChargeableItem;
    }

    @Override
    public long displayCost(LabMachineEntity machine) {
        return 0;
    }

    @Override
    public boolean produce(LabMachineEntity machine) {
        return false;
    }

    @Override
    public void beforeOpen(LabMachineEntity machine, Player player) {}
}
