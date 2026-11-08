package tmr.lolilabs.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.*;

/** Both vanilla and tiered Botania sparks share this renderer. */
@Mixin(value=vazkii.botania.client.render.entity.BaseSparkRenderer.class,remap=false)
public abstract class SparkDepthMixin {
    @Inject(method="render",at=@At("HEAD"),cancellable=true,remap=false)
    private void loliLabs$defer(vazkii.botania.common.entity.SparkBaseEntity entity,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource buffers,int light,org.spongepowered.asm.mixin.injection.callback.CallbackInfo callback){
        if(tmr.lolilabs.botania.SparkRenderPass.defer((vazkii.botania.client.render.entity.BaseSparkRenderer)(Object)this,entity,yaw,partial,pose,light))callback.cancel();
    }
    @Redirect(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/MultiBufferSource;m_6299_(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;",remap=false),remap=false)
    private VertexConsumer loliLabs$sparkDepth(MultiBufferSource buffers,RenderType type){return buffers.m_6299_(type==vazkii.botania.client.core.helper.RenderHelper.SPARK?tmr.lolilabs.botania.LabSparkRenderType.SPARK:type);}
}
