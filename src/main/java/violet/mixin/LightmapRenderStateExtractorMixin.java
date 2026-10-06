package violet.mixin;

import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.injection.Inject;
import violet.features.render.Fullbright;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {

    @Inject(method = "extract", at = @At("RETURN"))
    private void violet$fullbright(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
        if (!renderState.needsUpdate || !Fullbright.instance.isActive()) return;

        Fullbright.Mode mode = Fullbright.mode.value();
        if (mode == Fullbright.Mode.Ambient) {
            renderState.ambientColor = LightmapRenderStateExtractor.WHITE;
        } else if (mode == Fullbright.Mode.Gamma) {
            renderState.brightness = 1600.0f;
        }
    }
}
