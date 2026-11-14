package tmr.lolilabs.integration.jade;

import tmr.lolilabs.LoliLabs;
import tmr.lolilabs.api.WorkableProgressScale;
import tmr.lolilabs.api.WorkableProgressView;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public enum LabMachineWorkableProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(LoliLabs.MOD_ID, "workable");

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof WorkableProgressView workable) || !workable.hasWorkableProgress())
            return;
        float fill = workable.workableFill();
        if (fill > 0F) {
            data.m_128350_("LabWorkFill", fill);
            data.m_128405_("LabDone", workable.workableDone());
            data.m_128405_("LabTotal", workable.workableTotal());
            data.m_128379_("LabActive", workable.workableActive());
            return;
        }
        int scale = WorkableProgressScale.UI;
        data.m_128405_("LabProgress", workable.workableProgressScaled(scale));
        data.m_128405_("LabDuration", scale);
        data.m_128379_("LabActive", workable.workableActive());
        var outputs = workable.workableOutputs();
        if (!outputs.isEmpty()) {
            data.m_128365_("LabOutput", outputs.get(0).m_41739_(new CompoundTag()));
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var data = accessor.getServerData();
        if (data.m_128441_("LabWorkFill")) {
            LabJadeBars.flowing(
                    tooltip,
                    data.m_128457_("LabWorkFill"),
                    Component.m_237115_("tooltip.loli_labs.fortune.progress")
                            .m_7220_(Component.m_237113_(String.format(" (%d / %d)", data.m_128451_("LabDone"),
                                    data.m_128451_("LabTotal")))),
                    true);
            return;
        }
        if (!data.m_128441_("LabDuration")) return;
        int progress = data.m_128451_("LabProgress");
        int duration = data.m_128451_("LabDuration");
        if (duration <= 0) return;
        if (data.m_128471_("LabActive") || progress > 0) {
            Component text = duration < 20 ? Component.m_237110_("loli_labs.jade.progress_tick", progress, duration) :
                    Component.m_237110_("loli_labs.jade.progress_sec",
                            Math.round(progress / 20.0F), Math.round(duration / 20.0F));
            LabJadeBars.work(tooltip, (float) progress / (float) duration, text);
        }
        if (!data.m_128441_("LabOutput")) return;
        var stack = ItemStack.m_41712_(data.m_128469_("LabOutput"));
        if (stack.m_41619_()) return;
        tooltip.add(Component.m_237115_("loli_labs.jade.recipe_outputs"));
        IElementHelper helper = tooltip.getElementHelper();
        tooltip.add(helper.smallItem(stack));
        tooltip.append(Component.m_237113_(" ")
                .m_7220_(Component.m_237113_("[").m_7220_(stack.m_41786_().m_6881_()).m_7220_(Component.m_237113_("]"))
                        .m_130940_(ChatFormatting.WHITE)));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public int getDefaultPriority() {
        return 460;
    }
}
