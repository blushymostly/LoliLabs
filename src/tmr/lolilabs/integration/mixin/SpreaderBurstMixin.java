package tmr.lolilabs.integration.mixin;

import tmr.lolilabs.magically.LabManaSpreaderBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;

/** Adjust only our four tiers before Botania applies lenses and simulates the burst. */
@Mixin(value = ManaSpreaderBlockEntity.class, remap = false)
public abstract class SpreaderBurstMixin {

    @ModifyVariable(method = "getBurst", at = @At("STORE"), ordinal = 0, remap = false)
    private BurstProperties loliLabs$tier(BurstProperties properties) {
        if ((Object) this instanceof LabManaSpreaderBlockEntity lab) {
            properties.maxMana = lab.tier.spreaderBurst;
            properties.color = switch (lab.tier) {
                case ETHEREAL -> 0x4CE8FF;
                case GHOST -> 0x99D83E;
                case ARCANE -> 0xB34BFF;
                case DRAGON -> 0xFF7235;
            };
        }
        return properties;
    }
}
