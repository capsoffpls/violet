package violet.misc;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.io.IOException;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.*;

import static violet.Main.*;

public class Utils {
    public static final GuiMessageTag violetIndicator = new GuiMessageTag(0x5ca0bf, null, Component.nullToEmpty("Message from violet mod."), "violet Mod");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Plays a sound at master volume category.
     *
     * @param event  the sound event to play
     * @param volume volume multiplier (1.0 = default)
     * @param pitch  pitch multiplier (1.0 = default)
     */
    public static void playSound(SoundEvent event, float volume, float pitch) {
        mc.getSoundManager().play(SimpleSoundInstance.forUI(event, pitch, volume));
    }

    /**
     * Plays a sound from a registry entry at master volume category.
     *
     * @param event  registry reference to the sound event
     * @param volume volume multiplier (1.0 = default)
     * @param pitch  pitch multiplier (1.0 = default)
     */
    public static void playSound(Holder.Reference<SoundEvent> event, float volume, float pitch) {
        playSound(event.value(), volume, pitch);
    }

    /**
     * Plays a sound by its namespaced identifier string at master volume category.
     *
     * @param event  sound identifier, e.g. {@code "minecraft:entity.player.levelup"}
     * @param volume volume multiplier (1.0 = default)
     * @param pitch  pitch multiplier (1.0 = default)
     */
    public static void playSound(String event, float volume, float pitch) {
        playSound(SoundEvent.createVariableRangeEvent(Identifier.parse(event)), volume, pitch);
    }
    

