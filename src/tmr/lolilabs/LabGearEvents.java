package tmr.lolilabs;

import java.util.ArrayList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LoliLabs.MOD_ID)
public final class LabGearEvents {
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.m_9236_().f_46443_) return;
        Player player = event.player;
        ItemStack flight = ItemStack.f_41583_;
        for (ItemStack stack : player.m_150109_().f_35975_) {
            if (!(stack.m_41720_() instanceof LabArmor)) continue;
            var tag = stack.m_41784_();
            int solar = tag.m_128451_("LabSolar");
            if (solar > 0 && player.m_9236_().m_46461_() && !player.m_9236_().m_46471_()
                    && player.m_9236_().m_45527_(player.m_20183_().m_7494_())) {
                new ItemPower(stack, 20000000, false).receiveEnergy(100 * solar, false);
            }
            if (ItemPower.stored(stack) < 20) continue;
            if (tag.m_128471_("LabFlight")) flight = stack;
            if (player.f_19797_ % 20 != 0) continue;
            effect(player, stack, "LabNightVision", MobEffects.f_19611_);
            effect(player, stack, "LabWaterBreathing", MobEffects.f_19608_);
            effect(player, stack, "LabSpeed", MobEffects.f_19596_);
            effect(player, stack, "LabJump", MobEffects.f_19603_);
            effect(player, stack, "LabRegen", MobEffects.f_19605_);
            if (tag.m_128471_("LabExtinguish") && player.m_6060_() && ItemPower.spend(stack, 100)) player.m_20095_();
            if (tag.m_128471_("LabFood") && player.m_36324_().m_38702_() < 20 && ItemPower.spend(stack, 200)) player.m_36324_().m_38707_(1, 0.2F);
            if (tag.m_128471_("LabClearEffects")) {
                for (var active : new ArrayList<>(player.m_21220_())) {
                    if (!active.m_19544_().m_19486_() && ItemPower.spend(stack, 100)) player.m_21195_(active.m_19544_());
                }
            }
        }
        var abilities = player.m_150110_();
        var persistent = player.getPersistentData();
        boolean granted = persistent.m_128471_("LabGrantedFlight");
        boolean powered = !flight.m_41619_() && (!abilities.f_35935_ || ItemPower.spend(flight, 20));
        if (!player.m_7500_() && !player.m_5833_()) {
            if (powered && !abilities.f_35936_) {
                abilities.f_35936_ = true;
                persistent.m_128379_("LabGrantedFlight", true);
                player.m_6885_();
            } else if (!powered && granted) {
                abilities.f_35936_ = false;
                abilities.f_35935_ = false;
                persistent.m_128473_("LabGrantedFlight");
                player.m_6885_();
            }
        } else if (granted) persistent.m_128473_("LabGrantedFlight");
        boolean fast = powered && !flight.m_41619_() && flight.m_41784_().m_128471_("LabFastFlight") && abilities.f_35935_;
        if (fast && !persistent.m_128471_("LabBoostedFlight")) {
            persistent.m_128350_("LabPreviousFlightSpeed", abilities.m_35942_());
            persistent.m_128379_("LabBoostedFlight", true);
            abilities.m_35943_(abilities.m_35942_() * 2F);
            player.m_6885_();
        } else if (!fast && persistent.m_128471_("LabBoostedFlight")) {
            abilities.m_35943_(persistent.m_128457_("LabPreviousFlightSpeed"));
            persistent.m_128473_("LabBoostedFlight");
            persistent.m_128473_("LabPreviousFlightSpeed");
            player.m_6885_();
        }
    }

    private static void effect(Player player, ItemStack stack, String key, MobEffect effect) {
        if (stack.m_41784_().m_128471_(key) && ItemPower.spend(stack, 20)) player.m_7292_(new MobEffectInstance(effect, 220, 0, true, false));
    }

    @SubscribeEvent public static void hurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getAmount() <= 0) return;
        float absorbed = 0;
        for (ItemStack stack : player.m_150109_().f_35975_) {
            if (!(stack.m_41720_() instanceof LabArmor)) continue;
            float amount = Math.min(event.getAmount() * 0.2F, ItemPower.stored(stack) / 1000F);
            if (amount > 0 && ItemPower.spend(stack, (int)Math.ceil(amount * 1000))) absorbed += amount;
        }
        event.setAmount(Math.max(0, event.getAmount() - absorbed));
    }

    @SubscribeEvent public static void fall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        for (ItemStack stack : player.m_150109_().f_35975_) {
            if (stack.m_41720_() instanceof LabArmor && stack.m_41784_().m_128471_("LabFallProtection") && ItemPower.spend(stack, 100)) {
                event.setDamageMultiplier(0); return;
            }
        }
    }
}
