package violet.features.chat;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import violet.config.Config;
import violet.config.Feature;
import violet.config.SettingBool;
import violet.config.SettingJson;
import violet.config.SettingKeybind;
import violet.events.ChatMsgEvent;
import violet.events.InputEvent;
import violet.hud.clickgui.Settings;
import violet.hud.clickgui.components.FlatTextbox;
import violet.misc.Rendering;
import violet.misc.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static violet.Main.mc;
import static violet.misc.Utils.getHoveredMsg;

public class ChatCleaner {
    public static final Feature instance = new Feature("ChatCleaner");
    public static final String tooltip = "Block selected messages or patterns from appearing on chat.";

    public static final SettingJson blacklist = new SettingJson(new JsonObject(), "blacklist", instance);
    public static final SettingKeybind addMessageKey = new SettingKeybind(InputConstants.UNKNOWN.getValue(), "addMessageKey", instance);
    public static final SettingBool showFeedback = new SettingBool(true, "showFeedback", instance);

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();

        list.add(new Settings.Keybind("Add/Remove Message", addMessageKey, "Add or remove hovered message from blacklist."));
        list.add(new Settings.Toggle("Send Feedback", showFeedback, "Sends the changed status of message upon keybinding it."));

        Settings.BigButton openBlacklist = new Settings.BigButton("Open Blacklist", _ -> Utils.setScreen(buildBlacklistSettings()));
        openBlacklist.button.verticalSizing(Sizing.fixed(18));
        list.add(openBlacklist);

        return list;
    }

    public static Settings buildSettings() {
        Settings settings = new Settings(getSettingsList());
        settings.setTitle(Component.literal("Chat Cleaner"));
        return settings;
    }

    public static BlacklistSettings buildBlacklistSettings() {
        List<FlowLayout> list = new ArrayList<>();

        Settings.BigButton addButton = new Settings.BigButton("+ Add Pattern", _ -> {
            blacklist.edit(json -> json.addProperty(String.valueOf(json.size()), ""));
            Config.computeHash();
            Utils.setScreen(buildBlacklistSettings());
        });
        addButton.button.verticalSizing(Sizing.fixed(18));
        list.add(addButton);

        JsonObject json = blacklist.value();
        for (var entry : json.entrySet()) {
            list.add(new BlacklistEntry(entry.getKey(), entry.getValue().getAsString()));
        }

        BlacklistSettings settings = new BlacklistSettings(list);
        settings.setTitle(Component.literal("Chat Cleaner - Blacklist"));
        return settings;
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (!instance.isActive()) return;
        if (!(mc.gui.screen() instanceof ChatScreen)) return;
        if (!addMessageKey.isKey(event.key)) return;
        if (event.action != InputConstants.PRESS) return;

        String message = getHoveredMsg(true);
        if (message.isEmpty()) return;

        event.cancel();

        blacklist.edit(json -> {
            boolean found = false;
            for (var entry : json.entrySet()) {
                if (entry.getValue().getAsString().equals(message)) {
                    found = true;
                    json.remove(entry.getKey());
                    break;
                }
            }

            if (found) {
                if (showFeedback.value())
                    Utils.infoFormat("§cRemoved from blacklist: \"§7{}§c\".", message);
            } else {
                json.addProperty(String.valueOf(json.size()), message);
                if (showFeedback.value())
                    Utils.infoFormat("§aAdded to blacklist: \"§7{}§a\".", message);
            }
        });
    }

    @EventHandler
    private static void onMsg(ChatMsgEvent event) {
        if (!instance.isActive()) return;

        for (var entry : blacklist.value().entrySet()) {
            String pattern = entry.getValue().getAsString();
            if (pattern.isEmpty()) continue;
            try {
                if (Pattern.compile(pattern).matcher(event.messagePlain).matches()) {
                    event.cancel();
                    return;
                }
            } catch (Exception ignored) {}
        }
    }

    public static class BlacklistEntry extends FlowLayout {
        public BlacklistEntry(String key, String value) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(2, 2, 4, 5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.verticalAlignment(VerticalAlignment.CENTER);

            FlatTextbox input = new FlatTextbox(Sizing.fixed(265));
            input.text(value);
            input.onChanged().subscribe(val -> blacklist.edit(json -> json.addProperty(key, val)));

            ButtonComponent deleteButton = UIComponents.button(Component.literal("🗑"), _ -> {
                blacklist.edit(json -> json.remove(key));
                Config.computeHash();
                Utils.setScreen(buildBlacklistSettings());
            });
            deleteButton.margins(Insets.left(-3)); // Insets.right(x) does not work at all
            deleteButton.sizing(Sizing.fixed(18), Sizing.fixed(18));
            deleteButton.renderer((context, button, _) -> {
                context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
                Rendering.drawBorder(context, button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xffff4444);
            });

            this.child(input);
            this.child(deleteButton);
        }
    }

    public static class BlacklistSettings extends Settings {
        public BlacklistSettings(List<FlowLayout> settings) {
            super(settings);
        }

        @Override
        public void onClose() {
            Utils.setScreen(buildSettings());
        }
    }
}