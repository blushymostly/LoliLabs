package tmr.lolilabs;

import net.minecraftforge.fml.ModList;

public final class MachineDefinitions {

    public static void register() {
        solar("dispersion_solar_panel", 131072L);
        solar("gravitomagnetic_solar_panel", 262144L);
        solar("anisotropic_solar_panel", 524288L);
        solar("fluctuation_solar_panel", 1048576L);
        solar("aberration_solar_panel", 2097152L);
        solar("parametric_solar_panel", 4194304L);
        solar("chronodynamic_solar_panel", 8388608L);
        solar("antimaterial_solar_panel", 16777216L);
        solar("sparkling_panel", 33554432L);
        solar("asmodeus_demonic_panel", 134217728L);
        solar("belphegor_demonic_panel", 268435456L);
        solar("beelzebub_demonic_panel", 536870912L);
        solar("leviathan_demonic_panel", 1073741824L);
        solar("lucifer_ultimate_demonic_panel", 2147483648L);
        solar("chaos_guardian_panel", 268435456L);
        solar("cold_guardian_panel", 536870912L);
        solar("flame_guardian_panel", 1073741824L);
        solar("infinity_guardian_panel", 2147483648L);
        solar("infinity_demon_guardian_panel", 4294967296L);
        solar("dragon_slayer_ultimate_panel", 8589934592L);
        if (ModList.get().isLoaded("thaumaturge")) {
            // OG EU/t siblings: gravitomagnetic / anisotropic / fluctuation, then 3× / ~4× arcane.
            solar("ethereal_panel", 262144L);
            solar("ghost_panel", 524288L);
            solar("arcane_panel", 1048576L);
            solar("dragon_panel", 3145728L);
            solar("ultimate_infernal_panel", 4660349L);
        }
        unifier("energy_unifier");
        matter("matter_solid_t0_x1", "matter", 0, 1);
        matter("matter_solid_t0_x6", "matter", 0, 6);
        matter("matter_solid_t0_x12", "matter", 0, 12);
        matter("matter_solid_t1_x1", "matter_loose_blue", 1, 1);
        matter("matter_solid_t1_x6", "matter_loose_blue", 1, 6);
        matter("matter_solid_t1_x12", "matter_loose_blue", 1, 12);
        matter("matter_solid_t2_x1", "matter_loose_pink", 2, 1);
        matter("matter_solid_t2_x6", "matter_loose_pink", 2, 6);
        matter("matter_solid_t2_x12", "matter_loose_pink", 2, 12);
        matter("matter_solid_t3_x1", "matter_luminous_pink", 3, 1);
        matter("matter_solid_t3_x6", "matter_luminous_pink", 3, 6);
        matter("matter_solid_t3_x12", "matter_luminous_pink", 3, 12);
        matter("matter_solid_t4_x1", "matter_luminous_red", 4, 1);
        matter("matter_solid_t4_x6", "matter_luminous_red", 4, 6);
        matter("matter_solid_t4_x12", "matter_luminous_red", 4, 12);
        matter("matter_solid_t5_x1", "matter_turquoise", 5, 1);
        matter("matter_solid_t5_x6", "matter_turquoise", 5, 6);
        matter("matter_solid_t5_x12", "matter_turquoise", 5, 12);
        matter("matter_solid_t6_x1", "matter_luminous_turquoise", 6, 1);
        matter("matter_solid_t6_x6", "matter_luminous_turquoise", 6, 6);
        matter("matter_solid_t6_x12", "matter_luminous_turquoise", 6, 12);
        matter("matter_solid_t7_x1", "matter_purple", 7, 1);
        matter("matter_solid_t7_x6", "matter_purple", 7, 6);
        matter("matter_solid_t7_x12", "matter_purple", 7, 12);
        matter("matter_solid_t8_x1", "matter_mutated_violet_turquoise", 8, 1);
        matter("matter_solid_t8_x6", "matter_mutated_violet_turquoise", 8, 6);
        matter("matter_solid_t8_x12", "matter_mutated_violet_turquoise", 8, 12);
        matter("matter_solid_t9_x1", "matter_sparkling", 9, 1);
        matter("matter_solid_t9_x6", "matter_sparkling", 9, 6);
        matter("matter_solid_t9_x12", "matter_sparkling", 9, 12);
    }

    private static void solar(String id, long rate) {
        LabMachines.solar(id, rate);
    }

    private static void unifier(String id) {
        LabMachines.unifier(id);
    }

    private static void matter(String id, String matter, int tier, int batch) {
        LabMachines.matter(id, matter, tier, batch);
    }
}
