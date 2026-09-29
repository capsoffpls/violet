package violet.features.player;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import violet.config.Feature;
import violet.config.SettingBool;
import violet.config.SettingEnum;

import java.util.function.Predicate;

import static violet.Main.mc;

public class UseDelay {
    public static final Feature instance = new Feature("useDelay");

    public static final SettingEnum<Mode> mode = new SettingEnum<>(Mode.All, Mode.class, "mode", instance);
    public static final SettingBool onlyCreative = new SettingBool(false, "onlyCreative", instance);

    @SuppressWarnings("DataFlowIssue") // mc.player cant be null if called from mixin method
    public static boolean canChangeDelay() {
        if (onlyCreative.value() && mc.player.gameMode() != GameType.CREATIVE) return false;

        Mode currentMode = mode.value();
        if (currentMode == Mode.All) return true;

        ItemStack main = mc.player.getMainHandItem();
        ItemStack stack = main.isEmpty() ? mc.player.getOffhandItem() : main;
        return !stack.isEmpty() && currentMode.test(stack);
    }

    private static boolean matches(Mode mode, ItemStack stack) {
        return !stack.isEmpty() && mode.test(stack);
    }

    private static boolean isBlock(ItemStack stack) {
        return stack.getItem() instanceof BlockItem;
    }

    public enum Mode {
        All(stack -> true),
        Block(UseDelay::isBlock),
        Other(stack -> !isBlock(stack));

        private final Predicate<ItemStack> predicate;

        Mode(Predicate<ItemStack> predicate) {
            this.predicate = predicate;
        }

        public boolean test(ItemStack stack) {
            return predicate.test(stack);
        }
    }
}