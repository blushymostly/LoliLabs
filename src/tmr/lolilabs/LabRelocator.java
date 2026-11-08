package tmr.lolilabs;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public final class LabRelocator extends Item {

    public LabRelocator() {
        super(new Properties().m_41487_(1).m_41486_());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new ItemPower(stack, 10000000, false);
    }

    @Override
    public InteractionResult m_6225_(UseOnContext context) {
        var player = context.m_43723_();
        if (player == null || !player.m_6144_()) return InteractionResult.PASS;
        if (!context.m_43725_().f_46443_) {
            var tag = context.m_43722_().m_41784_();
            int selected = tag.m_128451_("LabSelectedPoint");
            CompoundTag point = new CompoundTag();
            point.m_128359_("Dimension", player.m_9236_().m_46472_().m_135782_().toString());
            point.m_128347_("X", player.m_20185_());
            point.m_128347_("Y", player.m_20186_());
            point.m_128347_("Z", player.m_20189_());
            point.m_128350_("Yaw", player.m_146908_());
            point.m_128350_("Pitch", player.m_146909_());
            tag.m_128365_("LabPoint" + selected, point);
            player.m_5661_(Component.m_237110_("message.loli_labs.point_saved", selected + 1), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (level.f_46443_) return InteractionResultHolder.m_19090_(stack);
        var tag = stack.m_41784_();
        int selected = tag.m_128451_("LabSelectedPoint");
        if (player.m_6144_()) {
            selected = (selected + 1) % 9;
            tag.m_128405_("LabSelectedPoint", selected);
            player.m_5661_(Component.m_237110_("message.loli_labs.point_selected", selected + 1), true);
            return InteractionResultHolder.m_19090_(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !tag.m_128425_("LabPoint" + selected, 10))
            return InteractionResultHolder.m_19100_(stack);
        var point = tag.m_128469_("LabPoint" + selected);
        var target = serverPlayer.f_8924_.m_129880_(
                ResourceKey.m_135785_(Registries.f_256858_, new ResourceLocation(point.m_128461_("Dimension"))));
        if (target == null) return InteractionResultHolder.m_19100_(stack);
        double x = point.m_128459_("X"), y = point.m_128459_("Y"), z = point.m_128459_("Z");
        var destination = net.minecraft.core.BlockPos.m_274561_(x, y, z);
        // Saved destinations may have been built over since the point was recorded.
        if (!target.m_6857_().m_61937_(destination) ||
                !target.m_8055_(destination).m_60812_(target, destination).m_83281_() ||
                !target.m_8055_(destination.m_7494_()).m_60812_(target, destination.m_7494_()).m_83281_() ||
                !ItemPower.spend(stack, 100000))
            return InteractionResultHolder.m_19100_(stack);
        serverPlayer.m_8999_(target, x, y, z, point.m_128457_("Yaw"), point.m_128457_("Pitch"));
        return InteractionResultHolder.m_19090_(stack);
    }

    @Override
    public void m_7373_(ItemStack stack, Level level, java.util.List<Component> tooltip,
                        net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.m_237110_("tooltip.loli_labs.energy", ItemPower.stored(stack), 10000000));
        tooltip.add(Component.m_237115_("tooltip.loli_labs.relocator"));
        tooltip.add(Component.m_237110_("message.loli_labs.point_selected",
                stack.m_41784_().m_128451_("LabSelectedPoint") + 1));
    }
}
