package tmr.lolilabs.api;

/** Narrow mana readout for overlays without greenhouse types. */
public interface ManaView {

    int manaStored();

    int manaCapacity();
}
