package violet.features.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import violet.commands.CommandHandler;
import violet.config.Feature;
import violet.config.SettingBool;
import violet.config.SettingJson;
import violet.events.InputEvent;
import violet.hud.clickgui.Settings;
import violet.hud.clickgui.components.*;
import violet.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static violet.Main.mc;

/*
    copied from nofrills
 */

public class CommandKeybinds {
    public static final Feature instance = new Feature("customKeybinds");

    public static final SettingBool allowInGui = new SettingBool(false, "allowInGui", instance.key());
    public static final SettingJson data = new SettingJson(new JsonObject(), "data", instance.key());

    public static List<FlowLayout> getSettingsList() {
        List<FlowLayout> list = new ArrayList<>();
        Settings.Toggle allowInGuiToggle = new Settings.Toggle("Allow in GUI", allowInGui, "Allow keybinds to work while any container GUI (inventory/chest/furnace/etc.) is open.");
        list.add(allowInGuiToggle);
        Settings.BigButton button = new Settings.BigButton("Add New Keybind", btn -> {
            data.edit(object -> {
                if (!object.has("binds")) {
                    object.add("binds", new JsonArray());
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("name", "New Keybind");
                obj.addProperty("key", InputConstants.UNKNOWN.getValue());
                obj.addProperty("command", "");
                obj.addProperty("enabled", true);
                obj.addProperty("modifier", Modifier.Any.name());
                obj.addProperty("serverFilter", "");
                object.get("binds").getAsJsonArray().add(obj);
            });
            mc.gui.setScreen(buildSettings());
        });
        button.button.verticalSizing(Sizing.fixed(18));
        list.add(button);
        if (data.value().has("binds")) {
            JsonArray binds = data.value().get("binds").getAsJsonArray();
            for (int i = 0; i < binds.size(); i++) {
                list.add(new Setting(i));
            }
        }
        return list;
    }

    public static Settings buildSettings() {
        Settings settings = new Settings(getSettingsList());
        settings.setTitle(Component.literal("Command Keybinds"));
        return settings;
    }

    private static Modifier getModifierType(String modifier) {
        for (Modifier value : Modifier.values()) {
            if (value.name().equals(modifier)) {
                return value;
            }
        }
        return Modifier.Any;
    }

    private static int getRequiredModifier(Modifier modifier) {
        return switch (modifier) {
            case Any -> -1;
            case None -> 0;
            case Shift -> InputConstants.MOD_SHIFT;
            case Alt -> InputConstants.MOD_ALT;
            case Ctrl -> InputConstants.MOD_CONTROL;
        };
    }

    @EventHandler
    public static void onKey(InputEvent event) {
        if (event.action != InputConstants.PRESS) return;
        if (instance.isActive() && ((allowInGui.value() && mc.gui.screen() instanceof AbstractContainerScreen) || mc.gui.screen() == null)) {
            if (data.value().has("binds")) {
                for (JsonElement entry : data.value().get("binds").getAsJsonArray()) {
                    JsonObject bind = entry.getAsJsonObject();
                    if (!bind.has("enabled") || !bind.get("enabled").getAsBoolean()) {
                        continue;
                    }
                    if (bind.get("key").getAsInt() != event.key) {
                        continue;
                    }
                    if (bind.has("modifier")) {
                        String modifier = bind.get("modifier").getAsString();
                        int required = getRequiredModifier(getModifierType(modifier));
                        if (required != -1 && event.modifiers != required) {
                            continue;
                        }
                    }
                    if (bind.has("serverFilter")) {
                        String filter = bind.get("serverFilter").getAsString();
                        if (!filter.isEmpty() && !Utils.getServerIP().equals(filter)) {
                            continue;
                        }
                    }
                    String command = bind.get("command").getAsString();
                    if (!command.isEmpty()) {
                        if (event.action == InputConstants.PRESS) {
                            if (command.startsWith(String.valueOf(VioletCommands.getPrefix()))) {
                                CommandHandler.handle(command, false);
                            } else Utils.say(command);
                        }
                        event.cancel();
                        break;
                    }
                }
            }
        }
    }

    public enum Modifier {
        Any,
        None,
        Shift,
        Ctrl,
        Alt
    }

    public static class Setting extends FlowLayout {
        public int index;

        public Setting(int index) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);

            this.index = index;
            this.padding(Insets.of(5, 5, 4, 5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);

            FlatTextbox input = new FlatTextbox(Sizing.fixed(140));
            input.margins(Insets.of(0, 0, 0, 6));
            input.text(this.getData().has("name") ? this.getData().get("name").getAsString() : "New Keybind");
            input.tooltip(Component.literal("The name of this command keybind."));
            input.onChanged().subscribe(value -> data.edit(obj -> this.getData(obj).addProperty("name", value)));
            ToggleButton mainToggle = new ToggleButton(this.getData().get("enabled").getAsBoolean());
            mainToggle.sizing(Sizing.fixed(50), Sizing.fixed(18)).margins(Insets.of(1, 0, 0, 3));
            mainToggle.tooltip(Component.literal("The main toggle for this command keybind."));
            mainToggle.onToggled().subscribe(toggle -> data.edit(obj -> this.getData(obj).addProperty("enabled", toggle)));
            ButtonComponent editButton = UIComponents.button(Component.literal("Edit").withColor(0xffffff), button -> mc.gui.setScreen(this.buildKeybindSettings()));
            editButton.sizing(Sizing.fixed(48), Sizing.fixed(18)).margins(Insets.of(1, 0, 0, 0));
            editButton.renderer(Settings.buttonRendererWhite);
            ButtonComponent delete = UIComponents.button(Component.literal("Delete").withColor(0xffffff), button -> {
                data.edit(object -> object.get("binds").getAsJsonArray().remove(this.index));
                mc.gui.setScreen(buildSettings());
            });
            delete.positioning(Positioning.relative(100, 50)).verticalSizing(Sizing.fixed(18));
            delete.renderer(Settings.buttonRendererWhite);

            this.child(input);
            this.child(mainToggle);
            this.child(editButton);
            this.child(delete);
        }

        public JsonObject getData(JsonObject object) {
            return object.get("binds").getAsJsonArray().get(this.index).getAsJsonObject();
        }

        public JsonObject getData() {
            return this.getData(data.value());
        }

        public FlowLayout buildCommandInputSetting() {
            FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
            layout.padding(Insets.of(5));
            layout.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Component.literal("Command"));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            FlatTextbox input = new FlatTextbox(Sizing.fixed(200));
            input.text(this.getData().get("command").getAsString());
            input.tooltip(Component.literal("The message/command that this keybind will send."));
            input.onChanged().subscribe(value -> data.edit(obj -> this.getData(obj).addProperty("command", value)));
            layout.child(label);
            layout.child(input);
            return layout;
        }

