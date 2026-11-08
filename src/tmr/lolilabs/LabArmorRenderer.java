package tmr.lolilabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class LabArmorRenderer extends Model {

    private record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {}

    private static final Map<String, Map<String, List<Vertex[]>>> CACHE = new HashMap<>();
    private final HumanoidModel<?> humanoid;
    private final EquipmentSlot slot;
    private final Map<String, List<Vertex[]>> mesh;

    private LabArmorRenderer(HumanoidModel<?> humanoid, EquipmentSlot slot, String texture) {
        super(RenderType::m_110458_);
        this.humanoid = humanoid;
        this.slot = slot;
        String model = texture.startsWith("king") ? "king" : texture;
        if (!model.equals("king") && !model.equals("gold"))
            model += slot == EquipmentSlot.HEAD ? "head" : slot == EquipmentSlot.CHEST ? "body" : "leg";
        this.mesh = CACHE.computeIfAbsent(model, LabArmorRenderer::load);
    }

    public static void initialize(Consumer<IClientItemExtensions> consumer, String texture) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public Model getGenericArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                                              HumanoidModel<?> original) {
                return new LabArmorRenderer(original, slot, texture);
            }
        });
    }

    private static Map<String, List<Vertex[]>> load(String name) {
        Map<String, List<Vertex[]>> result = new HashMap<>();
        List<float[]> positions = new ArrayList<>(), uv = new ArrayList<>(), normals = new ArrayList<>();
        String group = "";
        ResourceLocation location = new ResourceLocation(LoliLabs.MOD_ID, "models/worn/" + name + ".obj");
        try (var reader = Minecraft.m_91087_().m_91098_().m_215597_(location)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.trim().split("\\s+");
                if (values.length < 2) continue;
                switch (values[0]) {
                    case "v", "vt", "vn" -> {
                        float[] data = new float[values.length - 1];
                        for (int i = 0; i < data.length; i++) data[i] = Float.parseFloat(values[i + 1]);
                        (values[0].equals("v") ? positions : values[0].equals("vt") ? uv : normals).add(data);
                    }
                    case "g", "o" -> group = values[1].toLowerCase();
                    case "f" -> {
                        List<Vertex> polygon = new ArrayList<>();
                        for (int i = 1; i < values.length; i++) {
                            String[] indices = values[i].split("/", -1);
                            float[] pos = positions.get(index(indices[0], positions.size()));
                            float[] tex = indices.length > 1 && !indices[1].isEmpty() ?
                                    uv.get(index(indices[1], uv.size())) : new float[] { 0, 0 };
                            float[] normal = indices.length > 2 && !indices[2].isEmpty() ?
                                    normals.get(index(indices[2], normals.size())) : new float[] { 0, 1, 0 };
                            polygon.add(new Vertex(pos[0], -pos[1], -pos[2], tex[0], 1 - tex[1], normal[0], -normal[1],
                                    -normal[2]));
                        }
                        var faces = result.computeIfAbsent(group, ignored -> new ArrayList<>());
                        if (polygon.size() == 4) faces.add(polygon.toArray(Vertex[]::new));
                        else for (int i = 1; i < polygon.size() - 1; i++) faces.add(new Vertex[] { polygon.get(0),
                                polygon.get(i), polygon.get(i + 1), polygon.get(i + 1) });
                    }
                }
            }
        } catch (IOException error) {
            throw new IllegalStateException("Cannot load legacy armor " + location, error);
        }
        return result;
    }

    private static int index(String value, int size) {
        int result = Integer.parseInt(value);
        return result > 0 ? result - 1 : size + result;
    }

    @Override
    public void m_7695_(PoseStack pose, VertexConsumer buffer, int light, int overlay, float red, float green,
                        float blue, float alpha) {
        for (var entry : mesh.entrySet()) {
            String group = entry.getKey();
            ModelPart part;
            if (group.contains("head") && slot == EquipmentSlot.HEAD) part = humanoid.f_102808_;
            else if (group.contains("body") && slot == EquipmentSlot.CHEST) part = humanoid.f_102810_;
            else if (group.contains("leftarm") && slot == EquipmentSlot.CHEST) part = humanoid.f_102812_;
            else if (group.contains("rightarm") && slot == EquipmentSlot.CHEST) part = humanoid.f_102811_;
            else if (group.contains("leftleg") && (slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET))
                part = humanoid.f_102814_;
            else if (group.contains("rightleg") && (slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET))
                part = humanoid.f_102813_;
            else continue;
            pose.m_85836_();
            part.m_104299_(pose);
            var matrices = pose.m_85850_();
            for (Vertex[] face : entry.getValue()) for (Vertex vertex : face) {
                buffer.m_252986_(matrices.m_252922_(), vertex.x, vertex.y, vertex.z)
                        .m_85950_(red, green, blue, alpha).m_7421_(vertex.u, vertex.v)
                        .m_85969_(overlay).m_86008_(light)
                        .m_252939_(matrices.m_252943_(), vertex.nx, vertex.ny, vertex.nz).m_5752_();
            }
            pose.m_85849_();
        }
    }
}
