package tmr.lolilabs;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class LabArmorBox extends Item {

    private final String style;

    public LabArmorBox(String style) {
        super(new Properties().m_41487_(1));
        this.style = style;
    }

    @Override
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        var box = player.m_21120_(hand);
        if (!level.f_46443_) {
            box.m_41774_(1);
            for (String part : new String[] { "head", "chest", "legs", "feet" }) {
                var armor = new ItemStack(LabItems.CATALOG.get("electric_" + part + "_" + style).get());
                armor.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY)
                        .ifPresent(power -> power.receiveEnergy(power.getMaxEnergyStored(), false));
                if (!player.m_150109_().m_36054_(armor)) player.m_36176_(armor, false);
            }
        }
        return InteractionResultHolder.m_19090_(box);
    }
}
