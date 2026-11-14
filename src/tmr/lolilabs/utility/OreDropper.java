package tmr.lolilabs.utility;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Rolls one ore into stacks. The job keeps that result; a blocked output must not roll again. */
public interface OreDropper {

    List<ItemStack> drops(ItemStack ore, int fortune, boolean smelt, int fixedCount);
}
