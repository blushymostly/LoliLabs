package tmr.lolilabs.botania;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import vazkii.botania.client.render.entity.BaseSparkRenderer;
import vazkii.botania.common.entity.SparkBaseEntity;

/** Draw translucent sparks after block-entity models, with color-only writes and normal depth testing. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="loli_labs",value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class SparkRenderPass {
    private record Draw(BaseSparkRenderer renderer,SparkBaseEntity entity,float yaw,float partial,Matrix4f pose,int light){}
    private static final java.util.List<Draw> queued=new java.util.ArrayList<>();
    private static boolean flushing;
    public static int rendered;
    public static boolean defer(BaseSparkRenderer renderer,SparkBaseEntity entity,float yaw,float partial,PoseStack pose,int light){
        if(flushing)return false;
        queued.add(new Draw(renderer,entity,yaw,partial,new Matrix4f(pose.m_85850_().m_252922_()),light));return true;
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void render(net.minecraftforge.client.event.RenderLevelStageEvent event){
        if(event.getStage()==net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_SKY){queued.clear();return;}
        if(event.getStage()!=net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_PARTICLES||queued.isEmpty())return;
        var buffer=Minecraft.m_91087_().m_91269_().m_110104_();buffer.m_109911_();
        flushing=true;
        try{for(var draw:queued){var pose=new PoseStack();pose.m_85850_().m_252922_().set(draw.pose);draw.renderer.render(draw.entity,draw.yaw,draw.partial,pose,buffer,draw.light);rendered++;}buffer.m_109912_(LabSparkRenderType.SPARK);}
        finally{flushing=false;queued.clear();}
    }
}
