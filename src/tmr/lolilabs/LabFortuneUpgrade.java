package tmr.lolilabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
public final class LabFortuneUpgrade extends Item {
    private final int fortune;
    public LabFortuneUpgrade(int fortune){super(new Properties());this.fortune=fortune;}
    @Override public InteractionResultHolder<ItemStack> m_7203_(Level level,Player player,InteractionHand hand){
        var upgrade=player.m_21120_(hand);
        var tool=player.m_21120_(hand==InteractionHand.MAIN_HAND?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND);
        if (!(tool.m_41720_() instanceof LabMiningTool)) return InteractionResultHolder.m_19100_(upgrade);
        var enchantments=EnchantmentHelper.m_44831_(tool);
        if (enchantments.getOrDefault(Enchantments.f_44986_,0)>=fortune || enchantments.containsKey(Enchantments.f_44985_)) return InteractionResultHolder.m_19100_(upgrade);
        if (!level.f_46443_) { enchantments.put(Enchantments.f_44986_,fortune);EnchantmentHelper.m_44865_(enchantments,tool);if(!player.m_7500_())upgrade.m_41774_(1); }
        return InteractionResultHolder.m_19090_(upgrade);
    }
}
