package tmr.lolilabs.energistics.machine;

import tmr.lolilabs.api.WorkableProgressScale;
import tmr.lolilabs.energistics.LabMachineEntity;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/** Per-kind machine logic. The entity keeps one block-entity type and delegates here. */
public interface MachineBehaviour {

    int UI_PROGRESS_SCALE = WorkableProgressScale.UI;

    void serverTick(LabMachineEntity machine);

    long generation(LabMachineEntity machine);

    long capacity(LabMachineEntity machine);

    long outputVoltage(LabMachineEntity machine);

    boolean emitsEnergy(LabMachineEntity machine, Direction side);

    boolean acceptsEnergy(LabMachineEntity machine, Direction side);

    boolean connectsEU(LabMachineEntity machine, Direction side);

    boolean workingLight(LabMachineEntity machine);

    boolean tracksActive();

    IItemHandler accessibleInventory(LabMachineEntity machine);

    int slotLimit(int slot);

    boolean validSlot(LabMachineEntity machine, int slot, ItemStack stack);

    boolean accepts(LabMachineEntity machine, ItemStack stack);

    /** Cost shown in the meter. Matter reports a full batch; generators report zero. */
    long displayCost(LabMachineEntity machine);

    /** Runs one Energy-Paid Craft step. Generators do nothing. */
    boolean produce(LabMachineEntity machine);

    void beforeOpen(LabMachineEntity machine, Player player);
}
