package com.sisariku.ciallomine;

import net.minecraft.text.Text;

public class CinemaMode {
    private static boolean enabled = false;
    private static float targetVertical = 0f, currentVertical = 0f;
    private static float targetHorizontal = 0f, currentHorizontal = 0f;
    private static float animationSpeed = 1.0f;
    private static boolean hideHand = false;

    // ── 字幕 ──
    public static float titleX, titleY, subtitleX, subtitleY, actionbarX, actionbarY;
    public static Text storedTitle, storedSubtitle, storedActionbar;
    private static int titleDur, subtitleDur, actionbarDur;

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

    /// 设置字幕（带文本）
    public static void setTitleOverride(float x, float y, int durTicks, String text) {
        titleX = x; titleY = y; titleDur = durTicks;
        storedTitle = text != null ? Text.literal(text) : null;
    }
    public static void setSubtitleOverride(float x, float y, int durTicks, String text) {
        subtitleX = x; subtitleY = y; subtitleDur = durTicks;
        storedSubtitle = text != null ? Text.literal(text) : null;
    }
    public static void setActionbarOverride(float x, float y, int durTicks, String text) {
        actionbarX = x; actionbarY = y; actionbarDur = durTicks;
        storedActionbar = text != null ? Text.literal(text) : null;
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
        if (titleDur > 0 && --titleDur <= 0)     { titleX = titleY = 0; storedTitle = null; }
        if (subtitleDur > 0 && --subtitleDur <= 0) { subtitleX = subtitleY = 0; storedSubtitle = null; }
        if (actionbarDur > 0 && --actionbarDur <= 0) { actionbarX = actionbarY = 0; storedActionbar = null; }
    }

    public static void finishDisable() {
        enabled = false;
        currentHorizontal = targetHorizontal = currentVertical = targetVertical = 0f;
        hideHand = false;
        titleX = titleY = subtitleX = subtitleY = actionbarX = actionbarY = 0;
        storedTitle = storedSubtitle = storedActionbar = null;
        titleDur = subtitleDur = actionbarDur = 0;
    }
}
