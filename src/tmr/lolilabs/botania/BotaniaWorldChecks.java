package tmr.lolilabs.botania;

import tmr.lolilabs.*;

import com.ic2ceu.icceu.*;

import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

/** Opt-in integrated-world regression fixture. Never touches the player's worlds. */
public final class BotaniaWorldChecks {

    private static int phase, ticks;
    public static boolean tooltipPreview, itemTooltipPreview;
    private static volatile boolean cursorReady, clickChecked;
    private static boolean clickQueued;
    private static vazkii.botania.common.entity.ManaSparkEntity foregroundSpark;
    private static Runnable deferredOpen;
    private static int deferTicks;
    private static boolean deliveryRequested;
    private static volatile boolean delivered;
    private static final java.util.List<BlockPos> burstTargets = new java.util.ArrayList<>();
    private static volatile boolean built;
    private static volatile Throwable failure;
    private static volatile BlockPos origin;

    private static void require(boolean ok, String message) {
        if (!ok) throw new IllegalStateException(message);
    }

    public static boolean active() {
        return phase > 0;
    }

    public static void begin(Minecraft mc) {
        phase = 1;
        mc.m_231466_().m_233157_("greenhouse-verification-" + System.currentTimeMillis(),
                new net.minecraft.world.level.LevelSettings("Loli Labs greenhouse regression",
                        net.minecraft.world.level.GameType.CREATIVE, false, net.minecraft.world.Difficulty.PEACEFUL,
                        true, new net.minecraft.world.level.GameRules(),
                        net.minecraft.world.level.WorldDataConfiguration.f_244649_),
                new net.minecraft.world.level.levelgen.WorldOptions(0, false, false),
                access -> access.m_175515_(net.minecraft.core.registries.Registries.f_256729_)
                        .m_246971_(net.minecraft.world.level.levelgen.presets.WorldPresets.f_226438_).m_203334_()
                        .m_247748_());
    }

    private static ItemStack upgrade(GreenhouseUpgrade.Kind kind, int level) {
        String id = kind.itemId() + (kind.maxLevel > 1 ? "_lvl_" + level : "");
        return new ItemStack(LabBotaniaBlocks.ITEM_CATALOG.get(id).get());
    }

    private static void clickWidget(Minecraft mc, String name) throws Exception {
        var screen = ((brachy.modularui.api.IMuiScreen) mc.f_91080_).screen();
        var panel = screen.getPanelManager().getMainPanel();
        var widget = brachy.modularui.widget.WidgetTree.findFirstWithName(panel, name);
        var ctx = screen.getContext();
        int x = widget.getArea().x + widget.getArea().width / 2, y = widget.getArea().y + widget.getArea().height / 2;
        ctx.updateState(x, y, 0);
        var refresh = brachy.modularui.screen.ModularPanel.class.getDeclaredMethod("findHoveredWidgets");
        refresh.setAccessible(true);
        refresh.invoke(panel);
        ctx.onFrameUpdate();
        require(ctx.getTopHovered() == widget, "Power control not clickable");
        screen.mousePressed(0);
        screen.mouseReleased(0);
    }

