package violet.features.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import violet.config.Feature;
import violet.config.SettingBool;
import violet.config.SettingEnum;
import violet.events.BlockBreakingCooldownEvent;

import java.util.function.Predicate;

import static violet.Main.mc;

public class BreakDelay {
    public static final Feature instance = new Feature("breakDelay");

    public static final SettingEnum<Mode> mode = new SettingEnum<>(Mode.All, Mode.class, "mode", instance);
    public static final SettingBool onlyCreative = new SettingBool(false, "onlyCreative", instance);

    @EventHandler
    private static void onBlockBreakingCooldown(BlockBreakingCooldownEvent event) {
        if (instance.isActive() && canChangeDelay()) event.cooldown = 0;
    }

    @SuppressWarnings("DataFlowIssue") // mc.player cant be null if called from mixin method
    private static boolean canChangeDelay() {
        if (onlyCreative.value() && mc.player.gameMode() != GameType.CREATIVE) return false;
        return mode.value().test(mc.player.getMainHandItem());
    }

    private static boolean isTool(ItemStack stack) {
        return stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HOES)
                || stack.is(ItemTags.SWORDS)
                || stack.is(Items.SHEARS);
    }

    public enum Mode {
        All(stack -> true),
        Tools(BreakDelay::isTool),
        NonTools(stack -> !isTool(stack));

        private final Predicate<ItemStack> predicate;

        Mode(Predicate<ItemStack> predicate) {
            this.predicate = predicate;
        }

        public boolean test(ItemStack stack) {
            return predicate.test(stack);
        }
    }
}