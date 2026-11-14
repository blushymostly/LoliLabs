package tmr.lolilabs.api;

import com.ic2ceu.icceu.api.energy.EnergyBuffer;

import net.minecraft.nbt.CompoundTag;

/** Single Foreign EU store: whole EU + fraction behind one EnergyBuffer. */
public final class ForeignEuStore {

    private final EnergyBuffer buffer = new EnergyBuffer(1);

    public EnergyBuffer buffer() {
        return buffer;
    }

    public long stored() {
        return buffer.stored();
    }

    public int fraction() {
        return buffer.fraction();
    }

    public long capacity() {
        return buffer.capacity();
    }

    /** Load facade fields into the buffer at the given capacity before network/internal IO. */
    public void prepare(long capacity, long stored, int fraction) {
        buffer.resize(Math.max(1, capacity));
        buffer.restore(Math.max(0, stored));
        buffer.restoreFraction(fraction);
    }

    public long changeQuanta(long capacity, long stored, int fraction, long quanta) {
        prepare(capacity, stored, fraction);
        return buffer.changeQuanta(quanta);
    }

    public boolean consume(long capacity, long stored, int fraction, long amount) {
        prepare(capacity, stored, fraction);
        return buffer.consume(amount);
    }

    public void write(CompoundTag tag, String storedKey, String fractionKey, long stored, int fraction) {
        tag.m_128356_(storedKey, stored);
        tag.m_128405_(fractionKey, fraction);
    }

    public long readStored(CompoundTag tag, String storedKey) {
        return Math.max(0, tag.m_128454_(storedKey));
    }

    public int readFraction(CompoundTag tag, String fractionKey) {
        return Math.floorMod(tag.m_128451_(fractionKey), 40);
    }
}