    /**
     * Sends a chat message or command on behalf of the player.
     * Messages starting with {@code /} are sent as commands
     *
     * @param message the message or command to send
     */
    public static void say(String message) {
        if (mc.player != null && !message.isEmpty()) {
            if (message.startsWith("/")) {
                mc.player.connection.sendCommand(message.substring(1));
            } else {
                mc.player.connection.sendChat(message);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    /// info (client side message)
    /////////////////////////////////////////////////////////////////////////////////////
    /// 
    public static MutableComponent getTag() {
        return Component.literal("[Violet] ").withColor(0x5ca0bf);
    }

    public static MutableComponent getShortTag() {
        return Component.literal("[V] ").withColor(0x5ca0bf);
    }

    /**
     * Sends client-side information message
     * @param message
     */
    public static void info(String message) {
        infoRaw(Component.literal(message));
    }

    public static void infoButton(String message, String command) {
        ClickEvent click = new ClickEvent.RunCommand(command);
        infoRaw(Component.literal(message).setStyle(Style.EMPTY.withClickEvent(click)));
    }

    public static void infoLink(String message, String url) {
        ClickEvent click = new ClickEvent.OpenUrl(URI.create(url));
        infoRaw(Component.literal(message).setStyle(Style.EMPTY.withClickEvent(click)));
    }

    public static void infoRaw(MutableComponent message) {
        if (message.getStyle().getColor() == null) {
            message.withColor(0xffffff);
        }
        mc.gui.hud.getChat().addMessage(getTag().append(message), null, GuiMessageSource.SYSTEM_CLIENT, violetIndicator);
    }

    public static void infoFormat(String message, Object... values) {
        infoRaw(Component.literal(format(message, values)));
    }
    ///////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Moves the player along with validating the pos by sending movepacket
     */
    public static void setPlayerPos(double x, double y, double z) {
        if (mc.player == null) return;
        mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(x,y,z,true,mc.player.horizontalCollision));
        mc.player.setPos(x, y,z);
    }
    
    /**
     * Checks if a PlayerEntity is a real player, and not an enemy or NPC. Some NPCs might falsely return true for a few seconds after spawning.
     */
    public static boolean isPlayer(Player entity) {
        ClientPacketListener handler = mc.getConnection();
        if (handler != null) {
            PlayerInfo listEntry = handler.getPlayerInfo(entity.getUUID());
            if (listEntry != null) {
                String displayName = listEntry.getProfile().name();
                if (displayName != null) {
                    String name = ChatFormatting.stripFormatting(displayName);
                    return !name.isEmpty() && !name.contains(" ");
                }
            }
        }
        return entity == mc.player;
    }

    /**
     * Check if the provided entity is a living entity (and in the case of player entities, if it isn't a real player).
     */
    public static boolean isMob(Entity entity) {
        if (entity instanceof Player player) {
            return !isPlayer(player);
        }
        return entity instanceof LivingEntity;
    }

    /**
     * Used to determine if game is in ready to play state, usually used before doing info() as it crashed the game
     */
    public static boolean canUpdate() {
        return mc != null && mc.level != null && mc.player != null;
    }


    public static void sendPingPacket() {
        ClientPacketListener handler = mc.getConnection();
        if (handler != null) {
            handler.send(new ServerboundPingRequestPacket(Util.getMillis()));
        }
    }

    /**
     * Returns the armor that the entity is wearing.
     */
    public static List<ItemStack> getEntityArmor(LivingEntity entity) {
        if (entity != null) {
            return List.of(
                    entity.getItemBySlot(EquipmentSlot.HEAD),
                    entity.getItemBySlot(EquipmentSlot.CHEST),
                    entity.getItemBySlot(EquipmentSlot.LEGS),
                    entity.getItemBySlot(EquipmentSlot.FEET)
            );
        }
        return List.of();
    }

    /**
     * Returns the custom data compound of the provided ItemStack, or else null.
     */
    public static CompoundTag getCustomData(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CustomData data = stack.get(DataComponents.CUSTOM_DATA);
            if (data != null) {
                return data.tag;
            }
        }
        return null;
    }


    public static void atomicWrite(Path path, String content) throws IOException {
        Path parent = path.getParent();
        String fileName = path.getFileName().toString();
        Path tempPath = parent.resolve(Utils.format("{}-Temp-{}.{}",
                fileName.substring(0, fileName.indexOf(".")),
                Util.getMillis(),
                fileName.substring(fileName.indexOf(".") + 1)
        ));
        if (!Files.exists(parent)) {
            Files.createDirectory(parent);
        }
        Files.writeString(tempPath, content);
        try {
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.deleteIfExists(tempPath);
    }

    public static void atomicWrite(Path path, JsonObject content) throws IOException {
        atomicWrite(path, GSON.toJson(content));
    }

    /**
     * Used for Entity mixins to check if the mixin is being applied to our own player entity.
     * <p>
     * <code>
     * if (isSelf(this)) {
     * do stuff...
     * }
     * </code>
     */
    public static boolean isSelf(Object entity) {
        return entity == mc.player;
    }

    public static String toLower(String string) {
        return string.toLowerCase(Locale.ROOT);
    }

    public static String toUpper(String string) {
        return string.toUpperCase(Locale.ROOT);
    }


    /**
     * Gets the string out of a Text object and removes any formatting codes.
     */
    public static String toPlain(Component text) {
        if (text != null) {
            return ChatFormatting.stripFormatting(text.getString());
        }
        return "";
    }

    public static Optional<Integer> parseInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> parseHex(String value) {
        try {
            return Optional.of((int) Long.parseLong(value.replace("0x", ""), 16));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Double> parseDouble(String value) {
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Long> parseLong(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static String parseDate(Calendar calendar) {
        return format("{} {}",
                calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()),
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(calendar.getTime())
        );
    }

    /**
     * Formats the string by replacing each set of curly brackets "{}" with one of the values in order, similarly to Rust's format macro.
     */
    public static String format(String string, Object... values) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (String section : Splitter.on("{}").split(string)) {
            builder.append(section);
            if (index < values.length) {
                builder.append(values[index]);
            }
            index++;
        }
        return builder.toString();
    }

    public static String formatDecimal(double number) {
        return formatDecimal(number, 2);
    }

    public static String formatDecimal(float number) {
        return formatDecimal(number, 2);
    }

    public static String formatDecimal(double number, int spaces) {
        return new DecimalFormat("0." + "0".repeat(spaces)).format(number);
    }

    public static String formatDecimal(float number, int spaces) {
        return formatDecimal((double) number, spaces);
    }

    public static long getMeasuringTime() {
        return Util.getMillis();
    }

    public static String getPercentageColor(double percentage, boolean inverse) {
        if (percentage > 0.66) {
            return inverse ? "§a" : "§c";
        }
        if (percentage > 0.33) {
            return "§6";
        }
        return inverse ? "§c" : "§a";
    }

    public static String getPercentageColor(float percentage, boolean inverse) {
        return getPercentageColor((double) percentage, inverse);
    }

    public static String getPercentageColor(double percentage) {
        return getPercentageColor(percentage, false);
    }

    public static String getPercentageColor(float percentage) {
        return getPercentageColor((double) percentage, false);
    }

    public static void setScreen(Screen screen) {
        mc.schedule(() -> mc.gui.setScreen(screen));
    }

    public static void showTitle(MutableComponent title, MutableComponent subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        mc.gui.hud.setTitle(title);
        mc.gui.hud.setSubtitle(subtitle);
        mc.gui.hud.setTimes(fadeInTicks, stayTicks, fadeOutTicks);
    }

    public static void showTitle(String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        showTitle(Component.literal(title), Component.literal(subtitle), fadeInTicks, stayTicks, fadeOutTicks);
    }

    public static String getServerIP() {
        ServerData info = mc.getCurrentServer();
        if (info == null) return "singleplayer";
        return toLower(info.ip);
    }


    public static String getHoveredMsg(boolean singleLine) {
        ChatComponent chatHud = mc.gui.hud.getChat();

        double mouseX = mc.mouseHandler.getScaledXPos(mc.getWindow());
        double mouseY = mc.mouseHandler.getScaledYPos(mc.getWindow());

        double scale = mc.options.chatScale().get();
        double lineSpacing = mc.options.chatLineSpacing().get();
        int lineHeight = (int) (9.0 * (lineSpacing + 1.0));

        double chatX = mouseX / scale - 4.0;
        double chatY = (mc.getWindow().getGuiScaledHeight() - mouseY - 40.0) / (scale * lineHeight);

        double chatWidth = Mth.floor(ChatComponent.getWidth(mc.options.chatWidth().get()) / scale);
        if (chatX < -4.0 || chatX > chatWidth || chatY < 0.0) return "";

        int visibleEnd = Math.min(
                chatHud.trimmedMessages.size(),
                chatHud.chatScrollbarPos + ChatComponent.getHeight(mc.options.chatHeightFocused().get()) / lineHeight
        );
        List<GuiMessage.Line> visibleMessages = chatHud.trimmedMessages.subList(chatHud.chatScrollbarPos, visibleEnd);

        int i = Mth.floor(chatY);
        if (i < 0 || i >= visibleMessages.size()) return "";

        StringBuilder builder = new StringBuilder();
        List<GuiMessage.Line> lines = new ArrayList<>();
        if (singleLine) {
            lines.add(visibleMessages.get(i));
        } else {
            for (int index = i + 1; index < visibleMessages.size(); index++) {
                GuiMessage.Line line = visibleMessages.get(index);
                if (line.endOfEntry()) break;
                lines.addFirst(line);
            }
            for (int index = i; index >= 0; index--) {
                GuiMessage.Line line = visibleMessages.get(index);
                lines.add(line);
                if (line.endOfEntry()) break;
            }
        }

        for (GuiMessage.Line line : lines) {
            line.content().accept((index, style, codePoint) -> {
                builder.appendCodePoint(codePoint);
                return true;
            });
        }
        return ChatFormatting.stripFormatting(builder.toString());
    }
}
