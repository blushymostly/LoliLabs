package tmr.lolilabs;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import java.util.List;

public final class FluidCellItem extends Item {

    private final int capacity;

    public FluidCellItem(int capacity) {
        super(new Item.Properties().m_41487_(1));
        this.capacity = capacity;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new FluidHandlerItemStack(stack, capacity);
    }

    @Override
    public InteractionResult m_6225_(UseOnContext context) {
        Player player = context.m_43723_();
        if (player == null) return InteractionResult.PASS;
        if (context.m_43725_().f_46443_) return InteractionResult.SUCCESS;
        return FluidUtil.interactWithFluidHandler(player, context.m_43724_(), context.m_43725_(),
                context.m_8083_(), context.m_43719_()) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        BlockHitResult hit = m_41435_(level, player,
                player.m_6144_() ? ClipContext.Fluid.NONE : ClipContext.Fluid.SOURCE_ONLY);
        if (hit.m_6662_() != HitResult.Type.BLOCK) return InteractionResultHolder.m_19098_(stack);
        BlockPos pos = hit.m_82425_();
        if (!level.m_7966_(player, pos) || !player.m_36204_(pos.m_121945_(hit.m_82434_()), hit.m_82434_(), stack)) {
            return InteractionResultHolder.m_19100_(stack);
        }
        if (level.f_46443_) return InteractionResultHolder.m_19090_(stack);
        FluidActionResult result;
        if (player.m_6144_()) {
            var handler = FluidUtil.getFluidHandler(stack).orElse(null);
            if (handler == null || handler.getFluidInTank(0).getAmount() < 1000)
                return InteractionResultHolder.m_19098_(stack);
            FluidStack fluid = handler.getFluidInTank(0).copy();
            fluid.setAmount(1000);
            result = FluidUtil.tryPlaceFluid(player, level, hand, pos.m_121945_(hit.m_82434_()), stack, fluid);
        } else result = FluidUtil.tryPickUpFluid(stack, player, level, pos, hit.m_82434_());
        if (!result.isSuccess()) return InteractionResultHolder.m_19098_(stack);
        player.m_21008_(hand, result.getResult());
        return InteractionResultHolder.m_19090_(result.getResult());
    }

    @Override
    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.m_237110_("tooltip.loli_labs.fluid_capacity", capacity));
        FluidUtil.getFluidHandler(stack).ifPresent(handler -> {
            FluidStack fluid = handler.getFluidInTank(0);
            tooltip.add(fluid.isEmpty() ? Component.m_237115_("tooltip.loli_labs.fluid_empty") :
                    Component.m_237110_("tooltip.loli_labs.fluid_stored", fluid.getDisplayName(), fluid.getAmount()));
        });
    }
}
