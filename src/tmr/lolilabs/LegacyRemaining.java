package tmr.lolilabs;

import net.minecraft.world.item.Item;

import mekanism.api.Upgrade;
import mekanism.common.lib.transmitter.TransmissionType;

public final class LegacyRemaining {

    public static void register() {
        LabItems.CATALOG.put("seal_of_sealing", LabItems.ITEMS.register("seal_of_sealing", () -> new LabSeal()));
        LabItems.CATALOG.put("focus_energy_ball",
                LabItems.ITEMS.register("focus_energy_ball", () -> new LabManaFocus()));
        LabItems.CATALOG.put("upgrade_recycler",
                LabItems.ITEMS.register("upgrade_recycler", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("upgrade_cardiacamplifier",
                LabItems.ITEMS.register("upgrade_cardiacamplifier", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("upgrade_demineralizer",
                LabItems.ITEMS.register("upgrade_demineralizer", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("upgrade_quantum_generator",
                LabItems.ITEMS.register("upgrade_quantum_generator", () -> new LabEternalGenerator()));
        LabItems.CATALOG.put("box_nano", LabItems.ITEMS.register("box_nano", () -> new LabArmorBox("king1")));
        LabItems.CATALOG.put("box_quantum", LabItems.ITEMS.register("box_quantum", () -> new LabArmorBox("king8")));
        LabItems.CATALOG.put("electric_scribing_tool",
                LabItems.ITEMS.register("electric_scribing_tool", () -> new LabScribingWand()));
        LabItems.CATALOG.put("upgrade_gas_puller",
                LabItems.ITEMS.register("upgrade_gas_puller", () -> new LabIOUpgrade(false, TransmissionType.GAS)));
        LabItems.CATALOG.put("upgrade_gas_ejector",
                LabItems.ITEMS.register("upgrade_gas_ejector", () -> new LabIOUpgrade(true, TransmissionType.GAS)));
        LabItems.CATALOG.put("upgrade_liquid_tightener", LabItems.ITEMS.register("upgrade_liquid_tightener",
                () -> new LabIOUpgrade(false, TransmissionType.FLUID)));
        LabItems.CATALOG.put("upgrade_speed_1",
                LabItems.ITEMS.register("upgrade_speed_1", () -> new LabMachineUpgrade(Upgrade.SPEED, 2)));
        LabItems.CATALOG.put("upgrade_speed_2",
                LabItems.ITEMS.register("upgrade_speed_2", () -> new LabMachineUpgrade(Upgrade.SPEED, 4)));
        LabItems.CATALOG.put("multiply_module_1",
                LabItems.ITEMS.register("multiply_module_1", () -> new LabMachineUpgrade(Upgrade.SPEED, 2)));
        LabItems.CATALOG.put("multiply_module_2",
                LabItems.ITEMS.register("multiply_module_2", () -> new LabMachineUpgrade(Upgrade.SPEED, 4)));
        LabItems.CATALOG.put("multiply_module_3",
                LabItems.ITEMS.register("multiply_module_3", () -> new LabMachineUpgrade(Upgrade.SPEED, 8)));
        LabItems.CATALOG.put("upgrade_resource_gen0", LabItems.ITEMS.register("upgrade_resource_gen0",
                () -> new LabMachineUpgrade(Upgrade.STONE_GENERATOR, 1)));
        LabItems.CATALOG.put("upgrade_resource_gen1", LabItems.ITEMS.register("upgrade_resource_gen1",
                () -> new LabMachineUpgrade(Upgrade.STONE_GENERATOR, 2)));
        LabItems.CATALOG.put("upgrade_resource_gen2", LabItems.ITEMS.register("upgrade_resource_gen2",
                () -> new LabMachineUpgrade(Upgrade.STONE_GENERATOR, 4)));
        LabItems.CATALOG.put("upgrade_resource_gen3", LabItems.ITEMS.register("upgrade_resource_gen3",
                () -> new LabMachineUpgrade(Upgrade.STONE_GENERATOR, 8)));
        LabItems.CATALOG.put("upgrade_resource_gen4", LabItems.ITEMS.register("upgrade_resource_gen4",
                () -> new LabMachineUpgrade(Upgrade.STONE_GENERATOR, 8)));
        LabItems.CATALOG.put("upgrade_singular_buck0",
                LabItems.ITEMS.register("upgrade_singular_buck0", () -> new LabMachineUpgrade(Upgrade.ENERGY, 1)));
        LabItems.CATALOG.put("upgrade_singular_buck1",
                LabItems.ITEMS.register("upgrade_singular_buck1", () -> new LabMachineUpgrade(Upgrade.ENERGY, 2)));
        LabItems.CATALOG.put("upgrade_singular_buck2",
                LabItems.ITEMS.register("upgrade_singular_buck2", () -> new LabMachineUpgrade(Upgrade.ENERGY, 4)));
        LabItems.CATALOG.put("particle_accelerator_1",
                LabItems.ITEMS.register("particle_accelerator_1", () -> new LabMachineUpgrade(Upgrade.SPEED, 2)));
        LabItems.CATALOG.put("particle_accelerator_2",
                LabItems.ITEMS.register("particle_accelerator_2", () -> new LabMachineUpgrade(Upgrade.SPEED, 4)));
        LabItems.CATALOG.put("particle_accelerator_3",
                LabItems.ITEMS.register("particle_accelerator_3", () -> new LabMachineUpgrade(Upgrade.SPEED, 8)));
        LabItems.CATALOG.put("drill_bit_upgrade_2",
                LabItems.ITEMS.register("drill_bit_upgrade_2", () -> new LabMachineUpgrade(Upgrade.SPEED, 2)));
        LabItems.CATALOG.put("drill_bit_upgrade_3",
                LabItems.ITEMS.register("drill_bit_upgrade_3", () -> new LabMachineUpgrade(Upgrade.SPEED, 4)));
        LabItems.CATALOG.put("drill_bit_upgrade_4",
                LabItems.ITEMS.register("drill_bit_upgrade_4", () -> new LabMachineUpgrade(Upgrade.SPEED, 8)));
        LabItems.CATALOG.put("drill_bit_upgrade_5",
                LabItems.ITEMS.register("drill_bit_upgrade_5", () -> new LabMachineUpgrade(Upgrade.SPEED, 8)));
        LabItems.CATALOG.put("drill_luck_upgrade_1",
                LabItems.ITEMS.register("drill_luck_upgrade_1", () -> new LabFortuneUpgrade(1)));
        LabItems.CATALOG.put("drill_luck_upgrade_2",
                LabItems.ITEMS.register("drill_luck_upgrade_2", () -> new LabFortuneUpgrade(2)));
        LabItems.CATALOG.put("drill_luck_upgrade_3",
                LabItems.ITEMS.register("drill_luck_upgrade_3", () -> new LabFortuneUpgrade(3)));
        LabItems.CATALOG.put("copper_radiator",
                LabItems.ITEMS.register("copper_radiator", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("iridium_radiator",
                LabItems.ITEMS.register("iridium_radiator", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("thermal_insulator1",
                LabItems.ITEMS.register("thermal_insulator1", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("thermal_insulator2",
                LabItems.ITEMS.register("thermal_insulator2", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("thermal_insulator3",
                LabItems.ITEMS.register("thermal_insulator3", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("wind_rotor0",
                LabItems.ITEMS.register("wind_rotor0", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("wind_rotor1",
                LabItems.ITEMS.register("wind_rotor1", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("wind_rotor2",
                LabItems.ITEMS.register("wind_rotor2", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("wind_rotor3",
                LabItems.ITEMS.register("wind_rotor3", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("wind_rotor4",
                LabItems.ITEMS.register("wind_rotor4", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("grinding_ball0",
                LabItems.ITEMS.register("grinding_ball0", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("grinding_ball1",
                LabItems.ITEMS.register("grinding_ball1", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("grinding_ball2",
                LabItems.ITEMS.register("grinding_ball2", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_neutronium",
                LabItems.ITEMS.register("fluid_singularity_molten_neutronium", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_adamantium_dragon", LabItems.ITEMS
                .register("fluid_singularity_molten_adamantium_dragon", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_gladium",
                LabItems.ITEMS.register("fluid_singularity_molten_gladium", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_bedrockium",
                LabItems.ITEMS.register("fluid_singularity_molten_bedrockium", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_crystal_matrix", LabItems.ITEMS
                .register("fluid_singularity_molten_crystal_matrix", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_sannarium",
                LabItems.ITEMS.register("fluid_singularity_molten_sannarium", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_ametrine",
                LabItems.ITEMS.register("fluid_singularity_molten_ametrine", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_chrysolite",
                LabItems.ITEMS.register("fluid_singularity_molten_chrysolite", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_adamantium_arcane", LabItems.ITEMS
                .register("fluid_singularity_molten_adamantium_arcane", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_adamantium_ethereal", LabItems.ITEMS
                .register("fluid_singularity_molten_adamantium_ethereal", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_mobius",
                LabItems.ITEMS.register("fluid_singularity_molten_mobius", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_super_alloy",
                LabItems.ITEMS.register("fluid_singularity_molten_super_alloy", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_sebastyanit",
                LabItems.ITEMS.register("fluid_singularity_molten_sebastyanit", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_nether_star",
                LabItems.ITEMS.register("fluid_singularity_molten_nether_star", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_iridium_ore",
                LabItems.ITEMS.register("fluid_singularity_molten_iridium_ore", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_skyfiry",
                LabItems.ITEMS.register("fluid_singularity_molten_skyfiry", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_infinity_ingot", LabItems.ITEMS
                .register("fluid_singularity_molten_infinity_ingot", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_niobium",
                LabItems.ITEMS.register("fluid_singularity_molten_niobium", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_spark_matter", LabItems.ITEMS
                .register("fluid_singularity_molten_spark_matter", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_radvirium",
                LabItems.ITEMS.register("fluid_singularity_molten_radvirium", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_adamantium_ghost", LabItems.ITEMS
                .register("fluid_singularity_molten_adamantium_ghost", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_adamantium",
                LabItems.ITEMS.register("fluid_singularity_molten_adamantium", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity_molten_fluorite",
                LabItems.ITEMS.register("fluid_singularity_molten_fluorite", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("fluid_singularity",
                LabItems.ITEMS.register("fluid_singularity", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_yellow",
                LabItems.ITEMS.register("laser_focus_yellow", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_brown",
                LabItems.ITEMS.register("laser_focus_brown", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_green",
                LabItems.ITEMS.register("laser_focus_green", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_lime",
                LabItems.ITEMS.register("laser_focus_lime", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_gray",
                LabItems.ITEMS.register("laser_focus_gray", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_red",
                LabItems.ITEMS.register("laser_focus_red", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_lightblue",
                LabItems.ITEMS.register("laser_focus_lightblue", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_magenta",
                LabItems.ITEMS.register("laser_focus_magenta", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_orange",
                LabItems.ITEMS.register("laser_focus_orange", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_purple",
                LabItems.ITEMS.register("laser_focus_purple", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_cyan",
                LabItems.ITEMS.register("laser_focus_cyan", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_white",
                LabItems.ITEMS.register("laser_focus_white", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_lightgray",
                LabItems.ITEMS.register("laser_focus_lightgray", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_blue",
                LabItems.ITEMS.register("laser_focus_blue", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_black",
                LabItems.ITEMS.register("laser_focus_black", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("laser_focus_pink",
                LabItems.ITEMS.register("laser_focus_pink", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("neutron_star",
                LabItems.ITEMS.register("neutron_star", () -> new LabLargeBattery(1000000000L)));
        LabItems.CATALOG.put("charged_neutron_star",
                LabItems.ITEMS.register("charged_neutron_star", () -> new LabLargeBattery(1000000000000L)));
        LabItems.CATALOG.put("astrolion_micro_star",
                LabItems.ITEMS.register("astrolion_micro_star", () -> new LabLargeBattery(1000000000L)));
        LabItems.CATALOG.put("astrolion_core",
                LabItems.ITEMS.register("astrolion_core", () -> new LabLargeBattery(1000000000L)));
        LabItems.CATALOG.put("astrolion_dead",
                LabItems.ITEMS.register("astrolion_dead", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("sun_micro_star",
                LabItems.ITEMS.register("sun_micro_star", () -> new LabLargeBattery(100000000000L)));
        LabItems.CATALOG.put("sun_core", LabItems.ITEMS.register("sun_core", () -> new LabLargeBattery(100000000000L)));
        LabItems.CATALOG.put("sun_dead", LabItems.ITEMS.register("sun_dead", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("sirius_micro_star",
                LabItems.ITEMS.register("sirius_micro_star", () -> new LabLargeBattery(50000000000000L)));
        LabItems.CATALOG.put("sirius_core",
                LabItems.ITEMS.register("sirius_core", () -> new LabLargeBattery(50000000000000L)));
        LabItems.CATALOG.put("sirius_dead",
                LabItems.ITEMS.register("sirius_dead", () -> new Item(new Item.Properties())));
        LabItems.CATALOG.put("zodius_micro_star",
                LabItems.ITEMS.register("zodius_micro_star", () -> new LabLargeBattery(1000000000000000L)));
        LabItems.CATALOG.put("zodius_core",
                LabItems.ITEMS.register("zodius_core", () -> new LabLargeBattery(1000000000000000L)));
        LabItems.CATALOG.put("zodius_dead",
                LabItems.ITEMS.register("zodius_dead", () -> new Item(new Item.Properties())));
    }
}
