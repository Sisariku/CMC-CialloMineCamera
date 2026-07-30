package com.sisariku.ciallomine.mixin;

import com.sisariku.ciallomine.WaypointState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// 航点移动时平滑更新玩家位置
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (!WaypointState.isMoving()) return;
        Vec3d pos = WaypointState.getMovePos();
        if (pos != null) {
            var self = (ClientPlayerEntity)(Object)this;
            self.setPos(pos.x, pos.y, pos.z);
            self.setYaw(WaypointState.getMoveYaw());
            self.setPitch(WaypointState.getMovePitch());
            self.setVelocity(0, 0, 0);
        }
    }
}
