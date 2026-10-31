package tmr.lolilabs;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class LabArmor extends ArmorItem {
    public final String texture;
    public LabArmor(Type type, String texture) {
        super(ArmorMaterials.NETHERITE, type, new Properties().m_41486_());
        this.texture = texture;
    }
    @Override public void initializeClient(Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        LabArmorRenderer.initialize(consumer, texture);
    }
    @Override public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) { return new ItemPower(stack, 20000000, false); }
    @Override public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String layer) {
        return "loli_labs:textures/legacy/armor/" + texture + ".png";
    }
    @Override public int getMaxDamage(ItemStack stack) { return 0; }
    @Override public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> broken) { return 0; }
    @Override public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.m_237110_("tooltip.loli_labs.energy", ItemPower.stored(stack), new ItemPower(stack, 20000000, false).getMaxEnergyStored()));
    }
    @Override public boolean m_142522_(ItemStack stack) { return ItemPower.stored(stack) > 0; }
    @Override public int m_142158_(ItemStack stack) { return Math.round(13F * ItemPower.stored(stack) / new ItemPower(stack, 20000000, false).getMaxEnergyStored()); }
    @Override public int m_142159_(ItemStack stack) { return 0x66FFF0; }
}
