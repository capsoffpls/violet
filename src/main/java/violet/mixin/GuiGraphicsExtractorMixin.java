package violet.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;
import violet.features.chat.CommandTooltip;
import violet.features.render.TooltipScale;
import violet.misc.Utils;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
    @Shadow
    @Final
    private Matrix3x2fStack pose;

    @Shadow
    public abstract int guiWidth();

    @Shadow
    public abstract int guiHeight();

    @ModifyExpressionValue(method = "componentHoverEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/HoverEvent$ShowText;value()Lnet/minecraft/network/chat/Component;"))
    private Component getHoveredText(Component original, @Local(argsOnly = true, name = "hoveredStyle") Style hoveredStyle) {
        if (CommandTooltip.instance.isActive() && hoveredStyle.getClickEvent() instanceof ClickEvent.RunCommand(
                String command
        )) {
            return original.copy().append("\n\n").append(Utils.getShortTag().append(command));
        }
        return original;
    }

    @Inject(method = "tooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/TooltipRenderUtil;extractTooltipBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIILnet/minecraft/resources/Identifier;)V"))
    private void violet$tooltipRenderer(final Font font, final List<ClientTooltipComponent> lines, final int xo, final int yo, final ClientTooltipPositioner positioner, final @Nullable Identifier style, boolean extraSpaceAfterFirstLine, CallbackInfo ci, @Local(name = "textWidth") int textWidth, @Local(name = "tempHeight") int tempHeight) {
        if (TooltipScale.instance.isActive()) {
            if (TooltipScale.isDynamic()) {
                int screenX = this.guiWidth();
                int screenY = this.guiHeight();
                float scaleX = Math.min((float) screenX / (textWidth + 8), 1.0f);
                float scaleY = Math.min((float) screenY / (tempHeight + 8), 1.0f);
                float scale = Math.min(scaleX, scaleY);
                float offsetY = yo + (tempHeight * scale - yo);
                this.pose.translate(xo - xo * scale, offsetY - offsetY * scale);
                this.pose.scale(scale, scale);
            } else if (TooltipScale.isCustom()) {
                float scale = (float) TooltipScale.scale.value();
                this.pose.translate(xo - xo * scale, yo - yo * scale);
                this.pose.scale(scale, scale);
            }
        }
    }
}

