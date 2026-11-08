package tmr.lolilabs;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;

public final class LabPlainArmor extends ArmorItem {
    private final String texture;

    public LabPlainArmor(Type type, String texture) {
        super(ArmorMaterials.NETHERITE, type, new Properties().m_41486_());
        this.texture = texture;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String layer) {
        int index = slot == EquipmentSlot.LEGS ? 2 : 1;
        return "loli_labs:textures/models/armor/" + texture + "_layer_" + index + ".png";
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }
}
