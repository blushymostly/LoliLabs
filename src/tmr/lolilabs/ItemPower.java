package tmr.lolilabs;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

public final class ItemPower implements IEnergyStorage, ICapabilityProvider {

    private final ItemStack stack;
    private final int capacity;
    private final boolean discharge;
    private final LazyOptional<IEnergyStorage> capability = LazyOptional.of(() -> this);

    public ItemPower(ItemStack stack, int capacity, boolean discharge) {
        this.stack = stack;
        this.capacity = capacity;
        this.discharge = discharge;
    }

    public static int stored(ItemStack stack) {
        return Math.max(0, stack.m_41784_().m_128451_("LabEnergy"));
    }

    public static boolean spend(ItemStack stack, int amount) {
        if (amount < 0) return false;
        int stored = stored(stack);
        if (stored < amount) return false;
        stack.m_41784_().m_128405_("LabEnergy", stored - amount);
        return true;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate) {
        int received = Math.max(0, Math.min(amount, getMaxEnergyStored() - getEnergyStored()));
        if (!simulate) stack.m_41784_().m_128405_("LabEnergy", getEnergyStored() + received);
        return received;
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        int extracted = discharge ? Math.max(0, Math.min(amount, getEnergyStored())) : 0;
        if (!simulate) stack.m_41784_().m_128405_("LabEnergy", getEnergyStored() - extracted);
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return Math.min(stored(stack), getMaxEnergyStored());
    }

    @Override
    public int getMaxEnergyStored() {
        int level = Math.min(5, stack.m_41784_().m_128451_("LabPowerLevel"));
        return (int) Math.min(Integer.MAX_VALUE, (long) capacity * (1 + Math.max(0, level)));
    }

    @Override
    public boolean canExtract() {
        return discharge;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> requested, Direction side) {
        return requested == ForgeCapabilities.ENERGY ? capability.cast() : LazyOptional.empty();
    }
}
