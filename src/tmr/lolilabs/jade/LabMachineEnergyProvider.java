package tmr.lolilabs.jade;

import tmr.lolilabs.LabMachineEntity;
import tmr.lolilabs.LoliLabs;
import tmr.lolilabs.botania.GreenhouseBlockEntity;
import tmr.lolilabs.utility.FortuneEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Locale;

/** Flowing EU storage bar for Lab / Fortune / Greenhouse machines. */
public enum LabMachineEnergyProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(LoliLabs.MOD_ID, "electric_container");

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        var be = accessor.getBlockEntity();
        if (be instanceof LabMachineEntity machine) {
            long capacity = machine.capacity();
            if (capacity <= 0) return;
            data.m_128356_("LabStored", machine.energy);
            data.m_128356_("LabCapacity", capacity);
            data.m_128356_("LabRate", machine.enabled ? machine.generation() : 0);
            if (machine.definition.kindCode() == 2) {
                data.m_128356_("LabCost", machine.definition.cycleCost());
                data.m_128405_("LabBatch", machine.definition.batch);
            }
            return;
        }
        if (be instanceof FortuneEntity fortune) {
            long capacity = fortune.capacity();
            if (capacity <= 0) return;
            data.m_128356_("LabStored", fortune.energy);
            data.m_128356_("LabCapacity", capacity);
            data.m_128379_("LabInfinite", fortune.upgrade(3));
            return;
        }
        if (be instanceof GreenhouseBlockEntity greenhouse) {
            var controller = greenhouse.core();
            if (controller == null) return;
            long capacity = controller.domain.energyCap;
            if (capacity <= 0) return;
            data.m_128356_("LabStored", controller.domain.energy);
            data.m_128356_("LabCapacity", capacity);
            data.m_128405_("LabMana", controller.domain.mana);
            data.m_128405_("LabManaCap", Math.max(1, controller.domain.manaCap));
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var data = accessor.getServerData();
        if (!data.m_128441_("LabCapacity")) return;
        long stored = data.m_128454_("LabStored");
        long capacity = data.m_128454_("LabCapacity");
        if (capacity <= 0) return;
        float fill = data.m_128471_("LabInfinite") ? 1F : (float) ((double) stored / (double) capacity);
        LabJadeBars.energy(
                tooltip,
                fill,
                Component.m_237110_("gui.loli_labs.meter_stored", format(stored), format(capacity)));
        if (data.m_128441_("LabManaCap")) {
            int mana = data.m_128451_("LabMana");
            int manaCap = data.m_128451_("LabManaCap");
            LabJadeBars.flowing(
                    tooltip,
                    (float) ((double) mana / (double) Math.max(1, manaCap)),
                    Component.m_237110_("gui.loli_labs.greenhouse.mana", mana, manaCap),
                    true);
        }
        if (data.m_128441_("LabCost")) {
            tooltip.add(Component
                    .m_237110_("gui.loli_labs.meter_cost", format(data.m_128454_("LabCost")),
                            data.m_128451_("LabBatch"))
                    .m_130940_(ChatFormatting.RED));
        } else if (data.m_128441_("LabRate") && data.m_128454_("LabRate") > 0) {
            tooltip.add(Component.m_237110_("gui.loli_labs.meter_rate", format(data.m_128454_("LabRate")))
                    .m_130940_(ChatFormatting.GREEN));
        }
    }

    private static String format(long value) {
        return String.format(Locale.US, "%,d", value);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public int getDefaultPriority() {
        return 450;
    }
}
