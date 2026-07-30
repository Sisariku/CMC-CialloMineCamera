package com.sisariku.ciallomine.mixin;

import com.sisariku.ciallomine.OverShoulderState;
import com.sisariku.ciallomine.WaypointState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

    @Inject(method = "update", at = @At("TAIL"))
    private void afterUpdate(CallbackInfo ci) {
        var self = (Camera)(Object)this;
        var acc = (CameraAccessor)self;

        // ── 优先级1：航点播放 / 移动 ──
        if (WaypointState.isPlaying()) {
            Vec3d pos = WaypointState.getPlayPos();
            if (pos != null) {
                acc.invokeSetPos(pos.x, pos.y, pos.z);
                acc.invokeSetRotation(WaypointState.getPlayYaw(), WaypointState.getPlayPitch());
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

            float yawRad   = self.getYaw()   * MathHelper.RADIANS_PER_DEGREE;
            float pitchRad = self.getPitch() * MathHelper.RADIANS_PER_DEGREE;
            float cosYaw   = MathHelper.cos(yawRad);
            float sinYaw   = MathHelper.sin(yawRad);

            double lx = -sinYaw * MathHelper.cos(pitchRad);
            double ly = -MathHelper.sin(pitchRad);
            double lz =  cosYaw * MathHelper.cos(pitchRad);
            double rx =  cosYaw;
            double rz =  sinYaw;

            acc.invokeSetPos(
                self.getPos().x - lx * dist + rx * offset,
                self.getPos().y - ly * dist + hgt,
                self.getPos().z - lz * dist + rz * offset
            );
        }
    }
}
