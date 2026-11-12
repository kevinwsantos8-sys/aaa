package com.shadowkatana.client.mixin;

import com.shadowkatana.client.render.KatanaAnim;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Levanta o braco direito do jogador ao sacar/guardar a katana. */
@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin {

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"), require = 0)
    private void shadowkatana$drawArm(LivingEntity entity, float limbAngle, float limbDistance,
                                      float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity player)) return;
        float t = KatanaAnim.progress(player, animationProgress);
        if (t > 0f && t < 1f) {
            BipedEntityModel<?> self = (BipedEntityModel<?>) (Object) this;
            KatanaAnim.applyArm(self.rightArm, t);
        }
    }
}
