package tmr.lolilabs;

import net.minecraft.resources.ResourceLocation;
import org.zeith.solarflux.block.SolarPanelBlock;
import org.zeith.solarflux.panels.SolarPanel;

public final class SparklingPanel extends SolarPanel {
    public SparklingPanel(String name, SolarPanelData data, Boolean custom) {
        super(name, data, custom);
    }

    @Override
    protected SolarPanelBlock createBlock() {
        // Preserve the original registry ID so existing worlds retain their panels.
        return new SparklingBlock(this, new ResourceLocation("solarflux", "sp_custom_sparkling"));
    }
}
