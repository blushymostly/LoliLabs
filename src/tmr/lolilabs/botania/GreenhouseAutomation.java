package tmr.lolilabs.botania;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/** A live view of the controller, available on all assembled outer parts. No private legacy slots. */
public final class GreenhouseAutomation implements IItemHandler {
    private final GreenhouseBlockEntity part;
    public GreenhouseAutomation(GreenhouseBlockEntity part){this.part=part;}
    private GreenhouseBlockEntity core(){var core=part.core();return core!=null&&(!part.isPart()||core.isFormed())?core:null;}
    public int getSlots(){return 40;}
    public ItemStack getStackInSlot(int slot){var core=core();return core==null?ItemStack.f_41583_:core.inventory.getStackInSlot(slot);}
    public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){var core=core();return core==null?stack:core.inventory.insertItem(slot,stack,simulate);}
    public ItemStack extractItem(int slot,int amount,boolean simulate){var core=core();return core==null||slot<8?ItemStack.f_41583_:core.inventory.extractItem(slot,amount,simulate);}
    public int getSlotLimit(int slot){var core=core();return core==null?64:core.inventory.getSlotLimit(slot);}
    public boolean isItemValid(int slot,ItemStack stack){var core=core();return core!=null&&core.inventory.isItemValid(slot,stack);}
}
