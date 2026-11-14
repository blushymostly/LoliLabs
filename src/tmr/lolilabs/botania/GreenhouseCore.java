package tmr.lolilabs.botania;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/** Mana, Foreign EU, upgrades, and flower work for one greenhouse. */
public final class GreenhouseCore {

    public static final int FLOWER_BASE = 0;
    public static final int FLOWER_MAX = LabBotaniaTiers.BASE_FLOWER_SLOTS + LabBotaniaTiers.EXPANSION_SLOTS;
    public static final int UPGRADE_BASE = FLOWER_MAX;
    public static final int CRYSTAL_BASE = UPGRADE_BASE + LabBotaniaTiers.UPGRADE_SLOTS;
    public static final int SLOTS = CRYSTAL_BASE + LabBotaniaTiers.CRYSTAL_SLOTS;

    public boolean enabled = true;
    public GreenhouseStructure.Status structureStatus = GreenhouseStructure.Status.INCOMPLETE;
    public int mana;
    public long energy;
    public int euFraction;
    public int progress;
    public int manaCap = LabBotaniaTiers.BASE_MANA_CAPACITY;
    public long energyCap = LabBotaniaTiers.BASE_EU_CAPACITY;
    public final long[] upgradeInstalledAt = { Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE };
    public final GreenhouseFlowers flowers = new GreenhouseFlowers();
    public final ItemStackHandler inventory;
    private final boolean part;
    private final tmr.lolilabs.eu.ForeignEuStore euStore = new tmr.lolilabs.eu.ForeignEuStore();
    private final net.minecraft.world.item.Item[] observedUpgrades = new net.minecraft.world.item.Item[4];

    public GreenhouseCore(ItemStackHandler inventory, boolean part) {
        this.inventory = inventory;
        this.part = part;
        recomputeCaps();
    }

    public boolean formed(boolean removed) {
        return !removed && !part && structureStatus == GreenhouseStructure.Status.FORMED;
    }

    public boolean validUpgrade(int slot, ItemStack stack) {
        var upgrade = GreenhouseUpgrade.of(stack);
        return upgrade != null && (upgrade.kind == GreenhouseUpgrade.kindForSlot(slot) ||
                (slot == 8 && upgrade.kind == GreenhouseUpgrade.Kind.PROCESSING));
    }

    public GreenhouseUpgrade activeUpgrade(int slot) {
        var stack = inventory.getStackInSlot(UPGRADE_BASE + slot);
        return validUpgrade(slot, stack) ? GreenhouseUpgrade.of(stack) : null;
    }

    public int upgradeLevel(int slot) {
        var upgrade = activeUpgrade(slot);
        return upgrade == null ? 0 : upgrade.level;
    }

    /** Rehome old freely placed upgrades. Duplicate/unsupported stacks remain extractable, never deleted. */
    public void migrateUpgradeSlots() {
        var pending = new java.util.ArrayList<ItemStack>();
        for (int i = 0; i < LabBotaniaTiers.UPGRADE_SLOTS; i++) {
            var stack = inventory.getStackInSlot(UPGRADE_BASE + i);
            if (!stack.m_41619_()) pending.add(stack);
            inventory.setStackInSlot(UPGRADE_BASE + i, ItemStack.f_41583_);
        }
        var overflow = new java.util.ArrayList<ItemStack>();
        for (var stack : pending) {
            var upgrade = GreenhouseUpgrade.of(stack);
            int target = upgrade == null ? -1 : GreenhouseUpgrade.slotFor(upgrade.kind);
            if (target >= 0 && inventory.getStackInSlot(UPGRADE_BASE + target).m_41619_())
                inventory.setStackInSlot(UPGRADE_BASE + target, stack);
            else overflow.add(stack);
        }
        for (var stack : overflow) for (int i = 0; i < LabBotaniaTiers.UPGRADE_SLOTS; i++)
            if (inventory.getStackInSlot(UPGRADE_BASE + i).m_41619_()) {
                inventory.setStackInSlot(UPGRADE_BASE + i, stack);
                break;
            }
    }

    public void recomputeCaps() {
        int manaBuf = LabBotaniaTiers.BASE_MANA_CAPACITY;
        long euBuf = LabBotaniaTiers.BASE_EU_CAPACITY;
        for (int i = UPGRADE_BASE; i < CRYSTAL_BASE; i++) {
            var upgrade = activeUpgrade(i - UPGRADE_BASE);
            if (upgrade == null) continue;
            int lvl = upgrade.level - 1;
            if (upgrade.kind == GreenhouseUpgrade.Kind.MANA_BUFFER)
                manaBuf += LabBotaniaTiers.MANA_BUFFER[Math.min(lvl, LabBotaniaTiers.MANA_BUFFER.length - 1)];
            if (upgrade.kind == GreenhouseUpgrade.Kind.EU_BUFFER)
                euBuf += LabBotaniaTiers.EU_BUFFER[Math.min(lvl, LabBotaniaTiers.EU_BUFFER.length - 1)];
        }
        manaCap = Math.max(manaBuf, mana);
        energyCap = Math.max(euBuf, energy);
        mana = Math.min(mana, manaCap);
        energy = Math.min(energy, energyCap);
    }

