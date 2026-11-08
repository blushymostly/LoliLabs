package tmr.lolilabs.botania;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tmr.lolilabs.LoliLabs;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.SparkAttachable;

@Mod.EventBusSubscriber(modid = LoliLabs.MOD_ID)
public final class LabBotaniaCapabilities {
    private static final ResourceLocation MANA = new ResourceLocation(LoliLabs.MOD_ID, "mana_receiver");
    private static final ResourceLocation SPARK = new ResourceLocation(LoliLabs.MOD_ID, "spark_attachable");

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity be = event.getObject();
        if (be instanceof LabManaPoolBlockEntity pool) {
            event.addCapability(MANA, provider(BotaniaForgeCapabilities.MANA_RECEIVER, pool));
            event.addCapability(SPARK, provider(BotaniaForgeCapabilities.SPARK_ATTACHABLE, pool));
            event.addCapability(new ResourceLocation(LoliLabs.MOD_ID,"wandable"),provider(BotaniaForgeCapabilities.WANDABLE,pool));
        } else if (be instanceof LabManaSpreaderBlockEntity spreader) {
            event.addCapability(MANA, provider(BotaniaForgeCapabilities.MANA_RECEIVER, spreader));
            event.addCapability(new ResourceLocation(LoliLabs.MOD_ID,"wandable"),provider(BotaniaForgeCapabilities.WANDABLE,spreader));
        } else if (be instanceof GreenhouseBlockEntity greenhouse) {
            event.addCapability(MANA, provider(BotaniaForgeCapabilities.MANA_RECEIVER, greenhouse));
            event.addCapability(SPARK, provider(BotaniaForgeCapabilities.SPARK_ATTACHABLE, greenhouse));
        }
    }

    public static <T> ICapabilityProvider provider(Capability<T> cap, T value) {
        LazyOptional<T> opt = LazyOptional.of(() -> value);
        return new ICapabilityProvider() {
            @Override public <C> LazyOptional<C> getCapability(Capability<C> capability, Direction side) {
                return cap.orEmpty(capability, opt);
            }
        };
    }

    private LabBotaniaCapabilities() {}
}
