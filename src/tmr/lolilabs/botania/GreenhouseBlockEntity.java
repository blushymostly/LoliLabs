package tmr.lolilabs.botania;

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
import tmr.lolilabs.LoliLabs;

public final class GreenhouseBlockEntity extends BlockEntity implements com.ic2ceu.icceu.EUReceiver, MenuProvider,
        brachy.modularui.api.IUIHolder<brachy.modularui.factory.PosGuiData>,
        vazkii.botania.api.mana.ManaPool, vazkii.botania.api.mana.spark.SparkAttachable, software.bernie.geckolib.animatable.GeoBlockEntity {

    public static final int FLOWER_BASE = 0;
    public static final int FLOWER_MAX = LabBotaniaTiers.BASE_FLOWER_SLOTS + LabBotaniaTiers.EXPANSION_SLOTS;
    public static final int UPGRADE_BASE = FLOWER_MAX;
    public static final int CRYSTAL_BASE = UPGRADE_BASE + LabBotaniaTiers.UPGRADE_SLOTS;
    public static final int SLOTS = CRYSTAL_BASE + LabBotaniaTiers.CRYSTAL_SLOTS;

    private final software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache animationCache=software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache(this);
    @Override public software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache getAnimatableInstanceCache(){return animationCache;}
    @Override public void registerControllers(software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar controllers){
        controllers.add(new software.bernie.geckolib.core.animation.AnimationController<GreenhouseBlockEntity>(this,"greenhouse",0,state->{
            if(enabled&&isFormed())return state.setAndContinue(software.bernie.geckolib.core.animation.RawAnimation.begin().thenLoop("animation.model.loop"));
            return software.bernie.geckolib.core.object.PlayState.STOP;
        }));
    }
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(){return new net.minecraft.world.phys.AABB(m_58899_()).m_82400_(4);}
    public boolean enabled = true;
    public GreenhouseStructure.Status structureStatus = GreenhouseStructure.Status.INCOMPLETE;
    public int mana;
    public long energy;
    public int euFraction;
    private boolean loadingInventory;
    public final GreenhouseFlowers flowers=new GreenhouseFlowers();
    private boolean inventorySync;
    public final long[] upgradeInstalledAt={Long.MIN_VALUE,Long.MIN_VALUE,Long.MIN_VALUE,Long.MIN_VALUE};
    private final net.minecraft.world.item.Item[] observedUpgrades=new net.minecraft.world.item.Item[4];
    private boolean returnLegacyItems;
    private boolean inManaNetwork;
    public int progress;
    public int manaCap = LabBotaniaTiers.BASE_MANA_CAPACITY;
    public long energyCap = LabBotaniaTiers.BASE_EU_CAPACITY;
    private final boolean part;
    private final com.ic2ceu.icceu.EnergyBuffer eu = new com.ic2ceu.icceu.EnergyBuffer(1);
    private LazyOptional<IItemHandler> items = LazyOptional.empty();

    public final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override protected void onContentsChanged(int slot) {
            if(loadingInventory||(f_58857_!=null&&f_58857_.f_46443_))return;
            if(slot>=UPGRADE_BASE+8&&slot<CRYSTAL_BASE){int index=slot-UPGRADE_BASE-8;var stack=getStackInSlot(slot);var item=stack.m_41619_()?null:stack.m_41720_();if(item!=observedUpgrades[index]){observedUpgrades[index]=item;upgradeInstalledAt[index]=item!=null&&f_58857_!=null?f_58857_.m_46467_():Long.MIN_VALUE;}}
            GreenhouseBlockEntity.this.m_6596_();
            GreenhouseBlockEntity.this.recomputeCaps();
            if(slot<8||slot>=UPGRADE_BASE){inventorySync=true;returnLegacyItems=true;}
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            if (slot >= UPGRADE_BASE && slot < CRYSTAL_BASE) return validUpgrade(slot-UPGRADE_BASE,stack);
            if(slot>=8&&slot<40)return GreenhouseUpgrade.of(stack)==null;
            return slot<8&&GreenhouseFlowers.acceptsFlower(stack);
        }
        @Override public int getSlotLimit(int slot) {
            return slot >= UPGRADE_BASE && slot < CRYSTAL_BASE ? 1 : slot<8?flowerLimit():64;
        }
    };

    public GreenhouseBlockEntity(BlockPos pos, BlockState state) {
        super(LabBotaniaBlocks.GREENHOUSE_ENTITY.get(), pos, state);
        this.part = state.m_60734_() instanceof GreenhousePartBlock;
        items = LazyOptional.of(() -> new GreenhouseAutomation(this));
        recomputeCaps();
    }

    public boolean isPart() { return part; }
    public boolean isFormed() { return !m_58901_() && !part && structureStatus == GreenhouseStructure.Status.FORMED; }
    public GreenhouseBlockEntity core() { return part ? GreenhouseStructure.controllerOf(this) : this; }

    public boolean validUpgrade(int slot,ItemStack stack){var upgrade=GreenhouseUpgrade.of(stack);return upgrade!=null&&(upgrade.kind==GreenhouseUpgrade.kindForSlot(slot)||(slot==8&&upgrade.kind==GreenhouseUpgrade.Kind.PROCESSING));}
    public GreenhouseUpgrade activeUpgrade(int slot){var stack=inventory.getStackInSlot(UPGRADE_BASE+slot);return validUpgrade(slot,stack)?GreenhouseUpgrade.of(stack):null;}
    public int upgradeLevel(int slot){var upgrade=activeUpgrade(slot);return upgrade==null?0:upgrade.level;}
    /** Rehome old freely placed upgrades. Duplicate/unsupported stacks remain extractable, never deleted. */
    private void migrateUpgradeSlots(){
        var pending=new java.util.ArrayList<ItemStack>();
        for(int i=0;i<LabBotaniaTiers.UPGRADE_SLOTS;i++){var stack=inventory.getStackInSlot(UPGRADE_BASE+i);if(!stack.m_41619_())pending.add(stack);inventory.setStackInSlot(UPGRADE_BASE+i,ItemStack.f_41583_);}
        var overflow=new java.util.ArrayList<ItemStack>();
        for(var stack:pending){var upgrade=GreenhouseUpgrade.of(stack);int target=upgrade==null?-1:GreenhouseUpgrade.slotFor(upgrade.kind);
            if(target>=0&&inventory.getStackInSlot(UPGRADE_BASE+target).m_41619_())inventory.setStackInSlot(UPGRADE_BASE+target,stack);else overflow.add(stack);}
        for(var stack:overflow)for(int i=0;i<LabBotaniaTiers.UPGRADE_SLOTS;i++)if(inventory.getStackInSlot(UPGRADE_BASE+i).m_41619_()){inventory.setStackInSlot(UPGRADE_BASE+i,stack);break;}
    }
    public void recomputeCaps() {
        int manaBuf = LabBotaniaTiers.BASE_MANA_CAPACITY;
        long euBuf = LabBotaniaTiers.BASE_EU_CAPACITY;
        for (int i = UPGRADE_BASE; i < CRYSTAL_BASE; i++) {
            var upgrade = activeUpgrade(i-UPGRADE_BASE);
            if (upgrade == null) continue;
            int lvl = upgrade.level - 1;
            if (upgrade.kind == GreenhouseUpgrade.Kind.MANA_BUFFER) manaBuf += LabBotaniaTiers.MANA_BUFFER[Math.min(lvl, LabBotaniaTiers.MANA_BUFFER.length - 1)];
            if (upgrade.kind == GreenhouseUpgrade.Kind.EU_BUFFER) euBuf += LabBotaniaTiers.EU_BUFFER[Math.min(lvl, LabBotaniaTiers.EU_BUFFER.length - 1)];
        }
        manaCap = Math.max(manaBuf,mana);
        energyCap = Math.max(euBuf,energy);
        mana = Math.min(mana, manaCap);
        energy = Math.min(energy, energyCap);
        eu.resize(energyCap);
        eu.restore(energy);
    }

    public int flowerSlots(){return 8;}
    public int flowerLimit(){var upgrade=activeUpgrade(10);return upgrade==null?1:LabBotaniaTiers.MULTIPLICATION[upgrade.level-1];}
    public boolean processing(){var upgrade=activeUpgrade(8);return upgrade!=null&&upgrade.kind==GreenhouseUpgrade.Kind.PROCESSING;}
    public boolean hasCrystallization(){return false;}
    public int workBonusAndPay(){
        var first=activeUpgrade(8);int bonus=first!=null&&first.kind==GreenhouseUpgrade.Kind.EFFICIENCY?10:0;
        var speed=activeUpgrade(11);long cost=speed==null?0:LabBotaniaTiers.EU_SPEED_COST[speed.level-1];
        if(speed!=null&&cost<=energy){energy-=cost;bonus+=LabBotaniaTiers.EU_SPEED_PCT[speed.level-1];}
        return bonus;
    }

    private void returnStack(ItemStack stack){
        for(int target=8;target<40&&!stack.m_41619_();target++){
            // Legacy upgrades are extractable here, but the slot still rejects new upgrade insertions.
            if(inventory.getStackInSlot(target).m_41619_()){inventory.setStackInSlot(target,stack);return;}
            stack=inventory.insertItem(target,stack,false);
        }
        if(!stack.m_41619_()){
            var front=m_58900_().m_61143_(GreenhouseCoreBlock.FACING);
            net.minecraft.world.Containers.m_18992_(f_58857_,f_58858_.m_123341_()+front.m_122429_()*2+.5,f_58858_.m_123342_()+.5,f_58858_.m_123343_()+front.m_122431_()*2+.5,stack);
        }
    }
    private void returnLegacyItems(){
        if(!returnLegacyItems||f_58857_==null||f_58857_.f_46443_)return;
        // No hidden inventory: removed upgrades and old expansion/crystal contents are returned.
        for(int i=0;i<SLOTS;i++)if(i>=40&&(i<UPGRADE_BASE+8||i>=CRYSTAL_BASE)||i>=UPGRADE_BASE+8&&i<CRYSTAL_BASE&&!inventory.getStackInSlot(i).m_41619_()&&!validUpgrade(i-UPGRADE_BASE,inventory.getStackInSlot(i))){
            var stack=inventory.extractItem(i,64,false);if(stack.m_41619_())continue;
            returnStack(stack);
        }
        for(int slot=0;slot<8;slot++){
            var stack=inventory.getStackInSlot(slot);int keep=GreenhouseFlowers.acceptsFlower(stack)?Math.min(stack.m_41613_(),flowerLimit()):0;
            if(stack.m_41613_()>keep){var excess=inventory.extractItem(slot,stack.m_41613_()-keep,false);returnStack(excess);}
        }
        returnLegacyItems=false;
    }
    public boolean refreshStructure() {
        var match = GreenhouseStructure.match(this);
        if (match.status() == GreenhouseStructure.Status.UNLOADED) {
            structureStatus = match.status();
            return false;
        }
        var before=structureStatus;
        structureStatus = match.formed() ? GreenhouseStructure.Status.FORMED : match.status();
        updateFormedBlocks(match.formed());
        if(before!=structureStatus)syncSettings();
        return true;
    }

    public void updateFormedBlocks(boolean formed){
        if(f_58857_==null||f_58857_.f_46443_)return;
        var positions=new java.util.ArrayList<BlockPos>();positions.add(m_58899_());
        for(var relative:GreenhouseStructure.relativePattern().keySet())positions.add(m_58899_().m_7918_(relative.m_123341_(),relative.m_123342_(),relative.m_123343_()));
        for(var pos:positions){
            if(!f_58857_.m_46805_(pos))continue;
            var state=f_58857_.m_8055_(pos);
            if(state.m_61138_(GreenhouseCoreBlock.FORMED)){
                var next=state.m_61124_(GreenhouseCoreBlock.FORMED,formed).m_61124_(GreenhouseCoreBlock.LIT,formed&&enabled);
                if(next!=state)f_58857_.m_46597_(pos,next);
            }
        }
    }
    public static void tick(Level level, BlockPos pos, BlockState state, GreenhouseBlockEntity be) {
        if (be.part || level.f_46443_) return;
        be.returnLegacyItems();
        if (!be.refreshStructure()) { be.m_6596_(); return; }
        if(be.inManaNetwork!=be.isFormed()){
            vazkii.botania.api.BotaniaAPI.instance().getManaNetworkInstance().fireManaNetworkEvent(be,vazkii.botania.api.mana.ManaBlockType.POOL,be.isFormed()?vazkii.botania.api.mana.ManaNetworkAction.ADD:vazkii.botania.api.mana.ManaNetworkAction.REMOVE);be.inManaNetwork=be.isFormed();
        }
        if (!be.enabled || !be.isFormed()) return;
        be.recomputeCaps();
        be.flowers.tick(be);
        if(be.flowers.hasWork(be)&&be.mana<be.manaCap){
            int bonus=be.workBonusAndPay();
            be.progress+=bonus;while(be.progress>=100){be.progress-=100;be.flowers.tick(be);}
        }
        if(be.inventorySync||level.m_46467_()%20==0){be.inventorySync=false;be.syncSettings();}
        be.m_6596_();
    }

    public void syncSettings() {
        if(!part)updateFormedBlocks(isFormed());
        m_6596_();
        if (f_58857_ != null) f_58857_.m_7260_(f_58858_, m_58900_(), m_58900_(), 3);
    }

    @Override public CompoundTag m_5995_() {
        var n = new CompoundTag();
        n.m_128379_("Enabled", enabled);
        n.m_128379_("Formed",isFormed());
        n.m_128405_("Mana", mana);
        n.m_128356_("Energy", energy);
        n.m_128405_("EUFraction",euFraction);
        n.m_128405_("UpgradeLayoutVersion",4);
        for(int i=0;i<4;i++)n.m_128356_("UpgradeInstalledAt"+i,upgradeInstalledAt[i]);
        n.m_128405_("ManaCapacity",manaCap);
        n.m_128356_("EnergyCapacity",energyCap);
        n.m_128405_("Progress", progress);
        n.m_128359_("Structure", structureStatus.name());
        var display=new net.minecraft.nbt.ListTag();for(int i=0;i<8;i++)display.add(inventory.getStackInSlot(i).m_41739_(new CompoundTag()));n.m_128365_("DisplayFlowers",display);
        return n;
    }

    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket m_58483_() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.m_195640_(this);
    }

    @Override protected void m_183515_(CompoundTag n) {
        super.m_183515_(n);
        n.m_128405_("UpgradeLayoutVersion",4);
        for(int i=0;i<4;i++)n.m_128356_("UpgradeInstalledAt"+i,upgradeInstalledAt[i]);
        n.m_128405_("EUFraction",euFraction);
        n.m_128365_("FlowerState",flowers.save());
        n.m_128379_("Formed",isFormed());
        n.m_128379_("Enabled", enabled);
        n.m_128405_("Mana", mana);
        n.m_128356_("Energy", energy);
        n.m_128405_("Progress", progress);
        if (!part) n.m_128365_("Inv", inventory.serializeNBT());
    }

    @Override public void m_142466_(CompoundTag n) {
        super.m_142466_(n);
        structureStatus=n.m_128471_("Formed")?GreenhouseStructure.Status.FORMED:GreenhouseStructure.Status.INCOMPLETE;
        enabled = !n.m_128441_("Enabled") || n.m_128471_("Enabled");
        mana = n.m_128451_("Mana");
        energy = n.m_128454_("Energy");
        progress = Math.floorMod(n.m_128451_("Progress"),100);
        flowers.load(n.m_128437_("FlowerState",10));
        if(n.m_128425_("DisplayFlowers",9)){var display=n.m_128437_("DisplayFlowers",10);loadingInventory=true;try{for(int i=0;i<Math.min(8,display.size());i++)inventory.setStackInSlot(i,ItemStack.m_41712_(display.m_128728_(i)));}finally{loadingInventory=false;}}
        euFraction=Math.max(0,Math.min(39,n.m_128451_("EUFraction")));
        if(n.m_128425_("Inv",10)){
            loadingInventory=true;
            try{inventory.deserializeNBT(n.m_128469_("Inv"));if(n.m_128451_("UpgradeLayoutVersion")<4)migrateUpgradeSlots();returnLegacyItems=true;}
            finally{loadingInventory=false;}
            recomputeCaps();
        }else if(n.m_128441_("EnergyCapacity")){
            manaCap=Math.max(1,n.m_128451_("ManaCapacity"));energyCap=Math.max(1,n.m_128454_("EnergyCapacity"));
        }else recomputeCaps();
        for(int i=0;i<4;i++){upgradeInstalledAt[i]=n.m_128441_("UpgradeInstalledAt"+i)?n.m_128454_("UpgradeInstalledAt"+i):Long.MIN_VALUE;var stack=inventory.getStackInSlot(UPGRADE_BASE+8+i);observedUpgrades[i]=stack.m_41619_()?null:stack.m_41720_();}
    }

    @Override public boolean acceptsEnergy(Direction side) {var core=core();return core!=null&&core.enabled&&core.isFormed()&&core.energy<core.energyCap;}
    @Override public boolean connectsEU(Direction side) {var core=core();return core!=null&&core.isFormed();}
    @Override public long receivePacket(Direction side, long quanta, long voltage) {
        if(part){var core=core();return core==null?0:core.receivePacket(side,quanta,voltage);}
        if (!acceptsEnergy(side) || quanta <= 0) return 0;
        eu.resize(energyCap);
        eu.restore(energy);
        eu.restoreFraction(euFraction);
        long accepted = eu.changeQuanta(quanta);
        energy = eu.stored();
        euFraction=eu.fraction();
        m_6596_();
        return accepted;
    }
    public Level labLevel() { return f_58857_; }
    public BlockPos labPos() { return f_58858_; }

    @Override public Level getManaReceiverLevel() { return f_58857_; }
    @Override public BlockPos getManaReceiverPos() { return f_58858_; }
    @Override public int getCurrentMana() { var core=core();return core==null?0:core.mana; }
    @Override public boolean isFull() { var core=core();return core==null||core.mana>=core.manaCap; }
    @Override public void receiveMana(int amount) { if(part){var core=core();if(core!=null)core.receiveMana(amount);return;}mana=(int)Math.max(0,Math.min((long)manaCap,(long)mana+amount));m_6596_(); }
    @Override public boolean canReceiveManaFromBursts() { var core=core();return core!=null&&core.enabled&&core.isFormed(); }

    @Override public int getMaxMana(){var core=core();return core==null?0:core.manaCap;}
    @Override public boolean isOutputtingPower(){return true;}
    @Override public java.util.Optional<net.minecraft.world.item.DyeColor> getColor(){return java.util.Optional.empty();}
    @Override public void setColor(java.util.Optional<net.minecraft.world.item.DyeColor> color){}
    @Override public boolean canAttachSpark(ItemStack stack){var core=core();return core!=null&&core.isFormed()&&m_58899_().m_123342_()==core.m_58899_().m_123342_()+1;}
    @Override public int getAvailableSpaceForMana(){return Math.max(0,getMaxMana()-getCurrentMana());}
    @Override public vazkii.botania.api.mana.spark.ManaSpark getAttachedSpark(){
        var core=core();if(core==null||f_58857_==null)return null;
        for(var entity:f_58857_.m_45933_((net.minecraft.world.entity.Entity)null,new net.minecraft.world.phys.AABB(core.m_58899_().m_7918_(-1,2,-1),core.m_58899_().m_7918_(2,3,2))))if(entity instanceof vazkii.botania.api.mana.spark.ManaSpark spark)return spark;
        return null;
    }
    @Override public boolean areIncomingTranfersDone(){return isFull();}
    @Override public void m_7651_(){if(inManaNetwork)vazkii.botania.api.BotaniaAPI.instance().getManaNetworkInstance().fireManaNetworkEvent(this,vazkii.botania.api.mana.ManaBlockType.POOL,vazkii.botania.api.mana.ManaNetworkAction.REMOVE);inManaNetwork=false;super.m_7651_();}

    @Override public Component m_5446_() { return Component.m_237115_("block." + LoliLabs.MOD_ID + ".multi_greenhouse_core"); }
    @Override public AbstractContainerMenu m_7208_(int id, Inventory inv, Player player) { return null; }

    @Override public brachy.modularui.screen.ModularPanel<?> buildUI(brachy.modularui.factory.PosGuiData data, brachy.modularui.value.sync.PanelSyncManager sync, brachy.modularui.screen.UISettings settings) {
        return GreenhouseUI.build(this, sync, settings);
    }

    @Override public brachy.modularui.screen.ModularScreen createScreen(brachy.modularui.factory.PosGuiData data, brachy.modularui.screen.ModularPanel<?> panel) {
        return com.ic2ceu.icceu.mui.GuiStyle.screen(panel);
    }

    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return items.cast();
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); items.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); items = LazyOptional.of(() -> new GreenhouseAutomation(this)); }
}
