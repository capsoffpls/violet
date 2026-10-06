package violet.mixin;

import static violet.Main.mc;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.sugar.Local;
import violet.features.render.DebugScreen;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;extractLines(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/util/List;ZI)V", ordinal = 0))
    private void violet$onExtractLeftLines(GuiGraphicsExtractor graphics, CallbackInfo ci, @Local(name = "leftLines") List<String> leftLines) {
        if (!DebugScreen.instance.isActive() || mc.player == null) return;
        violet$modifyLines(leftLines);
    }

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;extractLines(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/util/List;ZI)V", ordinal = 1))
    private void violet$onExtractRightLines(GuiGraphicsExtractor graphics, CallbackInfo ci, @Local(name = "rightLines") List<String> rightLines) {
        if (!DebugScreen.instance.isActive() || mc.player == null) return;
        violet$modifyLines(rightLines);
    }

    @Unique
    private void violet$modifyLines(List<String> lines) {
        // mc.player != null jest sprawdzane przed wywołaniem
        lines.replaceAll(line -> {
            if (line.startsWith("Facing:")) return "Facing: %s (%%.%df / %%.%df)".formatted(
                    mc.player.getDirection(),
                    DebugScreen.facingPrecision.value(),
                    DebugScreen.facingPrecision.value()
            ).formatted(
                    Mth.wrapDegrees(mc.player.getYRot()),
                    mc.player.getXRot()
            );

            if (line.startsWith("XYZ:")) return "XYZ: %%.%df / %%.%df / %%.%df".formatted(
                    DebugScreen.xyzPrecision.value(),
                    DebugScreen.xyzPrecision.value(),
                    DebugScreen.xyzPrecision.value()
            ).formatted(
                    mc.player.getX(),
                    mc.player.getY(),
                    mc.player.getZ()
            );

            return line;
        });
    }
}