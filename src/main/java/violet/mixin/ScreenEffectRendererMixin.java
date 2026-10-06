package violet.mixin;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.joml.Matrix4f;
import violet.features.render.LowFire;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
	@Inject(method = "submitFire", at = @At("HEAD"), cancellable = true)
	private static void violet$onRenderFireOverlay2(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, TextureAtlasSprite sprite, CallbackInfo ci) {
		if (LowFire.noRender.value()) ci.cancel();
	}

	@Inject(method = "buildFireQuad", at = @At("HEAD"))
	private static void violet$onBuildFireQuad(TextureAtlasSprite sprite, VertexConsumer builder, Matrix4f pose, CallbackInfo ci) {
		if (LowFire.instance.isActive()) {
			pose.translate(0.0F, -0.3F, 0.0F);
		}
	}
}