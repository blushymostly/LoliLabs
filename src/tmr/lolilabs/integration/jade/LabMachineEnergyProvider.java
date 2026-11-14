package tmr.lolilabs.integration.jade;

import tmr.lolilabs.LoliLabs;
import tmr.lolilabs.api.ForeignEnergyView;
import tmr.lolilabs.api.ManaView;

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

public enum LabMachineEnergyProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(LoliLabs.MOD_ID, "electric_container");

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        var be = accessor.getBlockEntity();
        if (!(be instanceof ForeignEnergyView energy)) return;
        long capacity = energy.foreignEuCapacity();
        if (capacity <= 0) return;
        data.m_128356_("LabStored", energy.foreignEuStored());
        data.m_128356_("LabCapacity", capacity);
        if (energy.foreignEuRate() > 0) data.m_128356_("LabRate", energy.foreignEuRate());
        if (energy.foreignEuCraftCost() > 0) {
            data.m_128356_("LabCost", energy.foreignEuCraftCost());
            data.m_128405_("LabBatch", energy.foreignEuCraftBatch());
        }
        if (energy.foreignEuInfinite()) data.m_128379_("LabInfinite", true);
        if (be instanceof ManaView mana) {
            data.m_128405_("LabMana", mana.manaStored());
            data.m_128405_("LabManaCap", mana.manaCapacity());
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
