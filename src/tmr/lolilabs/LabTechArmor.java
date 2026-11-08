package tmr.lolilabs;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class LabTechArmor extends LabArmor implements vazkii.botania.api.mana.ManaDiscountArmor {

    private final String[] features;
    private final int solar;

    public LabTechArmor(Type type, String texture, int solar, String... features) {
        super(type, texture);
        this.features = features;
        this.solar = solar;
    }

    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {}

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (level.f_46443_) return;
        for (String feature : features) stack.m_41784_().m_128379_(feature, true);
        if (solar > 0) stack.m_41784_().m_128405_("LabSolar", Math.max(solar, stack.m_41784_().m_128451_("LabSolar")));
    }

    @Override
    public float getDiscount(ItemStack stack, int slot, Player player, ItemStack tool) {
        for (String feature : features)
            if (feature.equals("LabManaDiscount") && ItemPower.stored(stack) > 0) return 0.1F;
        return 0;
    }
}
