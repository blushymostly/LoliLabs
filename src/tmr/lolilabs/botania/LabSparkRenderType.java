package tmr.lolilabs.botania;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.*;

/** Translucent spark pixels must never punch their quad into the world depth buffer. */
public abstract class LabSparkRenderType extends RenderType {
    private LabSparkRenderType(){super("unused",DefaultVertexFormat.f_85820_,VertexFormat.Mode.QUADS,256,false,true,()->{},()->{});}
    public static final RenderType SPARK=m_173215_("loli_labs:spark_color_only",DefaultVertexFormat.f_85820_,VertexFormat.Mode.QUADS,256,false,true,
        CompositeState.m_110628_().m_173292_(f_173103_).m_173290_(f_110145_).m_110685_(f_110139_).m_110671_(f_110152_).m_110663_(f_110113_).m_110661_(f_110110_).m_110687_(f_110115_).m_110675_(f_110123_).m_110691_(true));
}
