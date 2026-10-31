package tmr.lolilabs;

import java.util.List;
import java.util.UUID;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class LabSword extends SwordItem {
    private static final UUID DAMAGE = UUID.fromString("7a32c4d4-9be2-48c6-a8bb-2f82a1fdc67f");
    private static final UUID SPEED = UUID.fromString("f369c1b2-97cf-49ca-a9f9-91a5bb67d0ef");
    public LabSword() { super(Tiers.NETHERITE, 10, -2.4F, new Properties().m_41486_()); }
    @Override public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag tag) { return new ItemPower(stack, 10000000, false); }
    @Override public boolean m_7579_(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!ItemPower.spend(stack, 200)) return true;
        CompoundTag tag = stack.m_41784_();
        if (tag.m_128471_("LabFire")) target.m_20254_(5);
        if (tag.m_128451_("LabVampirism") > 0) attacker.m_5634_(tag.m_128451_("LabVampirism"));
        var effects = new net.minecraft.world.effect.MobEffect[] {MobEffects.f_19614_, MobEffects.f_19604_, MobEffects.f_19610_, MobEffects.f_19613_, MobEffects.f_19615_};
        String[] keys = {"LabPoison", "LabConfusion", "LabBlindness", "LabWeakness", "LabWither"};
        for (int i = 0; i < keys.length; i++) if (tag.m_128471_(keys[i])) target.m_7292_(new MobEffectInstance(effects[i], 100));
        int knockback = tag.m_128451_("LabKnockback");
        if (knockback > 0) target.m_147240_(0.5D * knockback, attacker.m_20185_() - target.m_20185_(), attacker.m_20189_() - target.m_20189_());
        int radius = Math.min(3, tag.m_128451_("LabRadius"));
        if (radius > 0 && !attacker.m_9236_().f_46443_ && attacker instanceof net.minecraft.world.entity.player.Player player) {
            for (LivingEntity nearby : attacker.m_9236_().m_6443_(LivingEntity.class,
                    target.m_20191_().m_82400_(radius), entity -> entity != target && entity != attacker
                    && !(entity instanceof net.minecraft.world.entity.player.Player) && !attacker.m_7307_(entity))) {
                if (!ItemPower.spend(stack, 200)) break;
                nearby.m_6469_(attacker.m_269291_().m_269075_(player), 6F + tag.m_128451_("LabDamage"));
            }
        }
        return true;
    }
    @Override public boolean m_6813_(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) { ItemPower.spend(stack, 100); return true; }
    @Override public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return ImmutableMultimap.of();
        double damage = ItemPower.stored(stack) >= 200 ? 13D + stack.m_41784_().m_128451_("LabDamage") * 2D : 0D;
        return ImmutableMultimap.<Attribute, AttributeModifier>builder()
                .put(Attributes.f_22281_, new AttributeModifier(DAMAGE, "Loli Labs sword damage", damage, AttributeModifier.Operation.ADDITION))
                .put(Attributes.f_22283_, new AttributeModifier(SPEED, "Loli Labs sword speed", -2.4D, AttributeModifier.Operation.ADDITION)).build();
    }
    @Override public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.m_237110_("tooltip.loli_labs.energy", ItemPower.stored(stack), new ItemPower(stack, 10000000, false).getMaxEnergyStored()));
    }
    @Override public boolean m_142522_(ItemStack stack) { return ItemPower.stored(stack) > 0; }
    @Override public int m_142158_(ItemStack stack) { return Math.round(13F * ItemPower.stored(stack) / new ItemPower(stack, 10000000, false).getMaxEnergyStored()); }
    @Override public int m_142159_(ItemStack stack) { return 0x66FFF0; }
    @Override public int getMaxDamage(ItemStack stack) { return 0; }
}
