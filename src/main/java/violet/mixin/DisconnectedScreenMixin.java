package violet.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import violet.features.misc.ReconnectButton;

import static violet.Main.mc;


@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {
    @Shadow @Final private LinearLayout layout;
    @Unique private Button reconnectBtn;
    @Unique private double time = ReconnectButton.autoReconnectTime.value() * 20;

    protected DisconnectedScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;arrangeElements()V", shift = At.Shift.BEFORE))
    private void addButtons(CallbackInfo ci) {
        if (ReconnectButton.lastServerConnection != null && ReconnectButton.instance.isActive()) {
            reconnectBtn = new Button.Builder(Component.literal(getText()), _ -> tryConnecting()).build();
            layout.addChild(reconnectBtn);
        }
    }

    @Override
    public void tick() {
        if (!ReconnectButton.autoReconnect.value() || !ReconnectButton.instance.isActive()) return;

        if (time <= 0) {
            tryConnecting();
        } else {
            time -= 1;
            if (reconnectBtn != null) reconnectBtn.setMessage(Component.literal(getText()));
        }
    }

    @Unique
    private String getText() {
        String reconnectText = "Reconnect";
        if (ReconnectButton.autoReconnect.value()) reconnectText += " " + String.format("(%.1f)", time / 20);
        return reconnectText;
    }

    @Unique
    private void tryConnecting() {
        var lastServer = ReconnectButton.lastServerConnection;
        ConnectScreen.startConnecting(new TitleScreen(), mc, lastServer.left(), lastServer.right(), false, null);
    }
}
