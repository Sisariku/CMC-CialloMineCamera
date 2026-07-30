package com.sisariku.ciallomine;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

public class CinemaMode {
    private static boolean enabled = false;
    private static float targetVertical = 0f, currentVertical = 0f;
    private static float targetHorizontal = 0f, currentHorizontal = 0f;
    private static float animationSpeed = 1.0f;
    private static boolean hideHand = false;

    // ── 字幕位置 + 文本快照 ──
    public static float titleX, titleY, subtitleX, subtitleY, actionbarX, actionbarY;
    public static Text storedTitle, storedSubtitle, storedActionbar;
    private static int titleDur, subtitleDur, actionbarDur; // ticks, 0=永久

    public static boolean isEnabled() { return enabled; }
    public static float getCurrentVertical() { return currentVertical; }
    public static float getCurrentHorizontal() { return currentHorizontal; }
    public static boolean shouldHideHand() { return enabled && hideHand; }

    public static void enable(float horizontal, float vertical, float speed, boolean hide) {
        enabled = true;
        targetHorizontal = Math.clamp(horizontal, 0f, 16f);
        targetVertical   = Math.clamp(vertical,   0f, 16f);
        animationSpeed = Math.abs(speed);
        hideHand = hide;
    }

    public static void disable(float speed) {
        targetHorizontal = 0f; targetVertical = 0f; animationSpeed = Math.abs(speed);
    }
    public static void disable() { targetHorizontal = 0f; targetVertical = 0f; }

    /// 设置字幕偏移 + 快照当前原版文本 + 持续时间
    public static void setTitleOverride(float x, float y, int durTicks) {
        titleX = x; titleY = y; titleDur = durTicks;
        storedTitle = copyHudText("title");
    }
    public static void setSubtitleOverride(float x, float y, int durTicks) {
        subtitleX = x; subtitleY = y; subtitleDur = durTicks;
        storedSubtitle = copyHudText("subtitle");
    }
    public static void setActionbarOverride(float x, float y, int durTicks) {
        actionbarX = x; actionbarY = y; actionbarDur = durTicks;
        storedActionbar = copyHudText("overlayMessage");
    }

    /// 反射读取 InGameHud 的私有字段
    private static Text copyHudText(String fieldName) {
        try {
            var hud = MinecraftClient.getInstance().inGameHud;
            if (hud == null) return null;
            Field f = hud.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (Text) f.get(hud);
        } catch (Exception e) { return null; }
    }

    public static void tick() {
        if (enabled) {
            float delta = animationSpeed * 0.05f;
            if (currentHorizontal < targetHorizontal)
                currentHorizontal = Math.min(currentHorizontal + delta, targetHorizontal);
            else if (currentHorizontal > targetHorizontal)
                currentHorizontal = Math.max(currentHorizontal - delta, targetHorizontal);
            if (currentVertical < targetVertical)
                currentVertical = Math.min(currentVertical + delta, targetVertical);
            else if (currentVertical > targetVertical)
                currentVertical = Math.max(currentVertical - delta, targetVertical);
            if (targetHorizontal <= 0.001f && currentHorizontal <= 0.001f
             && targetVertical   <= 0.001f && currentVertical   <= 0.001f)
                finishDisable();
        }
        // 字幕倒计时：到期清除文本 + 偏移
        if (titleDur > 0 && --titleDur <= 0) { clearTitle(); }
        if (subtitleDur > 0 && --subtitleDur <= 0) { clearSubtitle(); }
        if (actionbarDur > 0 && --actionbarDur <= 0) { clearActionbar(); }
    }

    private static void clearTitle()     { titleX = titleY = 0; storedTitle = null; }
    private static void clearSubtitle()  { subtitleX = subtitleY = 0; storedSubtitle = null; }
    private static void clearActionbar() { actionbarX = actionbarY = 0; storedActionbar = null; }

    public static void finishDisable() {
        enabled = false;
        currentHorizontal = targetHorizontal = currentVertical = targetVertical = 0f;
        hideHand = false;
        clearTitle(); clearSubtitle(); clearActionbar();
        titleDur = subtitleDur = actionbarDur = 0;
    }
}
