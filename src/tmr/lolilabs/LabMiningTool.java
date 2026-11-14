package tmr.lolilabs;

import com.ic2ceu.icceu.api.item.ChargeableItem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** EU multitool using IC2Modern's public battery/tool contract. */
public final class LabMiningTool extends Item implements ChargeableItem {

    private final boolean foil;

    public LabMiningTool(boolean foil) {
        super(new Properties().m_41487_(1).m_41486_());
        this.foil = foil;
    }

    public long maxCharge() {
        return 1_000_000_000L;
    }

    public int energyTier() {
        return 4;
    }

    public long transferLimit() {
        return 524288;
    }

    @Override
    public long charge(ItemStack stack) {
        var tag = stack.m_41784_();
        if (!tag.m_128425_("Charge", 99) && tag.m_128425_("mekData", 10)) {
            long migrated = 0;
            var old = tag.m_128469_("mekData").m_128437_("EnergyContainers", 10);
            for (int i = 0; i < old.size(); i++) try {
                migrated = Math.min(maxCharge(),
                        migrated + new java.math.BigDecimal(old.m_128728_(i).m_128461_("stored"))
                                .divideToIntegralValue(java.math.BigDecimal.valueOf(4))
                                .min(java.math.BigDecimal.valueOf(maxCharge())).longValueExact());
            } catch (NumberFormatException | ArithmeticException ignored) {}
            tag.m_128356_("Charge", migrated);
        }
        return ChargeableItem.super.charge(stack);
    }

    private int mode(ItemStack stack) {
        return Math.floorMod(stack.m_41784_().m_128451_("LabSpeed"), 3);
    }

    private long cost(ItemStack stack) {
        return new long[] { 100, 400, 1600 }[mode(stack)];
    }

    private boolean harvest(BlockState state) {
        return state.m_204336_(BlockTags.f_144280_) || state.m_204336_(BlockTags.f_144282_) ||
                state.m_204336_(BlockTags.f_144283_);
    }

    @Override
    public float m_8102_(ItemStack stack, BlockState state) {
        return charge(stack) >= cost(stack) && harvest(state) ? new float[] { 8, 32, 128 }[mode(stack)] : 1;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return charge(stack) >= cost(stack) && harvest(state) &&
                net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(Tiers.NETHERITE, state);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction action) {
        return charge(stack) >= cost(stack) &&
                (net.minecraftforge.common.ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(action) ||
                        net.minecraftforge.common.ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(action) ||
                        net.minecraftforge.common.ToolActions.DEFAULT_AXE_ACTIONS.contains(action));
    }

    @Override
    public boolean m_6813_(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity user) {
        if (!level.f_46443_ && state.m_60800_(level, pos) > 0) discharge(stack, cost(stack));
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        var stack = player.m_21120_(hand);
        if (player.m_6144_()) {
            if (!level.f_46443_) {
                stack.m_41784_().m_128405_("LabSpeed", (mode(stack) + 1) % 3);
                player.m_5661_(Component.m_237110_("message.loli_labs.vajra_mode", mode(stack) + 1), true);
            }
            return InteractionResultHolder.m_19090_(stack);
        }
        return InteractionResultHolder.m_19098_(stack);
    }

    /** Hide vanilla durability bar; EU overlay is drawn by ElectricChargeRenderer (shows at full charge). */
    @Override
    public boolean m_142522_(ItemStack stack) {
        return false;
    }

    @Override
    public int m_142158_(ItemStack stack) {
        return (int) Math.round(13D * charge(stack) / maxCharge());
    }

    @Override
    public int m_142159_(ItemStack stack) {
        return 0x38ddd2;
    }

    @Override
    public boolean m_5812_(ItemStack stack) {
        return foil || super.m_5812_(stack);
    }

    @Override
    public void m_7373_(ItemStack stack, Level level, java.util.List<Component> lines, TooltipFlag flag) {
        lines.add(Component.m_237113_(charge(stack) + " / " + maxCharge() + " EU"));
        lines.add(Component.m_237110_("message.loli_labs.vajra_mode", mode(stack) + 1));
    }
}
