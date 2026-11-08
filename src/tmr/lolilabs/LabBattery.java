package tmr.lolilabs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.List;

public class LabBattery extends Item {

    private final int capacity;

    public LabBattery(int capacity) {
        super(new Properties().m_41487_(1).m_41486_());
        this.capacity = capacity;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new ItemPower(stack, capacity, true);
    }

    @Override
    public void m_6883_(ItemStack stack, Level level, Entity owner, int slot, boolean selected) {
        if (level.f_46443_ || !(owner instanceof Player player) || stack.m_41784_().m_128471_("LabChargingDisabled"))
            return;
        for (int i = 0; i < player.m_150109_().m_6643_(); i++) {
            ItemStack target = player.m_150109_().m_8020_(i);
            if (target == stack || target.m_41720_() instanceof LabBattery) continue;
            target.getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> {
                int available = Math.min(ItemPower.stored(stack), 100000);
                int accepted = handler.receiveEnergy(available, true);
                if (accepted > 0 && ItemPower.spend(stack, accepted)) handler.receiveEnergy(accepted, false);
            });
            if (ItemPower.stored(stack) <= 0) break;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (!level.f_46443_) {
            boolean disabled = !stack.m_41784_().m_128471_("LabChargingDisabled");
            stack.m_41784_().m_128379_("LabChargingDisabled", disabled);
            player.m_5661_(
                    Component.m_237115_(disabled ? "message.loli_labs.charging_off" : "message.loli_labs.charging_on"),
                    true);
        }
        return InteractionResultHolder.m_19090_(stack);
    }

    @Override
    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.m_237110_("tooltip.loli_labs.energy", ItemPower.stored(stack),
                new ItemPower(stack, capacity, true).getMaxEnergyStored()));
        tooltip.add(Component.m_237115_("tooltip.loli_labs.charging"));
    }
}
