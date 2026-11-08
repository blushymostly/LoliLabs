package tmr.lolilabs.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.registries.ForgeRegistries;

/** Opt-in regression tests inside the existing disposable client fixture world. */
public final class UtilityWorldChecks {

    private static appeng.helpers.InterfaceLogicHost[] meInterfaces;
    private static BlockPos pos;
    private static int phase, ticks;
    private static float dayAngle;
    private static volatile boolean ready, mouseChecked;
    private static volatile Throwable failure;

    private static ItemStack item(String id, int count) {
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        require(item != null && item != Items.f_41852_, "Missing item " + id);
        return new ItemStack(item, count);
    }

    private static Block block(String id) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new IllegalStateException(message);
    }

    private static int output(FortuneEntity be, String id) {
        int total = 0;
        for (int i = 27; i < 54; i++) {
            var s = be.inventory.getStackInSlot(i);
            if (!s.m_41619_() && ForgeRegistries.ITEMS.getKey(s.m_41720_()).toString().equals(id))
                total += s.m_41613_();
        }
        return total;
    }

    private static void clear(FortuneEntity be) {
        for (int i = 0; i < 58; i++) be.inventory.setStackInSlot(i, ItemStack.f_41583_);
        be.reserved.clear();
        be.pending.clear();
        be.progress = be.totalWork = be.doneWork = 0;
        be.energy = 0;
        be.enabled = true;
    }

    private static void tick(FortuneEntity be, int count) {
        for (int i = 0; i < count; i++) be.tick();
    }

    public static void run(net.minecraft.server.level.ServerLevel world, BlockPos anchor) {
        pos = anchor.m_7918_(16, 0, 0);
        world.m_46597_(pos, LabUtility.FORTUNE.get().m_49966_());
        var be = (FortuneEntity) world.m_7702_(pos);
        for (int level : LabUtility.LEVELS) {
            be.inventory.setStackInSlot(54, item("loli_labs:fortune_module_" + level, 1));
            require(be.fortune() == level, "Fortune level mismatch");
        }
        var pickaxe = item("minecraft:diamond_pickaxe", 1);
        require(!be.valid(54, pickaxe), "Unenchanted pickaxe accepted");
        net.minecraft.world.item.enchantment.EnchantmentHelper
                .m_44865_(java.util.Map.of(net.minecraft.world.item.enchantment.Enchantments.f_44987_, 3), pickaxe);
        require(be.valid(54, pickaxe), "Fortune pickaxe rejected");
        be.inventory.setStackInSlot(54, pickaxe);
        require(be.fortune() == 3, "Pickaxe Fortune level ignored");
        require(!be.valid(55, pickaxe), "Pickaxe accepted as smelting upgrade");
        for (int level : LabUtility.LEVELS) {
            var module = item("loli_labs:fortune_module_" + level, 1);
            require(module.m_41720_().m_5812_(module), "Fortune module lacks enchantment glint");
        }
        require(!ForgeRegistries.ITEMS.containsKey(new ResourceLocation("ic2modern:iridium")),
                "Duplicate iridium remains registered");
        require(com.ic2ceu.icceu.LegacyItems.replacement("iridium") == item("ic2modern:raw_iridium", 1).m_41720_(),
                "Old iridium does not migrate");
        require(!ForgeRegistries.BLOCKS.containsKey(new ResourceLocation("ic2modern:copper_ore")) &&
                !ForgeRegistries.BLOCKS.containsKey(new ResourceLocation("ic2modern:copper_deepslate_ore")),
                "Duplicate copper ore block remains");
        require(com.ic2ceu.icceu.LegacyItems.replacement("copper_ore") == item("minecraft:copper_ore", 1).m_41720_(),
                "Copper item migration failed");
        var recipes = world.m_7465_();
        var rawIridium = item("ic2modern:raw_iridium", 1);
        int iridiumRecipes = 0;
        for (var recipe : recipes.m_44051_()) if (recipe instanceof com.ic2ceu.icceu.MachineRecipe m &&
                m.machine.equals("macerator") && m.ingredient.test(rawIridium)) {
                    iridiumRecipes++;
                    require(m.output().m_41613_() == 1, "Iridium 1:2 duplicate remains");
                }
        require(iridiumRecipes == 1, "Expected exactly one iridium maceration recipe");
        for (String metal : new String[] { "copper", "tin", "lead", "uranium", "iridium" })
            for (String variant : new String[] { "_ore", "_deepslate_ore" }) {
                String raw = metal.equals("copper") ? "minecraft:raw_copper" : "ic2modern:raw_" + metal,
                        ingot = metal.equals("copper") ? "minecraft:copper_ingot" : "ic2modern:" + metal + "_ingot";
                var ore = item(metal.equals("copper") ?
                        (variant.equals("_ore") ? "minecraft:copper_ore" : "minecraft:deepslate_copper_ore") :
                        "ic2modern:" + metal + variant, 1);
                require(FortuneEntity.ore(ore), "IC2 ore tag missing " + metal + variant);
                var drops = be.drops(ore, 0, false);
                require(drops.stream().anyMatch(d -> d.m_150930_(item(raw, 1).m_41720_())),
                        "IC2 ore did not drop raw material " + metal + variant);
                var smelted = be.drops(ore, 0, true);
                require(smelted.stream().anyMatch(d -> d.m_150930_(item(ingot, 1).m_41720_())),
                        "IC2 raw material failed auto-smelting " + metal + variant);
                var silk = item("minecraft:diamond_pickaxe", 1);
                net.minecraft.world.item.enchantment.EnchantmentHelper.m_44865_(
                        java.util.Map.of(net.minecraft.world.item.enchantment.Enchantments.f_44985_, 1), silk);
                var silkDrops = Block.m_49874_(((BlockItem) ore.m_41720_()).m_40614_().m_49966_(), world, pos, null,
                        null, silk);
                require(silkDrops.size() == 1 && silkDrops.get(0).m_150930_(ore.m_41720_()),
                        "Silk Touch lost IC2 ore block " + metal + variant);
            }
        System.out.println(
                "LOLI_LABS_IC2_RAW_ORES_OK: all ten IC2 ore variants yield raw materials, preserve Silk Touch and auto-smelt to ingots");
        for (int level : LabUtility.LEVELS) for (int repeat = 0; repeat < 3; repeat++) {
            clear(be);
            be.inventory.setStackInSlot(54, item("loli_labs:fortune_module_" + level, 1));
            be.inventory.setStackInSlot(55, item("loli_labs:fortune_upgrade_smelting", 1));
            be.inventory.setStackInSlot(57, item("loli_labs:fortune_upgrade_infinite", 1));
            be.inventory.setStackInSlot(0, item("minecraft:iron_ore", 1));
            tick(be, 20);
            require(output(be, "minecraft:iron_ingot") == level && be.pending.isEmpty(),
                    "Module output must be exactly 1:" + level + ", repeat " + repeat);
        }
        for (int repeat = 0; repeat < 4; repeat++) {
            clear(be);
            be.inventory.setStackInSlot(54, item("loli_labs:fortune_module_100", 1));
            be.inventory.setStackInSlot(55, item("loli_labs:fortune_upgrade_smelting", 1));
            be.inventory.setStackInSlot(57, item("loli_labs:fortune_upgrade_infinite", 1));
            be.inventory.setStackInSlot(0, item("minecraft:copper_ore", 1));
            tick(be, 20);
            require(output(be, "minecraft:copper_ingot") == 100,
                    "Variable copper base drop must not randomize the module's fixed ratio");
        }
        clear(be);
        be.inventory.setStackInSlot(54, item("loli_labs:fortune_module_100", 1));
        be.inventory.setStackInSlot(55, item("loli_labs:fortune_upgrade_smelting", 1));
        be.inventory.setStackInSlot(56, item("loli_labs:fortune_upgrade_stack", 1));
        be.inventory.setStackInSlot(57, item("loli_labs:fortune_upgrade_infinite", 1));
        be.inventory.setStackInSlot(0, item("minecraft:iron_ore", 64));
        be.inventory.setStackInSlot(1, item("minecraft:iron_ore", 64));
        be.tick();
        require(be.inventory.getStackInSlot(0).m_41619_() && be.inventory.getStackInSlot(1).m_41613_() == 64,
                "Second input slot must wait while the first is processed");
        require(be.jobMultiplier == 100 && be.totalWork == 64 && be.progress > 0 &&
                be.progress < LabUtility.TICKS.get(), "In-progress first-slot job missing before NBT");
        var moduleTag = be.m_187480_();
        var moduleRestored = new FortuneEntity(pos, be.m_58900_());
        moduleRestored.m_142466_(moduleTag);
        world.m_151523_(moduleRestored);
        be = moduleRestored;
        require(be.jobMultiplier == 100 && be.totalWork == 64 && be.progress > 0,
                "Fixed module ratio lost in an in-progress job");
        tick(be, 25);
        require(output(be, "minecraft:iron_ingot") + be.pending.stream().mapToInt(ItemStack::m_41613_).sum() == 6400,
                "First slot alone must yield exactly 6400 before the second starts");
        require(be.inventory.getStackInSlot(1).m_41613_() == 64,
                "Later slot must stay untouched until earlier slot and pending drain");
        for (int i = 27; i < 54; i++) be.inventory.setStackInSlot(i, ItemStack.f_41583_);
        be.pending.clear();
        be.progress = be.totalWork = be.doneWork = 0;
        tick(be, 25);
        require(output(be, "minecraft:iron_ingot") + be.pending.stream().mapToInt(ItemStack::m_41613_).sum() == 6400 &&
                be.inventory.getStackInSlot(1).m_41619_(), "Second slot must process only after the first is empty");
        for (int i = 27; i < 54; i++) require(be.inventory.getStackInSlot(i).m_41613_() <= 64, "Overstacked output");
        System.out.println(
                "LOLI_LABS_FIXED_FORTUNE_OK: all 11 exact module ratios; sequential slots; stack batching and mid-job NBT");
        clear(be);
        var handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP)
                .orElseThrow(() -> new IllegalStateException("No item handler"));
        require(handler.getSlots() == 54, "Automation exposes modules");
        require(handler.insertItem(0, item("minecraft:cobblestone", 1), true).m_41613_() == 1, "Non-ore accepted");
        require(handler.insertItem(0, item("minecraft:coal_ore", 64), false).m_41619_(), "Ore insertion failed");
        require(handler.extractItem(0, 1, false).m_41619_(), "Automation can steal input");
        tick(be, 25);
        require(be.inventory.getStackInSlot(0).m_41613_() == 64 && output(be, "minecraft:coal") == 0,
                "No-energy machine consumed ore");
        require(!be.getCapability(ForgeCapabilities.ENERGY).isPresent(), "Fortune transformer exposes FE");
        require(be.receivePacket(Direction.NORTH, 401, 401) == 401 && be.energy == 10 && be.fraction == 1,
                "Fractional EU mismatch");
        be.energy = 2048;
        tick(be, 20);
        require(be.inventory.getStackInSlot(0).m_41613_() == 63 && output(be, "minecraft:coal") == 1 && be.energy == 0,
                "Single-ore cycle or EU cost failed");
        clear(be);
        be.inventory.setStackInSlot(55, item("loli_labs:fortune_upgrade_smelting", 1));
        be.inventory.setStackInSlot(56, item("loli_labs:fortune_upgrade_stack", 1));
        be.inventory.setStackInSlot(57, item("loli_labs:fortune_upgrade_infinite", 1));
        be.inventory.setStackInSlot(0, item("minecraft:iron_ore", 64));
        be.inventory.setStackInSlot(1, item("minecraft:coal_ore", 64));
        tick(be, 20);
        require(output(be, "minecraft:iron_ingot") == 64 && output(be, "minecraft:coal") == 0 &&
                be.inventory.getStackInSlot(0).m_41619_() && be.inventory.getStackInSlot(1).m_41613_() == 64,
                "First occupied slot must finish before later slots start");
        tick(be, 20);
        require(output(be, "minecraft:iron_ingot") == 64 && output(be, "minecraft:coal") == 64,
                "Second slot processes only after the first is empty");
        require(be.energy == 0 && be.inventory.getStackInSlot(0).m_41619_() &&
                be.inventory.getStackInSlot(1).m_41619_(), "Infinite engine spent energy or stack inputs remain");
        clear(be);
        be.inventory.setStackInSlot(57, item("loli_labs:fortune_upgrade_infinite", 1));
        be.inventory.setStackInSlot(54, item("loli_labs:fortune_module_100", 1));
        be.inventory.setStackInSlot(56, item("loli_labs:fortune_upgrade_stack", 1));
        be.inventory.setStackInSlot(0, item("minecraft:diamond_ore", 64));
        tick(be, 20);
        require(output(be, "minecraft:diamond") + be.pending.stream().mapToInt(ItemStack::m_41613_).sum() > 64,
                "Fixed module did not increase ore output");
        clear(be);
        be.inventory.setStackInSlot(57, item("loli_labs:fortune_upgrade_infinite", 1));
        be.inventory.setStackInSlot(56, item("loli_labs:fortune_upgrade_stack", 1));
        be.inventory.setStackInSlot(0, item("minecraft:coal_ore", 64));
        be.tick();
        for (int i = 27; i < 54; i++) be.inventory.setStackInSlot(i, item("minecraft:cobblestone", 64));
        tick(be, 25);
        require(!be.pending.isEmpty() && be.inventory.getStackInSlot(0).m_41619_(),
                "Blocked job disappeared or duplicated input");
        var tag = be.m_187480_();
        var restored = new FortuneEntity(pos, be.m_58900_());
        restored.m_142466_(tag);
        world.m_151523_(restored);
        be = restored;
        require(be.pending.stream().mapToInt(ItemStack::m_41613_).sum() == 64, "Pending results lost on reload");
        be.inventory.setStackInSlot(27, ItemStack.f_41583_);
        be.tick();
        require(be.pending.isEmpty() && output(be, "minecraft:coal") == 64, "Reloaded blocked output failed");
        clear(be); // replaced below by registry: real hopper, aimed downward.
        var hopperPos = pos.m_121945_(Direction.UP);
        world.m_46597_(hopperPos, block("minecraft:hopper").m_49966_());
        var hopper = (net.minecraft.world.level.block.entity.HopperBlockEntity) world.m_7702_(hopperPos);
        hopper.m_6836_(0, item("minecraft:coal_ore", 4));
        for (int i = 0; i < 20; i++) net.minecraft.world.level.block.entity.HopperBlockEntity.m_155573_(world,
                hopperPos, world.m_8055_(hopperPos), hopper);
        require(be.inventory.getStackInSlot(0).m_41613_() > 0, "Vanilla hopper did not insert ore");
        world.m_46597_(hopperPos, block("minecraft:air").m_49966_());
        var chestPos = pos.m_121945_(Direction.WEST);
        world.m_46597_(chestPos, block("minecraft:chest").m_49966_());
        be.inventory.setStackInSlot(27, item("minecraft:diamond", 32));
        be.autoOutput();
        var chest = (net.minecraft.world.level.block.entity.ChestBlockEntity) world.m_7702_(chestPos);
        require(chest.m_8020_(0).m_41613_() == 32 && be.inventory.getStackInSlot(27).m_41619_(),
                "Automatic output to chest failed");
        world.m_46597_(chestPos, block("minecraft:air").m_49966_());
        var aePos = pos.m_121945_(Direction.EAST);
        world.m_46597_(aePos, block("ae2:interface").m_49966_());
        var ae = world.m_7702_(aePos);
        var aeHandler = ae.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.WEST)
                .orElseThrow(() -> new IllegalStateException("ME interface lacks item capability"));
        be.inventory.setStackInSlot(27, item("minecraft:diamond", 16));
        be.autoOutput();
        int inAE = 0;
        for (int i = 0; i < aeHandler.getSlots(); i++)
            if (aeHandler.getStackInSlot(i).m_150930_(item("minecraft:diamond", 1).m_41720_()))
                inAE += aeHandler.getStackInSlot(i).m_41613_();
        require(inAE == 16 && be.inventory.getStackInSlot(27).m_41619_(), "ME interface output failed");
        be.inventory.setStackInSlot(0, ItemStack.f_41583_);
        require(aeHandler.insertItem(1, item("minecraft:iron_ore", 8), false).m_41619_(),
                "ME interface stock setup failed");
        be.pullInterfaceInputs();
        require(be.inventory.getStackInSlot(0).m_41613_() == 8, "Configured ME interface did not feed transformer");
        world.m_46597_(aePos, block("minecraft:air").m_49966_());
        clear(be);
        for (int z = -2; z < 0; z++) {
            var p = pos.m_7918_(0, 0, z);
            world.m_46597_(p, block("ic2modern:glass_fibre_cable").m_49966_());
            var cable = (com.ic2ceu.icceu.CableEntity) world.m_7702_(p);
            cable.setOpen(Direction.NORTH, true);
            cable.setOpen(Direction.SOUTH, true);
            com.ic2ceu.icceu.CableBlock.refreshAround(world, p);
        }
        var sourcePos = pos.m_7918_(0, 0, -3);
        world.m_46597_(sourcePos,
                tmr.lolilabs.LabMachines.DEFINITIONS.get("dispersion_solar_panel").block.get().m_49966_());
        ((tmr.lolilabs.LabMachineEntity) world.m_7702_(sourcePos)).serverTick();
        require(be.energy > 0, "IC2 cable did not deliver EU");
        world.m_46597_(sourcePos, block("minecraft:air").m_49966_());
        clear(be);
        be.energy = be.capacity() / 2;
        be.enabled = false;
        be.inventory.setStackInSlot(54, item("loli_labs:fortune_module_10", 1));
        be.inventory.setStackInSlot(55, item("loli_labs:fortune_upgrade_smelting", 1));
        be.inventory.setStackInSlot(56, item("loli_labs:fortune_upgrade_stack", 1));
        be.sync();
        allInterfaceSides(world, be);
        // Both real network variants are above the machine, with the exact one-ore stock configuration from the user
        // world.
        meInterfaces = new appeng.helpers.InterfaceLogicHost[] { network(world, pos, false),
                network(world, pos.m_7918_(0, 0, 6), true) };
        System.out.println(
                "LOLI_LABS_UTILITY_WORLD_OK: native Fortune loot; sequential input slots; stack/smelting/infinite modules; EU and fractions; no FE; blocked-output NBT; real hopper, chest, ME interface and IC2 cables");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static appeng.helpers.InterfaceLogicHost placeInterface(net.minecraft.server.level.ServerLevel world,
                                                                    BlockPos position, Direction face, boolean part) {
        if (!part) {
            world.m_46597_(position, block("ae2:interface").m_49966_());
            return (appeng.helpers.InterfaceLogicHost) world.m_7702_(position);
        }
        world.m_46597_(position, block("ae2:cable_bus").m_49966_());
        var bus = (appeng.blockentity.networking.CableBusBlockEntity) world.m_7702_(position);
        var player = Minecraft.m_91087_().m_91092_().m_6846_().m_11314_().get(0);
        bus.addPart((appeng.api.parts.IPartItem) item("ae2:fluix_glass_cable", 1).m_41720_(), null, player);
        return (appeng.helpers.InterfaceLogicHost) bus
                .addPart((appeng.api.parts.IPartItem) item("ae2:cable_interface", 1).m_41720_(), face, player);
    }

    private static appeng.helpers.InterfaceLogicHost network(net.minecraft.server.level.ServerLevel world,
                                                             BlockPos target, boolean part) {
        world.m_46597_(target, LabUtility.FORTUNE.get().m_49966_());
        var machine = (FortuneEntity) world.m_7702_(target);
        clear(machine);
        machine.inventory.setStackInSlot(54, item("loli_labs:fortune_module_3", 1));
        machine.inventory.setStackInSlot(55, item("loli_labs:fortune_upgrade_smelting", 1));
        machine.inventory.setStackInSlot(56, item("loli_labs:fortune_upgrade_stack", 1));
        machine.inventory.setStackInSlot(57, item("loli_labs:fortune_upgrade_infinite", 1));
        var interfacePos = target.m_121945_(Direction.UP);
        var iface = placeInterface(world, interfacePos, Direction.DOWN, part);
        var drivePos = interfacePos.m_121945_(Direction.EAST);
        world.m_46597_(drivePos, block("ae2:drive").m_49966_());
        var drive = (appeng.blockentity.storage.DriveBlockEntity) world.m_7702_(drivePos);
        world.m_46597_(drivePos.m_121945_(Direction.EAST), block("ae2:creative_energy_cell").m_49966_());
        var cell = item("ae2:item_storage_cell_1k", 1);
        var inventory = appeng.api.storage.StorageCells.getCellInventory(cell, null);
        require(inventory.insert(appeng.api.stacks.AEItemKey.of(item("minecraft:iron_ore", 1)), 64,
                appeng.api.config.Actionable.MODULATE, appeng.api.networking.security.IActionSource.empty()) == 64,
                "Cannot preload AE cell");
        inventory.persist();
        drive.getInternalInventory().setItemDirect(0, cell);
        iface.getInterfaceLogic().getConfig().setStack(0,
                new appeng.api.stacks.GenericStack(appeng.api.stacks.AEItemKey.of(item("minecraft:iron_ore", 1)), 1));
        return iface;
    }

    private static void allInterfaceSides(net.minecraft.server.level.ServerLevel world, FortuneEntity machine) {
        for (boolean part : new boolean[] { false, true }) for (Direction side : Direction.values()) {
            var neighbor = machine.m_58899_().m_121945_(side);
            var host = placeInterface(world, neighbor, side.m_122424_(), part);
            clear(machine);
            var key = appeng.api.stacks.AEItemKey.of(item("minecraft:iron_ore", 1));
            host.getInterfaceLogic().getStorage().setStack(0, new appeng.api.stacks.GenericStack(key, 1));
            machine.pullInterfaceInputs();
            require(machine.inventory.getStackInSlot(0).m_41613_() == 1,
                    "ME input failed side=" + side + " part=" + part);
            require(host.getInterfaceLogic().getStorage().getStack(0) == null, "ME input duplicated stock");
            for (int i = 0; i < 27; i++) machine.inventory.setStackInSlot(i, item("minecraft:iron_ore", 64));
            host.getInterfaceLogic().getStorage().setStack(0, new appeng.api.stacks.GenericStack(key, 1));
            machine.pullInterfaceInputs();
            require(host.getInterfaceLogic().getStorage().getStack(0).amount() == 1, "Full input consumed ME stock");
            world.m_46597_(neighbor, block("minecraft:air").m_49966_());
        }
        clear(machine);
        System.out.println(
                "LOLI_LABS_ME_ALL_SIDES_OK: block and cable-part interfaces, all six sides including UP/DOWN; full-input backpressure");
    }

    public static void beginClient(Minecraft mc) {
        tmr.lolilabs.dimensions.DimensionsChecks.client(mc);
        phase = 1;
        ticks = 0;
        mc.f_91074_.m_6915_();
        mc.m_91092_().m_201446_(() -> {
            var player = mc.m_91092_().m_6846_().m_11314_().get(0);
            try {
                var world = (net.minecraft.server.level.ServerLevel) player.m_9236_();
                for (var iface : meInterfaces) {
                    var grid = iface.getInterfaceLogic().getActionableNode().getGrid();
                    require(grid != null, "ME grid did not form");
                    var stored = grid.getStorageService().getInventory().getAvailableStacks();
                    require(stored.get(appeng.api.stacks.AEItemKey.of(item("minecraft:iron_ingot", 1))) > 0,
                            "ME interface above machine did not supply iron ore and receive ingots: " +
                                    iface.getClass());
                }
                System.out.println(
                        "LOLI_LABS_UTILITY_ME_NETWORK_OK: full-block AND cable-part interfaces ABOVE machines, one iron ore in config, ores from cell and ingots returned to ME storage");
                var be = (FortuneEntity) world.m_7702_(pos);
                be.enabled = false;
                world.m_46597_(pos.m_121945_(Direction.UP), block("minecraft:air").m_49966_());
                for (int i = 0; i < 54; i++) be.inventory.setStackInSlot(i, ItemStack.f_41583_);
                be.inventory.setStackInSlot(54, item("loli_labs:fortune_module_10", 1));
                be.inventory.setStackInSlot(55, item("loli_labs:fortune_upgrade_smelting", 1));
                be.inventory.setStackInSlot(56, item("loli_labs:fortune_upgrade_stack", 1));
                be.energy = be.capacity() / 2;
                be.sync();
            } catch (Throwable e) {
                failure = e;
                return;
            }
            player.m_150110_().f_35935_ = true;
            player.m_6885_();
            player.m_8999_((net.minecraft.server.level.ServerLevel) player.m_9236_(), pos.m_123341_() + 2.7,
                    pos.m_123342_() + 1, pos.m_123343_() - 3.5, 35, 15);
        });
    }

    private static void shot(Minecraft mc, String name) throws java.io.IOException {
        var path = java.nio.file.Path.of(System.getProperty("loli_labs.worldShots"));
        try (var image = net.minecraft.client.Screenshot.m_92279_(mc.m_91385_())) {
            image.m_85066_(path.resolve(name + ".png"));
        }
    }

    public static void client(Minecraft mc) throws Exception {
        if (failure != null) throw new IllegalStateException(failure);
        if (++ticks < 40) return;
        if (phase == 1) {
            require(FortuneClient.renderedFrames > 2, "Fortune core renderer never ran");
            require(FortuneClient.rotation(0, 0) != FortuneClient.rotation(1, 0), "Fortune core animation is static");
            require(FortuneClient.coreModel != null && FortuneClient.coreModel != mc.m_91304_().m_119409_(),
                    "Fortune core model missing");
            shot(mc, "fortune-world");
            dayAngle = FortuneClient.lastAngle;
            phase = 6;
            ticks = 0;
            mc.m_91092_().m_201446_(
                    () -> ((net.minecraft.server.level.ServerLevel) mc.m_91092_().m_6846_().m_11314_().get(0).m_9236_())
                            .m_8615_(18000));
            return;
        }
        if (phase == 6) {
            require(FortuneClient.lastAngle != dayAngle, "Rendered core angle did not advance");
            shot(mc, "fortune-night");
            System.out.println(
                    "LOLI_LABS_UTILITY_ANIMATION_LIGHT_OK: actual core renderer advances angle; day/night world screenshots captured with non-emissive meshes");
            phase = 2;
            ticks = 0;
            mc.m_91092_().m_201446_(() -> {
                var player = mc.m_91092_().m_6846_().m_11314_().get(0);
                ((net.minecraft.server.level.ServerLevel) player.m_9236_()).m_8615_(6000);
                for (int i = 0; i < 36; i++) player.m_150109_().m_6836_(i, item("minecraft:iron_ingot", i + 1));
                brachy.modularui.factory.BlockEntityUIFactory.INSTANCE.open(player, pos);
            });
            return;
        }
        if (phase == 2) {
            require(mc.f_91080_ instanceof brachy.modularui.api.IMuiScreen, "Fortune GUI not opened");
            shot(mc, "fortune-gui");
            var screen = ((brachy.modularui.api.IMuiScreen) mc.f_91080_).screen();
            var panel = screen.getPanelManager().getMainPanel();
            var slot = brachy.modularui.widget.WidgetTree.findFirstWithName(panel, "input_1");
            require(slot.getArea().x == panel.getArea().x + 32 && slot.getArea().y == panel.getArea().y + 14,
                    "Fortune slot coordinates incorrect");
            for (int i = 0; i < 4; i++) {
                var module = brachy.modularui.widget.WidgetTree.findFirstWithName(panel, "module_" + i);
                require(module.getArea().x == panel.getArea().x + (i == 0 ? 14 : 180) &&
                        module.getArea().y == panel.getArea().y + (i == 0 ? 72 : 18 + (i - 1) * 18),
                        "Module slot does not match OG pixel bounds");
            }
            phase = 3;
            ticks = 0;
            mc.m_91092_().m_201446_(() -> {
                mc.m_91092_().m_6846_().m_11314_().get(0).f_36096_.m_142503_(item("minecraft:coal_ore", 4));
                ready = true;
            });
            return;
        }
        if (phase == 3) {
            if (!ready) return;
            var screen = ((brachy.modularui.api.IMuiScreen) mc.f_91080_).screen();
            var panel = screen.getPanelManager().getMainPanel();
            var slot = brachy.modularui.widget.WidgetTree.findFirstWithName(panel, "input_1");
            int x = slot.getArea().x + 9, y = slot.getArea().y + 9;
            mc.f_91074_.f_36096_.m_142503_(item("minecraft:coal_ore", 4));
            var context = screen.getContext();
            context.updateState(x, y, 0);
            var refresh = brachy.modularui.screen.ModularPanel.class.getDeclaredMethod("findHoveredWidgets");
            refresh.setAccessible(true);
            refresh.invoke(panel);
            context.onFrameUpdate();
            require(context.getTopHovered() == slot, "Fortune decoration intercepts input click");
            mc.f_91080_.m_6375_(x, y, 0);
            mc.f_91080_.m_6348_(x, y, 0);
            phase = 4;
            ticks = 0;
            return;
        }
        if (phase == 4) {
            mc.m_91092_().m_201446_(() -> {
                try {
                    var be = (FortuneEntity) mc.m_91092_().m_6846_().m_11314_().get(0).m_9236_().m_7702_(pos);
                    require(be.inventory.getStackInSlot(1).m_41613_() == 4, "Fortune mouse insertion failed");
                    be.inventory.setStackInSlot(57, item("loli_labs:fortune_upgrade_infinite", 1));
                    be.energy = 0;
                    be.sync();
                    mouseChecked = true;
                } catch (Throwable e) {
                    failure = e;
                }
            });
            phase = 5;
            ticks = 0;
            return;
        }
        if (phase == 5) {
            if (!mouseChecked) return;
            shot(mc, "fortune-infinite-gui");
            phase = 7;
            ticks = 0;
            return;
        }
        if (phase == 7) {
            shot(mc, "fortune-infinite-flow");
            phase = 8;
            ticks = 0;
            tmr.lolilabs.dimensions.DimensionsChecks.tooltipPreview = true;
            return;
        }
        if (phase == 8) {
            shot(mc, "fortune-tooltip-lines");
            tmr.lolilabs.dimensions.DimensionsChecks.tooltipPreview = false;
            tmr.lolilabs.dimensions.DimensionsChecks.show(mc);
            phase = 9;
            ticks = 0;
            return;
        }
        if (phase == 9) {
            shot(mc, "dimensions-world");
            System.out.println(
                    "LOLI_LABS_UTILITY_CLIENT_OK: OG OBJ model and ModularUI; actual ore mouse insertion; synchronized EU and infinite-engine meter");
            System.exit(0);
        }
    }
}
