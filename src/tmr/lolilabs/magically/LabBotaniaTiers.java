package tmr.lolilabs.magically;

/** Adamantium-tier mana constants (OG Magically TE ints unavailable; scaled from Botania MAX_MANA / Gaia spreader). */
public final class LabBotaniaTiers {

    public enum Tier {

        ETHEREAL("ethereal", 4_000_000, 1_280, 12_800),
        GHOST("ghost", 8_000_000, 2_560, 25_600),
        ARCANE("arcane", 16_000_000, 5_120, 51_200),
        DRAGON("dragon", 64_000_000, 10_240, 102_400);

        public final String id;
        public final int poolMana;
        public final int spreaderBurst;
        public final int spreaderCapacity;

        Tier(String id, int poolMana, int spreaderBurst, int spreaderCapacity) {
            this.id = id;
            this.poolMana = poolMana;
            this.spreaderBurst = spreaderBurst;
            this.spreaderCapacity = spreaderCapacity;
        }

        public String poolId() {
            return id + "_pool";
        }

        public String spreaderId() {
            return id + "_spreader";
        }

        public String sparkId() {
            return "stray_" + id + "_spark";
        }
    }

    public static final int[] MULTIPLICATION = { 4, 8 };
    public static final int[] MANA_SPEED_PCT = { 25, 50, 100 };
    public static final int[] MANA_SPEED_COST = { 1_000, 2_500, 5_000 };
    public static final int[] EU_SPEED_PCT = { 10, 15, 25 };
    public static final long[] EU_SPEED_COST = { 1_000L, 3_000L, 4_500L };
    public static final int[] MANA_BUFFER = { 5_000_000, 100_000_000, 100_000_000 };
    public static final long[] EU_BUFFER = { 100_000L, 500_000L, 2_000_000L };
    public static final int EFFICIENCY_PCT = 50;
    public static final int SAVING_PCT = 25;
    public static final long BASE_EU_CAPACITY = 100_000L;
    public static final int BASE_MANA_CAPACITY = 100_000;
    public static final int BASE_FLOWER_SLOTS = 16;
    public static final int EXPANSION_SLOTS = 32;
    public static final int CRYSTAL_SLOTS = 3;
    public static final int UPGRADE_SLOTS = 12;

    private LabBotaniaTiers() {}
}
