package tmr.lolilabs;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class LabManaFocus extends vazkii.botania.common.item.ManaBlasterItem {

    public LabManaFocus() {
        super(new Properties().m_41487_(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        var stack = player.m_21120_(hand);
        if (!level.f_46443_ && getLens(stack).m_41619_()) {
            var lens = net.minecraftforge.registries.ForgeRegistries.ITEMS
                    .getValue(new net.minecraft.resources.ResourceLocation("botania", "lens_damage"));
            if (lens != null) setLens(stack, new ItemStack(lens));
        }
        return super.m_7203_(level, player, hand);
    }
}
