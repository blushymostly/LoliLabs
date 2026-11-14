package tmr.lolilabs.energistics.machine;

import tmr.lolilabs.energistics.LabMachineEntity;
import tmr.lolilabs.energistics.LabMachines;

import net.minecraft.world.item.ItemStack;

public final class UnifierBehaviour extends GeneratorBehaviour {

    @Override
    int automationSlots() {
        return 36;
    }

    @Override
    int useSlots() {
        return 27;
    }

    @Override
    public long generation(LabMachineEntity machine) {
        long sum = 0;
        for (int i = 0; i < machine.inventory.getSlots(); i++) {
            ItemStack stack = machine.inventory.getStackInSlot(i);
            var panel = LabMachines.panel(stack);
            if (panel != null)
                sum = LabMachines.addSafe(sum, Math.multiplyExact(panel.rate(), stack.m_41613_()));
        }
        return sum;
    }

    @Override
    public boolean workingLight(LabMachineEntity machine) {
        return machine.enabled && generation(machine) > 0;
    }

    @Override
    public boolean accepts(LabMachineEntity machine, ItemStack stack) {
        return LabMachines.panel(stack) != null;
    }
}
