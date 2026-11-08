package tmr.lolilabs;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.*;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.screen.*;
import brachy.modularui.value.sync.*;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.slot.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

/** Real ModularUI slots and 64-bit synchronization, shared by server and client. */
public final class LabModularUI {
    public record SlotLayout(int storage,int x,int y,boolean output){}
    public static java.util.List<SlotLayout> slots(LabMachines.Definition d){
        var result=new java.util.ArrayList<SlotLayout>();
        if(d.kind==2){
            for(int i=0;i<d.batch*2;i++){int lane=i%d.batch;boolean out=i>=d.batch;result.add(new SlotLayout((out?12:0)+lane,d.batch==1?(out?108:62):(out?106:26)+(lane%3)*18,(d.batch==1?55:d.batch==6?46:28)+(lane/(d.batch==1?1:3))*18,out));}
            for(int i=0;i<5;i++)result.add(new SlotLayout(24+i,170,28+i*18,false));
        }else for(int i=0;i<(d.kind==0?6:36);i++)result.add(new SlotLayout(i,d.kind==0?53+i*18:26+(i%9)*18,d.kind==0?93:28+(i/9)*18,false));
        return java.util.List.copyOf(result);
    }
    private static brachy.modularui.drawable.UITexture art(String id){return UITexture.builder().location(LoliLabs.MOD_ID,"textures/gui/og/"+id+".png").imageSize(256,256).build();}
    private static UITexture crop(UITexture image,int x,int y,int width,int height){return image.getSubArea(x/256F,y/256F,(x+width)/256F,(y+height)/256F);}
    private static void image(ModularPanel<?> panel,UITexture image,int x,int y,int w,int h,int u,int v,int sw,int sh){panel.child(new tmr.lolilabs.botania.GuiDecoration().pos(x,y).size(w,h).background(crop(image,u,v,sw,sh)).decoration());}
    private static String compact(long v){String[] u={"","k","M","G","T","P","E"};double n=v;int i=0;while(n>=1000&&i<u.length-1){n/=1000;i++;}return i==0?Long.toString(v):String.format(java.util.Locale.ROOT,"%.2f%s",n,u[i]);}
    public static ModularPanel<?> build(LabMachineEntity m,PanelSyncManager sync,UISettings settings){
        settings.useTheme(com.ic2ceu.icceu.mui.GuiStyle.theme());
        int kind=m.definition.kind,height=kind==0?212:kind==1?256:216;
        var p=ModularPanel.defaultPanel("loli_labs_"+m.definition.id,212,height).background(brachy.modularui.api.drawable.IDrawable.NONE);
        var solar=art("solar");var unifier=art("unifier");var bg=kind==0?solar:kind==1?unifier:art("matter_solid_t"+m.definition.tier+"_x"+m.definition.batch);
        image(p,bg,0,0,212,height,0,0,212,height);
        var branding=art("loli_energistics_elements");
        p.child(new tmr.lolilabs.botania.GuiDecoration().name("energistics_logo").pos(51,-14).size(109,12).background(branding.getSubArea(0,49/256F,109/256F,61/256F)).decoration());
        // The OG solar atlas has four old outlines; replace only the charging row.
        if(kind==0)image(p,unifier,26,88,160,24,26,108,160,28);
        var eu=new LongSyncValue(()->m.energy);var capacity=new LongSyncValue(m::capacity);var rate=new LongSyncValue(()->m.enabled?m.generation():0);var cost=new LongSyncValue(()->m.definition.kind==2?m.definition.cycleCost():0);
        var output=new LongSyncValue(()->m.outputVoltage()*m.outputPackets());sync.syncValue("packet_output",output);
        sync.syncValue("energy",eu);sync.syncValue("capacity",capacity);sync.syncValue("generation",rate);sync.syncValue("cost",cost);
        int playerY=kind==0?127:kind==1?172:131;
        sync.registerSlotGroup("machine",kind==2?m.definition.batch:kind==0?6:9,100);
        for(var cell:slots(m.definition)){
            var back=kind==0?crop(unifier,25,27,18,18):brachy.modularui.api.drawable.IDrawable.NONE;
            p.child(new ItemSlot().name("machine_"+cell.storage()).pos(cell.x()-1,cell.y()-1).size(18).background(back)
                .slot(new ModularSlot(m.inventory,cell.storage()).filter(stack->m.validSlot(cell.storage(),stack)).slotGroup("machine").accessibility(!cell.output(),true)));
        }
        p.child(SlotGroupWidget.playerInventory(false,(index,slot)->slot.background(brachy.modularui.api.drawable.IDrawable.NONE)).name("player").pos(25,playerY-1).disableSortButtons());
        int meterY=kind==0?77:kind==2?105:145;
        image(p,bg,45,meterY,131,7,45,meterY,131,7);
        var common=UITexture.builder().location(LoliLabs.MOD_ID,"textures/gui/og/gui_multi_greenhouse_common.png").imageSize(256,256).build();
        var full=new FlowingGauge(common.getSubArea(0,12/256F,129/256F,17/256F));
        p.child(new ProgressWidget().name("energy_meter").pos(46,meterY+1).size(129,5).value(new DoubleSyncValue(()->(double)m.energy/Math.max(1,m.capacity())))
            .texture(brachy.modularui.api.drawable.IDrawable.NONE,full,ProgressDrawable.Direction.RIGHT)
            .tooltipAutoUpdate(true).tooltipDynamic(t->{
                t.addLine(Text.lang("gui.loli_labs.meter_stored",eu.getLongValue(),capacity.getLongValue()));
                t.addLine(kind==2?Text.lang("gui.loli_labs.meter_cost",cost.getLongValue(),m.definition.batch):Text.lang("gui.loli_labs.meter_rate",rate.getLongValue()));
            }));
        p.child(new tmr.lolilabs.botania.GuiDecoration().name("energy_icon").pos(kind==2?16:35,meterY).size(5,7).background(common.getSubArea(134/256F,15/256F,139/256F,22/256F)).setEnabledIf(w->eu.getLongValue()>0).decoration());
        if(kind==2){
            var preview=new ItemStack(LabItems.CATALOG.get(m.definition.matter).get());
            p.child(new ItemDrawable(preview).asWidget().name("matter_preview").pos(26,103).size(16).tooltip(t->t.addLine(preview.m_41786_())));
            
        }else{
            int y=kind==0?32:108;
            p.child(new TextWidget<>(()->Component.m_237110_("gui.loli_labs.energy",compact(eu.getLongValue()),compact(capacity.getLongValue()))).color(0xffe5fcff).pos(29,y).size(170,10));
            p.child(new TextWidget<>(()->Component.m_237110_("gui.loli_labs.generation",compact(rate.getLongValue()))).color(0xffe5fcff).pos(29,y+13).size(170,10));
            if(kind==0)p.child(new TextWidget<>(()->Component.m_237110_("gui.loli_labs.max_output",compact(output.getLongValue()))).color(0xffe5fcff).pos(29,y+26).size(170,10));
        }
        p.child(LabPowerToggle.create(new BooleanSyncValue(()->m.enabled,value->{m.enabled=value;m.syncSettings();}).allowC2S()).name("power_toggle").pos(213,16).size(18,13));
        p.child(new Widget<>().name("title_tooltip").pos(24,0).size(164,16).tooltip(t->t.addLine(m.m_5446_())));
        return p.excludeAreaInRecipeViewer();
    }
}
