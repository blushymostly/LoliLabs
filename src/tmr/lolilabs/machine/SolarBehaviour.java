package tmr.lolilabs.machine;

import tmr.lolilabs.LabMachineEntity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public final class SolarBehaviour extends GeneratorBehaviour {

    @Override
    int automationSlots() {
        return 6;
    }

    @Override
    int useSlots() {
        return 6;
    }

    @Override
    public long generation(LabMachineEntity machine) {
        return machine.definition.rate();
    }

    @Override
    public boolean workingLight(LabMachineEntity machine) {
        return machine.enabled;
    }

    @Override
    public void beforeOpen(LabMachineEntity machine, Player player) {
        // Old 27-slot panels kept surplus past the six chargers until the owner opened the UI.
        for (int i = 6; i < machine.inventory.getSlots(); i++) {
            ItemStack stack = machine.inventory.getStackInSlot(i);
            if (!stack.m_41619_()) {
                machine.inventory.setStackInSlot(i, ItemStack.f_41583_);
                ItemHandlerHelper.giveItemToPlayer(player, stack);
            }
        }
    }
}
