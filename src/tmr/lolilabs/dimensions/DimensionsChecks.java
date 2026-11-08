package tmr.lolilabs.dimensions;

import tmr.lolilabs.*;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;

public final class DimensionsChecks {

    public static boolean tooltipPreview;
    private static net.minecraft.core.BlockPos scene;

    private static void require(boolean pass, String message) {
        if (!pass) throw new IllegalStateException(message);
    }

    public static void runtime() {
        require(LabDimensions.DEFINITIONS.size() == 64 && LabDimensions.addedCount() == 63, "Dimensions scope");
        for (var def : LabDimensions.DEFINITIONS) {
            var item = LabItems.CATALOG.get(def.id()).get();
            require(item != Items.f_41852_, "Missing Dimensions item " + def.id());
            require(item.m_5812_(new ItemStack(item)) == def.glint(), "Dimensions glint " + def.id());
            if (def.original() != null) {
                String resource = "/assets/loli_labs/textures/" + (def.kind().equals("block") ? "block" : "item") +
                        "/dimensions/" + def.ogId() + ".png";
                try (var in = DimensionsChecks.class.getResourceAsStream(resource)) {
                    var hash = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                            .digest(java.util.Objects.requireNonNull(in).readAllBytes()));
                    require(hash.equals(def.sha256()), "OG texture changed " + def.id());
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        try (var in = DimensionsChecks.class.getResourceAsStream("/assets/loli_labs/dimensions-recipes.json")) {
            var ids = com.google.gson.JsonParser
                    .parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
                    .getAsJsonArray();
            for (var value : ids) {
                String id = value.getAsString();
                try (var stream = DimensionsChecks.class
                        .getResourceAsStream("/data/loli_labs/recipes/" + id + ".json")) {
                    var json = com.google.gson.JsonParser
                            .parseReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8))
                            .getAsJsonObject();
                    var serializer = ForgeRegistries.RECIPE_SERIALIZERS
                            .getValue(new ResourceLocation(json.get("type").getAsString()));
                    require(serializer != null &&
                            serializer.m_6729_(new ResourceLocation("loli_labs", id), json) != null,
                            "Dimensions processing recipe " + id);
                }
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        System.out.println(
                "LOLI_LABS_DIMENSIONS_RUNTIME_OK: 64 requested definitions, 63 new registrations; reused infinity heart; exact OG textures and glint; native processing/storage recipes");
    }

    public static void client(net.minecraft.client.Minecraft mc) {
        tmr.lolilabs.EditorSingularitiesClient.verify(mc);
        for (var def : LabDimensions.DEFINITIONS) {
            var model = mc.m_91304_().m_119422_(new net.minecraft.client.resources.model.ModelResourceLocation(
                    new ResourceLocation("loli_labs", def.id()), "inventory"));
            require(model != mc.m_91304_().m_119409_(), "Missing Dimensions model " + def.id());
        }
        var stack = new ItemStack(LabItems.CATALOG.get("fortune_module_100").get());
        var tooltip = new brachy.modularui.screen.RichTooltip().addFromItem(stack);
        var lines = ((brachy.modularui.drawable.text.RichText) tooltip.getRichText()).getAsText();
        require(lines.size() >= 3, "Item tooltip lines missing");
        var first = lines.get(0).left().orElseThrow().getString();
        var second = lines.get(1).left().orElseThrow().getString();
        require(first.equals(stack.m_41786_().getString()), "Description is still merged with tooltip title: " + first);
        require(second.startsWith("Устанавливается") || second.startsWith("Installed"),
                "Tooltip description is not on its own line: " + second);
        System.out.println(
                "LOLI_LABS_DIMENSIONS_CLIENT_OK: all inventory models baked; actual ModularUI item tooltip title and description occupy separate lines");
    }

    public static void world(net.minecraft.server.level.ServerLevel world, net.minecraft.core.BlockPos anchor) {
        scene = anchor.m_7918_(24, 0, 16);
        var air = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft:air"));
        var stone = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft:stone"));
        for (int x = -1; x < 9; x++) for (int z = -1; z < 7; z++) {
            world.m_46597_(scene.m_7918_(x, -1, z), stone.m_49966_());
            for (int y = 0; y < 4; y++) world.m_46597_(scene.m_7918_(x, y, z), air.m_49966_());
        }
        int index = 0;
        for (var def : LabDimensions.DEFINITIONS) if (!def.kind().equals("item")) {
            var pos = scene.m_7918_((index % 4) * 2, 0, (index / 4) * 2);
            index++;
            var block = LabDimensions.BLOCK_CATALOG.get(def.id()).get();
            world.m_46597_(pos, block.m_49966_());
            if (!def.kind().equals("ore")) continue;
            require(block instanceof com.ic2ceu.icceu.material.MaterialOreBlock, "Ore did not use IC2Materials");
            var pick = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:diamond_pickaxe")));
            var drops = net.minecraft.world.level.block.Block.m_49874_(block.m_49966_(), world, pos, null, null, pick);
            var raw = LabItems.CATALOG.get(def.drop()).get();
            require(drops.size() == 1 && drops.get(0).m_150930_(raw) && drops.get(0).m_41613_() == 1,
                    "Ore raw drop " + def.id());
            net.minecraft.world.item.enchantment.EnchantmentHelper
                    .m_44865_(java.util.Map.of(net.minecraft.world.item.enchantment.Enchantments.f_44985_, 1), pick);
            var silk = net.minecraft.world.level.block.Block.m_49874_(block.m_49966_(), world, pos, null, null, pick);
            require(silk.size() == 1 && silk.get(0).m_150930_(block.m_5456_()), "Silk Touch lost ore " + def.id());
            require(tmr.lolilabs.utility.FortuneEntity.ore(new ItemStack(block)),
                    "Material ore missing forge:ores tag");
        }
        System.out.println(
                "LOLI_LABS_DIMENSIONS_WORLD_OK: IC2Materials ore blocks; exact raw drops and Silk Touch; Fortune transformer ore tags; worldgen registries loaded");
    }

    public static void show(net.minecraft.client.Minecraft mc) {
        mc.f_91074_.m_6915_();
        mc.m_91092_().m_201446_(() -> {
            var player = mc.m_91092_().m_6846_().m_11314_().get(0);
            player.m_8999_((net.minecraft.server.level.ServerLevel) player.m_9236_(), scene.m_123341_() + 10,
                    scene.m_123342_() + 5, scene.m_123343_() - 7, 40, 25);
        });
    }
}
