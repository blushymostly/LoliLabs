package tmr.lolilabs.api;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Narrow craft progress readout for overlays without kind switches. */
public interface WorkableProgressView {

    boolean hasWorkableProgress();

    int workableProgressScaled(int scale);

    List<ItemStack> workableOutputs();

    /** Continuous fill for tick-based jobs; zero when unused. */
    default float workableFill() {
        return 0F;
    }

    default int workableDone() {
        return 0;
    }

    default int workableTotal() {
        return 0;
    }

    default boolean workableActive() {
        return hasWorkableProgress() && workableProgressScaled(WorkableProgressScale.UI) > 0;
    }
}
