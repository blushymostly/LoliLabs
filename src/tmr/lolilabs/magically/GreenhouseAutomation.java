package tmr.lolilabs.magically;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/** A live view of the controller, available on all assembled outer parts. No private legacy slots. */
public final class GreenhouseAutomation implements IItemHandler {

    private final GreenhouseBlockEntity part;

    public GreenhouseAutomation(GreenhouseBlockEntity part) {
        this.part = part;
    }

    private GreenhouseCore domain() {
        var controller = part.core();
        return controller != null && (!part.isPart() || controller.isFormed()) ? controller.domain : null;
    }

    public int getSlots() {
        return 40;
    }

    public ItemStack getStackInSlot(int slot) {
        var domain = domain();
        return domain == null ? ItemStack.f_41583_ : domain.inventory.getStackInSlot(slot);
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        var domain = domain();
        return domain == null ? stack : domain.inventory.insertItem(slot, stack, simulate);
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        var domain = domain();
        return domain == null || slot < 8 ? ItemStack.f_41583_ : domain.inventory.extractItem(slot, amount, simulate);
    }

    public int getSlotLimit(int slot) {
        var domain = domain();
        return domain == null ? 64 : domain.inventory.getSlotLimit(slot);
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        var domain = domain();
        return domain != null && domain.inventory.isItemValid(slot, stack);
    }
}
