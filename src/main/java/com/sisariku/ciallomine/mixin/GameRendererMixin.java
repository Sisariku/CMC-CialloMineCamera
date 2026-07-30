package com.sisariku.ciallomine.mixin;

import com.sisariku.ciallomine.CameraZoom;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /**
     * Modify FOV when zoom is active.
     * Inject at RETURN of getFov() to capture and override the return value.
     */
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void modifyFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        if (!CameraZoom.isEnabled()) return;
        double original = cir.getReturnValue();
        cir.setReturnValue(original / CameraZoom.getZoomFactor());
    }
}
