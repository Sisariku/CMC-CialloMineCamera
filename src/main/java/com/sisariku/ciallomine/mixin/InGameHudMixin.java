package com.sisariku.ciallomine.mixin;

import com.sisariku.ciallomine.CameraConfig;
import com.sisariku.ciallomine.CinemaMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
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
        var client = MinecraftClient.getInstance();
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int bp = (int)(v * 8);
        int lr = (int)(h * 8);
        if (v > 0.001f || h > 0.001f) {
            context.fill(0, 0, sw, bp, 0xFF000000);
            context.fill(0, sh - bp, sw, sh, 0xFF000000);
            context.fill(0, 0, lr, sh, 0xFF000000);
            context.fill(sw - lr, 0, sw, sh, 0xFF000000);
        }
        renderSubtitles(context, sw, sh, bp);
        ci.cancel();
    }

    private void renderSubtitles(DrawContext ctx, int sw, int sh, int barPixels) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        // 优先用 CinemaMode 快照，否则回退原版（title/subtitle 已通过 setXxxOverride 快照）
        Text t = CinemaMode.storedTitle;
        Text s = CinemaMode.storedSubtitle;
        Text a = CinemaMode.storedActionbar;

        if (t != null) {
            int x = (int)(sw / 2f + CinemaMode.titleX);
            int y = (int)(barPixels / 2f + 4 + CinemaMode.titleY);
            ctx.drawTextWithShadow(tr, t, x - tr.getWidth(t) / 2, y, 0xFFFFFFFF);
        }
        if (s != null) {
            int x = (int)(sw / 2f + CinemaMode.subtitleX);
            int y = (int)(barPixels / 2f + 16 + CinemaMode.subtitleY);
            ctx.drawTextWithShadow(tr, s, x - tr.getWidth(s) / 2, y, 0xFFFFFFFF);
        }
        if (a != null && barPixels > 0) {
            int x = (int)(sw / 2f + CinemaMode.actionbarX);
            int y = (int)(sh - barPixels / 2f + CameraConfig.get().actionbarOffset + CinemaMode.actionbarY);
            ctx.drawTextWithShadow(tr, a, x - tr.getWidth(a) / 2, y, 0xFFFFFFFF);
        }
    }
}
