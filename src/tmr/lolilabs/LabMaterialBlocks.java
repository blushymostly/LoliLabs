package tmr.lolilabs;

import com.ic2ceu.icceu.material.MaterialStorageBlock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

/** GT TagPrefix.block equivalents for every Loli Labs material that has an ingot. */
public final class LabMaterialBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            LoliLabs.MOD_ID);
    public static final Map<String, RegistryObject<Block>> STORAGE = new LinkedHashMap<>();

    static {
        for (var material : LabMaterialColors.MATERIALS.values()) {
            if (!"ingot".equals(material.form()) || STORAGE.containsKey(material.material())) continue;
            String id = material.material() + "_block";
            RegistryObject<Block> block = BLOCKS.register(id,
                    (java.util.function.Supplier<Block>) () -> new MaterialStorageBlock(material.primary(),
                            material.secondary()));
            STORAGE.put(material.material(), block);
            LabItems.CATALOG.put(id,
                    LabItems.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties())));
        }
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

    private LabMaterialBlocks() {}
}
