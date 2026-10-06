package violet.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import violet.features.player.UseDelay;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow private int rightClickDelay;

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isItemEnabled(Lnet/minecraft/world/flag/FeatureFlagSet;)Z"))
    private void violet$removeUseDelay(CallbackInfo ci) {
        if (UseDelay.instance.isActive() && UseDelay.canChangeDelay()) rightClickDelay = 0;
    }
}
