package com.sisariku.ciallomine.mixin;

import com.sisariku.ciallomine.CinemaMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!CinemaMode.isEnabled()) return;

        float h = CinemaMode.getCurrentHeight();
        // Always log to console when this fires with cinema mode active
        System.out.println("[CialloCamera] Mixin rendering: enabled=" + CinemaMode.isEnabled()
            + " height=" + h + " targetH=" + CinemaMode.getTargetHeight());

        if (h > 0.001f) {
            var client = MinecraftClient.getInstance();
            int sw = client.getWindow().getScaledWidth();
            int sh = client.getWindow().getScaledHeight();
            int bp = (int)(h * 8); // height 0–10 → 0–80 pixels per bar
            context.fill(0, 0, sw, bp, 0xFF000000);
            context.fill(0, sh - bp, sw, sh, 0xFF000000);
        }

        ci.cancel();
    }
}
