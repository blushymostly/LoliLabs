package tmr.lolilabs.energistics;

import tmr.lolilabs.LoliLabs;
import tmr.lolilabs.RemovedItems;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

@Mod.EventBusSubscriber(modid = LoliLabs.MOD_ID)
public final class LegacyMappings {

    @SubscribeEvent
    public static void data(ChunkDataEvent.Load event) {
        var entries = event.getData().m_128437_("block_entities", 10);
        for (int i = 0; i < entries.size(); i++) {
            var tag = entries.m_128728_(i);
            var pos = net.minecraft.world.level.block.entity.BlockEntity.m_187472_(tag);
            if (event.getChunk().m_8055_(pos).m_60734_() instanceof LabMachineBlock block &&
                    block.definition.id.equals("sparkling_panel") && !tag.m_128461_("id").equals("loli_labs:machine")) {
                tag.m_128359_("id", "loli_labs:machine");
                LabMachineEntity.migrateLegacy(tag);
                event.getChunk().m_5604_(tag);
            }
        }
    }

    @SubscribeEvent
    public static void missing(MissingMappingsEvent event) {
        for (var m : event.getMappings(ForgeRegistries.Keys.ITEMS, LoliLabs.MOD_ID)) {
            String id = m.getKey().m_135815_();
            String target = RemovedItems.ALIASES.get(id);
            if (id.equals("mt_core") || RemovedItems.DROPPED.contains(id)) {
                m.ignore();
                continue;
            }
            if (target != null)
                m.remap(ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(target)));
        }
        for (var m : event.getMappings(ForgeRegistries.Keys.BLOCKS, LoliLabs.MOD_ID)) {
            if (RemovedItems.DROPPED.contains(m.getKey().m_135815_())) m.ignore();
        }
        for (var m : event.getMappings(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, LoliLabs.MOD_ID)) {
            if (RemovedItems.DROPPED.contains(m.getKey().m_135815_())) m.ignore();
        }
        for (var m : event.getMappings(ForgeRegistries.Keys.MENU_TYPES, LoliLabs.MOD_ID)) {
            if (RemovedItems.DROPPED.contains(m.getKey().m_135815_())) m.ignore();
        }
    }

    @SubscribeEvent
    public static void chunk(ChunkEvent.Load event) {
        if (event.getChunk() instanceof LevelChunk chunk && event.getLevel() instanceof Level level &&
                !level.f_46443_) {
            for (var old : new java.util.ArrayList<>(chunk.m_62954_().values())) {
                var state = level.m_8055_(old.m_58899_());
                if (!(old instanceof LabMachineEntity) && state.m_60734_() instanceof LabMachineBlock block &&
                        block.definition.id.equals("sparkling_panel")) {
                    var replacement = new LabMachineEntity(old.m_58899_(), state);
                    replacement.m_142466_(old.m_187480_());
                    level.m_151523_(replacement);
                    replacement.m_6596_();
                }
            }
        }
    }
}
