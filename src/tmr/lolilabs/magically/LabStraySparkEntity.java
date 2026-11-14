package tmr.lolilabs.magically;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.common.entity.ManaSparkEntity;

/** Native Botania transfer behavior; entity type preserves the tier over save/load and spawn packets. */
public final class LabStraySparkEntity extends ManaSparkEntity {

    public final LabBotaniaTiers.Tier tier;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public LabStraySparkEntity(EntityType<?> type, Level level, LabBotaniaTiers.Tier tier) {
        super((EntityType) type, level);
        this.tier = tier;
    }

    @Override
    protected Item getSparkItem() {
        return LabBotaniaBlocks.ITEM_CATALOG.get(tier.sparkId()).get();
    }

    @Override
    public ItemStack m_142340_() {
        return new ItemStack(getSparkItem());
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> m_5654_() {
        return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
    }
}