    public int flowerSlots() {
        return 8;
    }

    public int flowerLimit() {
        var upgrade = activeUpgrade(10);
        return upgrade == null ? 1 : LabBotaniaTiers.MULTIPLICATION[upgrade.level - 1];
    }

    public boolean processing() {
        var upgrade = activeUpgrade(8);
        return upgrade != null && upgrade.kind == GreenhouseUpgrade.Kind.PROCESSING;
    }

    public boolean hasCrystallization() {
        return false;
    }

    public int workBonusAndPay() {
        var first = activeUpgrade(8);
        int bonus = first != null && first.kind == GreenhouseUpgrade.Kind.EFFICIENCY ? 10 : 0;
        var speed = activeUpgrade(11);
        long cost = speed == null ? 0 : LabBotaniaTiers.EU_SPEED_COST[speed.level - 1];
        if (speed != null && cost <= energy) {
            energy -= cost;
            bonus += LabBotaniaTiers.EU_SPEED_PCT[speed.level - 1];
        }
        return bonus;
    }

    public void tickWork() {
        recomputeCaps();
        flowers.tick(this);
        if (flowers.hasWork(this) && mana < manaCap) {
            progress += workBonusAndPay();
            while (progress >= 100) {
                progress -= 100;
                flowers.tick(this);
            }
        }
    }

    public void receiveMana(int amount) {
        mana = (int) Math.max(0, Math.min((long) manaCap, (long) mana + amount));
    }

    public boolean acceptsEnergy(boolean formed) {
        return enabled && formed && energy < energyCap;
    }

    public long receiveQuanta(long quanta) {
        long accepted = euStore.changeQuanta(energyCap, energy, euFraction, quanta);
        energy = euStore.stored();
        euFraction = euStore.fraction();
        return accepted;
    }

    public void observeUpgrade(int index, ItemStack stack, long gameTime) {
        var item = stack.m_41619_() ? null : stack.m_41720_();
        if (item != observedUpgrades[index]) {
            observedUpgrades[index] = item;
            upgradeInstalledAt[index] = item != null ? gameTime : Long.MIN_VALUE;
        }
    }

    public void writeClient(CompoundTag n, boolean formed) {
        n.m_128379_("Enabled", enabled);
        n.m_128379_("Formed", formed);
        n.m_128405_("Mana", mana);
        n.m_128356_("Energy", energy);
        n.m_128405_("EUFraction", euFraction);
        n.m_128405_("UpgradeLayoutVersion", 4);
        for (int i = 0; i < 4; i++) n.m_128356_("UpgradeInstalledAt" + i, upgradeInstalledAt[i]);
        n.m_128405_("ManaCapacity", manaCap);
        n.m_128356_("EnergyCapacity", energyCap);
        n.m_128405_("Progress", progress);
        n.m_128359_("Structure", structureStatus.name());
    }

    public void writeSave(CompoundTag n, boolean formed) {
        n.m_128405_("UpgradeLayoutVersion", 4);
        for (int i = 0; i < 4; i++) n.m_128356_("UpgradeInstalledAt" + i, upgradeInstalledAt[i]);
        n.m_128405_("EUFraction", euFraction);
        n.m_128365_("FlowerState", flowers.save());
        n.m_128379_("Formed", formed);
        n.m_128379_("Enabled", enabled);
        n.m_128405_("Mana", mana);
        n.m_128356_("Energy", energy);
        n.m_128405_("Progress", progress);
    }

    public void readScalars(CompoundTag n) {
        structureStatus = n.m_128471_("Formed") ? GreenhouseStructure.Status.FORMED :
                GreenhouseStructure.Status.INCOMPLETE;
        enabled = !n.m_128441_("Enabled") || n.m_128471_("Enabled");
        mana = n.m_128451_("Mana");
        energy = n.m_128454_("Energy");
        progress = Math.floorMod(n.m_128451_("Progress"), 100);
        flowers.load(n.m_128437_("FlowerState", 10));
        euFraction = Math.max(0, Math.min(39, n.m_128451_("EUFraction")));
    }

    public void readCaps(CompoundTag n) {
        if (n.m_128441_("EnergyCapacity")) {
            manaCap = Math.max(1, n.m_128451_("ManaCapacity"));
            energyCap = Math.max(1, n.m_128454_("EnergyCapacity"));
        } else recomputeCaps();
    }

    public void readUpgradeClocks(CompoundTag n) {
        for (int i = 0; i < 4; i++) {
            upgradeInstalledAt[i] = n.m_128441_("UpgradeInstalledAt" + i) ? n.m_128454_("UpgradeInstalledAt" + i) :
                    Long.MIN_VALUE;
            var stack = inventory.getStackInSlot(UPGRADE_BASE + 8 + i);
            observedUpgrades[i] = stack.m_41619_() ? null : stack.m_41720_();
        }
    }
}
