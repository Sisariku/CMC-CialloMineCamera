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

        float tickDelta = client.getRenderTickCounter().getTickDelta(true);
        float bodyYaw = MathHelper.lerp(tickDelta, entity.prevBodyYaw, entity.bodyYaw);
        var self = (PlayerEntityModel<?>) (Object) this;

        // ── 偏航 ──
        float yr = HeadLockState.getYawRange();
        if (yr >= 0f) {
            float lockedNetYaw = MathHelper.wrapDegrees(HeadLockState.getLockYaw() - bodyYaw);
            if (yr > 0f) {
                // 允许在锁定值 ±yr 范围内自由移动
                float freeNetYaw = MathHelper.wrapDegrees(headYaw); // setAngles 传入的偏航
                float diff = MathHelper.wrapDegrees(freeNetYaw - lockedNetYaw);
                diff = MathHelper.clamp(diff, -yr, yr);
                self.head.yaw = (lockedNetYaw + diff) * MathHelper.RADIANS_PER_DEGREE;
            } else {
                self.head.yaw = lockedNetYaw * MathHelper.RADIANS_PER_DEGREE;
            }
        }

        // ── 俯仰 ──
        float pr = HeadLockState.getPitchRange();
        if (pr >= 0f) {
            float lockP = HeadLockState.getLockPitch();
            if (pr > 0f) {
                float diff = MathHelper.clamp(headPitch - lockP, -pr, pr);
                self.head.pitch = (lockP + diff) * MathHelper.RADIANS_PER_DEGREE;
            } else {
                self.head.pitch = lockP * MathHelper.RADIANS_PER_DEGREE;
            }
        }
    }
}
