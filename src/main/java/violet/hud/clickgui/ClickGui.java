package violet.hud.clickgui;

import com.google.common.collect.Lists;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import meteordevelopment.orbit.EventBus;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import violet.Main;
import violet.commands.violetcommands.Silly;
import violet.config.Config;
import violet.events.ConfigChangeEvent;
import violet.features.chat.*;
import violet.features.misc.*;
import violet.features.movement.AutoSprint;
import violet.features.movement.NoJumpCooldown;
import violet.features.player.BreakDelay;
import violet.features.player.HotbarScroll;
import violet.features.player.NoFrontPerspective;
import violet.features.player.SneakFix;
import violet.features.player.UseDelay;
import violet.features.render.DebugScreen;
import violet.features.render.Fullbright;
import violet.features.render.HeldItemTooltip;
import violet.features.render.LowFire;
import violet.features.render.NoBlockBreakParticles;
import violet.features.render.TimeChanger;
import violet.features.render.TooltipScale;
import violet.features.render.Viewmodel;
import violet.features.render.Zoom;
import violet.hud.HudEditorScreen;
import violet.hud.clickgui.components.FlatTextbox;
import violet.hud.clickgui.components.PlainLabel;
import violet.misc.RenderColor;
import violet.misc.Rendering;
import violet.misc.Utils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import static violet.Main.mc;

public class ClickGui extends BaseOwoScreen<FlowLayout> {
    public List<Category> categories;
    public ScrollContainer<FlowLayout> mainScroll;
    public int mouseX = 0;
    public int mouseY = 0;
    private int hash = 0;

