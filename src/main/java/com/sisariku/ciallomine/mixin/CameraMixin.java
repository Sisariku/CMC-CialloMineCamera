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
        // ── 优先级1：航点播放 ──
        if (WaypointState.isPlaying()) {
            Vec3d wp = WaypointState.getPlayPos();
            if (wp != null) {
                this.pos = wp;
                this.yaw = WaypointState.getPlayYaw();
                this.pitch = WaypointState.getPlayPitch();
            }
            return;
        }

        // ── 优先级2：越肩视角 ──
        if (OverShoulderState.isEnabled()) {
            var client = MinecraftClient.getInstance();
            if (client.options.getPerspective().isFirstPerson()) return;

            float dist   = OverShoulderState.getDistance();
            float offset = OverShoulderState.getOffset();
            float hgt    = OverShoulderState.getHeight();

            var self = (Camera)(Object)this;
            float yawRad   = self.getYaw()   * MathHelper.RADIANS_PER_DEGREE;
            float pitchRad = self.getPitch() * MathHelper.RADIANS_PER_DEGREE;
            float cosYaw   = MathHelper.cos(yawRad);
            float sinYaw   = MathHelper.sin(yawRad);

            double lx = -sinYaw * MathHelper.cos(pitchRad);
            double ly = -MathHelper.sin(pitchRad);
            double lz =  cosYaw * MathHelper.cos(pitchRad);
            double rx =  cosYaw;
            double rz =  sinYaw;

            this.pos = new Vec3d(
                self.getPos().x - lx * dist + rx * offset,
                self.getPos().y - ly * dist + hgt,
                self.getPos().z - lz * dist + rz * offset
            );
        }
    }
}
