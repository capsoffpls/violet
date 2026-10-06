package violet.features.movement;

import static violet.Main.mc;


import com.mojang.blaze3d.platform.InputConstants;
import meteordevelopment.orbit.EventHandler;
import violet.config.Feature;
import violet.config.SettingKeybind;
import violet.events.InputEvent;
import violet.events.WorldTickEvent;
import violet.mixin.LivingEntityAccessor;

public class NoJumpCooldown {
    public static final Feature instance = new Feature("noJumpCooldown");

    public static final SettingKeybind toggleKey = new SettingKeybind(InputConstants.UNKNOWN.getValue(), "toggleKey", instance);

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (!instance.isActive() || mc.player == null) return;
        ((LivingEntityAccessor)mc.player).setJumpingCooldown(0);
    }

    @EventHandler
    private static void onKey(InputEvent event) {
        if (mc.gui.screen() != null) return;
    
        if (toggleKey.isKey(event.key) && event.action == InputConstants.PRESS) {
            instance.setActive(!instance.isActive());        
        }
    }
}