    private static void setup(Minecraft mc) {
        try {
            var server = mc.m_91092_();
            var player = server.m_6846_().m_11314_().get(0);
            var world = (net.minecraft.server.level.ServerLevel) player.m_9236_();
            origin = player.m_20183_().m_7918_(0, 5, 4);
            world.m_8615_(6000);
            world.m_8606_(0, 10000, false, false);
            for (int x = -9; x <= 9; x++) for (int z = -9; z <= 9; z++) {
                world.m_46597_(origin.m_7918_(x, -2, z), Blocks.f_50069_.m_49966_());
                for (int y = -1; y <= 5; y++) world.m_46597_(origin.m_7918_(x, y, z), Blocks.f_50016_.m_49966_());
            }
            for (var entry : GreenhouseStructure.relativePattern().entrySet()) {
                var block = switch (entry.getValue()) {
                    case BASE -> LabBotaniaBlocks.MULTI_GREENHOUSE_BASE.get();
                    case MANABRICKS -> LabBotaniaBlocks.DARK_MANABRICKS.get();
                    case MANAGLASS -> LabBotaniaBlocks.MANAGLASS.get();
                    default -> throw new IllegalStateException();
                };
                var r = entry.getKey();
                world.m_46597_(origin.m_7918_(r.m_123341_(), r.m_123342_(), r.m_123343_()), block.m_49966_());
            }
            world.m_46597_(origin, LabBotaniaBlocks.MULTI_GREENHOUSE_CORE.get().m_49966_());
            tmr.lolilabs.dimensions.DimensionsChecks.world(world, origin);
            tmr.lolilabs.utility.UtilityWorldChecks.run(world, origin);
            var greenhouse = (GreenhouseBlockEntity) world.m_7702_(origin);
            require(greenhouse.refreshStructure() && greenhouse.isFormed(), "Fixture did not form");
            for (var kind : GreenhouseUpgrade.Kind.values()) {
                int slot = GreenhouseUpgrade.slotFor(kind);
                if (slot < 0) continue;
                var stack = upgrade(kind, kind.maxLevel);
                require(greenhouse.inventory.isItemValid(GreenhouseBlockEntity.UPGRADE_BASE + slot, stack),
                        "Correct upgrade rejected " + kind);
                for (int i = 0; i < 12; i++) if (i != slot)
                    require(!greenhouse.inventory.isItemValid(GreenhouseBlockEntity.UPGRADE_BASE + i, stack),
                            "Upgrade accepted in wrong slot " + kind + " -> " + i);
            }
            for (var kind : new GreenhouseUpgrade.Kind[] { GreenhouseUpgrade.Kind.EFFICIENCY,
                    GreenhouseUpgrade.Kind.MANA_BUFFER, GreenhouseUpgrade.Kind.EU_SPEED,
                    GreenhouseUpgrade.Kind.MULTIPLICATION }) {
                int slot = GreenhouseUpgrade.slotFor(kind);
                greenhouse.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + slot, upgrade(kind, 2));
            }
            for (int i = 0; i < 8; i++) greenhouse.inventory.setStackInSlot(i,
                    new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("botania:endoflame")), 4));
            for (int i = 8; i < 40; i++) greenhouse.inventory.setStackInSlot(i,
                    new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:coal")), 64));
            var partPos = origin.m_7918_(-1, -1, 0);
            var part = (GreenhouseBlockEntity) world.m_7702_(partPos);
            require(part.connectsEU(Direction.WEST) && part.acceptsEnergy(Direction.WEST), "Outer shell refuses EU");
            part.receiveMana(60000);
            require(greenhouse.mana == 60000 && part.getCurrentMana() == 60000, "Shell mana did not reach controller");
            var sourcePos = origin.m_7918_(-5, -1, 0);
            world.m_46597_(sourcePos, LabMachines.DEFINITIONS.get("dispersion_solar_panel").block.get().m_49966_());
            for (int x = -4; x <= -2; x++) {
                var pos = origin.m_7918_(x, -1, 0);
                world.m_46597_(pos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation("ic2modern:glass_fibre_cable"))
                        .m_49966_());
                var cable = (CableEntity) world.m_7702_(pos);
                cable.setOpen(Direction.WEST, true);
                cable.setOpen(Direction.EAST, true);
                CableBlock.refreshAround(world, pos);
            }
            var source = (LabMachineEntity) world.m_7702_(sourcePos);
            source.serverTick();
            require(greenhouse.energy > 0, "Solar -> native cable -> outer shell -> controller transfer failed");
            long quantaBefore = greenhouse.energy * 40 + greenhouse.euFraction;
            for (var r : GreenhouseStructure.relativePattern().keySet()) {
                var shell = (GreenhouseBlockEntity) world
                        .m_7702_(origin.m_7918_(r.m_123341_(), r.m_123342_(), r.m_123343_()));
                require(shell.connectsEU(Direction.DOWN), "Disconnected assembled shell part " + r);
                require(shell.receivePacket(Direction.DOWN, 1, 1) == 1, "Shell lost fractional EU " + r);
            }
            require(greenhouse.energy * 40 + greenhouse.euFraction - quantaBefore == 26,
                    "Fractional EU not conserved across shell parts");
            for (var tier : LabBotaniaTiers.Tier.values()) {
                var spreaderPos = origin.m_7918_(tier.ordinal() - 2, -1, -3);
                world.m_46597_(spreaderPos, LabBotaniaBlocks.BLOCK_CATALOG.get(tier.spreaderId()).get().m_49966_());
                var ground = spreaderPos.m_7918_(0, -1, 0);
                require(net.minecraft.world.level.block.Block.m_152444_(world.m_8055_(ground), world, ground,
                        Direction.UP, spreaderPos), "Spreader hides the ground surface below its inset model: " + tier);
            }
            System.out.println(
                    "LOLI_LABS_SPREADER_GROUND_CULLING_OK: all four spreaders preserve the supporting ground top face");
            verifyFlowers(world, greenhouse);
            greenhouse.syncSettings();
            player.m_8999_(world, origin.m_123341_() + 5.5, origin.m_123342_() - 1, origin.m_123343_() - 6.5, 35, 0);
            System.out.println(
                    "LOLI_LABS_GREENHOUSE_WORLD_EU_OK: real solar/cable/shell/controller; 26 input parts; fractional EU; 11 typed upgrade kinds; shell mana forwarding");
            built = true;
        } catch (Throwable error) {
            failure = error;
        }
    }

    private static ItemStack item(String id, int count) {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)), count);
    }

    private static void verifyFlowers(net.minecraft.server.level.ServerLevel world, GreenhouseBlockEntity greenhouse) {
        for (String type : new String[] { "endoflame", "gourmaryllis", "munchdew", "entropinnyum", "rafflowsia" }) {
            var test = new GreenhouseBlockEntity(origin, greenhouse.m_58900_());
            test.inventory.setStackInSlot(0, item("botania:" + type, 1));
            String resource = switch (type) {
                case "gourmaryllis" -> "minecraft:apple";
                case "munchdew" -> "minecraft:oak_leaves";
                case "entropinnyum" -> "minecraft:tnt";
                case "rafflowsia" -> "botania:endoflame";
                default -> "minecraft:coal";
            };
            test.inventory.setStackInSlot(8, item(resource, 64));
            for (int tick = 0; tick < 200; tick++) test.flowers.tick(test);
            require(test.mana > 0, "No mana from " + type + " / " + resource);
            require(test.inventory.getStackInSlot(8).m_41613_() < 64, "Resource not consumed by " + type);
        }
        for (var kind : GreenhouseUpgrade.Kind.values()) for (int level = 1; level <= kind.maxLevel; level++) {
            var stack = upgrade(kind, level);
            var lines = new java.util.ArrayList<net.minecraft.network.chat.Component>();
            stack.m_41720_().m_7373_(stack, world, lines,
                    new net.minecraft.world.item.TooltipFlag.Default(false, false));
            require(!lines.isEmpty() && !lines.get(0).getString().contains("%s"),
                    "Missing item tooltip description " + kind + level);
        }
        for (var pixel : GreenhouseGauge.pixels()) if (pixel[1] < 37) {
            int side = pixel[0] < 122 ? 0 : 2;
            int both = 3 << side;
            double[] full = { 500, 500, 500, 500 };
            require(GreenhouseGauge.lit(pixel, both, full), "Top channel not filled with both upgrades");
            require(!GreenhouseGauge.lit(pixel, 1 << side, full) && !GreenhouseGauge.lit(pixel, 2 << side, full),
                    "One upgrade filled the top channel");
            full[side + 1] = 0;
            require(!GreenhouseGauge.lit(pixel, both, full), "Top channel skipped the second activation wave");
        }
        var balance = new GreenhouseBlockEntity(origin, greenhouse.m_58900_());
        for (int level = 1; level <= 2; level++) {
            balance.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + 9,
                    upgrade(GreenhouseUpgrade.Kind.MANA_BUFFER, level));
            require(balance.manaCap == LabBotaniaTiers.BASE_MANA_CAPACITY + (level == 1 ? 5000000 : 100000000),
                    "Wrong additive mana buffer " + level);
        }
        balance.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + 8,
                upgrade(GreenhouseUpgrade.Kind.EFFICIENCY, 1));
        require(balance.workBonusAndPay() == 10, "Efficiency must accelerate by ten percent");
        balance.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + 8, ItemStack.f_41583_);
        for (int level = 1; level <= 3; level++) {
            balance.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + 11,
                    upgrade(GreenhouseUpgrade.Kind.EU_SPEED, level));
            long cost = new long[] { 1000, 3000, 4500 }[level - 1];
            int expected = new int[] { 10, 15, 25 }[level - 1];
            balance.energy = cost * 20;
            int advanced = 0;
            for (int tick = 0; tick < 20; tick++) advanced += balance.workBonusAndPay();
            require(balance.energy == 0 && advanced == expected * 20,
                    "EU speed bonus or per-tick payment wrong " + level);
            balance.energy = cost - 1;
            require(balance.workBonusAndPay() == 0 && balance.energy == cost - 1,
                    "Unfunded speed upgrade spent energy or accelerated");
        }
        var pattern = GreenhouseStructure.relativePattern();
        require(pattern.size() == 26, "Structure must have 26 shell blocks");
        for (var entry : pattern.entrySet()) require(entry.getValue() ==
                (entry.getKey().m_123342_() == -1 ? GreenhouseStructure.Role.BASE : entry.getKey().m_123342_() == 0 ?
                        GreenhouseStructure.Role.MANAGLASS : GreenhouseStructure.Role.MANABRICKS),
                "Incorrect greenhouse layer");
        System.out.println(
                "LOLI_LABS_REQUESTED_UPGRADE_BALANCE_OK: mana +5M/+100M; EU 1000/3000/4500 per tick for 10/15/25 percent; efficiency 10 percent; exact three-layer structure");
        var single = new GreenhouseBlockEntity(origin, greenhouse.m_58900_());
        require(single.inventory.insertItem(0, item("minecraft:coal", 1), true).m_41613_() == 1,
                "Non-flower accepted in flower slot");
        require(single.inventory.insertItem(0, item("botania:endoflame", 64), true).m_41613_() == 63,
                "Flower limit without multiplication is not one");
        single.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + 10,
                upgrade(GreenhouseUpgrade.Kind.MULTIPLICATION, 1));
        require(single.inventory.insertItem(0, item("botania:endoflame", 64), true).m_41613_() == 60,
                "Multiplication I must accept four flowers");
        single.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + 10,
                upgrade(GreenhouseUpgrade.Kind.MULTIPLICATION, 2));
        require(single.inventory.insertItem(0, item("botania:endoflame", 64), true).m_41613_() == 56,
                "Multiplication II must accept eight flowers");
        single.inventory.setStackInSlot(0, item("botania:endoflame", 2));
        single.inventory.setStackInSlot(8, item("minecraft:coal", 64));
        single.flowers.tick(single);
        require(single.inventory.getStackInSlot(8).m_41613_() == 62, "Two flowers did not consume independently");
        for (int i = 0; i < 100; i++) single.flowers.tick(single);
        var saved = single.m_187480_();
        var restored = new GreenhouseBlockEntity(origin, single.m_58900_());
        restored.m_142466_(saved);
        for (int i = 0; i < 100; i++) {
            single.flowers.tick(single);
            restored.flowers.tick(restored);
        }
        require(single.mana == restored.mana &&
                single.inventory.getStackInSlot(8).m_41613_() == restored.inventory.getStackInSlot(8).m_41613_(),
                "Flower timers/consumption lost after NBT reload");
        single.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + 8,
                upgrade(GreenhouseUpgrade.Kind.PROCESSING, 1));
        single.inventory.setStackInSlot(1, item("botania:entropinnyum", 1));
        single.inventory.setStackInSlot(9, item("minecraft:tnt", 64));
        single.flowers.tick(single);
        require(single.inventory.getStackInSlot(9).m_41619_(), "Processing did not consume a resource stack");
        var roof = (GreenhouseBlockEntity) world.m_7702_(origin.m_7918_(0, 1, 0));
        require(roof.canAttachSpark(item("botania:spark", 1)), "Formed roof refuses spark");
        var spark = LabBotaniaBlocks.SPARK_TYPES.get(LabBotaniaTiers.Tier.ETHEREAL).get().m_20615_(world);
        spark.m_6034_(origin.m_123341_() + .5, origin.m_123342_() + 2.25, origin.m_123343_() + .5);
        world.m_7967_(spark);
        require(spark.getAttachedManaReceiver() instanceof vazkii.botania.api.mana.ManaPool,
                "Spark cannot find greenhouse mana pool");
        require(spark.getAttachedTile() != null && roof.getAttachedSpark() == spark,
                "Spark not attached to greenhouse roof");
        var poolPos = origin.m_7918_(4, 1, 0);
        world.m_46597_(poolPos, LabBotaniaBlocks.BLOCK_CATALOG.get("ethereal_pool").get().m_49966_());
        var target = (LabManaPoolBlockEntity) world.m_7702_(poolPos);
        var targetSpark = LabBotaniaBlocks.SPARK_TYPES.get(LabBotaniaTiers.Tier.ETHEREAL).get().m_20615_(world);
        targetSpark.m_6034_(poolPos.m_123341_() + .5, poolPos.m_123342_() + 1.25, poolPos.m_123343_() + .5);
        world.m_7967_(targetSpark);
        spark.setUpgrade(vazkii.botania.api.mana.spark.SparkUpgradeType.RECESSIVE);
        spark.registerTransfer(targetSpark);
        int before = greenhouse.mana;
        spark.m_8119_();
        require(target.getCurrentMana() > 0 && before - greenhouse.mana == target.getCurrentMana(),
                "Native spark transfer did not conserve greenhouse mana");
        spark.setUpgrade(vazkii.botania.api.mana.spark.SparkUpgradeType.NONE);
        verifyNativeSpreaders(world, greenhouse);
        // Full storage never consumes new fuel. Non-matching items are left intact.
        var full = new GreenhouseBlockEntity(origin, greenhouse.m_58900_());
        full.inventory.setStackInSlot(0, item("botania:endoflame", 1));
        full.inventory.setStackInSlot(8, item("minecraft:coal", 64));
        full.mana = full.manaCap;
        for (int i = 0; i < 10; i++) full.flowers.tick(full);
        require(full.inventory.getStackInSlot(8).m_41613_() == 64, "Full buffer consumes fuel");
        full.mana = 0;
        full.inventory.setStackInSlot(8, item("minecraft:cobblestone", 64));
        for (int i = 0; i < 10; i++) full.flowers.tick(full);
        require(full.inventory.getStackInSlot(8).m_41613_() == 64 && full.mana == 0, "Invalid resource consumed");
        long savedEnergy = greenhouse.energy;
        int savedProgress = greenhouse.progress;
        greenhouse.energy = 0;
        greenhouse.progress = 0;
        GreenhouseBlockEntity.tick(world, origin, greenhouse.m_58900_(), greenhouse);
        require(greenhouse.progress == 10, "Efficiency stops working when EU-speed upgrade has no power");
        greenhouse.energy = savedEnergy;
        greenhouse.progress = savedProgress;
        var oldPos = origin.m_7918_(8, 0, -7);
        world.m_46597_(oldPos, LabBotaniaBlocks.MULTI_GREENHOUSE_CORE.get().m_49966_());
        var old = (GreenhouseBlockEntity) world.m_7702_(oldPos);
        var oldInventory = new net.minecraftforge.items.ItemStackHandler(GreenhouseBlockEntity.SLOTS);
        oldInventory.setStackInSlot(0, item("minecraft:wheat_seeds", 4));
        oldInventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE, upgrade(GreenhouseUpgrade.Kind.EU_BUFFER, 1));
        var legacy = new net.minecraft.nbt.CompoundTag();
        legacy.m_128365_("Inv", oldInventory.serializeNBT());
        old.m_142466_(legacy);
        GreenhouseBlockEntity.tick(world, oldPos, old.m_58900_(), old);
        require(old.inventory.getStackInSlot(0).m_41619_() &&
                old.inventory.getStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE).m_41619_(),
                "Legacy hidden slots not emptied");
        int returned = 0;
        for (int i = 8; i < 40; i++) returned += old.inventory.getStackInSlot(i).m_41613_();
        require(returned == 5, "Legacy migration lost items instead of returning them to accessible inventory");
        world.m_46597_(oldPos, Blocks.f_50016_.m_49966_());
        world.m_46597_(origin.m_7918_(3, -1, -2), LabBotaniaBlocks.MULTI_GREENHOUSE_BASE.get().m_49966_());
        world.m_46597_(origin.m_7918_(5, -1, -2), LabMachines.DEFINITIONS.get("energy_unifier").block.get().m_49966_());
        world.m_46597_(origin.m_7918_(6, -1, -2),
                LabMachines.DEFINITIONS.get("matter_solid_t0_x1").block.get().m_49966_());
        for (int i = 0; i < GreenhouseBlockEntity.SLOTS; i++)
            greenhouse.inventory.setStackInSlot(i, ItemStack.f_41583_);
        greenhouse.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + 9,
                upgrade(GreenhouseUpgrade.Kind.MANA_BUFFER, 2));
        for (int i = 0; i < 8; i++) greenhouse.inventory.setStackInSlot(i, item("botania:endoflame", 1));
        greenhouse.inventory.setStackInSlot(8, item("minecraft:coal", 64));
        greenhouse.flowers.load(new net.minecraft.nbt.ListTag());
        greenhouse.mana = 0;
        for (int i = 0; i < 120; i++) GreenhouseBlockEntity.tick(world, origin, greenhouse.m_58900_(), greenhouse);
        require(greenhouse.mana >= 1300 && greenhouse.inventory.getStackInSlot(8).m_41613_() == 56,
                "User reproduction: eight flowers consume coal but do not accumulate mana with Mana Storage only");
        for (var tier : LabBotaniaTiers.Tier.values()) {
            var remote = (LabManaSpreaderBlockEntity) world.m_7702_(origin.m_7918_(tier.ordinal() - 2, -1, -3));
            require(remote.getCurrentMana() == 0, "Remote spreader still drains greenhouse mana");
        }
        greenhouse.enabled = false;
        greenhouse.syncSettings();
        require(world.m_8055_(origin.m_7918_(0, 0, -1)).getLightEmission(world, origin.m_7918_(0, 0, -1)) == 0,
                "Disabled greenhouse still emits light");
        greenhouse.enabled = true;
        greenhouse.syncSettings();
        for (var relative : GreenhouseStructure.relativePattern().keySet()) {
            var litPos = origin.m_7918_(relative.m_123341_(), relative.m_123342_(), relative.m_123343_());
            require(world.m_8055_(litPos).getLightEmission(world, litPos) == 15,
                    "Enabled assembled part does not emit light " + relative);
        }
        var probePos = origin.m_7918_(7, 0, 7);
        world.m_46597_(probePos, LabBotaniaBlocks.BLOCK_CATALOG.get("ethereal_pool").get().m_49966_());
        var probe = (LabManaPoolBlockEntity) world.m_7702_(probePos);
        probe.receiveMana(10000);
        var iron = new net.minecraft.world.entity.item.ItemEntity(world, probePos.m_123341_() + .5,
                probePos.m_123342_() + .25, probePos.m_123343_() + .5, item("minecraft:iron_ingot", 1));
        require(probe.collideEntityItem(iron) && probe.getCurrentMana() < 10000,
                "Custom pool does not perform native mana infusion");
        var poolTag = probe.m_187480_();
        require(poolTag.m_128461_("id").equals("loli_labs:mana_pool"), "Pool save ID changed");
        var reloadedPool = new LabManaPoolBlockEntity(probePos, world.m_8055_(probePos));
        reloadedPool.m_142466_(poolTag);
        require(reloadedPool.getCurrentMana() == probe.getCurrentMana(), "Pool mana lost on reload");
        var hopperPos = origin.m_7918_(-1, 2, -1);
        world.m_46597_(hopperPos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft:hopper")).m_49966_());
        ((net.minecraft.world.level.block.entity.HopperBlockEntity) world.m_7702_(hopperPos)).m_6836_(0,
                item("minecraft:coal", 1));
        var automation = world.m_7702_(origin.m_7918_(-1, 1, -1))
                .getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, Direction.UP)
                .orElseThrow(() -> new IllegalStateException("Outer roof has no item capability"));
        require(automation.getSlots() == 40, "Automation exposes private legacy or upgrade slots");
        var clockCopy = new GreenhouseBlockEntity(origin, greenhouse.m_58900_());
        clockCopy.m_142466_(greenhouse.m_187480_());
        require(java.util.Arrays.equals(clockCopy.upgradeInstalledAt, greenhouse.upgradeInstalledAt),
                "Upgrade installation timestamps did not survive save/load");
        System.out.println(
                "LOLI_LABS_POOL_INFUSION_NBT_OK: native infusion consumes mana; custom save ID and stored mana survive reload");
        System.out.println(
                "LOLI_LABS_GREENHOUSE_BUFFER_REPRO_OK: started at zero; eight single flowers; only mana buffer upgrade; 8 coal consumed; " +
                        greenhouse.mana + " mana retained; distant spreaders did not drain buffer");
        System.out.println(
                "LOLI_LABS_GREENHOUSE_FLOWERS_OK: five item-fed generators; four typed upgrades; 1/4/8 flower limits; independent consumption; processing; persisted timers; native spark transfer; collector output; buffer backpressure");
    }

    private static void verifyNativeSpreaders(net.minecraft.server.level.ServerLevel world,
                                              GreenhouseBlockEntity greenhouse) {
        var player = world.m_7654_().m_6846_().m_11314_().get(0);
        for (var tier : LabBotaniaTiers.Tier.values()) {
            var pos = origin.m_7918_(-6 + tier.ordinal() * 4, 0, 6);
            var targetPos = pos.m_7918_(0, 0, 6);
            var sourcePos = pos.m_7918_(0, 0, -1);
            world.m_46597_(pos, LabBotaniaBlocks.BLOCK_CATALOG.get(tier.spreaderId()).get().m_49966_());
            world.m_46597_(targetPos, LabBotaniaBlocks.BLOCK_CATALOG.get(tier.poolId()).get().m_49966_());
            world.m_46597_(sourcePos, LabBotaniaBlocks.BLOCK_CATALOG.get(tier.poolId()).get().m_49966_());
            var spreader = (LabManaSpreaderBlockEntity) world.m_7702_(pos);
            var source = (LabManaPoolBlockEntity) world.m_7702_(sourcePos);
            source.receiveMana(tier.spreaderBurst * 4);
            var wand = item("botania:twig_wand", 1);
            player.m_21008_(net.minecraft.world.InteractionHand.MAIN_HAND, wand);
            player.m_20260_(true);
            vazkii.botania.common.item.WandOfTheForestItem.setBindMode(wand, true);
            wand.m_41720_()
                    .m_6225_(new net.minecraft.world.item.context.UseOnContext(player,
                            net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.phys.BlockHitResult(
                                    net.minecraft.world.phys.Vec3.m_82512_(pos), Direction.UP, pos, false)));
            require(vazkii.botania.common.item.WandOfTheForestItem.getBindingAttempt(wand).filter(pos::equals)
                    .isPresent(), "Shift-click wand rotates instead of selecting spreader " + tier);
            wand.m_41720_()
                    .m_6225_(new net.minecraft.world.item.context.UseOnContext(player,
                            net.minecraft.world.InteractionHand.MAIN_HAND,
                            new net.minecraft.world.phys.BlockHitResult(
                                    net.minecraft.world.phys.Vec3.m_82512_(targetPos), Direction.UP, targetPos,
                                    false)));
            require(vazkii.botania.common.item.WandOfTheForestItem.getBindingAttempt(wand).filter(pos::equals)
                    .isEmpty(), "Target click did not complete wand binding " + tier);
            player.m_20260_(false);
            player.m_21008_(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.f_41583_);
            require(spreader.runBurstSimulation().getStartingMana() == tier.spreaderBurst,
                    "Tier burst properties were not injected " + tier);
            spreader.getItemHandler().m_6836_(0, item("botania:lens_power", 1));
            require(spreader.runBurstSimulation().getStartingMana() == tier.spreaderBurst * 2,
                    "Native lens did not modify tier burst " + tier);
            spreader.getItemHandler().m_6836_(0, ItemStack.f_41583_);
            require(vazkii.botania.xplat.XplatAbstractions.INSTANCE.findWandable(world, pos, world.m_8055_(pos),
                    spreader) == spreader, "Wand capability missing " + tier);
            var saved = spreader.m_187480_();
            require(saved.m_128461_("id").equals("loli_labs:mana_spreader"),
                    "Spreader saves under native Botania type instead of Loli Labs");
            var loaded = new LabManaSpreaderBlockEntity(pos, world.m_8055_(pos));
            loaded.m_142466_(saved);
            require(loaded.rotationX == spreader.rotationX && loaded.rotationY == spreader.rotationY,
                    "Aimed direction not persisted " + tier);
            LabManaSpreaderBlockEntity.tick(world, pos, world.m_8055_(pos), spreader);
            LabManaSpreaderBlockEntity.tick(world, pos, world.m_8055_(pos), spreader);
            require(source.getCurrentMana() < tier.spreaderBurst * 4, "Adjacent source pool not drained " + tier);
            burstTargets.add(targetPos);
        }
        System.out.println(
                "LOLI_LABS_NATIVE_SPREADER_SETUP_OK: native bindings; correct custom payloads; power lenses; adjacent-pool extraction; Loli Labs save identity and direction persistence");
    }

    private static void openMachine(Minecraft mc, String id, BlockPos pos, boolean powered) {
        if (mc.f_91080_ != null) mc.f_91074_.m_6915_();
        deferTicks = 10;
        deferredOpen = () -> doOpenMachine(mc, id, pos, powered);
    }

    private static void doOpenMachine(Minecraft mc, String id, BlockPos pos, boolean powered) {
        mc.m_91092_().m_201446_(() -> {
            try {
                var player = mc.m_91092_().m_6846_().m_11314_().get(0);
                var world = (net.minecraft.server.level.ServerLevel) player.m_9236_();
                if (!(world.m_7702_(pos) instanceof LabMachineEntity))
                    world.m_46597_(pos, LabMachines.DEFINITIONS.get(id).block.get().m_49966_());
                var machine = (LabMachineEntity) world.m_7702_(pos);
                machine.enabled = false;
                machine.energy = powered ? machine.capacity() / 2 : 0;
                machine.syncSettings();
                player.m_8999_(world, pos.m_123341_() + .5, pos.m_123342_(), pos.m_123343_() - 3, 0, 0);
                brachy.modularui.factory.BlockEntityUIFactory.INSTANCE.open(player, pos);
            } catch (Throwable error) {
                failure = error;
            }
        });
    }

    private static void preparePoolScene(Minecraft mc) {
        mc.f_91074_.m_6915_();
        mc.m_91092_().m_201446_(() -> {
            try {
                var player = mc.m_91092_().m_6846_().m_11314_().get(0);
                var world = (net.minecraft.server.level.ServerLevel) player.m_9236_();
                for (int x = -7; x <= 7; x++)
                    for (int z = -8; z <= -2; z++) world.m_46597_(origin.m_7918_(x, -2, z), Blocks.f_50069_.m_49966_());
                for (var tier : LabBotaniaTiers.Tier.values()) {
                    var pos = origin.m_7918_(-5 + tier.ordinal() * 3, -1, -5);
                    world.m_46597_(pos, LabBotaniaBlocks.BLOCK_CATALOG.get(tier.poolId()).get().m_49966_());
                    var pool = (LabManaPoolBlockEntity) world.m_7702_(pos);
                    pool.receiveMana(pool.getMaxMana() / 2);
                }
                player.m_150110_().f_35935_ = true;
                player.m_6885_();
                player.m_8999_(world, origin.m_123341_() + .5, origin.m_123342_() + 1, origin.m_123343_() - 12, 0, 25);
            } catch (Throwable error) {
                failure = error;
            }
        });
    }

    private static void prepareSparkScene(Minecraft mc, int type) {
        mc.m_91092_().m_201446_(() -> {
            try {
                var player = mc.m_91092_().m_6846_().m_11314_().get(0);
                var world = (net.minecraft.server.level.ServerLevel) player.m_9236_();
                var pos = origin.m_7918_(0, -2, -2);
                if (type == 0) {
                    for (int x = -2; x <= 1; x++) world.m_46597_(origin.m_7918_(x, -1, -3), Blocks.f_50016_.m_49966_());
                    world.m_46597_(pos, LabBotaniaBlocks.BLOCK_CATALOG.get("ethereal_pool").get().m_49966_());
                    ((LabManaPoolBlockEntity) world.m_7702_(pos)).receiveMana(10000);
                    player.m_8999_(world, origin.m_123341_() + .5, origin.m_123342_() - 1, origin.m_123343_() - 5.5, 0,
                            15);
                } else {
                    if (foregroundSpark != null) foregroundSpark.m_146870_();
                    foregroundSpark = type == 1 ? new vazkii.botania.common.entity.ManaSparkEntity(world) :
                            LabBotaniaBlocks.SPARK_TYPES.get(LabBotaniaTiers.Tier.ETHEREAL).get().m_20615_(world);
                    foregroundSpark.m_6034_(pos.m_123341_() + .5, pos.m_123342_() + 1.25, pos.m_123343_() + .5);
                    world.m_7967_(foregroundSpark);
                }
            } catch (Throwable error) {
                failure = error;
            }
        });
    }

    private static void shot(Minecraft mc, String name) throws java.io.IOException {
        var root = java.nio.file.Path.of(System.getProperty("loli_labs.worldShots"));
        java.nio.file.Files.createDirectories(root);
        try (var image = net.minecraft.client.Screenshot.m_92279_(mc.m_91385_())) {
            image.m_85066_(root.resolve(name + ".png"));
        }
    }

    private static boolean hasWidget(brachy.modularui.api.widget.IWidget panel, String name) {
        try {
            return brachy.modularui.widget.WidgetTree.findFirstWithName(panel, name) != null;
        } catch (java.util.NoSuchElementException expected) {
            return false;
        }
    }

    private static double progress(brachy.modularui.api.widget.IWidget widget) throws ReflectiveOperationException {
        var field = brachy.modularui.widgets.ProgressWidget.class.getDeclaredField("value");
        field.setAccessible(true);
        return ((brachy.modularui.api.value.IDoubleValue<?>) field.get(widget)).getDoubleValue();
    }

    public static void tick(Minecraft mc) throws Exception {
        if (failure != null) throw new IllegalStateException("World fixture failed", failure);
        if (mc.f_91073_ == null || mc.f_91074_ == null || mc.m_91092_() == null) return;
        if (deferredOpen != null && --deferTicks <= 0) {
            var task = deferredOpen;
            deferredOpen = null;
            task.run();
        }
        if (phase == 1) {
            phase = 2;
            ticks = 0;
            mc.m_91092_().m_201446_(() -> setup(mc));
            return;
        }
        if (phase == 50) {
            tmr.lolilabs.utility.UtilityWorldChecks.client(mc);
            return;
        }
        if (!built || ++ticks < (phase == 7 ? 12 : phase == 17 ? 14 : phase == 20 ? 3 : phase == 21 ? 20 : 80)) return;
        if (phase == 2) {
            var greenhouse = (GreenhouseBlockEntity) mc.f_91073_.m_7702_(origin);
            require(greenhouse != null && greenhouse.isFormed(), "Client did not receive formed state");
            int light = GreenhouseRenderer.exteriorLight(greenhouse, 0);
            require((light >>> 16) > 0, "Model still samples dark interior lighting");
            if (!deliveryRequested) {
                deliveryRequested = true;
                mc.m_91092_().m_201446_(() -> {
                    try {
                        var world = mc.m_91092_().m_6846_().m_11314_().get(0).m_9236_();
                        for (var target : burstTargets) {
                            var pool = world.m_7702_(target);
                            require(pool instanceof LabManaPoolBlockEntity,
                                    "Target pool disappeared: " + target + " state=" + world.m_8055_(target));
                            require(((LabManaPoolBlockEntity) pool).getCurrentMana() > 0,
                                    "Real aimed burst did not reach target " + target);
                        }
                        var hopperPos = origin.m_7918_(-1, 2, -1);
                        require(((net.minecraft.world.level.block.entity.HopperBlockEntity) world.m_7702_(hopperPos))
                                .m_8020_(0).m_41619_(), "Real hopper did not insert coal through assembled roof");
                        require(((GreenhouseBlockEntity) world.m_7702_(origin)).inventory.getStackInSlot(8)
                                .m_41613_() == 57, "Hopper coal did not reach controller resource inventory");
                        world.m_46597_(hopperPos, Blocks.f_50016_.m_49966_());
                        System.out.println(
                                "LOLI_LABS_GREENHOUSE_HOPPER_OK: actual vanilla hopper inserted coal through an outer roof part into controller resources");
                        require(world.m_45517_(net.minecraft.world.level.LightLayer.BLOCK, origin.m_7918_(0, 0, -2)) >
                                0, "Greenhouse state emits light but does not illuminate surrounding air");
                        System.out.println(
                                "LOLI_LABS_GREENHOUSE_WORLD_LIGHT_OK: enabled parts emit level 15; disabled parts emit zero; surrounding air receives block light");
                        System.out.println(
                                "LOLI_LABS_NATIVE_SPREADER_DELIVERY_OK: all four tiers delivered actual aimed mana bursts to bound targets");
                        delivered = true;
                    } catch (Throwable error) {
                        failure = error;
                    }
                });
                return;
            }
            if (!delivered) return;
            for (var target : burstTargets) {
                var pool = mc.f_91073_.m_7702_(target);
                require(pool instanceof LabManaPoolBlockEntity && ((LabManaPoolBlockEntity) pool).getCurrentMana() > 0,
                        "Pool mana not synchronized to client " + target);
            }
            LabSparkRenderType.SPARK.m_110185_();
            try {
                require(!org.lwjgl.opengl.GL11.glGetBoolean(org.lwjgl.opengl.GL11.GL_DEPTH_WRITEMASK) &&
                        org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_DEPTH_TEST),
                        "Spark render pass still writes depth or ignores terrain depth");
            } finally {
                LabSparkRenderType.SPARK.m_110188_();
            }
            require(SparkRenderPass.rendered > 0, "Deferred vanilla/custom spark pass was never drawn");
            System.out.println(
                    "LOLI_LABS_POOL_CLIENT_SPARK_DEPTH_OK: positive synchronized pool mana; actual GPU spark depth writes disabled and depth test enabled; deferred pass rendered");
            shot(mc, "greenhouse-world");
            phase = 3;
            ticks = 0;
            mc.m_91092_().m_201446_(() -> {
                var player = mc.m_91092_().m_6846_().m_11314_().get(0);
                player.m_8999_((net.minecraft.server.level.ServerLevel) player.m_9236_(), origin.m_123341_() + .5,
                        origin.m_123342_() - 1, origin.m_123343_() - 4.5, 0, 0);
                brachy.modularui.factory.BlockEntityUIFactory.INSTANCE.open(player, origin);
            });
            return;
        }
        if (phase == 3) {
            require(mc.f_91080_ instanceof brachy.modularui.api.IMuiScreen,
                    "Server did not open ModularUI: " + mc.f_91080_);
            var screen = (brachy.modularui.api.IMuiScreen) mc.f_91080_;
            var panel = screen.screen().getPanelManager().getMainPanel();
            require(panel.getArea().width == 242 && panel.getArea().height == 302, "Wrong actual GUI dimensions");
            for (int i = 8; i < 12; i++) {
                var item = brachy.modularui.widget.WidgetTree.findFirstWithName(panel, "upgrade_" + i);
                var cell = GreenhouseUI.upgradeCell(i);
                require(item.getArea().x == panel.getArea().x + cell.x() &&
                        item.getArea().y == panel.getArea().y + cell.y(), "Actual upgrade widget shifted " + i);
            }
            var mana = brachy.modularui.widget.WidgetTree.findFirstWithName(panel, "mana_meter");
            var eu = brachy.modularui.widget.WidgetTree.findFirstWithName(panel, "energy_meter");
            require(progress(mana) > 0 && progress(eu) > 0, "Resource meters did not synchronize/fill");
            require(hasWidget(panel, "upgrade_activation"), "Socket activation drawable missing");
            require(!hasWidget(panel, "additional_upgrades") && !hasWidget(panel, "upgrade_0"),
                    "Extra upgrade slots still visible");
            require(GreenhouseRenderer.visibleFlowers((GreenhouseBlockEntity) mc.f_91073_.m_7702_(origin)) == 8,
                    "Installed flowers not synchronized to model");
            shot(mc, "greenhouse-gui");
            System.out.println(
                    "LOLI_LABS_GREENHOUSE_WORLD_GUI_OK: real ModularUI slot positions; EU/mana meters filled; four slots only; upgrade side gauges filled; world/gui screenshots saved");
            phase = 26;
            ticks = 0;
            return;
        }
        if (phase == 26) {
            clickWidget(mc, "power");
            phase = 27;
            ticks = 0;
            return;
        }
        if (phase == 27) {
            require(!((GreenhouseBlockEntity) mc.f_91073_.m_7702_(origin)).enabled,
                    "Power toggle did not disable server controller");
            shot(mc, "greenhouse-power-off");
            clickWidget(mc, "power");
            phase = 28;
            ticks = 0;
            return;
        }
        if (phase == 28) {
            require(((GreenhouseBlockEntity) mc.f_91073_.m_7702_(origin)).enabled,
                    "Power toggle did not re-enable server controller");
            shot(mc, "greenhouse-power-on");
            System.out.println(
                    "LOLI_LABS_OG_POWER_TOGGLE_OK: actual mouse clicks disabled and re-enabled controller; synchronized original slider states");
            phase = 29;
            ticks = 0;
            mc.m_91092_().m_201446_(() -> {
                var g = (GreenhouseBlockEntity) mc.m_91092_().m_6846_().m_11314_().get(0).m_9236_().m_7702_(origin);
                for (var kind : new GreenhouseUpgrade.Kind[] { GreenhouseUpgrade.Kind.EFFICIENCY,
                        GreenhouseUpgrade.Kind.MANA_BUFFER, GreenhouseUpgrade.Kind.MULTIPLICATION,
                        GreenhouseUpgrade.Kind.EU_SPEED })
                    g.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + GreenhouseUpgrade.slotFor(kind),
                            upgrade(kind, 1));
            });
            return;
        }
        if (phase == 29) {
            shot(mc, "greenhouse-gui-four-upgrades");
            phase = 30;
            ticks = 0;
            mc.m_91092_().m_201446_(() -> {
                var g = (GreenhouseBlockEntity) mc.m_91092_().m_6846_().m_11314_().get(0).m_9236_().m_7702_(origin);
                for (int i = 8; i < 10; i++)
                    g.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + i, ItemStack.f_41583_);
            });
            return;
        }
        if (phase == 30) {
            shot(mc, "greenhouse-gui-two-right");
            phase = 31;
            ticks = 0;
            mc.m_91092_().m_201446_(() -> {
                var g = (GreenhouseBlockEntity) mc.m_91092_().m_6846_().m_11314_().get(0).m_9236_().m_7702_(origin);
                g.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + 11, ItemStack.f_41583_);
            });
            return;
        }
        if (phase == 31) {
            shot(mc, "greenhouse-gui-one-right");
            phase = 32;
            ticks = 0;
            mc.m_91092_().m_201446_(() -> {
                var g = (GreenhouseBlockEntity) mc.m_91092_().m_6846_().m_11314_().get(0).m_9236_().m_7702_(origin);
                g.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + 8,
                        upgrade(GreenhouseUpgrade.Kind.EFFICIENCY, 1));
                g.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + 9,
                        upgrade(GreenhouseUpgrade.Kind.MANA_BUFFER, 1));
            });
            return;
        }
        if (phase == 32) {
            shot(mc, "greenhouse-gui-two-left");
            System.out.println(
                    "LOLI_LABS_TOP_CHANNEL_SCENES_OK: paired upgrades on both, right-only and left-only sides; one right upgrade leaves its top channel inactive");
            phase = 20;
            ticks = 0;
            mc.m_91092_().m_201446_(() -> {
                mc.m_91092_().m_6846_().m_11314_().get(0).f_36096_.m_142503_(item("minecraft:coal", 4));
                cursorReady = true;
            });
            return;
        }
        if (phase == 20) {
            if (!cursorReady) return;
            var screen = ((brachy.modularui.api.IMuiScreen) mc.f_91080_).screen();
            var panel = screen.getPanelManager().getMainPanel();
            var slot = brachy.modularui.widget.WidgetTree.findFirstWithName(panel, "crop_output_1");
            var context = screen.getContext();
            int x = slot.getArea().x + 9, y = slot.getArea().y + 9;
            mc.f_91074_.f_36096_.m_142503_(item("minecraft:coal", 4));
            context.updateState(x, y, 0);
            var refreshHover = brachy.modularui.screen.ModularPanel.class.getDeclaredMethod("findHoveredWidgets");
            refreshHover.setAccessible(true);
            refreshHover.invoke(panel);
            context.onFrameUpdate();
            require(context.getTopHovered() == slot,
                    "Animation overlay still intercepts resource-slot clicks: " + context.getTopHovered());
            mc.f_91080_.m_6375_(x, y, 0);
            mc.f_91080_.m_6348_(x, y, 0);
            phase = 21;
            ticks = 0;
            return;
        }
        if (phase == 21) {
            if (!clickQueued) {
                clickQueued = true;
                mc.m_91092_().m_201446_(() -> {
                    try {
                        var g = (GreenhouseBlockEntity) mc.m_91092_().m_6846_().m_11314_().get(0).m_9236_()
                                .m_7702_(origin);
                        require(g.inventory.getStackInSlot(9).m_41613_() == 4,
                                "Actual mouse pickup did not insert the carried resource stack");
                        clickChecked = true;
                    } catch (Throwable error) {
                        failure = error;
                    }
                });
                return;
            }
            if (!clickChecked) return;
            System.out.println(
                    "LOLI_LABS_GREENHOUSE_MOUSE_INSERT_OK: actual GUI hit test and cursor pickup inserted four coal into lower resource slot");
            phase = 6;
            ticks = 0;
            mc.m_91092_().m_201446_(() -> {
                var g = (GreenhouseBlockEntity) mc.m_91092_().m_6846_().m_11314_().get(0).m_9236_().m_7702_(origin);
                for (int i = 8; i < 12; i++)
                    g.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + i, ItemStack.f_41583_);
            });
            return;
        }
        if (phase == 6) {
            shot(mc, "greenhouse-gui-inactive");
            phase = 7;
            ticks = 0;
            mc.m_91092_().m_201446_(() -> {
                var g = (GreenhouseBlockEntity) mc.m_91092_().m_6846_().m_11314_().get(0).m_9236_().m_7702_(origin);
                g.inventory.setStackInSlot(GreenhouseBlockEntity.UPGRADE_BASE + 9,
                        upgrade(GreenhouseUpgrade.Kind.MANA_BUFFER, 2));
            });
            return;
        }
        if (phase == 7) {
            shot(mc, "greenhouse-gui-activation");
            phase = 8;
            ticks = 0;
            return;
        }
        if (phase == 8) {
            shot(mc, "greenhouse-gui-mana-only");
            System.out.println(
                    "LOLI_LABS_GREENHOUSE_ACTIVATION_SHOTS_OK: empty channels, early wave from Mana Storage socket, completed left branch; no occupancy-percentage gauges");
            phase = 17;
            ticks = 0;
            mc.f_91074_.m_6915_();
            deferTicks = 10;
            deferredOpen = () -> mc.m_91092_().m_201446_(() -> brachy.modularui.factory.BlockEntityUIFactory.INSTANCE
                    .open(mc.m_91092_().m_6846_().m_11314_().get(0), origin));
            return;
        }
        if (phase == 17) {
            if (!(mc.f_91080_ instanceof brachy.modularui.api.IMuiScreen)) return;
            var panel = ((brachy.modularui.api.IMuiScreen) mc.f_91080_).screen().getPanelManager().getMainPanel();
            var widget = (brachy.modularui.widget.Widget<?>) brachy.modularui.widget.WidgetTree.findFirstWithName(panel,
                    "upgrade_activation");
            var gauge = (GreenhouseGauge) widget.getBackground();
            if (gauge.distance(1) < 0) return;
            require(gauge.distance(1) >= 250, "Completed upgrade animation restarted on GUI reopen");
            shot(mc, "greenhouse-gui-reopened");
            System.out.println(
                    "LOLI_LABS_UPGRADE_REOPEN_OK: first rendered frame retains completed activation instead of replaying");
            phase = 13;
            ticks = 0;
            tooltipPreview = true;
            return;
        }
        if (phase == 13) {
            shot(mc, "greenhouse-upgrade-tooltip");
            tooltipPreview = false;
            itemTooltipPreview = true;
            phase = 18;
            ticks = 0;
            return;
        }
        if (phase == 18) {
            shot(mc, "upgrade-item-tooltip");
            itemTooltipPreview = false;
            phase = 14;
            ticks = 0;
            mc.f_91074_.m_6915_();
            mc.m_91092_().m_201446_(() -> {
                var player = mc.m_91092_().m_6846_().m_11314_().get(0);
                var world = (net.minecraft.server.level.ServerLevel) player.m_9236_();
                world.m_8615_(18000);
                player.m_8999_(world, origin.m_123341_() + 5.5, origin.m_123342_() - 1, origin.m_123343_() - 6.5, 35,
                        0);
            });
            return;
        }
        if (phase == 14) {
            shot(mc, "greenhouse-night");
            mc.m_91092_().m_201446_(
                    () -> ((net.minecraft.server.level.ServerLevel) mc.m_91092_().m_6846_().m_11314_().get(0).m_9236_())
                            .m_8615_(6000));
            phase = 4;
            ticks = 0;
            mc.f_91074_.m_6915_();
            mc.m_91092_().m_201446_(() -> {
                var player = mc.m_91092_().m_6846_().m_11314_().get(0);
                player.m_8999_((net.minecraft.server.level.ServerLevel) player.m_9236_(), origin.m_123341_() + 1.5,
                        origin.m_123342_() - 1, origin.m_123343_() - 4.8, 0, 30);
            });
            return;
        }
        if (phase == 4) {
            shot(mc, "spreader-ground-contact");
            phase = 5;
            ticks = 0;
            mc.m_91092_().m_201446_(() -> {
                var player = mc.m_91092_().m_6846_().m_11314_().get(0);
                var world = (net.minecraft.server.level.ServerLevel) player.m_9236_();
                for (int x = -4; x <= 4; x++)
                    for (int z = -6; z <= 4; z++) world.m_46597_(origin.m_7918_(x, -2, z), Blocks.f_50016_.m_49966_());
                player.m_8999_(world, origin.m_123341_() + .5, origin.m_123342_() - 4, origin.m_123343_() - 5.5, 0,
                        -30);
            });
            return;
        }
        if (phase == 5) {
            shot(mc, "greenhouse-underside");
            System.out.println("LOLI_LABS_GREENHOUSE_UNDERSIDE_SHOT_OK: greenhouse floor and spreaders from below");
            phase = 9;
            ticks = 0;
            openMachine(mc, "dispersion_solar_panel", origin.m_7918_(-5, -1, 0), true);
            return;
        }
        if (phase >= 9 && phase <= 12) {
            var panel = ((brachy.modularui.api.IMuiScreen) mc.f_91080_).screen().getPanelManager().getMainPanel();
            boolean powered = phase != 10;
            require(brachy.modularui.widget.WidgetTree.findFirstWithName(panel, "energy_icon").isEnabled() == powered,
                    "Lightning icon does not follow stored EU");
            require((progress(brachy.modularui.widget.WidgetTree.findFirstWithName(panel, "energy_meter")) > 0) ==
                    powered, "Shared spark meter has wrong fill");
            shot(mc, phase == 9 ? "solar-panel-gui" :
                    phase == 10 ? "solar-panel-empty-gui" : phase == 11 ? "unifier-gui" : "matter-generator-gui");
            if (phase == 9) {
                phase = 10;
                ticks = 0;
                mc.m_91092_().m_201446_(() -> {
                    var machine = (LabMachineEntity) mc.m_91092_().m_6846_().m_11314_().get(0).m_9236_()
                            .m_7702_(origin.m_7918_(-5, -1, 0));
                    machine.energy = 0;
                    machine.syncSettings();
                });
                return;
            }
            if (phase == 10) {
                phase = 11;
                ticks = 0;
                openMachine(mc, "energy_unifier", origin.m_7918_(5, -1, -2), true);
                return;
            }
            if (phase == 11) {
                phase = 12;
                ticks = 0;
                openMachine(mc, "matter_solid_t0_x1", origin.m_7918_(6, -1, -2), true);
                return;
            }
            System.out.println(
                    "LOLI_LABS_SHARED_SPARK_METERS_OK: solar, unifier and matter generator; EU lightning on with power and off at zero; actual GUI screenshots");
            phase = 22;
            ticks = 0;
            preparePoolScene(mc);
            return;
        }
        if (phase == 22) {
            for (var tier : LabBotaniaTiers.Tier.values()) {
                var pool = (LabManaPoolBlockEntity) mc.f_91073_
                        .m_7702_(origin.m_7918_(-5 + tier.ordinal() * 3, -1, -5));
                require(pool != null && pool.getCurrentMana() == pool.getMaxMana() / 2,
                        "Filled pool scene did not sync " + tier);
            }
            shot(mc, "mana-pools-filled");
            phase = 23;
            ticks = 0;
            prepareSparkScene(mc, 0);
            return;
        }
        if (phase == 23) {
            shot(mc, "greenhouse-spark-baseline");
            phase = 24;
            ticks = 0;
            prepareSparkScene(mc, 1);
            return;
        }
        if (phase == 24) {
            shot(mc, "greenhouse-through-vanilla-spark");
            phase = 25;
            ticks = 0;
            prepareSparkScene(mc, 2);
            return;
        }
        if (phase == 25) {
            shot(mc, "greenhouse-through-custom-spark");
            System.out.println(
                    "LOLI_LABS_POOL_RENDER_SPARK_SCENES_OK: all four synchronized half-full pool surfaces; identical greenhouse camera without spark and through vanilla/custom foreground sparks");
            phase = 50;
            tmr.lolilabs.utility.UtilityWorldChecks.beginClient(mc);
        }
    }
}
