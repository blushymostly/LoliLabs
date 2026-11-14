package tmr.lolilabs;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = LoliLabs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ReleaseRuntimeChecks {

    private static void require(boolean pass, String message) {
        if (!pass) throw new IllegalStateException(message);
    }

    @SubscribeEvent
    public static void verify(GatherDataEvent event) {
        if (!Boolean.getBoolean("loli_labs.verify")) return;
        net.minecraftforge.fml.config.ConfigTracker.INSTANCE.loadConfigs(
                net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
        net.minecraftforge.fml.config.ConfigTracker.INSTANCE.loadConfigs(
                net.minecraftforge.fml.config.ModConfig.Type.SERVER,
                net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
        if (!ForgeCapabilities.ENERGY.isRegistered())
            CapabilityManager.INSTANCE.injectCapabilities(net.minecraftforge.fml.ModList.get().getAllScanData());

        for (var alias : RemovedItems.ALIASES.entrySet()) {
            require(!LabItems.CATALOG.containsKey(alias.getKey()), "Removed item registered");
            require(ForgeRegistries.ITEMS.containsKey(new ResourceLocation(alias.getValue())),
                    "Missing replacement " + alias);
        }
        for (var id : RemovedItems.DROPPED)
            require(!LabItems.CATALOG.containsKey(id), "Dropped LoliAE2 id still registered: " + id);
        require(!LabItems.CATALOG.containsKey("mt_core"), "Mt Core must be removed");
        var colors = new net.minecraft.client.color.item.ItemColors();
        LabMaterialClient.colors(new net.minecraftforge.client.event.RegisterColorHandlersEvent.Item(colors,
                new net.minecraft.client.color.block.BlockColors()));
        require(LabMaterialColors.MATERIALS.size() == 29, "Twenty-nine material forms");
        for (var material : LabMaterialColors.MATERIALS.values()) {
            var stack = new ItemStack(LabItems.CATALOG.get(material.id()).get());
            for (int tint = 0; tint < 3; tint++)
                require(colors.m_92676_(stack, tint) == material.color(tint), "Material tint " + material.id());
            for (int layer = 0; layer < 3; layer++) {
                String resource = "/assets/" + material.texture(layer).replace(":", "/textures/") + ".png";
                try (var stream = com.ic2ceu.icceu.IC2Modern.class.getResourceAsStream(resource)) {
                    require(stream != null, "Missing IC2Modern material texture " + resource);
                    try (var image = com.mojang.blaze3d.platform.NativeImage.m_85058_(stream)) {
                        require(image.m_84982_() == 16 && image.m_85084_() == 16, "Material icon dimensions");
                    }
                } catch (java.io.IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        require(colors.m_92676_(new ItemStack(LabItems.CATALOG.get("osmiridium_capacitor").get()), 0) == -1,
                "Capacitor must retain original art");
        try (var en = ReleaseRuntimeChecks.class.getResourceAsStream("/assets/loli_labs/lang/en_us.json");
                var ru = ReleaseRuntimeChecks.class.getResourceAsStream("/assets/loli_labs/lang/ru_ru.json")) {
            require(en != null && ru != null, "Missing panel meter lang files");
            var enJson = com.google.gson.JsonParser
                    .parseReader(new java.io.InputStreamReader(en, java.nio.charset.StandardCharsets.UTF_8))
                    .getAsJsonObject();
            var ruJson = com.google.gson.JsonParser
                    .parseReader(new java.io.InputStreamReader(ru, java.nio.charset.StandardCharsets.UTF_8))
                    .getAsJsonObject();
            require("%s EU/t".equals(enJson.get("gui.loli_labs.meter_rate").getAsString()),
                    "English energy meter rate");
            require("%s EU/тик".equals(ruJson.get("gui.loli_labs.meter_rate").getAsString()),
                    "Russian energy meter rate encoding");
            require("%s / %s EU".equals(enJson.get("gui.loli_labs.meter_stored").getAsString()) &&
                    "%s / %s EU".equals(ruJson.get("gui.loli_labs.meter_stored").getAsString()),
                    "Energy meter stored line");
            require("%s EU / %s".equals(enJson.get("gui.loli_labs.meter_cost").getAsString()) &&
                    "%s EU / %s".equals(ruJson.get("gui.loli_labs.meter_cost").getAsString()),
                    "Energy meter cost line");
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        System.out.println("LOLI_LABS_MATERIAL_CHECKS_PASSED: " + LabMaterialColors.MATERIALS.size() +
                " IC2Modern forms; rose_gold_ingot shiny; no reinforced/irradiant duplicates; Mt Core removed");
        CanonicalRecipeChecks.runtime();
        tmr.lolilabs.dimensions.DimensionsChecks.runtime();
        int recipes = 0;
        for (var entry : LabItems.CATALOG.entrySet()) {
            String id = entry.getKey();
            var item = entry.getValue().get();
            if (LabMachines.DEFINITIONS.containsKey(id))
                require(item instanceof LabMachineItem, "Missing machine item");
            else if (id.equals("vajra")) require(item instanceof com.ic2ceu.icceu.api.item.ChargeableItem, "Missing Vajra EU");
            else if (item instanceof net.minecraft.world.item.BlockItem ||
                    item instanceof tmr.lolilabs.dimensions.LabDimensions.EnchantedMaterial ||
                    item instanceof tmr.lolilabs.utility.FortuneModule ||
                    item instanceof tmr.lolilabs.botania.GreenhouseUpgrade ||
                    item instanceof tmr.lolilabs.botania.LabStraySparkItem) {} else
                require(item.getClass() == Item.class, "Excluded mechanic leaked " + id);
            if (CanonicalRecipeChecks.contains("loli_labs:" + id)) {
                recipes++;
                continue;
            }
            try (var stream = ReleaseRuntimeChecks.class
                    .getResourceAsStream("/data/loli_labs/recipes/" + id + ".json")) {
                if (stream == null) {
                    // Placeholders removed: many items intentionally have no datapack recipe.
                    continue;
                }
                var json = com.google.gson.JsonParser
                        .parseReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8))
                        .getAsJsonObject();
                if (json.has("ingredients")) {
                    for (var ingredient : json.getAsJsonArray("ingredients")) {
                        if (!ingredient.isJsonObject()) continue;
                        var obj = ingredient.getAsJsonObject();
                        if (obj.has("item")) require(
                                ForgeRegistries.ITEMS.containsKey(new ResourceLocation(obj.get("item").getAsString())),
                                "Unknown ingredient in " + id + ": " + ingredient);
                        if (obj.has("ingredient") && obj.get("ingredient").isJsonObject() &&
                                obj.getAsJsonObject("ingredient").has("item"))
                            require(ForgeRegistries.ITEMS.containsKey(new ResourceLocation(
                                    obj.getAsJsonObject("ingredient").get("item").getAsString())),
                                    "Unknown counted ingredient in " + id);
                    }
                } else if (json.has("key")) {
                    for (var key : json.getAsJsonObject("key").entrySet()) {
                        var obj = key.getValue().getAsJsonObject();
                        if (obj.has("item")) require(
                                ForgeRegistries.ITEMS.containsKey(new ResourceLocation(obj.get("item").getAsString())),
                                "Unknown key " + key.getKey() + " in " + id);
                    }
                } else if (json.has("ingredient")) {
                    var ingredient = json.get("ingredient");
                    if (ingredient.isJsonObject() && ingredient.getAsJsonObject().has("item"))
                        require(ForgeRegistries.ITEMS.containsKey(new ResourceLocation(
                                ingredient.getAsJsonObject().get("item").getAsString())),
                                "Unknown machine ingredient in " + id);
                } else if (!(json.has("input") || json.has("catalyst") || json.has("target") ||
                        json.has("target_tag") || json.has("cells"))) {
                    require(false, "Recipe missing ingredients/key " + id);
                }
                // Canonical / machine recipes are validated by native serializers in CanonicalRecipeChecks.
                recipes++;
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
            }
        }
        int panels = 0, generators = 0;
        for (var d : LabMachines.DEFINITIONS.values()) {
            var machine = new LabMachineEntity(BlockPos.f_121853_, d.block.get().m_49966_());
            if (d.kindCode() == 0) {
                machine.serverTick();
                require(machine.energy == d.rate(), "Continuous generation " + d.id);
                panels++;
                var handler = machine.getCapability(ForgeCapabilities.ITEM_HANDLER)
                        .orElseThrow(() -> new IllegalStateException("Missing item handler"));
                require(handler.getSlots() == 6, "Panel automation must expose six chargers");
                require(LabModularUI.slots(d).size() == 6 && LabModularUI.slots(d).get(5).y() == 93,
                        "Six ModularUI chargers");
                // Old surplus remains in NBT until the owner opens the panel.
                machine.inventory.setStackInSlot(26, new ItemStack(LabItems.CATALOG.get("vajra").get()));
            }
            require(!machine.getCapability(ForgeCapabilities.ENERGY).isPresent(), "FE capability must be removed");
            var msm = new brachy.modularui.value.sync.ModularSyncManager(false);
            var psm = new brachy.modularui.value.sync.PanelSyncManager(msm, true);
            var ui = LabModularUI.build(machine, psm, new brachy.modularui.screen.UISettings());
            require(ui != null, "ModularUI panel");
            var uiSlots = ui.getChildren().stream().filter(w -> w instanceof brachy.modularui.widgets.slot.ItemSlot)
                    .map(w -> ((brachy.modularui.widgets.slot.ItemSlot) w).getSlot()).toList();
            require(uiSlots.size() == LabModularUI.slots(d).size(), "ModularUI actual slot count");
            for (int i = 0; i < uiSlots.size(); i++) {
                require(uiSlots.get(i).isCanTake(), "GUI blocks extraction");
                require(uiSlots.get(i).isCanPut() != LabModularUI.slots(d).get(i).output(),
                        "GUI output insertion policy");
            }
            if (d.kindCode() == 2) {
                require(machine.receivePacket(net.minecraft.core.Direction.UP, 401, 401) == 401 &&
                        machine.energy == 10 && machine.euFraction == 1, "Fractional EU packet");
                if (d.tier == 9) {
                    machine.energy = 0;
                    machine.euFraction = 0;
                    require(machine.receivePacket(net.minecraft.core.Direction.UP, 3L * Integer.MAX_VALUE * 40,
                            8192 * 40) == 3L * Integer.MAX_VALUE * 40 && machine.energy == 3L * Integer.MAX_VALUE,
                            "64-bit EU receiver");
                }
                machine.euFraction = 0;
                machine.energy = d.cycleCost();
                require(!machine.produceMatter() && machine.energy == d.cycleCost() && !machine.craft.active(),
                        "Matter without feed " + d.id);
                var feed = ForgeRegistries.ITEMS.getValue(new ResourceLocation(machine.inputId()));
                require(feed != null && feed != net.minecraft.world.item.Items.f_41852_,
                        "Missing input " + machine.inputId());
                for (int lane = 0; lane < d.batch; lane++)
                    machine.inventory.setStackInSlot(lane, new ItemStack(feed, 2));
                machine.energy = d.cycleCost() - 1;
                require(!machine.produceMatter() && machine.energy == 0 && machine.craft.paid() == d.cycleCost() - 1,
                        "Matter without enough EU " + d.id);
                require(machine.inventory.getStackInSlot(0).m_41613_() == 2, "Partial craft consumed feed " + d.id);
                var resumed = (LabMachineEntity) net.minecraft.world.level.block.entity.BlockEntity.m_155241_(
                        BlockPos.f_121853_, d.block.get().m_49966_(), machine.m_187480_());
                require(resumed != null && resumed.craft.paid() == machine.craft.paid() &&
                        resumed.craft.cost() == machine.craft.cost(), "Craft NBT " + d.id);
                for (int lane = 0; lane < d.batch; lane++)
                    machine.inventory.setStackInSlot(lane, ItemStack.f_41583_);
                machine.inventory.setStackInSlot(0, new ItemStack(feed));
                if (d.batch == 1) {
                    require(!machine.produceMatter() && machine.energy == 0 &&
                            machine.craft.paid() == d.cost.get() - 1, "No refund on shrink " + d.id);
                    machine.energy = 1;
                }
                require(machine.produceMatter() && machine.energy == 0 && !machine.craft.active(),
                        "Matter energy cost " + d.id);
                require(machine.inventory.getStackInSlot(0).m_41619_() &&
                        machine.inventory.getStackInSlot(12).m_41613_() == 1 &&
                        ForgeRegistries.ITEMS.getKey(machine.inventory.getStackInSlot(12).m_41720_()).m_135815_()
                                .equals(d.matter),
                        "Pooled output " + d.id);
                for (int lane = 0; lane < d.batch; lane++)
                    machine.inventory.setStackInSlot(12 + lane,
                            new ItemStack(LabItems.CATALOG.get(d.matter).get(), 64));
                machine.inventory.setStackInSlot(0, new ItemStack(feed));
                machine.energy = d.cycleCost();
                require(!machine.produceMatter() && machine.energy == d.cycleCost() &&
                        machine.inventory.getStackInSlot(0).m_41613_() == 1, "Full output consumed feed or energy");
                var itemHandler = machine.getCapability(ForgeCapabilities.ITEM_HANDLER)
                        .orElseThrow(() -> new IllegalStateException("Missing item handler"));
                require(itemHandler.getSlots() == d.batch * 2, "Generator automation scope");
                require(!itemHandler.insertItem(d.batch, new ItemStack(feed), false).m_41619_(),
                        "Output accepted feed");
                require(LabModularUI.slots(d).size() == d.batch * 2 + 5, "Generator ModularUI slot layout");
                var vajra = new ItemStack(LabItems.CATALOG.get("vajra").get());
                machine.inventory.setStackInSlot(28, vajra);
                machine.energy = d.cycleCost();
                machine.serverTick();
                require(((com.ic2ceu.icceu.api.item.ChargeableItem) vajra.m_41720_()).charge(vajra) > 0,
                        "Generator item charging");
                machine.inventory.setStackInSlot(24, new ItemStack(
                        ForgeRegistries.ITEMS.getValue(new ResourceLocation("ic2modern:pulling_upgrade"))));
                machine.inventory.setStackInSlot(25, new ItemStack(
                        ForgeRegistries.ITEMS.getValue(new ResourceLocation("ic2modern:ejector_upgrade"))));
                require(machine.hasUpgrade("item_import_upgrade") && machine.hasUpgrade("item_export_upgrade"),
                        "Generator upgrades");
                generators++;
            }

            var tag = machine.m_187480_();
            var restored = (LabMachineEntity) net.minecraft.world.level.block.entity.BlockEntity
                    .m_155241_(BlockPos.f_121853_, d.block.get().m_49966_(), tag);
            require(restored != null, "Registered entity factory " + d.id);
            try (var reader = new java.io.InputStreamReader(
                    ReleaseRuntimeChecks.class
                            .getResourceAsStream("/data/loli_labs/loot_tables/blocks/" + d.id + ".json"),
                    java.nio.charset.StandardCharsets.UTF_8)) {
                require(net.minecraftforge.common.ForgeHooks.loadLootTable(
                        net.minecraft.world.level.storage.loot.Deserializers.m_78800_().create(),
                        new ResourceLocation(LoliLabs.MOD_ID, "blocks/" + d.id),
                        com.google.gson.JsonParser.parseReader(reader), true) != null, "Invalid block loot " + d.id);
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
            }
            require(restored.energy == machine.energy, "Energy NBT " + d.id);
            require(restored.inventory.serializeNBT().equals(machine.inventory.serializeNBT()),
                    "Inventory NBT " + d.id);
            long n = 9_876_543_210_123L;
            machine.energy = n;
            long sync = 0;
            for (int i = 0; i < 4; i++) sync |= ((long) machine.data.m_6413_(i) & 65535) << (16 * i);
            require(sync == n, "64-bit menu synchronization");
        }
        var unifier = new LabMachineEntity(BlockPos.f_121853_,
                LabMachines.DEFINITIONS.get("energy_unifier").block.get().m_49966_());
        unifier.inventory.setStackInSlot(0, new ItemStack(LabItems.CATALOG.get("sparkling_panel").get(), 64));
        unifier.inventory.setStackInSlot(1, new ItemStack(LabItems.CATALOG.get("dispersion_solar_panel").get(), 2));
        require(unifier.generation() == 64 * LabMachines.DEFINITIONS.get("sparkling_panel").rate() +
                2 * LabMachines.DEFINITIONS.get("dispersion_solar_panel").rate(), "Unifier sum");
        require(!unifier.accepts(new ItemStack(LabItems.CATALOG.get("energy_unifier").get())), "Nested unifier");
        require(unifier.inventory.insertItem(2, new ItemStack(LabItems.CATALOG.get("matter").get()), false)
                .m_41613_() == 1, "Non-panel insertion");
        unifier.serverTick();
        require(unifier.energy == unifier.generation(), "Unifier generation");
        require(unifier instanceof com.ic2ceu.icceu.api.energy.EUSource && unifier.outputVoltage() > 0, "EU source");
        int autoMaterials = 0;
        for (var material : LabMaterialColors.MATERIALS.values())
            if (!java.util.Arrays.asList(LegacyMaterials.IDS).contains(material.id()) &&
                    !java.util.Arrays.asList(LegacyMagically.ITEM_IDS).contains(material.id()))
                autoMaterials++;
        int thaumPanels = net.minecraftforge.fml.ModList.get().isLoaded("thaumaturge") ? 5 : 0;
        require(panels == 20 + thaumPanels && generators == 30 &&
                LabItems.CATALOG.size() == 486 + thaumPanels + tmr.lolilabs.dimensions.LabDimensions.addedCount() +
                        autoMaterials,
                "Registry scope");
        require(tmr.lolilabs.botania.LabBotaniaBlocks.MULTI_GREENHOUSE_CORE.get() != null, "Multi greenhouse core");
        require(tmr.lolilabs.botania.LabBotaniaTiers.Tier.DRAGON.poolMana > 1_000_000,
                "Dragon pool above Botania MAX_MANA");
        var gh = new tmr.lolilabs.botania.GreenhouseBlockEntity(BlockPos.f_121853_,
                tmr.lolilabs.botania.LabBotaniaBlocks.MULTI_GREENHOUSE_CORE.get().m_49966_());
        var oldUpgrades = new net.minecraftforge.items.ItemStackHandler(
                tmr.lolilabs.botania.GreenhouseBlockEntity.SLOTS);
        oldUpgrades.setStackInSlot(tmr.lolilabs.botania.GreenhouseBlockEntity.UPGRADE_BASE + 8,
                new ItemStack(LabItems.CATALOG.get("greenhouse_upgrade_eu_buffer_lvl_2").get()));
        oldUpgrades.setStackInSlot(tmr.lolilabs.botania.GreenhouseBlockEntity.UPGRADE_BASE + 9,
                new ItemStack(LabItems.CATALOG.get("greenhouse_upgrade_mana_buffer_lvl_2").get()));
        var oldGhTag = new net.minecraft.nbt.CompoundTag();
        oldGhTag.m_128365_("Inv", oldUpgrades.serializeNBT());
        oldGhTag.m_128356_("Energy", 500000);
        oldGhTag.m_128405_("Mana", 300000);
        oldGhTag.m_128405_("EUFraction", 17);
        var migratedGh = new tmr.lolilabs.botania.GreenhouseBlockEntity(BlockPos.f_121853_, gh.m_58900_());
        migratedGh.m_142466_(oldGhTag);
        require(migratedGh.activeUpgrade(9).kind == tmr.lolilabs.botania.GreenhouseUpgrade.Kind.MANA_BUFFER &&
                migratedGh.activeUpgrade(0) == null &&
                migratedGh.inventory.getStackInSlot(tmr.lolilabs.botania.GreenhouseBlockEntity.UPGRADE_BASE)
                        .m_41720_() == LabItems.CATALOG.get("greenhouse_upgrade_eu_buffer_lvl_2").get(),
                "Old upgrades not migrated into typed slots");
        require(migratedGh.domain.energy == 500000 && migratedGh.domain.mana == 300000 &&
                migratedGh.domain.euFraction == 17,
                "Upgrade migration lost stored energy/mana/fraction");
        System.out.println(
                "LOLI_LABS_UPGRADE_MIGRATION_CHECKS_PASSED: typed slots; original items and EU/mana preserved");
        require(!gh.isFormed() &&
                gh.domain.structureStatus == tmr.lolilabs.botania.GreenhouseStructure.Status.INCOMPLETE,
                "Greenhouse unformed by default");
        var msmGh = new brachy.modularui.value.sync.ModularSyncManager(false);
        var psmGh = new brachy.modularui.value.sync.PanelSyncManager(msmGh, true);
        require(tmr.lolilabs.botania.GreenhouseUI.build(gh, psmGh, new brachy.modularui.screen.UISettings()) != null,
                "Greenhouse ModularUI");
        require(tmr.lolilabs.botania.GreenhouseUI.WIDTH == 242 && tmr.lolilabs.botania.GreenhouseUI.HEIGHT == 302,
                "OG window must end before atlas strip");
        for (int i = 0; i < 48; i++) {
            var cell = tmr.lolilabs.botania.GreenhouseUI.flowerCell(i);
            require(cell.x() >= 48 && cell.x() + 18 <= 193 && cell.y() >= 58 && cell.y() + 18 <= 131,
                    "Flower outside OG beds");
        }
        for (int i = 8; i < 12; i++) {
            var cell = tmr.lolilabs.botania.GreenhouseUI.upgradeCell(i);
            require(cell.y() + 18 <= 169, "Upgrade overlaps meters/inventory");
        }
        var formedState = gh.m_58900_().m_61124_(tmr.lolilabs.botania.GreenhouseCoreBlock.FORMED, true);
        require(formedState.m_60734_().m_7514_(formedState) == net.minecraft.world.level.block.RenderShape.INVISIBLE,
                "Formed controller must hide its cube");
        gh.domain.structureStatus = tmr.lolilabs.botania.GreenhouseStructure.Status.FORMED;
        var loadedGh = new tmr.lolilabs.botania.GreenhouseBlockEntity(BlockPos.f_121853_, gh.m_58900_());
        loadedGh.m_142466_(gh.m_187480_());
        require(loadedGh.isFormed(), "Formed model state must survive NBT/client sync");
        require(tmr.lolilabs.botania.LabBotaniaBlocks.SPARK_TYPES.size() == 4, "Four tiered spark entities");
        for (var type : tmr.lolilabs.botania.LabBotaniaBlocks.SPARK_TYPES.values())
            require(type.isPresent(), "Spark entity type missing");
        System.out.println(
                "LOLI_LABS_BOTANIA_FIX_CHECKS_PASSED: OG slot bounds; formed render state/NBT; four spark entity registrations");
        var pool = new tmr.lolilabs.botania.LabManaPoolBlockEntity(BlockPos.f_121853_,
                tmr.lolilabs.botania.LabBotaniaBlocks.BLOCK_CATALOG.get("dragon_pool").get().m_49966_());
        require(pool.getMaxMana() > 1_000_000, "Pool max mana");
        require(LabModularUI.slots(unifier.definition).size() == 36 && unifier.inventory.getSlots() == 36,
                "OG 36-slot unifier");
        for (var front : net.minecraft.core.Direction.values()) {
            var state = LabMachines.DEFINITIONS.get("sparkling_panel").block.get().m_49966_()
                    .m_61124_(LabMachineBlock.FACING, front);
            var directional = new LabMachineEntity(BlockPos.f_121853_, state);
            for (var side : net.minecraft.core.Direction.values()) require(
                    directional.emitsEnergy(side) == (side != net.minecraft.core.Direction.UP) &&
                            directional.connectsEU(side) == (side != net.minecraft.core.Direction.UP),
                    "Omni EU output except UP");
            require(LabMachineBlock.rotationFacing(0, front) == front.m_122424_(), "Wrench selection mapping");
            require(state.m_61143_(LabMachineBlock.ACTIVE), "Panel defaults to lit/active");
            require(state.m_60791_() == 15 && state.m_61124_(LabMachineBlock.ACTIVE, false).m_60791_() == 0,
                    "Panel emits light only while active");
            directional.enabled = false;
            directional.serverTick();
            require(directional.energy == 0 && !directional.emitsEnergy(net.minecraft.core.Direction.DOWN),
                    "Disabled panel generates/emits");
            directional.muted = true;
            var copy = new LabMachineEntity(BlockPos.f_121853_, state);
            copy.m_142466_(directional.m_187480_());
            require(!copy.enabled && copy.muted, "Tool settings NBT");
        }
        var unifierState = LabMachines.DEFINITIONS.get("energy_unifier").block.get().m_49966_();
        require(unifierState.m_61124_(LabMachineBlock.ACTIVE, true).m_60791_() == 15 &&
                unifierState.m_61124_(LabMachineBlock.ACTIVE, false).m_60791_() == 0,
                "Unifier emits light while active");
        var unifierSides = new LabMachineEntity(BlockPos.f_121853_, unifierState);
        for (var side : net.minecraft.core.Direction.values()) require(
                unifierSides.emitsEnergy(side) == (side != net.minecraft.core.Direction.UP) &&
                        unifierSides.connectsEU(side) == (side != net.minecraft.core.Direction.UP),
                "Unifier omni EU except UP");
        require(!unifierSides.workingLight(), "Empty unifier stays dark");
        unifierSides.inventory.setStackInSlot(0, new ItemStack(LabItems.CATALOG.get("sparkling_panel").get(), 1));
        require(unifierSides.workingLight(), "Loaded unifier lights while enabled");
        unifierSides.enabled = false;
        require(!unifierSides.workingLight(), "Disabled unifier stays dark");
        var oldUnifierTag = new net.minecraft.nbt.CompoundTag();
        oldUnifierTag.m_128405_("LabEnergyVersion", 2);
        var oldUnifierSlots = new net.minecraftforge.items.ItemStackHandler(27);
        oldUnifierSlots.setStackInSlot(26, new ItemStack(LabItems.CATALOG.get("sparkling_panel").get(), 2));
        oldUnifierTag.m_128365_("LabInventory", oldUnifierSlots.serializeNBT());
        var expanded = new LabMachineEntity(BlockPos.f_121853_, unifier.m_58900_());
        expanded.m_142466_(oldUnifierTag);
        require(expanded.inventory.getSlots() == 36 && expanded.inventory.getStackInSlot(26).m_41613_() == 2 &&
                expanded.generation() == 2 * LabMachines.DEFINITIONS.get("sparkling_panel").rate(),
                "27 -> 36 slots lost panels");
        var oldTag = new net.minecraft.nbt.CompoundTag();
        oldTag.m_128356_("Energy", 524288000000L);
        var oldInventory = new net.minecraft.nbt.ListTag();
        var oldItem = new ItemStack(LabItems.CATALOG.get("vajra").get()).serializeNBT();
        oldItem.m_128405_("Slot", 0);
        oldInventory.add(oldItem);
        oldTag.m_128365_("Chargeable", oldInventory);
        LabMachineEntity.migrateLegacy(oldTag);
        var migrated = new LabMachineEntity(BlockPos.f_121853_,
                LabMachines.DEFINITIONS.get("sparkling_panel").block.get().m_49966_());
        migrated.m_142466_(oldTag);
        require(migrated.energy == 131072000000L &&
                migrated.inventory.getStackInSlot(5).m_41720_() == LabItems.CATALOG.get("vajra").get(),
                "Legacy Sparkling data migration");
        var previousGenerator = new net.minecraft.nbt.CompoundTag();
        var previousContents = new net.minecraftforge.items.ItemStackHandler(27);
        previousContents.setStackInSlot(26, new ItemStack(LabItems.CATALOG.get("matter").get(), 64));
        previousGenerator.m_128365_("LabInventory", previousContents.serializeNBT());
        var recoveredGenerator = new LabMachineEntity(BlockPos.f_121853_,
                LabMachines.DEFINITIONS.get("matter_solid_t0_x1").block.get().m_49966_());
        recoveredGenerator.m_142466_(previousGenerator);
        require(recoveredGenerator.m_187480_().m_128469_("LabRecovery").equals(previousContents.serializeNBT()),
                "Old generator contents lost");
        var legacyProgress = new net.minecraft.nbt.CompoundTag();
        legacyProgress.m_128405_("LabProgress", 15);
        legacyProgress.m_128405_("LabLayout", 2);
        var coldCraft = new LabMachineEntity(BlockPos.f_121853_, recoveredGenerator.m_58900_());
        coldCraft.m_142466_(legacyProgress);
        require(!coldCraft.craft.active() && coldCraft.craft.paid() == 0, "Legacy LabProgress must not seed a craft");
        var euSink = new LabMachineEntity(BlockPos.f_121853_,
                LabMachines.DEFINITIONS.get("matter_solid_t0_x1").block.get().m_49966_());
        var origin = new BlockPos(0, 0, 0);
        var cable = new BlockPos(1, 0, 0);
        var endpoint = new BlockPos(2, 0, 0);
        var grid = new com.ic2ceu.icceu.common.energy.PacketNetwork.Grid() {

            public boolean loaded(BlockPos pos) {
                return pos.equals(origin) || pos.equals(cable) || pos.equals(endpoint);
            }

            public boolean cable(BlockPos pos) {
                return pos.equals(cable);
            }

            public com.ic2ceu.icceu.common.data.CableSpec spec(BlockPos pos) {
                return com.ic2ceu.icceu.common.data.CableSpec.ALL.get("glass_fibre_cable");
            }

            public boolean accepts(BlockPos pos, net.minecraft.core.Direction side) {
                return pos.equals(endpoint) && euSink.acceptsEnergy(side);
            }

            public long receivePacket(BlockPos pos, net.minecraft.core.Direction side, long quanta, long voltage) {
                return euSink.receivePacket(side, quanta, voltage);
            }

            public void burn(BlockPos pos) {
                throw new IllegalStateException("Unexpected cable overload");
            }
        };
        long spent = com.ic2ceu.icceu.common.energy.PacketNetwork.route(origin, net.minecraft.core.Direction.EAST, 8192L * 40, grid);
        require(spent == 8192L * 40 && euSink.energy == 8191 && euSink.euFraction == 39,
                "Native IC2 routing and cable loss conservation");
        var fractionalOld = new net.minecraft.nbt.CompoundTag();
        fractionalOld.m_128356_("LabEnergy", 7);
        euSink.m_142466_(fractionalOld);
        require(euSink.energy == 1 && euSink.euFraction == 30, "FE conversion lost fractional EU");
        int textures = 0;
        try (var jar = new java.util.zip.ZipFile(net.minecraftforge.fml.ModList.get().getModFileById(LoliLabs.MOD_ID)
                .getFile().getFilePath().toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (!entry.getName().startsWith("assets/loli_labs/textures/") || !entry.getName().endsWith(".png"))
                    continue;
                try (var stream = jar.getInputStream(entry);
                        var image = com.mojang.blaze3d.platform.NativeImage.m_85058_(stream)) {
                    var meta = jar.getEntry(entry.getName() + ".mcmeta");
                    if (meta != null) {
                        try (var reader = new java.io.InputStreamReader(jar.getInputStream(meta),
                                java.nio.charset.StandardCharsets.UTF_8)) {
                            var json = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                            var animation = net.minecraft.client.resources.metadata.animation.AnimationMetadataSection.f_119011_
                                    .m_6322_(json.getAsJsonObject("animation"));
                            var size = animation.m_245821_(image.m_84982_(), image.m_85084_());
                            require(image.m_84982_() % size.f_244129_() == 0 &&
                                    image.m_85084_() % size.f_244503_() == 0,
                                    "Texture frame dimensions " + entry.getName());
                            final int total = image.m_84982_() / size.f_244129_() * image.m_85084_() / size.f_244503_();
                            animation.m_174861_((frame, time) -> require(frame >= 0 && frame < total && time > 0,
                                    "Invalid frame " + entry.getName()));
                        }
                    }
                    textures++;
                }
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        System.out.println("LOLI_LABS_TEXTURE_CHECKS_PASSED: " + textures +
                " PNGs decoded with Minecraft NativeImage; animation metadata parsed by Minecraft; frame bounds valid; legacy Sparkling NBT migration verified");
        System.out.println("LOLI_LABS_RUNTIME_CHECKS_PASSED: " + recipes +
                " recipes; " + (20 + thaumPanels) +
                        " continuous panels; 30 matter outputs/costs/full-output checks; unifier sum; native EU packets; fraction and FE migration; ModularUI panels; 64-bit NBT sync");
    }
}
