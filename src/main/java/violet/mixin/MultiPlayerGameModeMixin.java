package violet.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import violet.events.BlockBreakingCooldownEvent;

import static violet.Main.eventBus;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Shadow private int destroyDelay;

    @Redirect(method = "continueDestroyBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyDelay:I", opcode = Opcodes.PUTFIELD, ordinal = 1), require = 0)
    private void violet$creativeBreakDelayChange(MultiPlayerGameMode interactionManager, int value) {
        BlockBreakingCooldownEvent event = eventBus.post(new BlockBreakingCooldownEvent(value));
        destroyDelay = event.cooldown;
    }

    @Redirect(method = "continueDestroyBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyDelay:I", opcode = Opcodes.PUTFIELD, ordinal = 2), require = 0)
    private void violet$survivalBreakDelayChange(MultiPlayerGameMode interactionManager, int value) {
        BlockBreakingCooldownEvent event = eventBus.post(new BlockBreakingCooldownEvent(value));
        destroyDelay = event.cooldown;
    }

    @Redirect(method = "startDestroyBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyDelay:I", opcode = Opcodes.PUTFIELD), require = 0)
    private void violet$attackBlockBreakDelayChange(MultiPlayerGameMode interactionManager, int value) {
        BlockBreakingCooldownEvent event = eventBus.post(new BlockBreakingCooldownEvent(value));
        destroyDelay = event.cooldown;
    }
}