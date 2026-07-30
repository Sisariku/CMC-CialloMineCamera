package com.sisariku.ciallomine.mixin;

import com.sisariku.ciallomine.OverShoulderState;
import com.sisariku.ciallomine.WaypointState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow @Final @Mutable private Vec3d pos;
    @Shadow @Final @Mutable private float yaw;
    @Shadow @Final @Mutable private float pitch;

    @Inject(method = "update", at = @At("TAIL"))
    private void afterUpdate(CallbackInfo ci) {
        // ── 优先级1：航点平滑移动 ──
        if (WaypointState.isMoving()) {
            Vec3d wp = WaypointState.getPathPos();
            if (wp != null) {
                this.pos = wp;
                this.yaw = WaypointState.getPathYaw();
                this.pitch = WaypointState.getPathPitch();
            }
            return;
        }

        // ── 优先级2：越肩视角 ──
        if (OverShoulderState.isEnabled()) {
            // 仅第三人称时生效
            var client = MinecraftClient.getInstance();
            if (client.options.getPerspective().isFirstPerson()) return;

            float dist   = OverShoulderState.getDistance();
            float offset = OverShoulderState.getOffset();
            float hgt    = OverShoulderState.getHeight();

            float yawRad   = this.yaw   * MathHelper.RADIANS_PER_DEGREE;
            float pitchRad = this.pitch * MathHelper.RADIANS_PER_DEGREE;
            float cosYaw   = MathHelper.cos(yawRad);
            float sinYaw   = MathHelper.sin(yawRad);

            // lookDir: 相机前方
            double lx = -sinYaw * MathHelper.cos(pitchRad);
            double ly = -MathHelper.sin(pitchRad);
            double lz =  cosYaw * MathHelper.cos(pitchRad);
            // rightDir: 相机右侧
            double rx =  cosYaw;
            double rz =  sinYaw;

            this.pos = new Vec3d(
                this.pos.x - lx * dist + rx * offset,
                this.pos.y - ly * dist + hgt,
                this.pos.z - lz * dist + rz * offset
            );
        }
    }
}
