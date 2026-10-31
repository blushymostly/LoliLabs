package tmr.lolilabs;

import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import mekanism.common.item.gear.ItemAtomicDisassembler;

/** Powered mining, axes/shovels, energy capability and radial modes use Mekanism's implementation. */
public final class LabDisassembler extends ItemAtomicDisassembler {
    public LabDisassembler() { super(new Properties().m_41487_(1).m_41486_()); }
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer) { }
}
