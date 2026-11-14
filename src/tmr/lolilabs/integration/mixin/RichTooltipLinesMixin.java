package tmr.lolilabs.integration.mixin;

import net.minecraft.world.item.ItemStack;

import brachy.modularui.screen.RichTooltip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** ModularUI's spacer follows the title on the same text line unless the title ends with an explicit newline. */
@Mixin(value = RichTooltip.class, remap = false)
public abstract class RichTooltipLinesMixin {

    @Inject(method = "addFromItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void loliLabs$separateItemLines(ItemStack item, CallbackInfoReturnable<RichTooltip> result) {
        if (!brachy.modularui.ModularUI.isClientSide()) return;
        var tooltip = (RichTooltip) (Object) this;
        for (var line : brachy.modularui.api.MCHelper.getItemToolTip(item)) tooltip.add(line).newLine();
        tooltip.titleMargin(2);
        result.setReturnValue(tooltip);
    }
}
