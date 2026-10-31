package tmr.lolilabs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** Retains full entity NBT and UUID, and clears the seal only after a successful spawn. */
public final class LabSeal extends Item {
    public LabSeal() { super(new Properties().m_41487_(1)); }
    @Override public InteractionResult m_6880_(ItemStack stack,Player player,LivingEntity target,InteractionHand hand) {
        if (!(target instanceof Mob) || target instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                || target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                || target.m_20160_() || target.m_20159_() || stack.m_41784_().m_128425_("LabCapturedEntity",10)) return InteractionResult.FAIL;
        if (target instanceof net.minecraft.world.entity.TamableAnimal pet && !pet.m_21830_(player)) return InteractionResult.FAIL;
        if (player.m_9236_().f_46443_) return InteractionResult.SUCCESS;
        CompoundTag entity=new CompoundTag();
        if (!target.m_20223_(entity)) return InteractionResult.FAIL;
        stack.m_41784_().m_128365_("LabCapturedEntity",entity);
        target.m_146870_();
        return InteractionResult.CONSUME;
    }
    @Override public InteractionResult m_6225_(UseOnContext context) {
        var player=context.m_43723_(); var level=context.m_43725_(); var stack=context.m_43722_();
        if (player==null || !stack.m_41784_().m_128425_("LabCapturedEntity",10)) return InteractionResult.PASS;
        var pos=context.m_8083_().m_121945_(context.m_43719_());
        if (!level.m_7966_(player,pos) || !player.m_36204_(pos,context.m_43719_(),stack)) return InteractionResult.FAIL;
        if (level.f_46443_) return InteractionResult.SUCCESS;
        var entity=net.minecraft.world.entity.EntityType.m_20642_(stack.m_41784_().m_128469_("LabCapturedEntity").m_6426_(),level).orElse(null);
        if (entity==null) return InteractionResult.FAIL;
        entity.m_7678_(pos.m_123341_()+0.5D,pos.m_123342_(),pos.m_123343_()+0.5D,entity.m_146908_(),entity.m_146909_());
        if (!level.m_45784_(entity) || !level.m_7967_(entity)) return InteractionResult.FAIL;
        stack.m_41784_().m_128473_("LabCapturedEntity");
        return InteractionResult.CONSUME;
    }
}
