package tmr.lolilabs.utility;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;

/** Reserved ore, rolled drops, EU and progress are persisted together; output blockage never rerolls or voids a job. */
public final class FortuneEntity extends BlockEntity implements MenuProvider, com.ic2ceu.icceu.api.energy.EUReceiver,
                                 brachy.modularui.api.IUIHolder<brachy.modularui.factory.PosGuiData> {

    public long energy;
    public int fraction, progress, totalWork, doneWork;
    private final tmr.lolilabs.eu.ForeignEuStore euStore = new tmr.lolilabs.eu.ForeignEuStore();
    private final FortuneJob job = new FortuneJob();
    private final FortuneJob.Inventory slots = new Slots();
    private OreDropper dropsPort;
    private Ae2OreSource inputPort;
    public boolean enabled = true, jobSmelt;
    public int jobFortune, jobMultiplier;
    public final java.util.List<ItemStack> reserved = job.reserved, pending = job.pending;
    private static final net.minecraft.tags.TagKey<Block> ORES = net.minecraft.tags.TagKey.m_203882_(
            net.minecraft.core.registries.Registries.f_256747_,
            new net.minecraft.resources.ResourceLocation("forge", "ores"));
    public final ItemStackHandler inventory = new ItemStackHandler(58) {

        @Override
        protected void onContentsChanged(int slot) {
            m_6596_();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return valid(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 54 ? 1 : 64;
        }
    };
    private final IItemHandler automation = new IItemHandler() {

        public int getSlots() {
            return 54;
        }

        public ItemStack getStackInSlot(int i) {
            return inventory.getStackInSlot(i);
        }

        public ItemStack insertItem(int i, ItemStack s, boolean sim) {
            return i < 27 ? inventory.insertItem(i, s, sim) : s;
        }

        public ItemStack extractItem(int i, int n, boolean sim) {
            return i >= 27 ? inventory.extractItem(i, n, sim) : ItemStack.f_41583_;
        }

        public int getSlotLimit(int i) {
            return 64;
        }

        public boolean isItemValid(int i, ItemStack s) {
            return i < 27 && valid(i, s);
        }
    };
    private LazyOptional<IItemHandler> items = LazyOptional.of(() -> automation);

    public FortuneEntity(BlockPos pos, BlockState state) {
        super(LabUtility.ENTITY.get(), pos, state);
    }

    public static boolean ore(ItemStack stack) {
        return !stack.m_41619_() && stack.m_41720_() instanceof BlockItem item &&
                item.m_40614_().m_49966_().m_204336_(ORES);
    }

    public boolean valid(int slot, ItemStack stack) {
        if (slot < 27) return ore(stack);
        if (slot < 54) return false;
        if (slot == 54 && pickaxeFortune(stack) > 0) return true;
        return stack.m_41720_() instanceof FortuneModule module && module.kind == slot - 54;
    }

    public static int pickaxeFortune(ItemStack stack) {
        return stack.canPerformAction(net.minecraftforge.common.ToolActions.PICKAXE_DIG) ?
                Math.max(0, stack.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.f_44987_)) : 0;
    }

    public int fortune() {
        var stack = inventory.getStackInSlot(54);
        return stack.m_41720_() instanceof FortuneModule m && m.kind == 0 ? m.level : pickaxeFortune(stack);
    }

    public boolean upgrade(int kind) {
        return inventory.getStackInSlot(54 + kind).m_41720_() instanceof FortuneModule m && m.kind == kind;
    }

    public long capacity() {
        return LabUtility.CAPACITY.get();
    }

    public boolean working() {
        return totalWork > 0 || !reserved.isEmpty() || !pending.isEmpty();
    }

    public void tick() {
        if (f_58857_ == null || f_58857_.f_46443_) return;
        if (enabled) {
            ae2().pull(inventory);
            autoOutput();
            var roller = oreDropper();
            if (roller != null) {
                bindJob();
                boolean active = job.tickStep(roller, slots);
                saveJob();
                if (active) m_6596_();
            }
        }
        if (f_58857_.m_46467_() % 10 == 0) sync();
    }

    /** Checks and the UI write the public counters between ticks. The job is canonical only inside tickStep. */
    private void bindJob() {
        job.progress = progress;
        job.totalWork = totalWork;
        job.doneWork = doneWork;
        job.fortune = jobFortune;
        job.multiplier = jobMultiplier;
        job.smelt = jobSmelt;
    }

    private void saveJob() {
        progress = job.progress;
        totalWork = job.totalWork;
        doneWork = job.doneWork;
        jobFortune = job.fortune;
        jobMultiplier = job.multiplier;
        jobSmelt = job.smelt;
    }

    private OreDropper oreDropper() {
        if (dropsPort == null && f_58857_ instanceof net.minecraft.server.level.ServerLevel server)
            dropsPort = new ServerOreDropper(server, m_58899_());
        return dropsPort;
    }

    private Ae2OreSource ae2() {
        if (inputPort == null && f_58857_ != null) inputPort = new InterfaceAe2OreSource(f_58857_, m_58899_());
        return inputPort;
    }

    public java.util.List<ItemStack> drops(ItemStack ore, int fortune, boolean smelt) {
        return drops(ore, fortune, smelt, 0);
    }

    public java.util.List<ItemStack> drops(ItemStack ore, int fortune, boolean smelt, int fixedCount) {
        var roller = oreDropper();
        return roller == null ? java.util.List.of() : roller.drops(ore, fortune, smelt, fixedCount);
    }

    private ItemStack insertOutput(ItemStack value) {
        var left = value.m_41777_();
        for (int i = 27; i < 54 && !left.m_41619_(); i++) {
            var old = inventory.getStackInSlot(i);
            if (old.m_41619_()) {
                int n = Math.min(left.m_41613_(), left.m_41741_());
                inventory.setStackInSlot(i, left.m_255036_(n));
                left.m_41774_(n);
            } else if (ItemStack.m_150942_(old, left)) {
                int n = Math.min(left.m_41613_(), old.m_41741_() - old.m_41613_());
                if (n > 0) {
                    old = old.m_41777_();
                    old.m_41769_(n);
                    inventory.setStackInSlot(i, old);
                    left.m_41774_(n);
                }
            }
        }
        return left;
    }

    public static appeng.helpers.InterfaceLogicHost interfaceAt(net.minecraft.world.level.block.entity.BlockEntity be,
                                                                Direction face) {
        return InterfaceAe2OreSource.interfaceAt(be, face);
    }

    public void pullInterfaceInputs() {
        if (f_58857_ == null || !enabled) return;
        ae2().pull(inventory);
    }

    public void autoOutput() {
        for (Direction side : Direction.values()) {
            var next = f_58857_.m_7702_(m_58899_().m_121945_(side));
            if (next == null || next instanceof FortuneEntity) continue;
            var target = next.getCapability(ForgeCapabilities.ITEM_HANDLER, side.m_122424_()).orElse(null);
            if (target == null) continue;
            int budget = 4;
            for (int slot = 27; slot < 54 && budget > 0; slot++) {
                var offered = inventory.getStackInSlot(slot);
                if (offered.m_41619_()) continue;
                var left = ItemHandlerHelper.insertItemStacked(target, offered.m_41777_(), false);
                if (left.m_41613_() < offered.m_41613_()) {
                    inventory.setStackInSlot(slot, left);
                    budget--;
                }
            }
        }
    }

    @Override
    public boolean acceptsEnergy(Direction side) {
        return enabled && !upgrade(3) && energy < capacity();
    }

    @Override
    public long receivePacket(Direction side, long quanta, long voltage) {
        if (!acceptsEnergy(side) || quanta <= 0) return 0;
        long accepted = euStore.changeQuanta(capacity(), energy, fraction, quanta);
        energy = euStore.stored();
        fraction = euStore.fraction();
        m_6596_();
        return accepted;
    }

    public void sync() {
        m_6596_();
        if (f_58857_ != null) f_58857_.m_7260_(m_58899_(), m_58900_(), m_58900_(), 3);
    }

    private static ListTag writeStacks(java.util.List<ItemStack> stacks) {
        var list = new ListTag();
        for (var stack : stacks) list.add(stack.m_41739_(new CompoundTag()));
        return list;
    }

    private static void readStacks(CompoundTag tag, String key, java.util.List<ItemStack> stacks) {
        stacks.clear();
        var list = tag.m_128437_(key, 10);
        for (int i = 0; i < list.size(); i++) {
            var stack = ItemStack.m_41712_(list.m_128728_(i));
            if (!stack.m_41619_()) stacks.add(stack);
        }
    }

    @Override
    protected void m_183515_(CompoundTag tag) {
        super.m_183515_(tag);
        tag.m_128356_("Energy", energy);
        tag.m_128405_("Fraction", fraction);
        tag.m_128379_("Enabled", enabled);
        tag.m_128405_("Progress", progress);
        tag.m_128405_("TotalWork", totalWork);
        tag.m_128405_("DoneWork", doneWork);
        tag.m_128405_("JobFortune", jobFortune);
        tag.m_128405_("JobMultiplier", jobMultiplier);
        tag.m_128379_("JobSmelt", jobSmelt);
        tag.m_128365_("Inventory", inventory.serializeNBT());
        tag.m_128365_("Reserved", writeStacks(reserved));
        tag.m_128365_("Pending", writeStacks(pending));
    }

    @Override
    public void m_142466_(CompoundTag tag) {
        super.m_142466_(tag);
        energy = Math.max(0, tag.m_128454_("Energy"));
        fraction = Math.floorMod(tag.m_128451_("Fraction"), 40);
        enabled = !tag.m_128441_("Enabled") || tag.m_128471_("Enabled");
        progress = tag.m_128451_("Progress");
        totalWork = tag.m_128451_("TotalWork");
        doneWork = tag.m_128451_("DoneWork");
        jobFortune = tag.m_128451_("JobFortune");
        jobMultiplier = Math.max(0, Math.min(100, tag.m_128451_("JobMultiplier")));
        jobSmelt = tag.m_128471_("JobSmelt");
        if (tag.m_128441_("Inventory")) inventory.deserializeNBT(tag.m_128469_("Inventory"));
        readStacks(tag, "Reserved", reserved);
        readStacks(tag, "Pending", pending);
        bindJob();
    }

    @Override
    public CompoundTag m_5995_() {
        var tag = new CompoundTag();
        m_183515_(tag);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket m_58483_() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.m_195640_(this);
    }

    public void dropContents() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            Block.m_49840_(f_58857_, m_58899_(), inventory.getStackInSlot(i));
            inventory.setStackInSlot(i, ItemStack.f_41583_);
        }
        for (var stack : reserved) Block.m_49840_(f_58857_, m_58899_(), stack);
        for (var stack : pending) Block.m_49840_(f_58857_, m_58899_(), stack);
        reserved.clear();
        pending.clear();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == ForgeCapabilities.ITEM_HANDLER ? items.cast() : super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        items.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        items = LazyOptional.of(() -> automation);
    }

    @Override
    public Component m_5446_() {
        return Component.m_237115_("block.loli_labs.fortune_transformer");
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu m_7208_(int id,
                                                                       net.minecraft.world.entity.player.Inventory inventory,
                                                                       net.minecraft.world.entity.player.Player player) {
        return null;
    }

    @Override
    public brachy.modularui.screen.ModularPanel<?> buildUI(brachy.modularui.factory.PosGuiData data,
                                                           brachy.modularui.value.sync.PanelSyncManager sync,
                                                           brachy.modularui.screen.UISettings settings) {
        return FortuneUI.build(this, sync, settings);
    }

    @Override
    public brachy.modularui.screen.ModularScreen createScreen(brachy.modularui.factory.PosGuiData data,
                                                              brachy.modularui.screen.ModularPanel<?> panel) {
        return new brachy.modularui.screen.ModularScreen("loli_labs", panel)
                .useTheme(com.ic2ceu.icceu.mui.GuiStyle.theme());
    }

    private final class Slots implements FortuneJob.Inventory {

        public int firstOre() {
            for (int i = 0; i < 27; i++) if (ore(inventory.getStackInSlot(i))) return i;
            return -1;
        }

        public int count(int slot) {
            return inventory.getStackInSlot(slot).m_41613_();
        }

        public boolean upgrade(int kind) {
            return FortuneEntity.this.upgrade(kind);
        }

        public int moduleMultiplier() {
            var stack = inventory.getStackInSlot(54);
            return stack.m_41720_() instanceof FortuneModule module && module.kind == 0 ? module.level : 0;
        }

        public int fortune() {
            return FortuneEntity.this.fortune();
        }

        public boolean outputSpace() {
            for (int i = 27; i < 54; i++) if (inventory.getStackInSlot(i).m_41619_() ||
                    inventory.getStackInSlot(i).m_41613_() < inventory.getStackInSlot(i).m_41741_())
                return true;
            return false;
        }

        public boolean afford(long cost) {
            return energy >= cost;
        }

        public boolean pay(long cost) {
            if (!euStore.consume(capacity(), energy, fraction, cost)) return false;
            energy = euStore.stored();
            fraction = euStore.fraction();
            return true;
        }

        public ItemStack extract(int slot, int count) {
            return inventory.extractItem(slot, count, false);
        }

        public ItemStack insertOutput(ItemStack stack) {
            return FortuneEntity.this.insertOutput(stack);
        }
    }
}
