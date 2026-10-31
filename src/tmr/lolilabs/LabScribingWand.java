package tmr.lolilabs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
/** Thaumcraft research writing is replaced with powered Botania binding/configuration. */
public final class LabScribingWand extends vazkii.botania.common.item.WandOfTheForestItem {
    public LabScribingWand(){super(net.minecraft.ChatFormatting.AQUA,new Properties().m_41487_(1));}
    @Override public ICapabilityProvider initCapabilities(ItemStack stack,CompoundTag nbt){return new ItemPower(stack,1000000,false);}
    @Override public InteractionResult m_6225_(UseOnContext context){
        if(ItemPower.stored(context.m_43722_())<200)return InteractionResult.FAIL;
        var result=super.m_6225_(context);
        if(!context.m_43725_().f_46443_ && (result==InteractionResult.SUCCESS || result==InteractionResult.CONSUME))ItemPower.spend(context.m_43722_(),200);
        return result;
    }
}
