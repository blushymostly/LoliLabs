package tmr.lolilabs.integration.jade;

import tmr.lolilabs.energistics.LabMachineBlock;
import tmr.lolilabs.energistics.LabMachineEntity;
import tmr.lolilabs.magically.GreenhouseBlockEntity;
import tmr.lolilabs.magically.GreenhouseCoreBlock;
import tmr.lolilabs.magically.GreenhousePartBlock;
import tmr.lolilabs.utility.FortuneBlock;
import tmr.lolilabs.utility.FortuneEntity;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/** Jade hooks for Loli Labs machines — flowing EU meter matching in-GUI animation. */
@WailaPlugin
public final class LabJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(LabMachineEnergyProvider.INSTANCE, LabMachineEntity.class);
        registration.registerBlockDataProvider(LabMachineWorkableProvider.INSTANCE, LabMachineEntity.class);
        registration.registerBlockDataProvider(LabMachineEnergyProvider.INSTANCE, FortuneEntity.class);
        registration.registerBlockDataProvider(LabMachineWorkableProvider.INSTANCE, FortuneEntity.class);
        registration.registerBlockDataProvider(LabMachineEnergyProvider.INSTANCE, GreenhouseBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(LabMachineEnergyProvider.INSTANCE, LabMachineBlock.class);
        registration.registerBlockComponent(LabMachineWorkableProvider.INSTANCE, LabMachineBlock.class);
        registration.registerBlockComponent(LabMachineEnergyProvider.INSTANCE, FortuneBlock.class);
        registration.registerBlockComponent(LabMachineWorkableProvider.INSTANCE, FortuneBlock.class);
        registration.registerBlockComponent(LabMachineEnergyProvider.INSTANCE, GreenhouseCoreBlock.class);
        registration.registerBlockComponent(LabMachineEnergyProvider.INSTANCE, GreenhousePartBlock.class);
    }
}
