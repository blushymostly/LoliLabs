package tmr.lolilabs.botania;

import tmr.lolilabs.LoliLabs;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.screen.*;
import brachy.modularui.value.sync.*;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.slot.*;

/** OG layout: eight generating flowers, 32 consumable-resource inputs, exactly four upgrades. */
public final class GreenhouseUI {

    /** Working window ends before the OG atlas strip at y=302 (slot hint sprites). */
    public static final int WIDTH = 242, HEIGHT = 302;

    public record Cell(int x, int y) {}

    public static Cell flowerCell(int index) {
        return new Cell(49 + (index % 8) * 18, 59 + ((index % 32) / 8) * 18);
    }

    public static Cell seedCell(int index) {
        return new Cell(49 + (index % 8) * 18, 32);
    }

    public static Cell upgradeCell(int index) {
        if (index < 8) return new Cell(246 + (index % 4) * 18, 64 + (index / 4) * 18);
        return switch (index) {
            case 8 -> new Cell(54, 146);
            case 9 -> new Cell(92, 149);
            case 10 -> new Cell(132, 149);
            default -> new Cell(170, 146);
        };
    }

    private static UITexture crop(UITexture image, int x, int y, int w, int h) {
        return image.getSubArea(x / 340F, y / 340F, (x + w) / 340F, (y + h) / 340F);
    }

