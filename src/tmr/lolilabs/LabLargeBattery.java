package tmr.lolilabs;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraft.core.Direction;

/** FE requests remain int-sized, but stellar storage is persisted as a 64-bit value. */
public final class LabLargeBattery extends LabBattery {
    private final long capacity;
    public LabLargeBattery(long capacity) { super(Integer.MAX_VALUE); this.capacity=capacity; }
    @Override public ICapabilityProvider initCapabilities(ItemStack stack,CompoundTag nbt) { return new Power(stack,capacity); }
    @Override public void m_6883_(ItemStack stack,Level level,Entity owner,int slot,boolean selected) {
        if (level.f_46443_ || !(owner instanceof Player player) || stack.m_41784_().m_128471_("LabChargingDisabled")) return;
        var source=stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (source==null) return;
        for (int i=0;i<player.m_150109_().m_6643_();i++) {
            var target=player.m_150109_().m_8020_(i);
            if (target==stack || target.m_41720_() instanceof LabBattery) continue;
            var destination=target.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            if (destination==null) continue;
            int offered=source.extractEnergy(100000,true);
            int accepted=destination.receiveEnergy(offered,false);
            source.extractEnergy(accepted,false);
        }
    }
    @Override public void m_7373_(ItemStack stack,Level level,java.util.List<net.minecraft.network.chat.Component> tooltip,net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component.m_237110_("tooltip.loli_labs.energy",stack.m_41784_().m_128454_("LabLongEnergy"),capacity*(1L+Math.max(0,Math.min(5,stack.m_41784_().m_128451_("LabPowerLevel"))))));
        tooltip.add(net.minecraft.network.chat.Component.m_237115_("tooltip.loli_labs.charging"));
    }
    private static final class Power implements IEnergyStorage,ICapabilityProvider {
        final ItemStack stack;final long capacity;final LazyOptional<IEnergyStorage> cap=LazyOptional.of(()->this);
        Power(ItemStack stack,long capacity){this.stack=stack;this.capacity=capacity;}
        long max(){return capacity*(1L+Math.max(0,Math.min(5,stack.m_41784_().m_128451_("LabPowerLevel"))));}
        long stored(){return Math.max(0,Math.min(max(),stack.m_41784_().m_128454_("LabLongEnergy")));}
        @Override public int receiveEnergy(int amount,boolean simulate){int actual=(int)Math.min(Math.max(0,amount),max()-stored());if(!simulate)stack.m_41784_().m_128356_("LabLongEnergy",stored()+actual);return actual;}
        @Override public int extractEnergy(int amount,boolean simulate){int actual=(int)Math.min(Math.max(0,amount),stored());if(!simulate)stack.m_41784_().m_128356_("LabLongEnergy",stored()-actual);return actual;}
        @Override public int getEnergyStored(){return (int)Math.min(Integer.MAX_VALUE,stored());}
        @Override public int getMaxEnergyStored(){return (int)Math.min(Integer.MAX_VALUE,max());}
        @Override public boolean canReceive(){return true;}
        @Override public boolean canExtract(){return true;}
        @Override public <T> LazyOptional<T> getCapability(Capability<T> requested,Direction side){return requested==ForgeCapabilities.ENERGY?cap.cast():LazyOptional.empty();}
    }
}
