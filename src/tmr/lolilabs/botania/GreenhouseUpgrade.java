package tmr.lolilabs.botania;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Multi-greenhouse upgrade item with optional level (1-based). */
public final class GreenhouseUpgrade extends Item {

    public enum Kind {

        MULTIPLICATION("multiplication", 2),
        MANA_SPEED("mana_speed", 3),
        EU_SPEED("eu_energy_speed", 3),
        MANA_BUFFER("mana_buffer", 3),
        EU_BUFFER("eu_buffer", 3),
        CRYSTALLIZATION("crystallization", 1),
        EXPANSION("expansion", 1),
        EFFICIENCY("efficiency", 1),
        PROCESSING("processing", 1),
        EU_SAVING("eu_saving", 1),
        MANA_SAVING("mana_saving", 1);

        public final String id;
        public final int maxLevel;

        Kind(String id, int maxLevel) {
            this.id = id;
            this.maxLevel = maxLevel;
        }

        public String itemId() {
            return "greenhouse_upgrade_" + id;
        }
    }

    public final Kind kind;
    public final int level;

    public GreenhouseUpgrade(Kind kind, int level) {
        super(new Item.Properties());
        this.kind = kind;
        this.level = Math.max(1, Math.min(level, kind.maxLevel));
    }

    public net.minecraft.network.chat.Component effectDescription() {
        String key = "tooltip.loli_labs.greenhouse.effect." + kind.id;
        return switch (kind) {
            case MULTIPLICATION -> net.minecraft.network.chat.Component.m_237110_(key,
                    LabBotaniaTiers.MULTIPLICATION[level - 1]);
            case MANA_BUFFER -> net.minecraft.network.chat.Component.m_237110_(key,
                    LabBotaniaTiers.MANA_BUFFER[level - 1]);
            case EU_SPEED -> net.minecraft.network.chat.Component.m_237110_(key,
                    LabBotaniaTiers.EU_SPEED_PCT[level - 1], LabBotaniaTiers.EU_SPEED_COST[level - 1]);
            default -> net.minecraft.network.chat.Component.m_237115_(key);
        };
    }

    @Override
    public void m_7373_(ItemStack stack, net.minecraft.world.level.Level world,
                        java.util.List<net.minecraft.network.chat.Component> lines,
                        net.minecraft.world.item.TooltipFlag flag) {
        super.m_7373_(stack, world, lines, flag);
        lines.add(effectDescription().m_6881_().m_130940_(net.minecraft.ChatFormatting.GRAY));
    }

    /** Four OG slots use legacy backing indices 8..11 to preserve saved inventories. */
    public static Kind kindForSlot(int index) {
        return switch (index) {
            case 8 -> Kind.EFFICIENCY;
            case 9 -> Kind.MANA_BUFFER;
            case 10 -> Kind.MULTIPLICATION;
            case 11 -> Kind.EU_SPEED;
            default -> null;
        };
    }

    public static int slotFor(Kind kind) {
        if (kind == Kind.PROCESSING) return 8;
        for (int i = 8; i < 12; i++) if (kindForSlot(i) == kind) return i;
        return -1;
    }

    public static GreenhouseUpgrade of(ItemStack stack) {
        return stack.m_41720_() instanceof GreenhouseUpgrade upgrade ? upgrade : null;
    }
}
