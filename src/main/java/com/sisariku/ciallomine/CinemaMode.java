package com.sisariku.ciallomine;

public class CinemaMode {
    private static boolean enabled = false;
    /// 上下黑边（垂直遮幅）厚度
    private static float targetVertical = 0f;
    private static float currentVertical = 0f;
    /// 左右黑边（水平遮幅）厚度
    private static float targetHorizontal = 0f;
    private static float currentHorizontal = 0f;
    private static float animationSpeed = 1.0f;
    private static boolean hideHand = false;

    // ── 字幕位置覆盖 ──
    public static float titleX, titleY;
    public static float subtitleX, subtitleY;
    public static float actionbarX, actionbarY;
    private static int titleDur, subtitleDur, actionbarDur; // ticks, 0=永久

    public static boolean isEnabled() { return enabled; }
    public static float getCurrentVertical() { return currentVertical; }
    public static float getCurrentHorizontal() { return currentHorizontal; }
    public static boolean shouldHideHand() { return enabled && hideHand; }

    /// @param horizontal 左右黑边厚度 (0~16)
    /// @param vertical   上下黑边厚度 (0~16)
    public static void enable(float horizontal, float vertical, float speed, boolean hide) {
        enabled = true;
        targetHorizontal = Math.clamp(horizontal, 0f, 16f);
        targetVertical   = Math.clamp(vertical,   0f, 16f);
        animationSpeed = Math.abs(speed);
        hideHand = hide;
    }

    public static void disable(float speed) {
        targetHorizontal = 0f;
        targetVertical   = 0f;
        animationSpeed = Math.abs(speed);
    }

    public static void disable() {
        targetHorizontal = 0f;
        targetVertical   = 0f;
    }

    /// 设置字幕位置覆盖
    public static void setTitleOverride(float x, float y, int durTicks) {
        titleX = x; titleY = y; titleDur = durTicks;
    }
    public static void setSubtitleOverride(float x, float y, int durTicks) {
        subtitleX = x; subtitleY = y; subtitleDur = durTicks;
    }
    public static void setActionbarOverride(float x, float y, int durTicks) {
        actionbarX = x; actionbarY = y; actionbarDur = durTicks;
    }

    public static void tick() {
        // ── 黑边动画 ──
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

        // ── 字幕倒计时 ──
        if (titleDur > 0 && --titleDur <= 0) { titleX = 0; titleY = 0; }
        if (subtitleDur > 0 && --subtitleDur <= 0) { subtitleX = 0; subtitleY = 0; }
        if (actionbarDur > 0 && --actionbarDur <= 0) { actionbarX = 0; actionbarY = 0; }
    }

    public static void finishDisable() {
        enabled = false;
        currentHorizontal = 0f; targetHorizontal = 0f;
        currentVertical   = 0f; targetVertical   = 0f;
        hideHand = false;
        // 清除字幕覆盖
        titleX = titleY = subtitleX = subtitleY = actionbarX = actionbarY = 0;
        titleDur = subtitleDur = actionbarDur = 0;
    }

    public static void logFeedback(String msg) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player != null)
            client.player.sendMessage(net.minecraft.text.Text.literal("§6[CialloCamera] §r" + msg), false);
    }
}
