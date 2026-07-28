package com.sisariku.ciallomine.mixin;

import com.sisariku.ciallomine.HeadLockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin {

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void onSetAngles(LivingEntity entity, float limbAngle, float limbDistance,
                              float animProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if (!HeadLockState.isLocked()) return;
        var client = MinecraftClient.getInstance();
        if (client.player == null || !entity.equals(client.player)) return;

        // Compute interpolated body yaw
        float tickDelta = client.getRenderTickCounter().getTickDelta(true);
        float bodyYaw = MathHelper.lerp(tickDelta, entity.prevBodyYaw, entity.bodyYaw);

        // Net head yaw = target - body, wrapped to [-180,180] to prevent flip
        float netYaw = MathHelper.wrapDegrees(HeadLockState.getLockYaw() - bodyYaw);

        var self = (PlayerEntityModel<?>) (Object) this;
        self.head.yaw = netYaw * MathHelper.RADIANS_PER_DEGREE;
        self.head.pitch = HeadLockState.getPitchRadians();
    }
}
