package tmr.lolilabs.verify;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Cross-domain verify orchestration. Domain runners stay independent; this suite only sequences them.
 */
public final class VerifySuite {

    private VerifySuite() {}

    public static void world(ServerLevel world, BlockPos origin) {
        DimensionsChecks.world(world, origin);
        UtilityWorldChecks.run(world, origin);
    }

    public static void beginUtilityClient(Minecraft mc) {
        UtilityWorldChecks.beginClient(mc);
    }

    public static void utilityClient(Minecraft mc) throws Exception {
        UtilityWorldChecks.client(mc);
    }
}
