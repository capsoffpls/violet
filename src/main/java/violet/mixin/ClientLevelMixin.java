package violet.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import violet.events.EntityRemovedEvent;
import violet.events.WorldTickEvent;
import violet.features.render.NoBlockBreakParticles;

//import violet.features.general.NoBlockBreakParticles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static violet.Main.eventBus;
import static violet.Main.mc;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {

    protected ClientLevelMixin(WritableLevelData properties, ResourceKey<Level> registryRef, RegistryAccess registryManager, Holder<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onWorldTick(CallbackInfo ci) {
        if (mc.player != null) {
            eventBus.post(new WorldTickEvent());
        }
    }

    @Inject(method = "removeEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setRemoved(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"))
    private void onBeforeRemoveEntity(int id, Entity.RemovalReason reason, CallbackInfo ci, @Local(name = "entity") Entity entity) {
        eventBus.post(new EntityRemovedEvent(entity, reason));
    }


    @Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
    private void onBreakParticle(BlockPos pos, BlockState blockState, CallbackInfo ci) {
        if (NoBlockBreakParticles.instance.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "addBreakingBlockEffects", at = @At("HEAD"), cancellable = true)
    private void onBreakingParticle(BlockPos pos, Direction direction, boolean playSound, CallbackInfo ci) {
        if (NoBlockBreakParticles.instance.isActive()) {
            ci.cancel();
        }
    }

}