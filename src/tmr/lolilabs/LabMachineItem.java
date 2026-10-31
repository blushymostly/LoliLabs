package tmr.lolilabs;

import java.util.List;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.chat.Component;

public final class LabMachineItem extends BlockItem {
    private final LabMachines.Definition definition;
    public LabMachineItem(Block block,Item.Properties properties,LabMachines.Definition definition){super(block,properties);this.definition=definition;}
    @Override public void m_7373_(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag){
        super.m_7373_(stack,level,lines,flag);
        if(definition.kind==0){lines.add(Component.m_237110_("gui.loli_labs.generation",definition.rate()));lines.add(Component.m_237115_("tooltip.loli_labs.all_weather"));}
        if(definition.kind==1)lines.add(Component.m_237115_("tooltip.loli_labs.unifier"));
        if(definition.kind==2){lines.add(Component.m_237110_("gui.loli_labs.cost",definition.cycleCost(),definition.batch));lines.add(Component.m_237115_("item.loli_labs."+definition.matter));}
        var tag=stack.m_41737_("BlockEntityTag");
        if(tag!=null)lines.add(Component.m_237110_("gui.loli_labs.energy",tag.m_128454_("LabEnergy"),"—"));
    }
}
