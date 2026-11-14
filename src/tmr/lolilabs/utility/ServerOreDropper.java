package tmr.lolilabs.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/** Server loot table plus optional smelt. A fixed module count replaces the Fortune roll. */
public final class ServerOreDropper implements OreDropper {

    private final ServerLevel level;
    private final BlockPos pos;

    public ServerOreDropper(ServerLevel level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    @Override
    public List<ItemStack> drops(ItemStack ore, int fortune, boolean smelt, int fixedCount) {
        if (!(ore.m_41720_() instanceof BlockItem item)) return List.of();
        var tool = new ItemStack(ForgeRegistries.ITEMS
                .getValue(new net.minecraft.resources.ResourceLocation("minecraft:diamond_pickaxe")));
        // Modules specify an exact output ratio. Only a pickaxe uses the probabilistic Fortune enchantment.
        net.minecraft.world.item.enchantment.EnchantmentHelper.m_44865_(java.util.Map
                .of(net.minecraft.world.item.enchantment.Enchantments.f_44987_, fixedCount > 0 ? 0 : fortune), tool);
        var nativeDrops = Block.m_49874_(item.m_40614_().m_49966_(), level, pos, null, null, tool);
        var result = new ArrayList<ItemStack>();
        var seen = new ArrayList<ItemStack>();
        for (var drop : nativeDrops) {
            if (drop.m_41619_()) continue;
            if (fixedCount > 0) {
                if (seen.stream().anyMatch(other -> ItemStack.m_150942_(other, drop))) continue;
                seen.add(drop.m_41777_());
            }
            int count = fixedCount > 0 ? fixedCount : drop.m_41613_();
            var output = drop.m_255036_(1);
            if (smelt) {
                var recipe = level.m_7465_().m_44015_(net.minecraft.world.item.crafting.RecipeType.f_44108_,
                        new SimpleContainer(output), level);
                if (recipe.isPresent()) {
                    output = recipe.get().m_5874_(new SimpleContainer(output), level.m_9598_()).m_41777_();
                    count *= output.m_41613_();
                }
            }
            if (output.m_41619_()) continue;
            while (count > 0) {
                int n = Math.min(count, output.m_41741_());
                result.add(output.m_255036_(n));
                count -= n;
            }
        }
        return result;
    }
}
