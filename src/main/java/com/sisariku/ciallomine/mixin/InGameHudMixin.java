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

    private void renderSubtitles(DrawContext ctx, int sw, int sh, int barPixels) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        // title
        if (title != null) {
            int x = (int)(sw / 2f + CinemaMode.titleX);
            int y = (int)(barPixels / 2f + 4 + CinemaMode.titleY);
            ctx.drawTextWithShadow(tr, title, x - tr.getWidth(title) / 2, y, 0xFFFFFFFF);
        }
        // subtitle
        if (subtitle != null) {
            int x = (int)(sw / 2f + CinemaMode.subtitleX);
            int y = (int)(barPixels / 2f + 16 + CinemaMode.subtitleY);
            ctx.drawTextWithShadow(tr, subtitle, x - tr.getWidth(subtitle) / 2, y, 0xFFFFFFFF);
        }
        // actionbar
        if (overlayMessage != null && barPixels > 0) {
            int x = (int)(sw / 2f + CinemaMode.actionbarX);
            int y = (int)(sh - barPixels / 2f + CameraConfig.get().actionbarOffset + CinemaMode.actionbarY);
            ctx.drawTextWithShadow(tr, overlayMessage, x - tr.getWidth(overlayMessage) / 2, y, 0xFFFFFFFF);
        }
    }
}
