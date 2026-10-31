package tmr.lolilabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraft.core.Direction;
/** Endgame adaptation of the old perpetual AE power card. */
public final class LabEternalGenerator extends LabBattery {
    public LabEternalGenerator(){super(Integer.MAX_VALUE);}
    @Override public void m_6883_(ItemStack stack,Level level,Entity owner,int slot,boolean selected) {
        if(!level.f_46443_) stack.m_41784_().m_128405_("LabEnergy",Integer.MAX_VALUE);
        super.m_6883_(stack,level,owner,slot,selected);
    }
    @Override public ICapabilityProvider initCapabilities(ItemStack stack,CompoundTag nbt) {
        return new Power();
    }
    @Override public void m_7373_(ItemStack stack,Level level,java.util.List<net.minecraft.network.chat.Component> tooltip,net.minecraft.world.item.TooltipFlag flag){
        tooltip.add(net.minecraft.network.chat.Component.m_237113_("1,000,000 FE/transfer — ∞"));
        tooltip.add(net.minecraft.network.chat.Component.m_237115_("tooltip.loli_labs.charging"));
    }
    private static final class Power implements ICapabilityProvider,IEnergyStorage {
        final LazyOptional<IEnergyStorage> cap=LazyOptional.of(()->this);
        public int receiveEnergy(int amount,boolean simulate){return 0;}
        public int extractEnergy(int amount,boolean simulate){return Math.min(1000000,Math.max(0,amount));}
        public int getEnergyStored(){return Integer.MAX_VALUE;}
        public int getMaxEnergyStored(){return Integer.MAX_VALUE;}
        public boolean canExtract(){return true;}
        public boolean canReceive(){return false;}
        public <T> LazyOptional<T> getCapability(Capability<T> requested,Direction side){return requested==ForgeCapabilities.ENERGY?cap.cast():LazyOptional.empty();}
    }
}
