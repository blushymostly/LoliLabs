package tmr.lolilabs.api;

/** Narrow energy readout for overlays (Jade) without machine-kind knowledge. */
public interface ForeignEnergyView {

    long foreignEuStored();

    long foreignEuCapacity();

    /** EU/t shown as generation; zero when idle or not a generator. */
    default long foreignEuRate() {
        return 0L;
    }

    /** Matter-style craft cost; zero when not energy-paid craft. */
    default long foreignEuCraftCost() {
        return 0L;
    }

    default int foreignEuCraftBatch() {
        return 0;
    }

    default boolean foreignEuInfinite() {
        return false;
    }
}