        public FlowLayout buildKeybindSetting() {
            FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
            layout.padding(Insets.of(5));
            layout.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Component.literal("Keybind"));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            KeybindButton keybind = new KeybindButton();
            keybind.verticalSizing(Sizing.fixed(20)).horizontalSizing(Sizing.fixed(100));
            keybind.bind(this.getData(data.value()).get("key").getAsInt());
            keybind.tooltip(Component.literal("The key bound to this command."));
            keybind.onBound().subscribe(key -> data.edit(object -> this.getData(object).addProperty("key", key)));
            layout.child(label);
            layout.child(keybind);
            return layout;
        }

        public FlowLayout buildModifierSetting() {
            FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
            layout.padding(Insets.of(5));
            layout.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Component.literal("Modifier Key"));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            EnumButton<Modifier> modifier = new EnumButton<>(this.getData(data.value()).has("modifier") ? this.getData(data.value()).get("modifier").getAsString() : "Any", Modifier.Any, Modifier.class);
            modifier.onChanged().subscribe(value -> data.edit(object -> this.getData(object).addProperty("modifier", value)));
            modifier.sizing(Sizing.fixed(80), Sizing.fixed(20));
            modifier.tooltip(Component.literal("The modifier key required to execute the keybind.\n\nAny: Executes regardless of modifier.\nNone: Executes only if no modifier (Shift, Alt, etc.) is held.\nShift: Executes only if Shift is held.\nCtrl: Executes only if Ctrl is held.\nAlt: Executes only if Alt is held."));
            layout.child(label);
            layout.child(modifier);
            return layout;
        }

        public FlowLayout buildserverFilterSetting() {
            FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
            layout.padding(Insets.of(5));
            layout.horizontalAlignment(HorizontalAlignment.LEFT);
            PlainLabel label = new PlainLabel(Component.literal("Server Filter"));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            FlatTextbox input = new FlatTextbox(Sizing.fixed(200));
            input.text(this.getData().has("serverFilter") ? this.getData().get("serverFilter").getAsString() : "");
            input.tooltip(Component.literal("Specific server this keybind only works on.\nFor example: \"hypixel.net\" will disable the keybind\nunless ip you connected with is exactly \"hypixel.net\"\n\nLeave empty to disable server filtering. or \"singleplayer\" for SP."));
            input.onChanged().subscribe(value -> data.edit(obj -> this.getData(obj).addProperty("serverFilter", value)));
            layout.child(label);
            layout.child(input);
            return layout;
        }

        public CommandKeybindsSettings buildKeybindSettings() {
            List<FlowLayout> list = new ArrayList<>();
            list.add(this.buildCommandInputSetting());
            list.add(this.buildKeybindSetting());
            list.add(this.buildModifierSetting());
            list.add(this.buildserverFilterSetting());
            CommandKeybindsSettings settings = new CommandKeybindsSettings(list);
            settings.setTitle(Component.literal("Command Keybind: " + (this.getData().has("name") ? this.getData().get("name").getAsString() : "New Keybind")));
            return settings;
        }
    }

    public static class CommandKeybindsSettings extends Settings {

        public CommandKeybindsSettings(List<FlowLayout> settings) {
            super(settings);
        }

        @Override
        public void onClose() {
            mc.gui.setScreen(buildSettings());
        }
    }
}
