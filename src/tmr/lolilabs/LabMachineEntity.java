package tmr.lolilabs;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;
import net.minecraftforge.registries.ForgeRegistries;

public final class LabMachineEntity extends BlockEntity
                                    implements com.ic2ceu.icceu.EUSource, com.ic2ceu.icceu.EUReceiver, MenuProvider,
                                    brachy.modularui.api.IUIHolder<brachy.modularui.factory.PosGuiData> {

    public long energy;
    public int progress;
    public boolean enabled = true, muted;
    private ItemStackHandler recovery = new ItemStackHandler(0);
    public final LabMachines.Definition definition;
    public final ItemStackHandler inventory = new ItemStackHandler(36) {

        @Override
        protected void onContentsChanged(int slot) {
            LabMachineEntity.this.m_6596_();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return validSlot(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return definition.kind == 2 && slot >= 24 ? 1 : 64;
        }
    };
    private final com.ic2ceu.icceu.EnergyBuffer eu = new com.ic2ceu.icceu.EnergyBuffer(1);
    public int euFraction;

    private void prepareEU() {
        eu.resize(capacity());
        eu.restore(energy);
        eu.restoreFraction(euFraction);
    }

    public Level euLevel() {
        return f_58857_;
    }

    public BlockPos euPos() {
        return f_58858_;
    }

    public com.ic2ceu.icceu.EnergyBuffer euBuffer() {
        return eu;
    }

    public long outputVoltage() {
        return definition.kind == 2 ? 0 : definition.voltage.get();
    }

    public int outputPackets() {
        return definition.packets.get();
    }

    public Direction outputSide() {
        return m_58900_().m_61143_(LabMachineBlock.FACING).m_122424_();
    }

    public boolean emitsEnergy(Direction side) {
        return enabled && definition.kind != 2 && side != Direction.UP;
    }

    public boolean acceptsEnergy(Direction side) {
        return enabled && definition.kind == 2 && energy < capacity();
    }

    public boolean connectsEU(Direction side) {
        return definition.kind == 2 || side != Direction.UP;
    }

    public boolean workingLight() {
        return definition.kind == 0 ? enabled : definition.kind == 1 && enabled && generation() > 0;
    }

    public void syncSettings() {
        m_6596_();
        if (f_58857_ != null) {
            var state = m_58900_();
            boolean lit = workingLight();
            if (definition.kind <= 1 && state.m_61143_(LabMachineBlock.ACTIVE) != lit)
                state = state.m_61124_(LabMachineBlock.ACTIVE, lit);
            f_58857_.m_7731_(f_58858_, state, 3);
        }
    }

    @Override
    public CompoundTag m_5995_() {
        return m_187480_();
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket m_58483_() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.m_195640_(this);
    }

    public void euChanged() {
        energy = eu.stored();
        euFraction = eu.fraction();
        m_6596_();
    }

    public long receivePacket(Direction side, long quanta, long voltage) {
        if (!acceptsEnergy(side) || quanta <= 0) return 0;
        prepareEU();
        long accepted = eu.changeQuanta(quanta);
        euChanged();
        return accepted;
    }

    private IItemHandler accessibleInventory() {
        return definition.kind == 2 ?
                new net.minecraftforge.items.wrapper.CombinedInvWrapper(
                        new net.minecraftforge.items.wrapper.RangedWrapper(inventory, 0, definition.batch),
                        new net.minecraftforge.items.wrapper.RangedWrapper(inventory, 12, 12 + definition.batch)) :
                new net.minecraftforge.items.wrapper.RangedWrapper(inventory, 0, definition.kind == 0 ? 6 : 36);
    }

    private LazyOptional<IItemHandler> itemCap = LazyOptional.of(this::accessibleInventory);
    public final ContainerData data = new ContainerData() {

        public int m_6413_(int index) {
            if (index < 16) {
                long value = switch (index / 4) {
                    case 0 -> energy;
                    case 1 -> capacity();
                    case 2 -> generation();
                    default -> definition.kind == 2 ? definition.cycleCost() : 0;
                };
                return (int) (value >>> (16 * (index % 4))) & 65535;
            }
            return switch (index) {
                case 16 -> progress;
                case 17 -> definition.kind;
                case 19 -> definition.tier;
                default -> definition.batch;
            };
        }

        public void m_8050_(int index, int value) {}

        public int m_6499_() {
            return 20;
        }
    };

    public LabMachineEntity(BlockPos pos, BlockState state) {
        super(LabMachines.ENTITY.get(), pos, state);
        definition = ((LabMachineBlock) state.m_60734_()).definition;
    }

    public static final String[] MATTER_CHAIN = { "matter", "matter_loose_blue", "matter_loose_pink",
            "matter_luminous_pink", "matter_luminous_red", "matter_turquoise", "matter_luminous_turquoise",
            "matter_purple", "matter_mutated_violet_turquoise", "matter_sparkling" };

    public String inputId() {
        return definition.tier == 0 ? "ic2modern:slag" : "loli_labs:" + MATTER_CHAIN[definition.tier - 1];
    }

    public boolean isFeed(ItemStack stack) {
        return !stack.m_41619_() && inputId().equals(ForgeRegistries.ITEMS.getKey(stack.m_41720_()).toString());
    }

    private static boolean chargeable(ItemStack stack) {
        return stack.m_41720_() instanceof com.ic2ceu.icceu.ChargeableItem;
    }

    public boolean validSlot(int slot, ItemStack stack) {
        if (definition.kind != 2) return slot < (definition.kind == 0 ? 6 : 27) && accepts(stack);
        if (slot < definition.batch) return isFeed(stack);
        if (slot >= 24 && slot < 28) return stack.m_41720_() instanceof com.ic2ceu.icceu.UpgradeItem u &&
                (u.kind.equals("pulling") || u.kind.equals("ejector"));
        return slot == 28 && chargeable(stack);
    }

    public boolean accepts(ItemStack stack) {
        return definition.kind == 2 ? isFeed(stack) || validSlot(24, stack) || chargeable(stack) :
                definition.kind == 1 ? LabMachines.panel(stack) != null : chargeable(stack);
    }

    public boolean hasUpgrade(String id) {
        for (int i = 24; i < 28; i++)
            if (inventory.getStackInSlot(i).m_41720_() instanceof com.ic2ceu.icceu.UpgradeItem u &&
                    u.kind.equals(id.equals("item_import_upgrade") ? "pulling" : "ejector"))
                return true;
        return false;
    }

    private boolean upgradeSide(String id, Direction side) {
        for (int i = 24; i < 28; i++) {
            var stack = inventory.getStackInSlot(i);
            if (stack.m_41720_() instanceof com.ic2ceu.icceu.UpgradeItem u &&
                    u.kind.equals(id.equals("item_import_upgrade") ? "pulling" : "ejector")) {
                int direction = com.ic2ceu.icceu.UpgradeItem.direction(stack);
                if (direction == 0 || com.ic2ceu.icceu.UpgradeItem.SIDES[direction - 1] == side) return true;
            }
        }
        return false;
    }

    public long generation() {
        if (definition.kind == 0) return definition.rate();
        if (definition.kind != 1) return 0;
        long sum = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            var stack = inventory.getStackInSlot(i);
            var d = LabMachines.panel(stack);
            if (d != null) sum = LabMachines.addSafe(sum, Math.multiplyExact(d.rate(), stack.m_41613_()));
        }
        return sum;
    }

    public long capacity() {
        return definition.kind == 2 ? definition.cycleCost() * 64 :
                Math.max(1_000_000, Math.min((Long.MAX_VALUE / 40) / 15625, generation()) * 15625);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LabMachineEntity machine) {
        machine.serverTick();
    }

    public void serverTick() {
        energy = Math.min(Math.max(0, energy), capacity());
        if (definition.kind <= 1 && f_58857_ != null) {
            var state = m_58900_();
            boolean lit = workingLight();
            if (state.m_61143_(LabMachineBlock.ACTIVE) != lit)
                f_58857_.m_7731_(f_58858_, state.m_61124_(LabMachineBlock.ACTIVE, lit), 3);
        }
        if (!enabled) return;
        if (definition.kind == 2) {
            if (hasUpgrade("item_import_upgrade")) importFeed();
            if (++progress >= 20) {
                progress = 0;
                produceMatter();
            }
            chargeItem(28);
        } else {
            energy = Math.min(capacity(), LabMachines.addSafe(energy, generation()));
            for (int i = 0; i < (definition.kind == 0 ? 6 : 27) && energy > 0; i++) chargeItem(i);
            prepareEU();
            com.ic2ceu.icceu.EUNetwork.distribute(this);
        }
        if (definition.kind == 2 && hasUpgrade("item_export_upgrade")) exportMatter();
        m_6596_();
    }

    public boolean produceMatter() {
        long unitCost = definition.cost.get();
        boolean changed = false;
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(LoliLabs.MOD_ID, definition.matter));
        for (int lane = 0; lane < definition.batch; lane++) {
            var input = inventory.getStackInSlot(lane);
            var output = inventory.getStackInSlot(12 + lane);
            if (!isFeed(input) || energy < unitCost ||
                    !output.m_41619_() && (output.m_41720_() != item || output.m_41613_() >= output.m_41741_()))
                continue;
            if (output.m_41619_()) inventory.setStackInSlot(12 + lane, new ItemStack(item));
            else {
                output.m_41769_(1);
                inventory.setStackInSlot(12 + lane, output);
            }
            inventory.extractItem(lane, 1, false);
            energy -= unitCost;
            changed = true;
        }
        if (changed) m_6596_();
        return changed;
    }

    private void chargeItem(int slot) {
        var stack = inventory.getStackInSlot(slot);
        if (stack.m_41720_() instanceof com.ic2ceu.icceu.ChargeableItem item) {
            long charged = item.charge(stack, Math.min(item.transferLimit(), energy));
            energy -= charged;
        }
    }

    private void importFeed() {
        if (f_58857_ == null) return;
        var input = new net.minecraftforge.items.wrapper.RangedWrapper(inventory, 0, definition.batch);
        for (Direction side : Direction.values()) {
            if (!upgradeSide("item_import_upgrade", side)) continue;
            var neighbor = f_58857_.m_7702_(f_58858_.m_121945_(side));
            if (neighbor == null) continue;
            var source = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, side.m_122424_()).orElse(null);
            if (source == null) continue;
            for (int i = 0; i < source.getSlots(); i++) {
                var offered = source.extractItem(i, 64, true);
                if (!isFeed(offered)) continue;
                var left = ItemHandlerHelper.insertItemStacked(input, offered, true);
                int count = offered.m_41613_() - left.m_41613_();
                if (count > 0) {
                    var taken = source.extractItem(i, count, false);
                    var remainder = ItemHandlerHelper.insertItemStacked(input, taken, false);
                    if (!remainder.m_41619_()) {
                        remainder = source.insertItem(i, remainder, false);
                        if (!remainder.m_41619_())
                            net.minecraft.world.Containers.m_18992_(f_58857_, f_58858_.m_123341_() + 0.5,
                                    f_58858_.m_123342_() + 0.5, f_58858_.m_123343_() + 0.5, remainder);
                    }
                }
            }
        }
    }

    private void exportMatter() {
        if (f_58857_ == null) return;
        for (Direction side : Direction.values()) {
            if (!upgradeSide("item_export_upgrade", side)) continue;
            var neighbor = f_58857_.m_7702_(f_58858_.m_121945_(side));
            if (neighbor == null) continue;
            var target = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, side.m_122424_()).orElse(null);
            if (target == null) continue;
            for (int i = 12; i < 12 + definition.batch; i++) {
                var stack = inventory.getStackInSlot(i);
                if (stack.m_41619_()) continue;
                var remaining = ItemHandlerHelper.insertItemStacked(target, stack.m_41777_(), false);
                if (remaining.m_41613_() != stack.m_41613_()) inventory.setStackInSlot(i, remaining);
            }
        }
    }

    public static void migrateLegacy(CompoundTag tag) {
        if (!tag.m_128425_("LabEnergy", 99) && tag.m_128425_("Energy", 99)) {
            tag.m_128356_("LabEnergy", tag.m_128454_("Energy"));
            var output = new ItemStackHandler(27);
            for (String key : new String[] { "Upgrades", "Chargeable" }) {
                var items = tag.m_128437_(key, 10);
                for (int i = 0; i < items.size(); i++) {
                    var saved = items.m_128728_(i);
                    int slot = saved.m_128451_("Slot");
                    if (slot >= 0 && slot < (key.equals("Upgrades") ? 5 : 1))
                        output.setStackInSlot(key.equals("Upgrades") ? slot : 5, ItemStack.m_41712_(saved));
                }
            }
            tag.m_128365_("LabInventory", output.serializeNBT());
        }
    }

    @Override
    public void m_142466_(CompoundTag tag) {
        migrateLegacy(tag);
        super.m_142466_(tag);
        enabled = !tag.m_128441_("LabEnabled") || tag.m_128471_("LabEnabled");
        muted = tag.m_128471_("LabMuted");
        energy = Math.max(0, tag.m_128454_("LabEnergy"));
        if (tag.m_128451_("LabEnergyVersion") < 2) {
            euFraction = (int) (energy % 4) * 10;
            energy /= 4;
        } else euFraction = Math.floorMod(tag.m_128451_("LabEUFraction"), 40);
        progress = Math.max(0, Math.min(19, tag.m_128451_("LabProgress")));
        if (tag.m_128425_("LabRecovery", 10)) recovery.deserializeNBT(tag.m_128469_("LabRecovery"));
        if (tag.m_128425_("LabInventory", 10)) {
            if (definition.kind == 2 && tag.m_128451_("LabLayout") < 2)
                recovery.deserializeNBT(tag.m_128469_("LabInventory"));
            else {
                var saved = tag.m_128469_("LabInventory").m_6426_();
                saved.m_128405_("Size", 36);
                inventory.deserializeNBT(saved);
            }
        }
    }

    @Override
    protected void m_183515_(CompoundTag tag) {
        super.m_183515_(tag);
        tag.m_128379_("LabEnabled", enabled);
        tag.m_128379_("LabMuted", muted);
        tag.m_128356_("LabEnergy", energy);
        tag.m_128405_("LabEnergyVersion", 2);
        tag.m_128405_("LabEUFraction", euFraction);
        tag.m_128405_("LabProgress", progress);
        tag.m_128405_("LabLayout", 2);
        tag.m_128365_("LabInventory", inventory.serializeNBT());
        tag.m_128365_("LabRecovery", recovery.serializeNBT());
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemCap = LazyOptional.of(this::accessibleInventory);
    }

    @Override
    public Component m_5446_() {
        return Component.m_237115_("block.loli_labs." + definition.id);
    }

    public void beforeOpen(Player player) {
        // Retain old 27-slot NBT until its owner opens the panel, then return surplus.
        if (definition.kind == 0) for (int i = 6; i < inventory.getSlots(); i++) {
            var stack = inventory.getStackInSlot(i);
            if (!stack.m_41619_()) {
                inventory.setStackInSlot(i, ItemStack.f_41583_);
                ItemHandlerHelper.giveItemToPlayer(player, stack);
            }
        }
        for (int i = 0; i < recovery.getSlots(); i++) {
            var stack = recovery.getStackInSlot(i);
            if (!stack.m_41619_()) {
                recovery.setStackInSlot(i, ItemStack.f_41583_);
                ItemHandlerHelper.giveItemToPlayer(player, stack);
            }
        }
        m_6596_();
    }

    @Override
    public AbstractContainerMenu m_7208_(int id, Inventory inventory, Player player) {
        return null;
    }

    @Override
    public brachy.modularui.screen.ModularPanel<?> buildUI(brachy.modularui.factory.PosGuiData data,
                                                           brachy.modularui.value.sync.PanelSyncManager sync,
                                                           brachy.modularui.screen.UISettings settings) {
        return LabModularUI.build(this, sync, settings);
    }

    @Override
    public brachy.modularui.screen.ModularScreen createScreen(brachy.modularui.factory.PosGuiData data,
                                                              brachy.modularui.screen.ModularPanel<?> panel) {
        return new brachy.modularui.screen.ModularScreen(LoliLabs.MOD_ID, panel)
                .useTheme(com.ic2ceu.icceu.mui.GuiStyle.theme());
    }
}
