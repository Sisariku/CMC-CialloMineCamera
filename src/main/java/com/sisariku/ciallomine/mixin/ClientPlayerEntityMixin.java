package com.sisariku.ciallomine.mixin;

import com.sisariku.ciallomine.WaypointState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// 航点播放时移动玩家实体位置（避免直接操作 Camera final 字段）
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (!WaypointState.isPlaying()) return;
        Vec3d pos = WaypointState.getPlayPos();
        if (pos != null) {
            var self = (ClientPlayerEntity)(Object)this;
            self.setPos(pos.x, pos.y, pos.z);
            self.setYaw(WaypointState.getPlayYaw());
            self.setPitch(WaypointState.getPlayPitch());
            self.setVelocity(0, 0, 0);
        }
    }
}
