package tmr.lolilabs.magically;

import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.*;

public final class GreenhouseStructure {

    public enum Status {
        FORMED,
        INCOMPLETE,
        UNLOADED
    }

    public record Match(Status status, List<BlockPos> parts) {

        public boolean formed() {
            return status == Status.FORMED;
        }
    }

    public enum Role {
        CORE,
        BASE,
        MANABRICKS,
        MANAGLASS
    }

    public static Map<BlockPos, Role> relativePattern() {
        var map = new LinkedHashMap<BlockPos, Role>();
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            map.put(new BlockPos(dx, -1, dz), Role.BASE);
        }
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            if (dx == 0 && dz == 0) continue;
            map.put(new BlockPos(dx, 0, dz), Role.MANAGLASS);
        }
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            map.put(new BlockPos(dx, 1, dz), Role.MANABRICKS);
        }
        return Map.copyOf(map);
    }

    private static Role roleOf(Block block) {
        if (block == LabBotaniaBlocks.MULTI_GREENHOUSE_BASE.get()) return Role.BASE;
        if (block == LabBotaniaBlocks.DARK_MANABRICKS.get()) return Role.MANABRICKS;
        if (block == LabBotaniaBlocks.MANAGLASS.get()) return Role.MANAGLASS;
        return null;
    }

    private static BlockPos offset(BlockPos origin, BlockPos rel) {
        return origin.m_7918_(rel.m_123341_(), rel.m_123342_(), rel.m_123343_());
    }

    public static Match match(GreenhouseBlockEntity controller) {
        var level = controller.labLevel();
        if (level == null) return new Match(Status.UNLOADED, List.of());
        return raw(level, controller.labPos());
    }

    private static Match raw(Level level, BlockPos origin) {
        var parts = new ArrayList<BlockPos>();
        for (var entry : relativePattern().entrySet()) {
            var pos = offset(origin, entry.getKey());
            if (!level.m_46805_(pos)) return new Match(Status.UNLOADED, List.of());
            if (roleOf(level.m_8055_(pos).m_60734_()) != entry.getValue())
                return new Match(Status.INCOMPLETE, List.of());
            parts.add(pos);
        }
        return new Match(Status.FORMED, List.copyOf(parts));
    }

    public static GreenhouseBlockEntity controllerOf(GreenhouseBlockEntity part) {
        var level = part.labLevel();
        if (level == null) return null;
        var partPos = part.labPos();
        for (var entry : relativePattern().entrySet()) {
            var candidate = partPos.m_7918_(-entry.getKey().m_123341_(), -entry.getKey().m_123342_(),
                    -entry.getKey().m_123343_());
            if (!level.m_46805_(candidate)) continue;
            if (level.m_7702_(candidate) instanceof GreenhouseBlockEntity core && !core.isPart() &&
                    match(core).formed()) {
                return core;
            }
        }
        return null;
    }

    private GreenhouseStructure() {}
}
