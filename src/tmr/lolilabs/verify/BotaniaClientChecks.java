package tmr.lolilabs.verify;

import tmr.lolilabs.LoliLabs;
import tmr.lolilabs.api.*;
import tmr.lolilabs.dimensions.*;
import tmr.lolilabs.dragonmight.*;
import tmr.lolilabs.energistics.*;
import tmr.lolilabs.energistics.machine.*;
import tmr.lolilabs.magically.*;
import tmr.lolilabs.utility.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Opt-in isolated client check: exercises real resource reload, Gecko baking and renderer lookup. */
@Mod.EventBusSubscriber(modid = LoliLabs.MOD_ID, value = Dist.CLIENT)
public final class BotaniaClientChecks {

    private static int ticks;
    private static boolean done;

    private static void require(boolean test, String message) {
        if (!test) throw new IllegalStateException(message);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (!Boolean.getBoolean("loli_labs.verifyClient") || event.phase != TickEvent.Phase.END) return;
        if (BotaniaWorldChecks.active()) {
            try {
                BotaniaWorldChecks.tick(Minecraft.m_91087_());
            } catch (Throwable error) {
                error.printStackTrace();
                System.exit(1);
            }
            return;
        }
        if (done) return;
        var mc = Minecraft.m_91087_();
        if (mc.m_91265_() != null || mc.f_91080_ == null || ++ticks < 30) return;
        done = true;
        var be = new GreenhouseBlockEntity(BlockPos.f_121853_, LabBotaniaBlocks.MULTI_GREENHOUSE_CORE.get().m_49966_());
        new GreenhouseRenderer().verifyAssets(be);
        for (var direction : net.minecraft.core.Direction.values())
            for (boolean formed : new boolean[] { false, true }) {
                var state = be.m_58900_().m_61124_(GreenhouseCoreBlock.FACING, direction)
                        .m_61124_(GreenhouseCoreBlock.FORMED, formed);
                require(mc.m_91304_().m_119430_().m_110893_(state) != mc.m_91304_().m_119409_(),
                        "Missing greenhouse blockstate " + state);
            }
        for (var tier : LabBotaniaTiers.Tier.values()) {
            var model = mc.m_91304_().m_119422_(
                    new ModelResourceLocation(new ResourceLocation(LoliLabs.MOD_ID, tier.spreaderId()), "inventory"));
            require(model != mc.m_91304_().m_119409_(), "Missing spreader model " + tier);
            var sprite = mc.m_91258_(net.minecraft.world.inventory.InventoryMenu.f_39692_)
                    .apply(new ResourceLocation(LoliLabs.MOD_ID, "models/spreaders/" + tier.spreaderId()));
            require(!sprite.m_245424_().m_246162_().m_135815_().contains("missing"), "Missing spreader sprite " + tier);
            var spark = new LabStraySparkEntity(LabBotaniaBlocks.SPARK_TYPES.get(tier).get(), null, tier);
            var renderer = mc.m_91290_().m_114382_(spark);
            require(renderer instanceof LabStraySparkRenderer, "Custom spark renderer not registered " + tier);
            var icon = ((LabStraySparkRenderer) renderer).getBaseIcon(spark);
            require(icon.m_245424_().m_246162_().toString().equals("loli_labs:item/magically/sparks/" + tier.sparkId()),
                    "Wrong world spark sprite " + tier);
            require(spark.m_142340_().m_41720_() == LabBotaniaBlocks.ITEM_CATALOG.get(tier.sparkId()).get(),
                    "Wrong spark pick/drop item");
            require(!spark.m_7755_().getString().startsWith("entity.loli_labs."),
                    "Spark entity name not localized " + tier);
        }
        System.out.println(
                "LOLI_LABS_BOTANIA_CLIENT_CHECKS_PASSED: original Gecko geometry/animation baked; four spreader models; four world spark renderers and atlas sprites");
        if (Boolean.getBoolean("loli_labs.verifyWorld")) {
            BotaniaWorldChecks.begin(mc);
            return;
        }

        System.exit(0); // Only this explicitly enabled, isolated verification JVM.
    }
}