    private boolean matchSearch(String text, String search) {
        return Utils.toLower(text).replace(" ", "").contains(Utils.toLower(search).replace(" ", ""));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() != GLFW.GLFW_KEY_LEFT && input.key() != GLFW.GLFW_KEY_RIGHT && input.key() != GLFW.GLFW_KEY_PAGE_DOWN && input.key() != GLFW.GLFW_KEY_PAGE_UP) {
            return super.keyPressed(input);
        } else {
            for (Category category : this.categories) {
                for (Module module : category.features) {
                    if (module.isInBoundingBox(this.mouseX, this.mouseY)) {
                        return category.scroll.onMouseScroll(0, 0, input.key() == GLFW.GLFW_KEY_PAGE_UP ? 4 : -4);
                    }
                }
            }
            return this.mainScroll.onMouseScroll(0, 0, input.key() == GLFW.GLFW_KEY_PAGE_UP ? 4 : -4);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Category category : this.categories) {
            for (Module module : category.features) {
                if (module.isInBoundingBox(this.mouseX, this.mouseY)) {
                    return category.scroll.onMouseScroll(0, 0, verticalAmount * 2);
                }
            }
        }
        return this.mainScroll.onMouseScroll(0, 0, verticalAmount * 2);
    }

    @Override
    public void drawComponentTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.drawComponentTooltip(context, mouseX, mouseY, delta);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        int height = context.guiHeight();
        context.text(this.font, "Left click a feature to toggle", 1, height - 20, RenderColor.white.argb);
        context.text(this.font, "Right click a feature open its settings", 1, height - 10, RenderColor.white.argb);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        FlowLayout parent = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());

        int viewmodelmultiplier = 1;
        if (Silly.silly) viewmodelmultiplier = 100;

        this.categories = Lists.newArrayList(
            // //////////////////////////////////////////////////////////////////////////////////
            // player
            // //////////////////////////////////////////////////////////////////////////////////
            new Category("Player", List.of(
                new Module("Break Delay", BreakDelay.instance, "<!> Disables the delay after breaking a block.", new Settings(List.of(
                    new Settings.EnumToggle<>("Only with", BreakDelay.mode, ""),
                    new Settings.Toggle("Only Creative", BreakDelay.onlyCreative, "Makes it not work without Creative Mode")
                ))),
                new Module("Hotbar Scroll", HotbarScroll.instance, "Utilities for hotbar scrolling", new Settings(List.of(
                    new Settings.Toggle("Lock Scroll", HotbarScroll.lockScroll, "Disables the ability to change slot with mouse wheel."),
                    new Settings.Toggle("No Overflow", HotbarScroll.noOverflow, "Locks scroll at the edges.")
                ))),
                new Module("No Front Perspective", NoFrontPerspective.instance, "Removes the front facing camera perspective."),
                new Module("Sneak Fix", SneakFix.instance, "Fixes the bug with camera bouncing while repeatedly sneaking."),
                new Module("Use Delay", UseDelay.instance, "<!> Disables the use delay.", new Settings(List.of(
                    new Settings.EnumToggle<>("Only for", UseDelay.mode, "Other - everything that isnt a block item."),
                    new Settings.Toggle("Only Creative", UseDelay.onlyCreative, "Makes it not work without Creative Mode")
                )))
            )),

            // /////////////////////////////////////////////////////////////////////////////////
            // movement
            // /////////////////////////////////////////////////////////////////////////////////
            new Category("Movement", List.of(
                new Module("AutoSprint", AutoSprint.instance, "Toggle Sprint (better than minecraft's as does on reset on world change)", new Settings(List.of(
                    new Settings.Keybind("Toggle", AutoSprint.toggleKey, "Swaps between sprinting and walking."),
                    new Settings.Toggle("Show Feedback", AutoSprint.feedback, "After toggling with keybind shows the current state.")
                ))),
                new Module("Jump Cooldown", NoJumpCooldown.instance, "<!> Removes the 10 tick jump delay. Some servers may consider this a cheat.", new Settings(List.of(
                    new Settings.Keybind("Toggle", NoJumpCooldown.toggleKey, "Disables/Enables the module.")
                )))
            )),

            // /////////////////////////////////////////////////////////////////////////////////
            // render
            // /////////////////////////////////////////////////////////////////////////////////
            new Category("Render", List.of(
                new Module("Debug Screen", DebugScreen.instance, "Modifiers for F3 screen", new Settings(List.of(
                    new Settings.SliderInt("XYZ precision", 1, 7, 1, DebugScreen.xyzPrecision, "Amount of digits after comma. Recommended 3 because everything after that doesn't matter\n except for very few edge cases (E-7 stepping) + floats loose precision past 7."),
                    new Settings.SliderInt("Facing precision", 1, 7, 1, DebugScreen.facingPrecision, "Amount of digits after comma. Recommended 2 as more dont affect movement at all.")
                ))),
                new Module("Fullbright", Fullbright.instance, "You know him, you love him.", new Settings(List.of(
                    new Settings.EnumToggle<>("Mode", Fullbright.mode, "The lighting mode.\n\nAmbient: Increases dimension ambient light, most reliable.\nGamma: Increases the Minecraft brightness setting to a high value.\nPotion: Permanently applies the Night Vision potion effect to your player."),
                    new Settings.Toggle("No Effect", Fullbright.noEffect, "Removes the Night Vision effect while active. Ignored if you use the Potion mode.")
                ))),
                new Module("Held Item Tooltip", HeldItemTooltip.instance, "Removes the text with held item's name."),
                new Module("Low Fire", LowFire.instance, "Lowers the fire overlay.", new Settings(List.of(
                    new Settings.Toggle("No Render", LowFire.noRender, "Skips the rendering entirerly")
                ))),
                //new Module("NBTTooltip", NBTTooltip.instance, "Shows NBT data in the tooltip of the item", new Settings(List.of(
                //    new Settings.Toggle("Only on Keybind", NBTTooltip.showWhileHeld, "Whether to show only when keybind is being held"),
                //    new Settings.Keybind("Show Keybind", NBTTooltip.showKeybind, "the keybind ^")
                //))),
                new Module("No Break Particles", NoBlockBreakParticles.instance, "Disables block breaking particles."),
                new Module("Time Changer", TimeChanger.instance, "Changes client-side time of day.", new Settings(List.of(
                    new Settings.SliderInt("Tick", 0, 24000, 1, TimeChanger.time, "6000 for noon, 18000 for midnight.")
                ))),
                new Module("Tooltip Scale", TooltipScale.instance, "Customize the scale of tooltips.", new Settings(List.of(
                    new Settings.EnumToggle<>("Mode", TooltipScale.mode, "The scaling mode.\n\nDynamic: Automatically scales down tooltips so that they always fit the screen.\nCustom: Scales tooltips using the Custom Scale value."),
                    new Settings.SliderDouble("Custom Scale", 0.0, 4.0, 0.01, TooltipScale.scale, "The custom scale multiplier. Ignored if using Dynamic mode.")
                ))),
                new Module("Viewmodel", Viewmodel.instance, "Easily customize the appearance of your held item.", new Settings(List.of(
                    new Settings.Toggle("No Haste", Viewmodel.noHaste, "Prevents Haste and Mining Fatigue from affecting your swing speed."),
                    new Settings.Toggle("No Equip Animation", Viewmodel.noEquip, "Removes the item swapping animation."),
                    new Settings.Toggle("No Bow Swing", Viewmodel.noBowSwing, "Removes the swing animation for all bows."),
                    new Settings.Toggle("Apply To Hand", Viewmodel.applyToHand, "Applies the viewmodel changes to the empty hand."),
                    new Settings.SliderInt("Swing Speed", 0, 100*viewmodelmultiplier, 1, Viewmodel.speed, "Apply a custom swing speed. Set to 0 to disable."),
                    new Settings.SliderDouble("Offset X", -2*viewmodelmultiplier, 2*viewmodelmultiplier, 0.01, Viewmodel.offsetX, "The X axis offset position of your held item."),
                    new Settings.SliderDouble("Offset Y", -2*viewmodelmultiplier, 2*viewmodelmultiplier, 0.01, Viewmodel.offsetY, "The Y axis offset position of your held item."),
                    new Settings.SliderDouble("Offset Z", -2*viewmodelmultiplier, 2*viewmodelmultiplier, 0.01, Viewmodel.offsetZ, "The Z axis offset position of your held item."),
                    new Settings.SliderDouble("Scale X", 0, 5*viewmodelmultiplier, 0.01, Viewmodel.scaleX, "The X axis scale of your held item."),
                    new Settings.SliderDouble("Scale Y", 0, 5*viewmodelmultiplier, 0.01, Viewmodel.scaleY, "The Y axis scale of your held item."),
                    new Settings.SliderDouble("Scale Z", 0, 5*viewmodelmultiplier, 0.01, Viewmodel.scaleZ, "The Z axis scale of your held item."),
                    new Settings.SliderDouble("Rotation X", -180, 180, 0.5, Viewmodel.rotX, "The X axis rotation of your held item."),
                    new Settings.SliderDouble("Rotation Y", -180, 180, 0.5, Viewmodel.rotY, "The Y axis rotation of your held item."),
                    new Settings.SliderDouble("Rotation Z", -180, 180, 0.5, Viewmodel.rotZ, "The Z axis rotation of your held item."),
                    new Settings.SliderDouble("Swing X", 0, 2*viewmodelmultiplier, 0.01, Viewmodel.swingX, "The X multiplier for swing animation offset."),
                    new Settings.SliderDouble("Swing Y", 0, 2*viewmodelmultiplier, 0.01, Viewmodel.swingY, "The Y multiplier for swing animation offset."),
                    new Settings.SliderDouble("Swing Z", 0, 2*viewmodelmultiplier, 0.01, Viewmodel.swingZ, "The Z multiplier for swing animation offset.")
                ))),
                new Module("Zoom", Zoom.instance, "Zoom", new Settings(List.of(
                    new Settings.DoubleInput("Zoom Scale", Zoom.scale, "how much to zoom in (min 1.0 as less freezes the game)"),
                    new Settings.Toggle("Cinematic Camera", Zoom.cinematic, "whether to turn on smooth camera like in optifine"),
                    new Settings.Keybind("zoom", Zoom.keybind, "zoom (hold)")
                )))
            )),

            // /////////////////////////////////////////////////////////////////////////////////
            //  chat
            // /////////////////////////////////////////////////////////////////////////////////
            new Category("Chat", List.of(
                new Module("Chat Rules", ChatRules.instance, "Create custom rules that activate when a matching message is sent in chat.", ChatRules.buildSettings()),
                new Module("Chat Tweaks", ChatPatches.instance, "Various features/improvements for the chat hud.", new Settings(List.of(
                    new Settings.Keybind("Copy Key", ChatPatches.copyKey, "Copies the hovered message to clipboard when pressed."),
                    new Settings.Keybind("Copy Line Key", ChatPatches.copyLineKey, "Copies the hovered line of a message to clipboard when pressed."),
                    new Settings.Toggle("Trim On Copy", ChatPatches.trimOnCopy, "Trims copied chat messages to remove any leading/trailing space characters."),
                    new Settings.Toggle("Message On Copy", ChatPatches.msgOnCopy, "Sends a feedback message in chat after copying any message."),
                    new Settings.SliderInt("Feedback Limit", 0, 512, 1, ChatPatches.copyMsgLength, "The max length of the copied message within the feedback message.\nHelps to prevent the chat from filling up when copying large messages."),
                    new Settings.Toggle("Keep History", ChatPatches.keepHistory, "Prevents the chat history from clearing on disconnect."),
                    new Settings.Toggle("Extra Lines", ChatPatches.extraLines, "Overrides the chat line limit. Allows you to keep more messages in the chat history."),
                    new Settings.SliderInt("Lines", 100, 5000, 10, ChatPatches.lines, "The chat line limit override.")
                ))),
                new Module("Command Aliases", CommandAliases.instance, "Create commands which send a specific message/command when ran.\nNote: A rejoin is required to fully apply the changes made.", CommandAliases.buildSettings()),
                new Module("Command Tooltip", CommandTooltip.instance, "Reveals the command that the hovered chat message would run when clicked."),
                new Module("Chat Cleaner", ChatCleaner.instance, ChatCleaner.tooltip, ChatCleaner.buildSettings())
            )),

            // /////////////////////////////////////////////////////////////////////////////////
            // misc
            // /////////////////////////////////////////////////////////////////////////////////
            new Category("Misc", List.of(
                new Module("ClickGui", ClickGuiFeature.instance, "this gui", new Settings(List.of(
                    new Settings.Keybind("Open GUI", ClickGuiFeature.openKey, "key to open this gui"),
                    new Settings.Toggle("Close If Opened", ClickGuiFeature.closeIfOpen, "If pressed while gui is already opened, it will close."),
                    new Settings.ColorPicker("Accent Color", ClickGuiFeature.accentColor, "color")
                ))),
                new Module("Command Keybinds", CommandKeybinds.instance, "Create keybinds that run a custom command when pressed.", CommandKeybinds.buildSettings()),
                new Module("No Confirm Screen", NoConfirmScreen.instance, "Skips 'confirm command execution' screen."),
                new Module("No Fps Limiter", NoFpsLimiter.instance, "Disables minecraft's \"limit fps when AFK/minimize\" very cool much wanted feature."),
                new Module("No Loading Screen", NoLoadingScreen.instance, "Removes \"loading terrain\" screen."),
                new Module("No Server Pack", NoServerPack.instance, "Skips server resource packs, both these required and not.", new Settings(List.of(
                        new Settings.Toggle("Dump Data", NoServerPack.dump, "Prints information about the resource pack to logs (im currently working on\nmaking it print to chat but its hard)")
                ))),
                new Module("Violet Commands", VioletCommands.instance, "Custom Violet Commands, defaulting to '.' as prefix", new Settings(List.of(
                    new Settings.TextInput("Prefix", VioletCommands.prefix, "Prefix of the commands. defaults to '.', more than 1 character will have no effect."),
                    new Settings.Toggle("Open Chat On Keybind", VioletCommands.openChatOnKeybind, "Whether to open chat upon pressing prefix on your keyboard."),
                    new Settings.Toggle("Add To Sent History", VioletCommands.addToHistory, "If on, previously executed commands will be avaible with ARROWUP key.")
                ))),
                new Module("Reconnect Button", ReconnectButton.instance, ReconnectButton.tooltip, new Settings(List.of(
                        new Settings.Toggle("Auto Reconnect", ReconnectButton.autoReconnect, "Automatically reconnects after given time."),
                        new Settings.SliderDouble("Reconnect Time", 0, 10, 0.05, ReconnectButton.autoReconnectTime, "Amount of time to wait before reconnecting")
                )))
            ))
        );


        this.categories.getLast().margins(Insets.of(5, 0, 3, 3));
        for (Category category : this.categories) {
            parent.child(category);
        }
        this.mainScroll = UIContainers.horizontalScroll(Sizing.fill(100), Sizing.fill(100), parent);
        this.mainScroll.scrollbarThiccness(2).scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xffffffff)));
        root.child(this.mainScroll);

        // open HUD Editor
        ButtonComponent hudEditorButton = UIComponents.button(Component.literal("Open HUD Editor"), button -> mc.gui.setScreen(new HudEditorScreen()));
        hudEditorButton.margins(Insets.of(0, 3, 0, 3));
        hudEditorButton.positioning(Positioning.relative(100, 100));
        hudEditorButton.renderer((context, button, delta) -> {
            context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
            Rendering.drawBorder(context, button.getX(), button.getY(), button.getWidth(), button.getHeight(), ClickGuiFeature.getAccentColor() | 0xFF000000);
        });
        root.child(hudEditorButton);

        //// presets button
        //ButtonComponent presetsButton = UIComponents.button(Component.literal("Presets"), button -> mc.gui.setScreen(new HudEditorScreen()));
        //presetsButton.margins(Insets.of(0, 3 + 20 + 1, 0, 3));
        //presetsButton.positioning(Positioning.relative(100, 100));
        //presetsButton.renderer((context, button, delta) -> {
        //    context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
        //    Rendering.drawBorder(context, button.getX(), button.getY(), button.getWidth(), button.getHeight(), ClickGuiFeature.getAccentColor() | 0xFF000000);
        //});
        //root.child(presetsButton);




        // search box
        FlatTextbox searchBox = new FlatTextbox(Sizing.fixed(200));
        searchBox.setSuggestion("Search...");
        searchBox.margins(Insets.of(0, 3, 0, 0));
        searchBox.positioning(Positioning.relative(50, 100));
        searchBox.onChanged().subscribe(value -> {
            if (value.isEmpty()) {
                searchBox.setSuggestion("Search...");
                for (Category category : this.categories) {
                    category.scroll.child().clearChildren();
                    for (Module module : category.features) {
                        module.horizontalSizing(Sizing.fixed(category.categoryWidth));
                        category.scroll.child().child(module);
                    }
                }
            } else {
                searchBox.setSuggestion("");
                for (Category category : this.categories) {
                    List<Module> features = new ArrayList<>(category.features);
                    features.removeIf(feature -> {
                        if (matchSearch(feature.label.getText(), value) || matchSearch(feature.label.getTooltip(), value)) {
                            return false;
                        }
                        if (feature.options != null) {
                            for (FlowLayout setting : feature.options.settings) {
                                for (UIComponent child : setting.children()) {
                                    if (child instanceof PlainLabel label) {
                                        if (matchSearch(label.getText(), value) || matchSearch(label.getTooltip(), value)) {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                        return true;
                    });
                    category.scroll.child().clearChildren();
                    for (Module module : features) {
                        module.horizontalSizing(Sizing.fixed(category.categoryWidth));
                        category.scroll.child().child(module);
                    }
                }
            }
        });
        root.child(searchBox);
    }

    @Override
    public void onClose() {
        if (hash != Config.getHash()) {
            Main.eventBus.post(new ConfigChangeEvent());
            Config.saveAsync();
            hash = Config.getHash();
        }
        if (this.uiAdapter != null) {
            this.uiAdapter.dispose();
        }
        super.onClose();
    }
}
