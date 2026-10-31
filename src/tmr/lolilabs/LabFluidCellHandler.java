package tmr.lolilabs;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

/** One tank shared by hand use, Forge pipes and ME drives; no duplicate storage NBT. */
public final class LabFluidCellHandler implements ICellHandler {
    @Override public boolean isCell(ItemStack stack) { return stack.m_41720_() instanceof FluidCellItem; }
    @Override public StorageCell getCellInventory(ItemStack stack, ISaveProvider save) {
        if (!isCell(stack)) return null;
        var handler=FluidUtil.getFluidHandler(stack).orElse(null);
        return handler == null ? null : new TankCell(stack,handler,save);
    }
    private record TankCell(ItemStack stack, IFluidHandlerItem handler, ISaveProvider save) implements StorageCell {
        @Override public long insert(AEKey key,long amount,Actionable mode,IActionSource source) {
            if (!(key instanceof AEFluidKey fluid) || amount<=0) return 0;
            int moved=handler.fill(fluid.toStack((int)Math.min(Integer.MAX_VALUE,amount)),mode.getFluidAction());
            if (moved>0 && mode==Actionable.MODULATE) persist();
            return moved;
        }
        @Override public long extract(AEKey key,long amount,Actionable mode,IActionSource source) {
            if (!(key instanceof AEFluidKey fluid) || amount<=0) return 0;
            int moved=handler.drain(fluid.toStack((int)Math.min(Integer.MAX_VALUE,amount)),mode.getFluidAction()).getAmount();
            if (moved>0 && mode==Actionable.MODULATE) persist();
            return moved;
        }
        @Override public void getAvailableStacks(KeyCounter result) {
            var fluid=handler.getFluidInTank(0);
            if (!fluid.isEmpty()) result.add(AEFluidKey.of(fluid),fluid.getAmount());
        }
        @Override public CellState getStatus() {
            int amount=handler.getFluidInTank(0).getAmount();
            return amount==0 ? CellState.EMPTY : amount>=handler.getTankCapacity(0) ? CellState.FULL : CellState.TYPES_FULL;
        }
        @Override public double getIdleDrain() { return 0.5D; }
        @Override public boolean canFitInsideCell() { return false; }
        @Override public void persist() { if (save != null) save.saveChanges(); }
        @Override public Component getDescription() { return stack.m_41786_(); }
    }
}
