package tmr.lolilabs.energistics.machine;

import tmr.lolilabs.LoliLabs;
import tmr.lolilabs.energistics.LabMachineEntity;

import com.ic2ceu.icceu.api.item.ChargeableItem;
import com.ic2ceu.icceu.common.item.UpgradeItem;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import net.minecraftforge.registries.ForgeRegistries;

public final class MatterBehaviour implements MachineBehaviour {

    @Override
    public void serverTick(LabMachineEntity machine) {
        if (!machine.enabled) return;
        if (machine.hasUpgrade("item_import_upgrade")) machine.importFeed();
        produce(machine);
        machine.chargeItem(28);
        if (machine.hasUpgrade("item_export_upgrade")) machine.exportMatter();
    }

    @Override
    public long generation(LabMachineEntity machine) {
        return 0;
    }

    @Override
    public long capacity(LabMachineEntity machine) {
        return machine.definition.cycleCost() * 64;
    }

    @Override
    public long outputVoltage(LabMachineEntity machine) {
        return 0;
    }

    @Override
    public boolean emitsEnergy(LabMachineEntity machine, Direction side) {
        return false;
    }

    @Override
    public boolean acceptsEnergy(LabMachineEntity machine, Direction side) {
        return machine.enabled && machine.energy < capacity(machine);
    }

    @Override
    public boolean connectsEU(LabMachineEntity machine, Direction side) {
        return true;
    }

    @Override
    public boolean workingLight(LabMachineEntity machine) {
        return false;
    }

    @Override
    public boolean tracksActive() {
        return false;
    }

    @Override
    public IItemHandler accessibleInventory(LabMachineEntity machine) {
        int batch = machine.definition.batch;
        return new CombinedInvWrapper(new RangedWrapper(machine.inventory, 0, batch),
                new RangedWrapper(machine.inventory, 12, 12 + batch));
    }

    @Override
    public int slotLimit(int slot) {
        return slot >= 24 ? 1 : 64;
    }

    @Override
    public boolean validSlot(LabMachineEntity machine, int slot, ItemStack stack) {
        if (slot < machine.definition.batch) return machine.isFeed(stack);
        if (slot >= 24 && slot < 28) return stack.m_41720_() instanceof UpgradeItem upgrade &&
                (upgrade.kind.equals("pulling") || upgrade.kind.equals("ejector"));
        return slot == 28 && stack.m_41720_() instanceof ChargeableItem;
    }

    @Override
    public boolean accepts(LabMachineEntity machine, ItemStack stack) {
        return machine.isFeed(stack) || validSlot(machine, 24, stack) || stack.m_41720_() instanceof ChargeableItem;
    }

    @Override
    public long displayCost(LabMachineEntity machine) {
        return machine.craft.active() ? machine.craft.cost() : machine.definition.cost.get();
    }

    @Override
    public boolean produce(LabMachineEntity machine) {
        int n = craftSize(machine);
        if (n <= 0) {
            machine.craft.clear();
            return false;
        }
        long total = Math.multiplyExact(machine.definition.cost.get(), n);
        if (!machine.craft.active()) machine.craft.begin(total);
        else if (machine.craft.cost() != total) machine.craft.resize(total);
        long paid = machine.craft.pay(machine.energy);
        machine.energy -= paid;
        if (!machine.craft.complete()) return false;
        consume(machine, n);
        give(machine, n);
        machine.craft.clear();
        machine.m_6596_();
        return true;
    }

    @Override
    public void beforeOpen(LabMachineEntity machine, Player player) {}

    private static int craftSize(LabMachineEntity machine) {
        int feeds = 0;
        for (int i = 0; i < machine.definition.batch; i++) {
            ItemStack stack = machine.inventory.getStackInSlot(i);
            if (machine.isFeed(stack)) feeds += stack.m_41613_();
        }
        Item item = output(machine);
        if (item == null || item == Items.f_41852_) return 0;
        int space = 0;
        for (int i = 0; i < machine.definition.batch; i++) {
            ItemStack stack = machine.inventory.getStackInSlot(12 + i);
            if (stack.m_41619_()) space += new ItemStack(item).m_41741_();
            else if (stack.m_41720_() == item) space += Math.max(0, stack.m_41741_() - stack.m_41613_());
        }
        return Math.min(feeds, Math.min(space, machine.definition.batch));
    }

    private static void consume(LabMachineEntity machine, int n) {
        int left = n;
        for (int i = 0; i < machine.definition.batch && left > 0; i++) {
            ItemStack stack = machine.inventory.getStackInSlot(i);
            if (!machine.isFeed(stack)) continue;
            int take = Math.min(left, stack.m_41613_());
            machine.inventory.extractItem(i, take, false);
            left -= take;
        }
    }

    private static void give(LabMachineEntity machine, int n) {
        Item item = output(machine);
        int left = n;
        for (int i = 0; i < machine.definition.batch && left > 0; i++) {
            int slot = 12 + i;
            ItemStack stack = machine.inventory.getStackInSlot(slot);
            if (stack.m_41619_()) {
                int put = Math.min(left, new ItemStack(item).m_41741_());
                machine.inventory.setStackInSlot(slot, new ItemStack(item, put));
                left -= put;
            } else if (stack.m_41720_() == item && stack.m_41613_() < stack.m_41741_()) {
                int put = Math.min(left, stack.m_41741_() - stack.m_41613_());
                stack.m_41769_(put);
                machine.inventory.setStackInSlot(slot, stack);
                left -= put;
            }
        }
    }

    private static Item output(LabMachineEntity machine) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(LoliLabs.MOD_ID, machine.definition.matter));
    }
}
