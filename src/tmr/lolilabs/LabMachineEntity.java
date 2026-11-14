package tmr.lolilabs;

import tmr.lolilabs.machine.MachineBehaviour;

import com.ic2ceu.icceu.api.machine.EnergyPaidCraft;

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
                                    implements com.ic2ceu.icceu.api.energy.EUSource, com.ic2ceu.icceu.api.energy.EUReceiver, MenuProvider,
                                    brachy.modularui.api.IUIHolder<brachy.modularui.factory.PosGuiData> {

    public long energy;
    public int progress;
    public boolean enabled = true, muted;
    private ItemStackHandler recovery = new ItemStackHandler(0);
    public final LabMachines.Definition definition;
    public final MachineBehaviour behaviour;
    /** One craft for the whole machine. Matter pays this; generators leave it idle. */
    public final EnergyPaidCraft craft = new EnergyPaidCraft();
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
            return behaviour.slotLimit(slot);
        }
    };
    private final tmr.lolilabs.eu.ForeignEuStore euStore = new tmr.lolilabs.eu.ForeignEuStore();
    public int euFraction;

    public void prepareEU() {
        euStore.prepare(capacity(), energy, euFraction);
    }

    public Level euLevel() {
        return f_58857_;
    }

    public BlockPos euPos() {
        return f_58858_;
    }

    public com.ic2ceu.icceu.api.energy.EnergyBuffer euBuffer() {
        return euStore.buffer();
    }

    public long outputVoltage() {
        return behaviour.outputVoltage(this);
    }

    public int outputPackets() {
        return definition.packets.get();
    }

    public Direction outputSide() {
        return m_58900_().m_61143_(LabMachineBlock.FACING).m_122424_();
    }

    public boolean emitsEnergy(Direction side) {
        return behaviour.emitsEnergy(this, side);
    }

    public boolean acceptsEnergy(Direction side) {
        return behaviour.acceptsEnergy(this, side);
    }

    public boolean connectsEU(Direction side) {
        return behaviour.connectsEU(this, side);
    }

    public boolean workingLight() {
        return behaviour.workingLight(this);
    }

    public void syncSettings() {
        m_6596_();
        if (f_58857_ != null && behaviour.tracksActive()) {
            var state = m_58900_();
            boolean lit = workingLight();
            if (state.m_61143_(LabMachineBlock.ACTIVE) != lit)
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
        energy = euStore.stored();
        euFraction = euStore.fraction();
        m_6596_();
    }

    public long receivePacket(Direction side, long quanta, long voltage) {
        if (!acceptsEnergy(side) || quanta <= 0) return 0;
        long accepted = euStore.changeQuanta(capacity(), energy, euFraction, quanta);
        euChanged();
        return accepted;
    }

    private IItemHandler accessibleInventory() {
        return behaviour.accessibleInventory(this);
    }

    private LazyOptional<IItemHandler> itemCap = LazyOptional.of(this::accessibleInventory);
    public final ContainerData data = new ContainerData() {

        public int m_6413_(int index) {
            if (index < 16) {
                long value = switch (index / 4) {
                    case 0 -> energy;
                    case 1 -> capacity();
                    case 2 -> generation();
                    default -> behaviour.displayCost(LabMachineEntity.this);
                };
                return (int) (value >>> (16 * (index % 4))) & 65535;
            }
            return switch (index) {
                case 16 -> craft.progress(MachineBehaviour.UI_PROGRESS_SCALE);
                case 17 -> definition.kindCode();
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
        behaviour = definition.createBehaviour();
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

    public boolean validSlot(int slot, ItemStack stack) {
        return behaviour.validSlot(this, slot, stack);
    }

    public boolean accepts(ItemStack stack) {
        return behaviour.accepts(this, stack);
    }

    public boolean hasUpgrade(String id) {
        for (int i = 24; i < 28; i++)
            if (inventory.getStackInSlot(i).m_41720_() instanceof com.ic2ceu.icceu.common.item.UpgradeItem u &&
                    u.kind.equals(id.equals("item_import_upgrade") ? "pulling" : "ejector"))
                return true;
        return false;
    }

    private boolean upgradeSide(String id, Direction side) {
        for (int i = 24; i < 28; i++) {
            var stack = inventory.getStackInSlot(i);
            if (stack.m_41720_() instanceof com.ic2ceu.icceu.common.item.UpgradeItem u &&
                    u.kind.equals(id.equals("item_import_upgrade") ? "pulling" : "ejector")) {
                int direction = com.ic2ceu.icceu.common.item.UpgradeItem.direction(stack);
                if (direction == 0 || com.ic2ceu.icceu.common.item.UpgradeItem.SIDES[direction - 1] == side) return true;
            }
        }
        return false;
    }

    public long generation() {
        return behaviour.generation(this);
    }

    public long capacity() {
        return behaviour.capacity(this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LabMachineEntity machine) {
        machine.serverTick();
    }

    public void serverTick() {
        energy = Math.min(Math.max(0, energy), capacity());
        behaviour.serverTick(this);
        m_6596_();
    }

    public boolean produceMatter() {
        return behaviour.produce(this);
    }

    public void chargeItem(int slot) {
        var stack = inventory.getStackInSlot(slot);
        if (stack.m_41720_() instanceof com.ic2ceu.icceu.api.item.ChargeableItem item) {
            long charged = item.charge(stack, Math.min(item.transferLimit(), energy));
            energy -= charged;
        }
    }

    public void importFeed() {
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

    public void exportMatter() {
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
        // Legacy LabProgress is the old 20-tick counter. Craft cold-starts unless LabCraft is present.
        if (tag.m_128425_("LabCraft", 10)) craft.load(tag.m_128469_("LabCraft"));
        if (tag.m_128425_("LabRecovery", 10)) recovery.deserializeNBT(tag.m_128469_("LabRecovery"));
        if (tag.m_128425_("LabInventory", 10)) {
            if (definition instanceof LabMachines.MatterSolidDef && tag.m_128451_("LabLayout") < 2)
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
        var craftTag = new CompoundTag();
        craft.save(craftTag);
        tag.m_128365_("LabCraft", craftTag);
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
        behaviour.beforeOpen(this, player);
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
