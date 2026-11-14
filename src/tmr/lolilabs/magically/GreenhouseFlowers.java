package tmr.lolilabs.magically;

import net.minecraft.nbt.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

/** Inventory-fed Botania generators. Each installed flower has its own persisted digestion state. */
public final class GreenhouseFlowers {

    private static final String[] IDS = { "endoflame", "gourmaryllis", "munchdew", "entropinnyum", "rafflowsia" };
    private static final int[] RAFF = { 2000, 2100, 2200, 2300, 3280, 4033, 4657, 5150, 6622, 7860, 10418, 12600, 14769,
            16671, 19000, 25400, 33471, 40900, 47579, 53600, 59057, 64264, 69217, 74483, 79352, 83869, 88059, 92129,
            96669, 100940, 105239, 112044, 118442, 124612, 130583, 136228, 141703, 178442, 213959, 247725, 279956,
            313671, 345833, 377227, 437689, 495526, 553702, 638554 };
    private static final double[] FOOD = { 0, 1, 1.3, 1.5, 1.6, 1.7, 1.75, 1.8 };

    public static String type(ItemStack stack) {
        if (stack.m_41619_()) return "";
        var id = ForgeRegistries.ITEMS.getKey(stack.m_41720_());
        if (id == null || !id.m_135827_().equals("botania")) return "";
        String path = id.m_135815_().replace("floating_", "");
        for (String supported : IDS) if (supported.equals(path)) return supported;
        return "";
    }

    public static boolean acceptsFlower(ItemStack stack) {
        return !type(stack).isEmpty();
    }

    private static Block block(ItemStack stack) {
        return Block.m_49814_(stack.m_41720_());
    }

    private static int fuel(ItemStack stack) {
        if (stack.m_41619_() || stack.m_41720_().hasCraftingRemainingItem(stack) ||
                block(stack) instanceof vazkii.botania.common.block.mana.ManaSpreaderBlock)
            return 0;
        return Math.max(0, net.minecraftforge.common.ForgeHooks.getBurnTime(stack,
                net.minecraft.world.item.crafting.RecipeType.f_44108_));
    }

    private static boolean leaf(ItemStack stack) {
        return block(stack).m_49966_().m_204336_(net.minecraft.tags.BlockTags.f_13035_);
    }

    private static boolean special(ItemStack stack) {
        return block(stack).m_49966_().m_204336_(vazkii.botania.common.lib.BotaniaTags.Blocks.SPECIAL_FLOWERS) &&
                !type(stack).equals("rafflowsia");
    }

    public static boolean acceptsResource(ItemStack stack) {
        return !stack.m_41619_() && (fuel(stack) > 0 || stack.getFoodProperties(null) != null || leaf(stack) ||
                stack.m_150930_(net.minecraft.world.level.block.Blocks.f_50077_.m_5456_()) || special(stack));
    }

    public static final class Unit {

        String flower = "";
        int time, pending, batch = 1, age, idle, repeat, streak = -1;
        boolean eating;
        final java.util.List<ItemStack> history = new java.util.ArrayList<>();

        CompoundTag save() {
            var n = new CompoundTag();
            n.m_128359_("Flower", flower);
            n.m_128405_("Time", time);
            n.m_128405_("Pending", pending);
            n.m_128405_("Batch", batch);
            n.m_128405_("Age", age);
            n.m_128405_("Idle", idle);
            n.m_128405_("Repeat", repeat);
            n.m_128405_("Streak", streak);
            n.m_128379_("Eating", eating);
            var list = new ListTag();
            for (var s : history) list.add(s.m_41739_(new CompoundTag()));
            n.m_128365_("History", list);
            return n;
        }

        static Unit load(CompoundTag n) {
            var u = new Unit();
            u.flower = n.m_128461_("Flower");
            u.time = Math.max(0, n.m_128451_("Time"));
            u.pending = Math.max(0, n.m_128451_("Pending"));
            u.batch = Math.max(1, Math.min(64, n.m_128451_("Batch")));
            u.age = n.m_128451_("Age");
            u.idle = n.m_128451_("Idle");
            u.repeat = Math.max(0, n.m_128451_("Repeat"));
            u.streak = n.m_128441_("Streak") ? n.m_128451_("Streak") : -1;
            u.eating = n.m_128471_("Eating");
            var list = n.m_128437_("History", 10);
            for (int i = 0; i < Math.min(47, list.size()); i++) u.history.add(ItemStack.m_41712_(list.m_128728_(i)));
            return u;
        }

        int remember(ItemStack food, int max) {
            for (int i = 0; i < history.size(); i++) if (ItemStack.m_150942_(history.get(i), food)) {
                var old = history.remove(i);
                history.add(0, old);
                return i;
            }
            history.add(0, food.m_255036_(1));
            if (history.size() >= max) history.remove(history.size() - 1);
            return max;
        }
    }