    public static ModularPanel<?> build(GreenhouseBlockEntity be, PanelSyncManager sync, UISettings settings) {
        settings.useTheme(com.ic2ceu.icceu.mui.GuiStyle.theme());
        var art = UITexture.builder().location(LoliLabs.MOD_ID, "textures/gui/og/gui_multi_greenhouse.png")
                .imageSize(340, 340).build();
        var elements = UITexture.builder()
                .location(LoliLabs.MOD_ID, "textures/gui/og/gui_multi_greenhouse_elements.png").imageSize(320, 320)
                .build();
        var common = UITexture.builder().location(LoliLabs.MOD_ID, "textures/gui/og/gui_multi_greenhouse_common.png")
                .imageSize(256, 256).build();
        var activeLine = elements.getSubArea(0, 0, 1 / 320F, 1 / 320F);
        var panel = ModularPanel.defaultPanel("loli_labs_multi_greenhouse", WIDTH, HEIGHT).background(IDrawable.NONE);
        panel.child(new GuiDecoration().pos(0, 0).size(WIDTH, HEIGHT).background(crop(art, 0, 0, WIDTH, HEIGHT))
                .decoration());
        panel.child(new GuiDecoration().name("magically_logo").pos(72, -14).size(98, 12)
                .background(common.getSubArea(0, 61 / 256F, 98 / 256F, 73 / 256F)).decoration());
        var enabled = new BooleanSyncValue(() -> be.domain.enabled, v -> {
            be.domain.enabled = v;
            be.syncSettings();
        }).allowC2S();
        var mana = new IntSyncValue(() -> be.domain.mana);
        var manaCap = new IntSyncValue(() -> be.domain.manaCap);
        var energy = new LongSyncValue(() -> be.domain.energy);
        var energyCap = new LongSyncValue(() -> be.domain.energyCap);
        sync.syncValue("enabled", enabled);
        sync.syncValue("mana", mana);
        sync.syncValue("manaCap", manaCap);
        sync.syncValue("energy", energy);
        sync.syncValue("energyCap", energyCap);
        var installed = new IntSyncValue(() -> upgradeMask(be));
        sync.syncValue("installed_upgrades", installed);
        var activationTimes = new LongSyncValue[4];
        for (int i = 0; i < 4; i++) {
            final int slot = i;
            activationTimes[i] = new LongSyncValue(() -> be.domain.upgradeInstalledAt[slot]);
            sync.syncValue("upgrade_installed_at_" + i, activationTimes[i]);
        }
        var activation = new GreenhouseGauge(installed::getIntValue, i -> activationTimes[i].getLongValue(),
                () -> be.labLevel() == null ? 0 : be.labLevel().m_46467_(),
                common.getSubArea(0, 17 / 256F, 129 / 256F, 22 / 256F));
        panel.child(new GuiDecoration().name("upgrade_activation").pos(33, 30).size(176, 117).background(activation)
                .decoration());
        sync.registerSlotGroup("flowers", 8, 100);
        sync.registerSlotGroup("resources", 8, 90);
        sync.registerSlotGroup("greenhouse_upgrades", 1, 200);
        for (int i = 0; i < 8; i++) {
            final int storage = i;
            var cell = seedCell(i);
            panel.child(new GuiDecoration().name("seed_hint_" + i).pos(cell.x() + 1, cell.y() + 1).size(16)
                    .background(crop(art, 0, 302, 16, 16))
                    .setEnabledIf(w -> be.inventory.getStackInSlot(storage).m_41619_()).decoration());
            panel.child(new ItemSlot().name("seed_" + i).pos(cell.x(), cell.y()).size(18).background(IDrawable.NONE)
                    .slot(new ModularSlot(be.inventory, storage)
                            .filter(stack -> be.inventory.isItemValid(storage, stack)).slotGroup("flowers"))
                    .tooltip(t -> t.addLine(Text.lang("tooltip.loli_labs.greenhouse.flowers"))));
        }
        for (int i = 0; i < 32; i++) {
            final int outputStorage = 8 + i;
            var cell = flowerCell(i);
            panel.child(new GuiDecoration().name("output_hint_" + i).pos(cell.x() + 1, cell.y() + 1).size(16)
                    .background(crop(art, 32, 302, 16, 16))
                    .setEnabledIf(w -> be.inventory.getStackInSlot(outputStorage).m_41619_()).decoration());
            panel.child(
                    new ItemSlot().name("crop_output_" + i).pos(cell.x(), cell.y()).size(18).background(IDrawable.NONE)
                            .slot(new ModularSlot(be.inventory, 8 + i)
                                    .filter(stack -> be.inventory.isItemValid(outputStorage, stack))
                                    .slotGroup("resources")));
        }
        for (int i = 8; i < 12; i++) {
            final int index = i;
            var cell = upgradeCell(i);
            var kind = GreenhouseUpgrade.kindForSlot(i);
            var level = new IntSyncValue(() -> be.upgradeLevel(index));
            sync.syncValue("upgrade_presence_" + i, level);
            panel.child(new ItemSlot().name("upgrade_" + i).pos(cell.x(), cell.y()).size(18).background(IDrawable.NONE)
                    .slot(new ModularSlot(be.inventory, GreenhouseBlockEntity.UPGRADE_BASE + i)
                            .filter(stack -> be.validUpgrade(index, stack)).slotGroup("greenhouse_upgrades"))
                    .tooltip(new GreenhouseUpgradeTooltip(index)));
        }
        panel.child(SlotGroupWidget.playerInventory(false, (index, slot) -> slot.background(IDrawable.NONE))
                .name("player").pos(39, 218).disableSortButtons());
        var manaFill = new tmr.lolilabs.FlowingGauge(common.getSubArea(0, 17 / 256F, 129 / 256F, 22 / 256F));
        var energyFill = new tmr.lolilabs.FlowingGauge(common.getSubArea(0, 12 / 256F, 129 / 256F, 17 / 256F));
        panel.child(new GuiDecoration().name("mana_icon").pos(51, 175).size(5, 7)
                .background(common.getSubArea(134 / 256F, 22 / 256F, 139 / 256F, 29 / 256F))
                .setEnabledIf(w -> mana.getIntValue() > 0).decoration());
        panel.child(new GuiDecoration().name("energy_icon").pos(51, 189).size(5, 7)
                .background(common.getSubArea(134 / 256F, 15 / 256F, 139 / 256F, 22 / 256F))
                .setEnabledIf(w -> energy.getLongValue() > 0).decoration());
        panel.child(new ProgressWidget().name("mana_meter").pos(61, 176).size(129, 5)
                .value(new DoubleSyncValue(() -> (double) be.domain.mana / Math.max(1, be.domain.manaCap)))
                .texture(IDrawable.NONE, manaFill, ProgressDrawable.Direction.RIGHT)
                .tooltipAutoUpdate(true).tooltipDynamic(t -> t.addLine(
                        Text.lang("gui.loli_labs.greenhouse.mana", mana.getIntValue(), manaCap.getIntValue()))));
        panel.child(new ProgressWidget().name("energy_meter").pos(61, 190).size(129, 5)
                .value(new DoubleSyncValue(() -> (double) be.domain.energy / Math.max(1, be.domain.energyCap)))
                .texture(IDrawable.NONE, energyFill, ProgressDrawable.Direction.RIGHT)
                .tooltipAutoUpdate(true).tooltipDynamic(t -> t.addLine(
                        Text.lang("gui.loli_labs.greenhouse.eu", energy.getLongValue(), energyCap.getLongValue()))));
        panel.child(tmr.lolilabs.LabPowerToggle.create(enabled).name("power").pos(244, 16).size(18, 13));
        if (Boolean.getBoolean("loli_labs.verifyWorld")) {
            var preview = new GreenhouseUpgradeTooltip(8);
            class Preview extends Widget<Preview> {

                private boolean hooked;

                @Override
                public boolean canHover() {
                    return false;
                }

                @Override
                public void drawForeground(brachy.modularui.screen.viewport.ModularGuiContext context) {
                    if (!hooked) {
                        hooked = true;
                        context.postRenderCallbacks.add(ctx -> {
                            if (BotaniaWorldChecks.tooltipPreview) preview.preview(ctx);
                            if (BotaniaWorldChecks.itemTooltipPreview) ctx.getGraphics().m_280153_(ctx.getFont(),
                                    new net.minecraft.world.item.ItemStack(LabBotaniaBlocks.ITEM_CATALOG
                                            .get("greenhouse_upgrade_mana_buffer_lvl_2").get()),
                                    35, 70);
                        });
                    }
                }
            }
            panel.child(new Preview().pos(0, 0).size(1).decoration());
        }
        return panel.excludeAreaInRecipeViewer();
    }

    public static int upgradeMask(GreenhouseBlockEntity be) {
        int flags = 0;
        for (int i = 0; i < 4; i++) if (be.activeUpgrade(i + 8) != null) flags |= 1 << i;
        return flags;
    }

    private GreenhouseUI() {}
}
