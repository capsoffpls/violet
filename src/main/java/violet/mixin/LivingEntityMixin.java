package violet.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import violet.features.render.Fullbright;
import violet.features.render.Viewmodel;
import violet.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean isHolding(Item item);

    @Inject(method = "getModifiedSwingDuration", at = @At("HEAD"), cancellable = true)
    private void violet$swingDuration(SwingAnimation animation, CallbackInfoReturnable<Integer> cir) {
        if (!Viewmodel.instance.isActive() || !Utils.isSelf(this)) return;

        if (Viewmodel.noBowSwing.value() && this.isHolding(Items.BOW)) {
            cir.setReturnValue(0);
            return;
        }

        if (Viewmodel.speed.value() > 0) {
            cir.setReturnValue(Viewmodel.speed.value());
            return;
        }

        if (Viewmodel.noHaste.value()) {
            cir.setReturnValue(animation.duration());
        }
    }

    @ModifyReturnValue(method = "hasEffect", at = @At("RETURN"))
    private boolean violet$hasNightVision(boolean original, Holder<MobEffect> effect) {
        if (Fullbright.instance.isActive() && Utils.isSelf(this) && effect == MobEffects.NIGHT_VISION) {
            if (Fullbright.noEffect.value() && !Fullbright.mode.value().equals(Fullbright.Mode.Potion)) {
                return false;
            }
        }
        return original;
    }
}
