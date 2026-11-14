package tmr.lolilabs.magically;

import tmr.lolilabs.LoliLabs;
import tmr.lolilabs.api.ForeignEnergyView;
import tmr.lolilabs.api.ManaView;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public final class GreenhouseBlockEntity extends BlockEntity
                                         implements com.ic2ceu.icceu.api.energy.EUReceiver, MenuProvider,
                                         brachy.modularui.api.IUIHolder<brachy.modularui.factory.PosGuiData>,
                                         vazkii.botania.api.mana.ManaPool,
                                         vazkii.botania.api.mana.spark.SparkAttachable,
                                         software.bernie.geckolib.animatable.GeoBlockEntity,
                                         ForeignEnergyView, ManaView {

    public static final int FLOWER_BASE = GreenhouseCore.FLOWER_BASE;
    public static final int FLOWER_MAX = GreenhouseCore.FLOWER_MAX;
    public static final int UPGRADE_BASE = GreenhouseCore.UPGRADE_BASE;
    public static final int CRYSTAL_BASE = GreenhouseCore.CRYSTAL_BASE;
    public static final int SLOTS = GreenhouseCore.SLOTS;

    private final boolean part = m_58900_().m_60734_() instanceof GreenhousePartBlock;
    private boolean loadingInventory;
    private boolean inventorySync;
    private boolean returnLegacyItems;
    private boolean inManaNetwork;
    public final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {

        @Override
        protected void onContentsChanged(int slot) {
            if (loadingInventory || (f_58857_ != null && f_58857_.f_46443_)) return;
            if (slot >= UPGRADE_BASE + 8 && slot < CRYSTAL_BASE) domain.observeUpgrade(slot - UPGRADE_BASE - 8,
                    getStackInSlot(slot), f_58857_ == null ? Long.MIN_VALUE : f_58857_.m_46467_());
            GreenhouseBlockEntity.this.m_6596_();
            domain.recomputeCaps();
            if (slot < 8 || slot >= UPGRADE_BASE) {
                inventorySync = true;
                returnLegacyItems = true;
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot >= UPGRADE_BASE && slot < CRYSTAL_BASE) return validUpgrade(slot - UPGRADE_BASE, stack);
            if (slot >= 8 && slot < 40) return GreenhouseUpgrade.of(stack) == null;
            return slot < 8 && GreenhouseFlowers.acceptsFlower(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= UPGRADE_BASE && slot < CRYSTAL_BASE ? 1 : slot < 8 ? flowerLimit() : 64;
        }
    };
    public final GreenhouseCore domain = new GreenhouseCore(inventory, part);
    private final software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache animationCache = software.bernie.geckolib.util.GeckoLibUtil
            .createInstanceCache(this);
    private LazyOptional<IItemHandler> items = LazyOptional.empty();

    public GreenhouseBlockEntity(BlockPos pos, BlockState state) {
        super(LabBotaniaBlocks.GREENHOUSE_ENTITY.get(), pos, state);
        items = LazyOptional.of(() -> new GreenhouseAutomation(this));
    }

    @Override
    public software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public void registerControllers(software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new software.bernie.geckolib.core.animation.AnimationController<GreenhouseBlockEntity>(this,
                "greenhouse", 0, state -> {
                    if (domain.enabled && isFormed())
                        return state.setAndContinue(software.bernie.geckolib.core.animation.RawAnimation.begin()
                                .thenLoop("animation.model.loop"));
                    return software.bernie.geckolib.core.object.PlayState.STOP;
                }));
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(m_58899_()).m_82400_(4);
    }

    public boolean isPart() {
        return part;
    }

    public boolean isFormed() {
        return domain.formed(m_58901_());
    }

    public GreenhouseBlockEntity core() {
        return part ? GreenhouseStructure.controllerOf(this) : this;
    }

    @Override
    public long foreignEuStored() {
        var controller = core();
        return controller == null ? 0L : controller.domain.energy;
    }

    @Override
    public long foreignEuCapacity() {
        var controller = core();
        return controller == null ? 0L : controller.domain.energyCap;
    }

    public int manaStored() {
        var controller = core();
        return controller == null ? 0 : controller.domain.mana;
    }

    public int manaCapacity() {
        var controller = core();
        return controller == null ? 0 : Math.max(1, controller.domain.manaCap);
    }

    public boolean validUpgrade(int slot, ItemStack stack) {
        return domain.validUpgrade(slot, stack);
    }

    public GreenhouseUpgrade activeUpgrade(int slot) {
        return domain.activeUpgrade(slot);
    }

    public int upgradeLevel(int slot) {
        return domain.upgradeLevel(slot);
    }

    public void recomputeCaps() {
        domain.recomputeCaps();
    }

    public int flowerSlots() {
        return domain.flowerSlots();
    }

    public int flowerLimit() {
        return domain.flowerLimit();
    }

    public boolean processing() {
        return domain.processing();
    }

    public boolean hasCrystallization() {
        return domain.hasCrystallization();
    }

    public int workBonusAndPay() {
        return domain.workBonusAndPay();
    }

    private void returnStack(ItemStack stack) {
        for (int target = 8; target < 40 && !stack.m_41619_(); target++) {
            // Legacy upgrades are extractable here, but the slot still rejects new upgrade insertions.
            if (inventory.getStackInSlot(target).m_41619_()) {
                inventory.setStackInSlot(target, stack);
                return;
            }
            stack = inventory.insertItem(target, stack, false);
        }
        if (!stack.m_41619_()) {
            var front = m_58900_().m_61143_(GreenhouseCoreBlock.FACING);
            net.minecraft.world.Containers.m_18992_(f_58857_, f_58858_.m_123341_() + front.m_122429_() * 2 + .5,
                    f_58858_.m_123342_() + .5, f_58858_.m_123343_() + front.m_122431_() * 2 + .5, stack);
        }
    }

    private void returnLegacyItems() {
        if (!returnLegacyItems || f_58857_ == null || f_58857_.f_46443_) return;
        // No hidden inventory: removed upgrades and old expansion/crystal contents are returned.
        for (int i = 0; i < SLOTS; i++) if (i >= 40 && (i < UPGRADE_BASE + 8 || i >= CRYSTAL_BASE) ||
                i >= UPGRADE_BASE + 8 && i < CRYSTAL_BASE && !inventory.getStackInSlot(i).m_41619_() &&
                        !validUpgrade(i - UPGRADE_BASE, inventory.getStackInSlot(i))) {
                            var stack = inventory.extractItem(i, 64, false);
                            if (stack.m_41619_()) continue;
                            returnStack(stack);
                        }
        for (int slot = 0; slot < 8; slot++) {
            var stack = inventory.getStackInSlot(slot);
            int keep = GreenhouseFlowers.acceptsFlower(stack) ? Math.min(stack.m_41613_(), flowerLimit()) : 0;
            if (stack.m_41613_() > keep) {
                var excess = inventory.extractItem(slot, stack.m_41613_() - keep, false);
                returnStack(excess);
            }
        }
        returnLegacyItems = false;
    }

    public boolean refreshStructure() {
        var match = GreenhouseStructure.match(this);
        if (match.status() == GreenhouseStructure.Status.UNLOADED) {
            domain.structureStatus = match.status();
            return false;
        }
        var before = domain.structureStatus;
        domain.structureStatus = match.formed() ? GreenhouseStructure.Status.FORMED : match.status();
        updateFormedBlocks(match.formed());
        if (before != domain.structureStatus) syncSettings();
        return true;
    }

    public void updateFormedBlocks(boolean formed) {
        if (f_58857_ == null || f_58857_.f_46443_) return;
        var positions = new java.util.ArrayList<BlockPos>();
        positions.add(m_58899_());
        for (var relative : GreenhouseStructure.relativePattern().keySet())
            positions.add(m_58899_().m_7918_(relative.m_123341_(), relative.m_123342_(), relative.m_123343_()));
        for (var pos : positions) {
            if (!f_58857_.m_46805_(pos)) continue;
            var state = f_58857_.m_8055_(pos);
            if (state.m_61138_(GreenhouseCoreBlock.FORMED)) {
                var next = state.m_61124_(GreenhouseCoreBlock.FORMED, formed).m_61124_(GreenhouseCoreBlock.LIT,
                        formed && domain.enabled);
                if (next != state) f_58857_.m_46597_(pos, next);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GreenhouseBlockEntity be) {
        if (be.part || level.f_46443_) return;
        be.returnLegacyItems();
        if (!be.refreshStructure()) {
            be.m_6596_();
            return;
        }
        if (be.inManaNetwork != be.isFormed()) {
            vazkii.botania.api.BotaniaAPI.instance().getManaNetworkInstance().fireManaNetworkEvent(be,
                    vazkii.botania.api.mana.ManaBlockType.POOL,
                    be.isFormed() ? vazkii.botania.api.mana.ManaNetworkAction.ADD :
                            vazkii.botania.api.mana.ManaNetworkAction.REMOVE);
            be.inManaNetwork = be.isFormed();
        }
        if (!be.domain.enabled || !be.isFormed()) return;
        be.domain.tickWork();
        if (be.inventorySync || level.m_46467_() % 20 == 0) {
            be.inventorySync = false;
            be.syncSettings();
        }
        be.m_6596_();
    }

    public void syncSettings() {
        if (!part) updateFormedBlocks(isFormed());
        m_6596_();
        if (f_58857_ != null) f_58857_.m_7260_(f_58858_, m_58900_(), m_58900_(), 3);
    }

    @Override
    public CompoundTag m_5995_() {
        var n = new CompoundTag();
        domain.writeClient(n, isFormed());
        var display = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < 8; i++) display.add(inventory.getStackInSlot(i).m_41739_(new CompoundTag()));
        n.m_128365_("DisplayFlowers", display);
        return n;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket m_58483_() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.m_195640_(this);
    }

    @Override
    protected void m_183515_(CompoundTag n) {
        super.m_183515_(n);
        domain.writeSave(n, isFormed());
        if (!part) n.m_128365_("Inv", inventory.serializeNBT());
    }

    @Override
    public void m_142466_(CompoundTag n) {
        super.m_142466_(n);
        domain.readScalars(n);
        if (n.m_128425_("DisplayFlowers", 9)) {
            var display = n.m_128437_("DisplayFlowers", 10);
            loadingInventory = true;
            try {
                for (int i = 0; i < Math.min(8, display.size()); i++)
                    inventory.setStackInSlot(i, ItemStack.m_41712_(display.m_128728_(i)));
            } finally {
                loadingInventory = false;
            }
        }
        if (n.m_128425_("Inv", 10)) {
            loadingInventory = true;
            try {
                inventory.deserializeNBT(n.m_128469_("Inv"));
                if (n.m_128451_("UpgradeLayoutVersion") < 4) domain.migrateUpgradeSlots();
                returnLegacyItems = true;
            } finally {
                loadingInventory = false;
            }
            domain.recomputeCaps();
        } else domain.readCaps(n);
        domain.readUpgradeClocks(n);
    }

    @Override
    public boolean acceptsEnergy(Direction side) {
        var controller = core();
        return controller != null && controller.domain.acceptsEnergy(controller.isFormed());
    }

    @Override
    public boolean connectsEU(Direction side) {
        var controller = core();
        return controller != null && controller.isFormed();
    }

    @Override
    public long receivePacket(Direction side, long quanta, long voltage) {
        if (part) {
            var controller = core();
            return controller == null ? 0 : controller.receivePacket(side, quanta, voltage);
        }
        if (!acceptsEnergy(side) || quanta <= 0) return 0;
        long accepted = domain.receiveQuanta(quanta);
        m_6596_();
        return accepted;
    }

    public Level labLevel() {
        return f_58857_;
    }

    public BlockPos labPos() {
        return f_58858_;
    }

    @Override
    public Level getManaReceiverLevel() {
        return f_58857_;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return f_58858_;
    }

    @Override
    public int getCurrentMana() {
        var controller = core();
        return controller == null ? 0 : controller.domain.mana;
    }

    @Override
    public boolean isFull() {
        var controller = core();
        return controller == null || controller.domain.mana >= controller.domain.manaCap;
    }

    @Override
    public void receiveMana(int amount) {
        if (part) {
            var controller = core();
            if (controller != null) controller.receiveMana(amount);
            return;
        }
        domain.receiveMana(amount);
        m_6596_();
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        var controller = core();
        return controller != null && controller.domain.enabled && controller.isFormed();
    }

    @Override
    public int getMaxMana() {
        var controller = core();
        return controller == null ? 0 : controller.domain.manaCap;
    }

    @Override
    public boolean isOutputtingPower() {
        return true;
    }

    @Override
    public java.util.Optional<net.minecraft.world.item.DyeColor> getColor() {
        return java.util.Optional.empty();
    }

    @Override
    public void setColor(java.util.Optional<net.minecraft.world.item.DyeColor> color) {}

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        var controller = core();
        return controller != null && controller.isFormed() &&
                m_58899_().m_123342_() == controller.m_58899_().m_123342_() + 1;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(0, getMaxMana() - getCurrentMana());
    }

    @Override
    public vazkii.botania.api.mana.spark.ManaSpark getAttachedSpark() {
        var controller = core();
        if (controller == null || f_58857_ == null) return null;
        for (var entity : f_58857_.m_45933_((net.minecraft.world.entity.Entity) null,
                new net.minecraft.world.phys.AABB(controller.m_58899_().m_7918_(-1, 2, -1),
                        controller.m_58899_().m_7918_(2, 3, 2))))
            if (entity instanceof vazkii.botania.api.mana.spark.ManaSpark spark) return spark;
        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return isFull();
    }

    @Override
    public void m_7651_() {
        if (inManaNetwork) vazkii.botania.api.BotaniaAPI.instance().getManaNetworkInstance().fireManaNetworkEvent(this,
                vazkii.botania.api.mana.ManaBlockType.POOL, vazkii.botania.api.mana.ManaNetworkAction.REMOVE);
        inManaNetwork = false;
        super.m_7651_();
    }

    @Override
    public Component m_5446_() {
        return Component.m_237115_("block." + LoliLabs.MOD_ID + ".multi_greenhouse_core");
    }

    @Override
    public AbstractContainerMenu m_7208_(int id, Inventory inv, Player player) {
        return null;
    }

    @Override
    public brachy.modularui.screen.ModularPanel<?> buildUI(brachy.modularui.factory.PosGuiData data,
                                                           brachy.modularui.value.sync.PanelSyncManager sync,
                                                           brachy.modularui.screen.UISettings settings) {
        return GreenhouseUI.build(this, sync, settings);
    }

    @Override
    public brachy.modularui.screen.ModularScreen createScreen(brachy.modularui.factory.PosGuiData data,
                                                              brachy.modularui.screen.ModularPanel<?> panel) {
        return com.ic2ceu.icceu.mui.GuiStyle.screen(panel);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return items.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        items.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        items = LazyOptional.of(() -> new GreenhouseAutomation(this));
    }
}
