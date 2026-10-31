package tmr.lolilabs;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.gui.screens.MenuScreens;

/** Original LoliEnergistics artwork, composed without modifying the PNGs. */
@Mod.EventBusSubscriber(modid=LoliLabs.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class LabMachineScreen extends AbstractContainerScreen<LabMachineMenu> {
    private static final ResourceLocation SOLAR=texture("solar"),UNIFIER=texture("unifier");
    public LabMachineScreen(LabMachineMenu menu,Inventory inventory,Component title){super(menu,inventory,title);f_97726_=212;f_97727_=menu.machineSlots==6?212:256;}
    @SubscribeEvent public static void setup(FMLClientSetupEvent event){event.enqueueWork(()->MenuScreens.m_96206_(LabMachines.MENU.get(),LabMachineScreen::new));}
    private static ResourceLocation texture(String name){return new ResourceLocation(LoliLabs.MOD_ID,"textures/gui/legacy/"+name+".png");}
    private int kind(){return f_97732_.data.m_6413_(17);}
    private int tier(){return Math.max(0,Math.min(9,f_97732_.data.m_6413_(19)));}
    private int batch(){int batch=f_97732_.data.m_6413_(18);return batch==6||batch==12?batch:1;}
    private ResourceLocation background(){return kind()==1?UNIFIER:kind()==2?texture("matter_solid_t"+tier()+"_x"+batch()):SOLAR;}
    private net.minecraft.world.item.ItemStack matterPreview(){return new net.minecraft.world.item.ItemStack(LabItems.CATALOG.get(LabMachineEntity.MATTER_CHAIN[tier()]).get());}
    private static void region(GuiGraphics g,ResourceLocation texture,int x,int y,int width,int height,int u,int v,int sourceWidth,int sourceHeight){
        g.m_280411_(texture,x,y,width,height,(float)u,(float)v,sourceWidth,sourceHeight,256,256);
    }
    @Override protected void m_7286_(GuiGraphics g,float partial,int mouseX,int mouseY){
        int x=f_97735_,y=f_97736_;var background=background();
        if(kind()==2){
            region(g,background,x,y,212,24,0,0,212,24);
            region(g,background,x,y+24,212,134,0,24,212,94);
            region(g,SOLAR,x,y+158,212,8,0,114,212,8);
            g.m_280509_(x+12,y+24,x+200,y+156,0xff414e62);
            for(int i=0;i<f_97732_.machineSlots;i++){
                var slot=f_97732_.f_38839_.get(i);
                region(g,UNIFIER,x+slot.f_40220_-1,y+slot.f_40221_-1,18,18,25,27,18,18);
            }
            // Output preview is decorative and cannot be taken by the player.
            region(g,UNIFIER,x+43,y+119,18,18,25,27,18,18);
            g.m_280480_(matterPreview(),x+44,y+120);
            g.m_280509_(x+102,y+61,x+112,y+64,0xffafb9c6);
            g.m_280509_(x+109,y+59,x+112,y+66,0xffafb9c6);
            region(g,UNIFIER,x+26,y+104,160,7,45,145,131,7);
            long capacity=Math.max(1,f_97732_.value(1));int fill=(int)Math.max(0,Math.min(156,156D*f_97732_.value(0)/capacity));
            if(fill>0){g.m_280509_(x+28,y+106,x+28+fill,y+109,0xff35bbb9);g.m_280509_(x+28,y+106,x+28+fill,y+107,0xffa4fff0);}
            g.m_280509_(x+43,y+143,x+181,y+155,0xff657282);g.m_280509_(x+44,y+144,x+180,y+154,0xff354152);
            for(int i=0;i<10;i++)g.m_280509_(x+110+i*6,y+147,x+114+i*6,y+150,i<=tier()?0xff35bbb9:0xff202833);
            region(g,UNIFIER,x,y+166,212,90,0,166,212,90);
            return;
        }
        if(kind()==0){
            // Solar panels use the original compact layout: stats, meter, six chargers.
            region(g,SOLAR,x,y,212,112,0,12,212,110);
            g.m_280509_(x+12,y+9,x+200,y+103,0xff414e62);
            g.m_280509_(x+17,y+17,x+195,y+63,0xff657282);
            g.m_280509_(x+18,y+18,x+194,y+62,0xff354152);
            region(g,UNIFIER,x+34,y+65,156,7,45,145,131,7);
            long capacity=Math.max(1,f_97732_.value(1));int fill=(int)Math.max(0,Math.min(152,152D*f_97732_.value(0)/capacity));
            if(fill>0){g.m_280509_(x+36,y+67,x+36+fill,y+70,0xff35bbb9);g.m_280509_(x+36,y+67,x+36+fill,y+68,0xffa4fff0);}
            for(int i=0;i<6;i++)region(g,UNIFIER,x+52+i*18,y+82,18,18,25,27,18,18);
            region(g,UNIFIER,x,y+122,212,90,0,166,212,90);
            return;
        }
        // Keep the original header/corners and extend only the frame's middle.
        if(kind()==1)region(g,background,x,y,212,166,0,0,212,166);
        else{
            int bottom=kind()==2?126:122;
            region(g,background,x,y,212,24,0,0,212,24);
            region(g,background,x,y+24,212,134,0,24,212,bottom-32);
            region(g,background,x,y+158,212,8,0,bottom-8,212,8);
        }
        // All screens share the original player's inventory at its native size.
        region(g,UNIFIER,x,y+166,212,90,0,166,212,90);
        // Reuse the unifier's dark console to make room for the full 27-slot grid.
        g.m_280509_(x+25,y+27,x+187,y+137,0xff5cadbd);
        region(g,UNIFIER,x+26,y+28,160,108,26,108,160,28);
        region(g,UNIFIER,x+26,y+137,160,7,26,108,160,28);
        ResourceLocation slots=kind()==2?texture("matter_solid_t"+tier()+"_x12"):UNIFIER;
        for(int i=0;i<27;i++){
            var slot=f_97732_.f_38839_.get(i);
            region(g,slots,x+slot.f_40220_-1,y+slot.f_40221_-1,18,18,25,27,18,18);
        }
        // The original energy meter frame; only the live fill is drawn in code.
        region(g,UNIFIER,x+26,y+145,160,7,45,145,131,7);
        long energy=f_97732_.value(0),capacity=Math.max(1,f_97732_.value(1));
        int bar=(int)Math.max(0,Math.min(156,156D*energy/capacity));
        if(bar>0){g.m_280509_(x+28,y+147,x+28+bar,y+150,0xff35bbb9);g.m_280509_(x+28,y+147,x+28+bar,y+148,0xffa4fff0);}
    }
    @Override protected void m_280003_(GuiGraphics g,int mouseX,int mouseY){
        if(kind()==2){g.m_280614_(f_96547_,Component.m_237110_("gui.loli_labs.level",tier()+1),49,146,0xffe5fcff,false);return;}
        if(kind()==0){
            g.m_280614_(f_96547_,Component.m_237110_("gui.loli_labs.energy",compact(f_97732_.value(0)),compact(f_97732_.value(1))),23,25,0xffffd34d,false);
            g.m_280614_(f_96547_,Component.m_237110_("gui.loli_labs.max_output",compact(Math.min(Integer.MAX_VALUE,f_97732_.value(1)))) ,23,39,0xffffd34d,false);
            g.m_280614_(f_96547_,Component.m_237110_("gui.loli_labs.generation",compact(f_97732_.value(2))),23,53,0xffffd34d,false);
            return;
        }
        g.m_280614_(f_96547_,Component.m_237110_("gui.loli_labs.energy",compact(f_97732_.value(0)),compact(f_97732_.value(1))),29,92,0xffe5fcff,false);
        g.m_280614_(f_96547_,kind()==2?Component.m_237110_("gui.loli_labs.cost",compact(f_97732_.value(3)),batch()):Component.m_237110_("gui.loli_labs.generation",compact(f_97732_.value(2))),29,105,0xffbbefee,false);
        var hint=Component.m_237115_(kind()==1?"gui.loli_labs.panels":kind()==2?"gui.loli_labs.output":"gui.loli_labs.charge");
        if(kind()==2)hint.m_130946_(" T"+tier()+" x"+batch());
        g.m_280614_(f_96547_,hint,29,118,0xffa2c4cf,false);
    }
    private boolean over(int mouseX,int mouseY,int x,int y,int width,int height){return mouseX>=f_97735_+x&&mouseX<f_97735_+x+width&&mouseY>=f_97736_+y&&mouseY<f_97736_+y+height;}
    private static String compact(long value){String[] units={"","k","M","G","T","P","E"};double n=value;int u=0;while(n>=1000&&u<units.length-1){n/=1000;u++;}return u==0?Long.toString(value):String.format(java.util.Locale.ROOT,"%.2f%s",n,units[u]);}
    @Override public void m_88315_(GuiGraphics g,int mouseX,int mouseY,float partial){
        m_280273_(g);super.m_88315_(g,mouseX,mouseY,partial);m_280072_(g,mouseX,mouseY);
        if(kind()==2)for(int i=0;i<f_97732_.machineSlots;i++){var slot=f_97732_.f_38839_.get(i);if(slot.m_6657_()&&over(mouseX,mouseY,slot.f_40220_,slot.f_40221_,16,16))return;}
        if(kind()==2&&over(mouseX,mouseY,43,119,18,18))g.m_280557_(f_96547_,matterPreview().m_41786_(),mouseX,mouseY);
        else if(kind()!=0&&over(mouseX,mouseY,20,0,172,16))g.m_280557_(f_96547_,f_96539_,mouseX,mouseY);
        else if(kind()==0?over(mouseX,mouseY,17,17,178,74):kind()==2?over(mouseX,mouseY,26,104,160,7)||over(mouseX,mouseY,43,143,138,12):over(mouseX,mouseY,25,89,162,65))g.m_280666_(f_96547_,List.of(f_96539_,
            Component.m_237110_("gui.loli_labs.energy",f_97732_.value(0),f_97732_.value(1)),
            kind()==2?Component.m_237110_("gui.loli_labs.cost",f_97732_.value(3),batch()):Component.m_237110_("gui.loli_labs.generation",f_97732_.value(2))),mouseX,mouseY);
        else if(kind()==2&&over(mouseX,mouseY,69,119,72,18))g.m_280557_(f_96547_,Component.m_237115_("gui.loli_labs.upgrades"),mouseX,mouseY);
        else if(kind()==2&&over(mouseX,mouseY,141,119,18,18))g.m_280557_(f_96547_,Component.m_237115_("gui.loli_labs.charge"),mouseX,mouseY);
    }
}
