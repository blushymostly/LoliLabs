package tmr.lolilabs;

import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.*;

public final class LabMachineMenu extends AbstractContainerMenu {

    public final ContainerData data;
    private final LabMachineEntity machine;
    public final int machineSlots;

    public LabMachineMenu(int id, Inventory inventory, int kind, int batch) {
        this(id, inventory, null, new ItemStackHandler(29), new SimpleContainerData(20), kind, batch);
        data.m_8050_(17, kind);
        data.m_8050_(18, batch);
    }

    public LabMachineMenu(int id, Inventory inventory, LabMachineEntity machine) {
        this(id, inventory, machine, machine.inventory, machine.data, machine.definition.kindCode(),
                machine.definition.batch);
    }

    private LabMachineMenu(int id, Inventory inventory, LabMachineEntity machine, IItemHandler handler,
                           ContainerData data, int kind, int batch) {
        super(LabMachines.MENU.get(), id);
        this.machine = machine;
        this.data = data;
        machineSlots = kind == 0 ? 6 : kind == 2 ? batch * 2 + 5 : 27;
        for (int i = 0; i < machineSlots; i++) {
            int storage = i, x, y;
            if (kind == 2) {
                if (i < batch * 2) {
                    int lane = i % batch;
                    boolean output = i >= batch;
                    storage = (output ? 12 : 0) + lane;
                    int cols = batch == 12 ? 3 : batch == 6 ? 3 : 1;
                    x = batch == 1 ? (output ? 116 : 68) : (output ? 126 : 34) + (lane % cols) * 18;
                    y = (batch == 1 ? 54 : batch == 6 ? 46 : 28) + (lane / cols) * 18;
                } else {
                    storage = 24 + i - batch * 2;
                    x = 70 + (i - batch * 2) * 18;
                    y = 120;
                }
            } else {
                x = kind == 0 ? 53 + i * 18 : 26 + (i % 9) * 18;
                y = kind == 0 ? 83 : 30 + (i / 9) * 18;
            }
            final int index = storage;
            m_38897_(new SlotItemHandler(handler, storage, x, y) {

                @Override
                public boolean m_5857_(ItemStack stack) {
                    if (machine != null) return machine.validSlot(index, stack);
                    if (kind == 1) return LabMachines.panel(stack) != null;
                    if (kind == 2) {
                        if (index < batch) {
                            int tier = Math.max(0, Math.min(9, data.m_6413_(19)));
                            String feed = tier == 0 ? "thermal:slag" :
                                    "loli_labs:" + LabMachineEntity.MATTER_CHAIN[tier - 1];
                            return feed.equals(net.minecraftforge.registries.ForgeRegistries.ITEMS
                                    .getKey(stack.m_41720_()).toString());
                        }
                        if (index >= 24 && index < 28)
                            return stack.m_41720_() == LabItems.CATALOG.get("item_import_upgrade").get() ||
                                    stack.m_41720_() == LabItems.CATALOG.get("item_export_upgrade").get();
                        if (index != 28) return false;
                    }
                    return stack.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY)
                            .map(net.minecraftforge.energy.IEnergyStorage::canReceive).orElse(false);
                }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            m_38897_(new Slot(inventory, col + row * 9 + 9, 26 + col * 18, (kind == 0 ? 128 : 172) + row * 18));
        for (int col = 0; col < 9; col++) m_38897_(new Slot(inventory, col, 26 + col * 18, kind == 0 ? 186 : 230));
        m_38884_(data);
    }

    public long value(int index) {
        long value = 0;
        for (int i = 0; i < 4; i++) value |= ((long) data.m_6413_(index * 4 + i) & 65535) << (16 * i);
        return value;
    }

    @Override
    public boolean m_6875_(Player player) {
        return machine == null || !machine.m_58901_() && machine.m_58904_() == player.m_9236_() &&
                player.m_20275_(machine.m_58899_().m_123341_() + 0.5, machine.m_58899_().m_123342_() + 0.5,
                        machine.m_58899_().m_123343_() + 0.5) <= 64;
    }

    @Override
    public ItemStack m_7648_(Player player, int index) {
        if (index < 0 || index >= f_38839_.size()) return ItemStack.f_41583_;
        Slot slot = f_38839_.get(index);
        if (!slot.m_6657_()) return ItemStack.f_41583_;
        ItemStack stack = slot.m_7993_(), copy = stack.m_41777_();
        if (index < machineSlots) {
            if (!m_38903_(stack, machineSlots, machineSlots + 36, true)) return ItemStack.f_41583_;
        } else if (machine != null && !machine.accepts(stack)) return ItemStack.f_41583_;
        else if (!m_38903_(stack, 0, machineSlots, false)) return ItemStack.f_41583_;
        if (stack.m_41619_()) slot.m_5852_(ItemStack.f_41583_);
        else slot.m_6654_();
        slot.m_142406_(player, stack);
        return copy;
    }
}
