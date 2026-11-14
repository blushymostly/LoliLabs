package tmr.lolilabs.energistics;

import tmr.lolilabs.api.MatterChain;

import net.minecraftforge.fml.ModList;

public final class MachineDefinitions {

    public static void register() {
        LabMachines.solar("dispersion_solar_panel", 131072L);
        LabMachines.solar("gravitomagnetic_solar_panel", 262144L);
        LabMachines.solar("anisotropic_solar_panel", 524288L);
        LabMachines.solar("fluctuation_solar_panel", 1048576L);
        LabMachines.solar("aberration_solar_panel", 2097152L);
        LabMachines.solar("parametric_solar_panel", 4194304L);
        LabMachines.solar("chronodynamic_solar_panel", 8388608L);
        LabMachines.solar("antimaterial_solar_panel", 16777216L);
        LabMachines.solar("sparkling_panel", 33554432L);
        LabMachines.solar("asmodeus_demonic_panel", 134217728L);
        LabMachines.solar("belphegor_demonic_panel", 268435456L);
        LabMachines.solar("beelzebub_demonic_panel", 536870912L);
        LabMachines.solar("leviathan_demonic_panel", 1073741824L);
        LabMachines.solar("lucifer_ultimate_demonic_panel", 2147483648L);
        LabMachines.solar("chaos_guardian_panel", 268435456L);
        LabMachines.solar("cold_guardian_panel", 536870912L);
        LabMachines.solar("flame_guardian_panel", 1073741824L);
        LabMachines.solar("infinity_guardian_panel", 2147483648L);
        LabMachines.solar("infinity_demon_guardian_panel", 4294967296L);
        LabMachines.solar("dragon_slayer_ultimate_panel", 8589934592L);
        if (ModList.get().isLoaded("thaumaturge")) {
            // OG EU/t siblings: gravitomagnetic / anisotropic / fluctuation, then 3× / ~4× arcane.
            LabMachines.solar("ethereal_panel", 262144L);
            LabMachines.solar("ghost_panel", 524288L);
            LabMachines.solar("arcane_panel", 1048576L);
            LabMachines.solar("dragon_panel", 3145728L);
            LabMachines.solar("ultimate_infernal_panel", 4660349L);
        }
        LabMachines.unifier("energy_unifier");
        LabMachines.matter("matter_solid_t0_x1", MatterChain.tierId(0), 0, 1);
        LabMachines.matter("matter_solid_t0_x6", MatterChain.tierId(0), 0, 6);
        LabMachines.matter("matter_solid_t0_x12", MatterChain.tierId(0), 0, 12);
        LabMachines.matter("matter_solid_t1_x1", MatterChain.tierId(1), 1, 1);
        LabMachines.matter("matter_solid_t1_x6", MatterChain.tierId(1), 1, 6);
        LabMachines.matter("matter_solid_t1_x12", MatterChain.tierId(1), 1, 12);
        LabMachines.matter("matter_solid_t2_x1", MatterChain.tierId(2), 2, 1);
        LabMachines.matter("matter_solid_t2_x6", MatterChain.tierId(2), 2, 6);
        LabMachines.matter("matter_solid_t2_x12", MatterChain.tierId(2), 2, 12);
        LabMachines.matter("matter_solid_t3_x1", MatterChain.tierId(3), 3, 1);
        LabMachines.matter("matter_solid_t3_x6", MatterChain.tierId(3), 3, 6);
        LabMachines.matter("matter_solid_t3_x12", MatterChain.tierId(3), 3, 12);
        LabMachines.matter("matter_solid_t4_x1", MatterChain.tierId(4), 4, 1);
        LabMachines.matter("matter_solid_t4_x6", MatterChain.tierId(4), 4, 6);
        LabMachines.matter("matter_solid_t4_x12", MatterChain.tierId(4), 4, 12);
        LabMachines.matter("matter_solid_t5_x1", MatterChain.tierId(5), 5, 1);
        LabMachines.matter("matter_solid_t5_x6", MatterChain.tierId(5), 5, 6);
        LabMachines.matter("matter_solid_t5_x12", MatterChain.tierId(5), 5, 12);
        LabMachines.matter("matter_solid_t6_x1", MatterChain.tierId(6), 6, 1);
        LabMachines.matter("matter_solid_t6_x6", MatterChain.tierId(6), 6, 6);
        LabMachines.matter("matter_solid_t6_x12", MatterChain.tierId(6), 6, 12);
        LabMachines.matter("matter_solid_t7_x1", MatterChain.tierId(7), 7, 1);
        LabMachines.matter("matter_solid_t7_x6", MatterChain.tierId(7), 7, 6);
        LabMachines.matter("matter_solid_t7_x12", MatterChain.tierId(7), 7, 12);
        LabMachines.matter("matter_solid_t8_x1", MatterChain.tierId(8), 8, 1);
        LabMachines.matter("matter_solid_t8_x6", MatterChain.tierId(8), 8, 6);
        LabMachines.matter("matter_solid_t8_x12", MatterChain.tierId(8), 8, 12);
        LabMachines.matter("matter_solid_t9_x1", MatterChain.tierId(9), 9, 1);
        LabMachines.matter("matter_solid_t9_x6", MatterChain.tierId(9), 9, 6);
        LabMachines.matter("matter_solid_t9_x12", MatterChain.tierId(9), 9, 12);
    }
}
