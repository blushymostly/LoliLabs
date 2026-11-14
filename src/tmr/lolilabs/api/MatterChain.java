package tmr.lolilabs.api;

/** Ordered solid-matter tiers shared by definitions, craft input, and UI. */
public final class MatterChain {

    public static final String[] TIERS = {
            "matter",
            "matter_loose_blue",
            "matter_loose_pink",
            "matter_luminous_pink",
            "matter_luminous_red",
            "matter_turquoise",
            "matter_luminous_turquoise",
            "matter_purple",
            "matter_mutated_violet_turquoise",
            "matter_sparkling"
    };

    private MatterChain() {}

    /** Tier 0 feeds IC2 slag; higher tiers feed the previous chain entry. */
    public static String inputId(int tier) {
        return tier == 0 ? "ic2modern:slag" : "loli_labs:" + TIERS[tier - 1];
    }

    public static String tierId(int tier) {
        return TIERS[tier];
    }
}
