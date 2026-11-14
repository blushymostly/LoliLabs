package tmr.lolilabs.utility;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Reserved ore, rolled drops, and progress. Output blockage never rerolls or voids a job. */
public final class FortuneJob {

    public final List<ItemStack> reserved = new ArrayList<>(), pending = new ArrayList<>();
    public int progress, totalWork, doneWork;
    public int fortune, multiplier;
    public boolean smelt;

    public boolean working() {
        return totalWork > 0 || !reserved.isEmpty() || !pending.isEmpty();
    }

    /** Reserve one batch, roll each reserved ore once, then flush. A full output keeps the pending stacks. */
    public boolean tickStep(OreDropper dropper, Inventory inventory) {
        if (!working()) begin(inventory);
        if (!working()) return false;
        int ticks = LabUtility.TICKS.get();
        progress = Math.min(ticks, progress + 1);
        int budget = 64;
        while (!reserved.isEmpty() && budget-- > 0) {
            var stack = reserved.get(0);
            for (var drop : dropper.drops(stack, fortune, smelt, multiplier)) queue(drop);
            stack.m_41774_(1);
            doneWork++;
            if (stack.m_41619_()) reserved.remove(0);
        }
        if (reserved.isEmpty() && progress >= ticks) flush(inventory);
        return true;
    }

    private void begin(Inventory inventory) {
        // One input slot at a time, first to last, until that slot is empty.
        int slot = inventory.firstOre();
        if (slot < 0) return;
        int count = Math.min(inventory.upgrade(2) ? 64 : 1, inventory.count(slot));
        long cost = Math.multiplyExact(LabUtility.COST.get(), count);
        if (!inventory.upgrade(3) && !inventory.afford(cost)) return;
        // Do not start more work while every output slot is occupied. A pending job can drain incrementally.
        if (!inventory.outputSpace()) return;
        if (!inventory.upgrade(3) && !inventory.pay(cost)) return;
        multiplier = inventory.moduleMultiplier();
        fortune = multiplier > 0 ? 0 : inventory.fortune();
        smelt = inventory.upgrade(1);
        totalWork = count;
        doneWork = 0;
        progress = 0;
        reserved.add(inventory.extract(slot, count));
    }

    private void queue(ItemStack stack) {
        if (stack.m_41619_()) return;
        for (var other : pending) if (ItemStack.m_150942_(stack, other) && other.m_41613_() < other.m_41741_()) {
            int n = Math.min(stack.m_41613_(), other.m_41741_() - other.m_41613_());
            other.m_41769_(n);
            stack.m_41774_(n);
            if (stack.m_41619_()) return;
        }
        pending.add(stack.m_41777_());
    }

    private void flush(Inventory inventory) {
        for (int i = 0; i < pending.size();) {
            var left = inventory.insertOutput(pending.get(i));
            if (left.m_41619_()) pending.remove(i);
            else {
                pending.set(i, left);
                i++;
            }
        }
        if (pending.isEmpty()) {
            progress = 0;
            totalWork = doneWork = 0;
        }
    }

    /** The block entity keeps the slot layout and the EU store. */
    public interface Inventory {

        int firstOre();

        int count(int slot);

        boolean upgrade(int kind);

        int moduleMultiplier();

        int fortune();

        boolean outputSpace();

        boolean afford(long cost);

        boolean pay(long cost);

        ItemStack extract(int slot, int count);

        ItemStack insertOutput(ItemStack stack);
    }
}
