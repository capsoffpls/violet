package violet.features.chat;

import com.mojang.blaze3d.platform.InputConstants;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.ChatScreen;
import violet.config.Feature;
import violet.config.SettingBool;
import violet.config.SettingInt;
import violet.config.SettingKeybind;
import violet.events.InputEvent;
import violet.misc.Utils;

import static violet.Main.mc;
import static violet.misc.Utils.getHoveredMsg;

/*
    copied from nofrills
 */
public class ChatPatches {
    public static final Feature instance = new Feature("chatPatches");

    public static final SettingKeybind copyKey = new SettingKeybind(InputConstants.UNKNOWN.getValue(), "copyKey", instance);
    public static final SettingKeybind copyLineKey = new SettingKeybind(InputConstants.UNKNOWN.getValue(), "copyLineKey", instance);
    public static final SettingBool trimOnCopy = new SettingBool(false, "trimOnCopy", instance);
    public static final SettingBool msgOnCopy = new SettingBool(false, "msgOnCopy", instance);
    public static final SettingInt copyMsgLength = new SettingInt(50, "copyMsgLength", instance);
    public static final SettingBool keepHistory = new SettingBool(false, "keepHistory", instance);
    public static final SettingBool extraLines = new SettingBool(false, "extraLines", instance);
    public static final SettingInt lines = new SettingInt(1000, "lines", instance);



    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && mc.gui.screen() instanceof ChatScreen && (copyKey.isKey(event.key) || copyLineKey.isKey(event.key))) {
            if (event.action == InputConstants.PRESS) {
                String message = getHoveredMsg(copyLineKey.isKey(event.key));
                if (message.isEmpty()) return;
                mc.keyboardHandler.setClipboard(trimOnCopy.value() ? message.trim() : message);
                if (msgOnCopy.value()) {
                    String type = copyLineKey.isKey(event.key) ? "Line" : "Message";
                    int length = copyMsgLength.value();
                    if (length == 0) {
                        Utils.infoFormat("§a{} copied to clipboard.", type);
                    } else {
                        Utils.infoFormat("§a{} copied to clipboard: \"§7{}§a\".",
                                type,
                                message.length() > length ? message.substring(0, length) + "..." : message
                        );
                    }
                }
            }
            event.cancel();
        }
    }
}
