package violet.features.misc;

import static violet.Main.mc;

import com.mojang.blaze3d.platform.InputConstants;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.Screen;
import violet.config.Feature;
import violet.config.SettingBool;
import violet.config.SettingColor;
import violet.config.SettingKeybind;
import violet.events.InputEvent;
import violet.hud.clickgui.ClickGui;
import violet.misc.RenderColor;
import violet.misc.Utils;

public class ClickGuiFeature {
    public static final Feature instance = new Feature("clickgui");

    public static final SettingKeybind openKey = new SettingKeybind(InputConstants.KEY_RSHIFT, "openKey", instance);
    public static final SettingBool closeIfOpen = new SettingBool(true, "closeIfOpen", instance);
    public static final SettingColor accentColor = new SettingColor(RenderColor.fromArgb(0x5ca0bf), "accentColor", instance);

    @EventHandler
    private static void onKey(InputEvent event) {
        if (openKey.isKey(event.key) && event.action == InputConstants.PRESS) {               // on keybind press
            Screen cScreen = mc.gui.screen();
            
            if (cScreen instanceof ClickGui && closeIfOpen.value()) {
                cScreen.onClose();
            } else if (cScreen == null) {
                Utils.setScreen(new ClickGui());
            }
        }       
    }

    public static int getAccentColor() {
        return accentColor.value().hex;
    }
}

