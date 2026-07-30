package com.sisariku.ciallomine.mixin;

import com.sisariku.ciallomine.CinemaMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow @Nullable private Text title;
    @Shadow @Nullable private Text subtitle;
    @Shadow @Nullable private Text overlayMessage;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!CinemaMode.isEnabled()) return;
        float v = CinemaMode.getCurrentVertical();
        float h = CinemaMode.getCurrentHorizontal();
        var client = MinecraftClient.getInstance();
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int bp = (int)(v * 8);
        int lr = (int)(h * 8);
        if (v > 0.001f || h > 0.001f) {
            context.fill(0,      0,       sw, bp,      0xFF000000);
            context.fill(0,      sh - bp, sw, sh,      0xFF000000);
            context.fill(0,      0,       lr, sh,      0xFF000000);
            context.fill(sw - lr, 0,      sw, sh,      0xFF000000);
        }
        renderSubtitles(context, sw, sh, bp);
        ci.cancel();
    }

    /// 在黑边上渲染 title 和 actionbar（电影字幕效果）
    private void renderSubtitles(DrawContext ctx, int sw, int sh, int barPixels) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int cx = sw / 2;

        if (title != null) {
            int y = Math.max(barPixels / 2 - 8, 2);
            ctx.drawCenteredTextWithShadow(tr, title, cx, y - 10, 0xFFFFFFFF);
        }
        if (subtitle != null) {
            int y = Math.max(barPixels / 2 - 8, 2);
            ctx.drawCenteredTextWithShadow(tr, subtitle, cx, y + 4, 0xFFFFFFFF);
        }
        if (overlayMessage != null) {
            int y = sh - barPixels + Math.max(barPixels / 2 - 4, 2);
            ctx.drawCenteredTextWithShadow(tr, overlayMessage, cx, y - 4, 0xFFFFFFFF);
        }
    }
}
