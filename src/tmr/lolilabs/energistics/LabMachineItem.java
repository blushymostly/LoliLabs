package tmr.lolilabs.energistics;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public final class LabMachineItem extends BlockItem {

    private final LabMachines.Definition definition;

    public LabMachineItem(Block block, Item.Properties properties, LabMachines.Definition definition) {
        super(block, properties);
        this.definition = definition;
    }

    @Override
    public void m_7373_(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        super.m_7373_(stack, level, lines, flag);
        if (definition.kind().isSolar()) {
            long rate = definition.rate();
            long capacity = Math.max(1_000_000, Math.min((Long.MAX_VALUE / 40) / 15625, rate) * 15625);
            lines.add(Component.m_237110_("ic2modern.solar.day_night", com.ic2ceu.icceu.common.EnergyText.number(rate),
                    com.ic2ceu.icceu.common.EnergyText.number(rate)));
            lines.add(Component.m_237110_("ic2modern.solar.capacity",
                    com.ic2ceu.icceu.common.EnergyText.number(capacity)));
            lines.add(Component.m_237110_("ic2modern.solar.output",
                    com.ic2ceu.icceu.common.EnergyText.number(definition.voltage.get() * definition.packets.get())));
            lines.add(Component.m_237110_("ic2modern.solar.charging_slots", 6));
            lines.add(Component.m_237115_("tooltip.loli_labs.all_weather"));
        }
        if (definition.kind().isUnifier()) lines.add(Component.m_237115_("tooltip.loli_labs.unifier"));
        if (definition.kind().isMatter()) {
            lines.add(Component.m_237110_("gui.loli_labs.cost", definition.cycleCost(), definition.batch));
            lines.add(Component.m_237115_("item.loli_labs." + definition.matter));
        }
        var tag = stack.m_41737_("BlockEntityTag");
        if (tag != null) lines.add(Component.m_237110_("gui.loli_labs.energy", tag.m_128454_("LabEnergy"), "—"));
    }
}
