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
        float v = CinemaMode.getCurrentVertical();
        float h = CinemaMode.getCurrentHorizontal();
        if (v < 0.001f && h < 0.001f) return;
        var client = MinecraftClient.getInstance();
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int bp = (int)(v * 8); // 上下黑边像素高度
        int lr = (int)(h * 8); // 左右黑边像素宽度
        context.fill(0,      0,       sw, bp,      0xFF000000); // 上
        context.fill(0,      sh - bp, sw, sh,      0xFF000000); // 下
        context.fill(0,      0,       lr, sh,      0xFF000000); // 左
        context.fill(sw - lr, 0,      sw, sh,      0xFF000000); // 右
        ci.cancel();
    }
}