    private final Unit[][] units = new Unit[8][64];

    public ListTag save() {
        var list = new ListTag();
        for (int s = 0; s < 8; s++) for (int i = 0; i < 64; i++) if (units[s][i] != null) {
            var n = units[s][i].save();
            n.m_128405_("Slot", s);
            n.m_128405_("Index", i);
            list.add(n);
        }
        return list;
    }

    public void load(ListTag list) {
        for (var row : units) java.util.Arrays.fill(row, null);
        for (int i = 0; i < list.size(); i++) {
            var n = list.m_128728_(i);
            int s = n.m_128451_("Slot"), j = n.m_128451_("Index");
            if (s >= 0 && s < 8 && j >= 0 && j < 64) units[s][j] = Unit.load(n);
        }
    }

    private static boolean consumes(String type, ItemStack stack) {
        if (stack.m_41619_()) return false;
        return switch (type) {
            case "endoflame" -> fuel(stack) > 0;
            case "gourmaryllis" -> stack.getFoodProperties(null) != null;
            case "munchdew" -> leaf(stack);
            case "entropinnyum" -> stack.m_150930_(net.minecraft.world.level.block.Blocks.f_50077_.m_5456_());
            case "rafflowsia" -> special(stack);
            default -> false;
        };
    }

    public boolean hasWork(GreenhouseCore core) {
        for (int s = 0; s < 8; s++) {
            var stack = core.inventory.getStackInSlot(s);
            String type = type(stack);
            if (type.isEmpty()) continue;
            for (int j = 0; j < Math.min(stack.m_41613_(), core.flowerLimit()); j++) {
                var u = units[s][j];
                if (u != null && u.flower.equals(type) && (u.time > 0 || u.pending > 0)) return true;
            }
            for (int i = 8; i < 40; i++) if (consumes(type, core.inventory.getStackInSlot(i))) return true;
        }
        return false;
    }

    public void tick(GreenhouseCore core) {
        for (int slot = 0; slot < 8; slot++) {
            var installed = core.inventory.getStackInSlot(slot);
            String type = type(installed);
            int count = type.isEmpty() ? 0 : Math.min(installed.m_41613_(), core.flowerLimit());
            for (int index = 0; index < count; index++) {
                Unit u = units[slot][index];
                if (u == null || !u.flower.equals(type)) {
                    u = new Unit();
                    u.flower = type;
                    units[slot][index] = u;
                }
                if (core.mana >= core.manaCap) continue;
                u.age++;
                if (u.pending > 0 && (u.flower.equals("endoflame") || u.time <= 0)) {
                    int amount = Math.min(u.pending, core.manaCap - core.mana);
                    core.mana += amount;
                    u.pending -= amount;
                    if (u.pending > 0) continue;
                }
                if (u.time > 0) {
                    u.time--;
                    if (type.equals("endoflame") && u.time > 0 && u.age % 2 == 0) u.pending += 3 * u.batch;
                    continue;
                }
                if (type.equals("munchdew") && u.eating) {
                    if (++u.idle >= 5) {
                        u.eating = false;
                        u.time = 1600;
                    }
                }
                if (u.time > 0 || u.pending > 0) continue;
                if (type.equals("munchdew") && u.age % 4 != 0 || type.equals("rafflowsia") && u.age % 40 != 0) continue;
                for (int input = 8; input < 40; input++) {
                    var stack = core.inventory.getStackInSlot(input);
                    if (stack.m_41619_()) continue;
                    boolean valid = consumes(type, stack);
                    if (!valid) continue;
                    int batch = core.processing() ? stack.m_41613_() : 1;
                    u.batch = batch;
                    switch (type) {
                        case "endoflame" -> u.time = Math.min(32000, fuel(stack)) / 2;
                        case "gourmaryllis" -> {
                            u.streak = Math.min(u.streak + 1, u.remember(stack, 7));
                            double mult = u.streak == 0 ? 1D / ++u.repeat : FOOD[u.streak];
                            if (u.streak != 0) u.repeat = 1;
                            var food = stack.getFoodProperties(null);
                            int value = food == null ? 0 : Math.min(12, food.m_38744_());
                            u.pending = Math.max(1, (int) (value * value * 70 * mult)) * batch;
                            u.time = Math.max(1, value * 10);
                        }
                        case "munchdew" -> {
                            u.pending = 160 * batch;
                            u.eating = true;
                            u.idle = 0;
                        }
                        case "entropinnyum" -> {
                            u.pending = 6500 * batch;
                            u.time = 80;
                        }
                        case "rafflowsia" -> {
                            u.streak = Math.min(u.streak + 1, u.remember(stack, 47));
                            if (u.streak != 0) u.repeat = 0;
                            u.pending = RAFF[u.streak] / ++u.repeat * batch;
                        }
                    }
                    core.inventory.extractItem(input, batch, false);
                    break;
                }
            }
        }
    }
}
