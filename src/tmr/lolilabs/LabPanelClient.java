package tmr.lolilabs;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * IC2Modern ToolGrid + ToolHighlight, derived from GTCEu quarter-face tool hints.
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
@Mod.EventBusSubscriber(modid = LoliLabs.MOD_ID, value = Dist.CLIENT)
public final class LabPanelClient {

    @SubscribeEvent
    public static void highlight(RenderHighlightEvent.Block event) {
        var mc = Minecraft.m_91087_();
        if (mc.f_91073_ == null || mc.f_91074_ == null) return;
        if (!(mc.f_91074_.m_21205_().m_41720_() instanceof com.ic2ceu.icceu.MachineTool tool)) return;
        var hit = event.getTarget();
        var pos = hit.m_82425_();
        if (!(mc.f_91073_.m_7702_(pos) instanceof LabMachineEntity machine)) return;
        boolean rotate = tool.wrench() && mc.f_91074_.m_6144_(), power = tool.definition.name().equals("mallet"),
                sound = tool.definition.name().equals("hammer");
        if (!rotate && !power && !sound) return;
        var face = hit.m_82434_();
        var location = hit.m_82450_();
        var selected = com.ic2ceu.icceu.ToolGrid.side(face, location.f_82479_ - pos.m_123341_(),
                location.f_82480_ - pos.m_123342_(), location.f_82481_ - pos.m_123343_());
        var pose = event.getPoseStack();
        var camera = event.getCamera().m_90583_();
        pose.m_85836_();
        try {
            pose.m_85837_(pos.m_123341_() - camera.f_82479_, pos.m_123342_() - camera.f_82480_,
                    pos.m_123343_() - camera.f_82481_);
            var lines = event.getMultiBufferSource().m_6299_(net.minecraft.client.renderer.RenderType.m_110504_());
            for (double edge : new double[] { .25, .75 }) {
                line(pose, lines, com.ic2ceu.icceu.ToolGrid.point(face, edge, 0, .012),
                        com.ic2ceu.icceu.ToolGrid.point(face, edge, 1, .012));
                line(pose, lines, com.ic2ceu.icceu.ToolGrid.point(face, 0, edge, .012),
                        com.ic2ceu.icceu.ToolGrid.point(face, 1, edge, .012));
            }
            for (double u : new double[] { .0125, .3875, .7625 })
                for (double v : new double[] { .0125, .3875, .7625 }) {
                    var center = com.ic2ceu.icceu.ToolGrid.point(face, u + .1125, v + .1125, 0);
                    var side = com.ic2ceu.icceu.ToolGrid.side(face, center.f_82479_, center.f_82480_, center.f_82481_);
                    if (rotate && side == (machine.definition.kind == 2 ?
                            machine.m_58900_().m_61143_(LabMachineBlock.FACING) : machine.outputSide()))
                        continue;
                    String icon = rotate ? "tool_front_facing_rotation" :
                            power ? (machine.enabled ? "tool_pause" : "tool_start") :
                                    (machine.muted ? "tool_sound" : "tool_mute");
                    com.ic2ceu.icceu.ToolHighlight.quad(pose, event.getMultiBufferSource(), face,
                            new ResourceLocation("ic2modern", "textures/gui/overlay/" + icon + ".png"), u, v, .225,
                            side == selected ? 255 : 68, 15728880, .015);
                }
        } finally {
            pose.m_85849_();
        }
    }

    private static void line(com.mojang.blaze3d.vertex.PoseStack pose, com.mojang.blaze3d.vertex.VertexConsumer out,
                             net.minecraft.world.phys.Vec3 a, net.minecraft.world.phys.Vec3 b) {
        double dx = a.f_82479_ - b.f_82479_, dy = a.f_82480_ - b.f_82480_, dz = a.f_82481_ - b.f_82481_,
                length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        for (var p : new net.minecraft.world.phys.Vec3[] { a, b })
            out.m_252986_(pose.m_85850_().m_252922_(), (float) p.f_82479_, (float) p.f_82480_, (float) p.f_82481_)
                    .m_6122_(120, 180, 255, 255)
                    .m_5601_((float) (dx / length), (float) (dy / length), (float) (dz / length)).m_5752_();
    }
}
