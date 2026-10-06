package violet.mixin;

import violet.events.InputEvent;
import violet.features.player.HotbarScroll;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.entity.player.Inventory;

import static violet.Main.eventBus;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void violet$onMouseButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (eventBus.post(new InputEvent(rawButtonInfo, action)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"), cancellable = true)
    private void violet$onBeforeSetSlot(long handle, double xoffset, double yoffset, CallbackInfo ci, @Local(name = "inventory") Inventory inventory) {
        if (!HotbarScroll.instance.isActive()) return;
        
        if (HotbarScroll.lockScroll.value()) ci.cancel();
        else if (HotbarScroll.noOverflow.value()) {
            int selected = inventory.getSelectedSlot();
            if (selected == 0 && (xoffset < 0.0 || yoffset > 0.0)) {
                ci.cancel();
            } else if (selected == 8 && (xoffset > 0.0 || yoffset < 0.0)) {
                ci.cancel();
            }
        }
    }
}
