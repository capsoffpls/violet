package violet.features.render;

import static violet.Main.mc;

import com.mojang.blaze3d.platform.InputConstants;
import meteordevelopment.orbit.EventHandler;
import violet.config.Feature;
import violet.config.SettingBool;
import violet.config.SettingDouble;
import violet.config.SettingKeybind;
import violet.events.InputEvent;

public class Zoom {
    public static final Feature instance = new Feature("zoom");

    public static final SettingKeybind keybind = new SettingKeybind(InputConstants.UNKNOWN.getValue(), "keybind", instance);
    public static final SettingDouble scale = new SettingDouble(4.0, "scale", instance);
    public static final SettingBool cinematic = new SettingBool(true, "cinematic", instance);

    public static boolean zoomedIn = false;
    public static float targetFov = 0;


    @EventHandler
    private static void onKey(InputEvent event) {
        if (instance.isActive() && keybind.isKey(event.key) && mc.gui.screen() == null) {
            if (event.action == InputConstants.PRESS) {
                mc.options.smoothCamera = cinematic.value();
                zoomedIn = true;
                targetFov = (float) (mc.options.fov().get() / Math.max(1, scale.value()));
            } else if (event.action == InputConstants.RELEASE) {
                zoomedIn = false;
                mc.options.smoothCamera = false; // unfortunately no revert to previous state
            }
        }
    }

    public static float getTargetFov() {
        return targetFov;
    }

    public static boolean isZoomedIn() {
        return zoomedIn && instance.isActive();
    }
}
