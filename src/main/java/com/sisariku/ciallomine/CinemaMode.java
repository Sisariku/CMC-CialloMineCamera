package com.sisariku.ciallomine;

/**
 * Client-side cinema mode state singleton.
 * Tracks whether cinema mode is active, bar height, animation speed, and hand visibility.
 */
public class CinemaMode {
    private static boolean enabled = false;
    private static float targetHeight = 0f;      // 0–5, target bar height
    private static float currentHeight = 0f;      // 0–5, animated current height
    private static float animationSpeed = 1.0f;   // units per tick toward target
    private static boolean hideHand = false;

    public static boolean isEnabled() { return enabled; }
    public static float getCurrentHeight() { return currentHeight; }
    public static float getTargetHeight() { return targetHeight; }
    public static boolean shouldHideHand() { return enabled && hideHand; }

    /** Enable cinema mode and set parameters. */
    public static void enable(float height, float speed, boolean hide) {
        enabled = true;
        targetHeight = Math.clamp(height, 0f, 16f);
        animationSpeed = Math.abs(speed);
        hideHand = hide;
        System.out.println("[CialloCamera] enable() called: height=" + targetHeight
            + " speed=" + animationSpeed + " hideHand=" + hideHand);
    }

    /** Disable with specific speed. */
    public static void disable(float speed) {
        targetHeight = 0f;
        animationSpeed = Math.abs(speed);
        System.out.println("[CialloCamera] disable(speed=" + speed + ") called");
    }

    /** Disable using current speed. */
    public static void disable() {
        targetHeight = 0f;
        System.out.println("[CialloCamera] disable() called: targetHeight=0, speed=" + animationSpeed);
    }

    /**
     * Tick the animation. Call each client tick.
     * Moves currentHeight toward targetHeight at animationSpeed per tick.
     */
    public static void tick() {
        if (!enabled) return;

        float delta = animationSpeed * 0.05f; // smooth per-tick step
        if (currentHeight < targetHeight) {
            currentHeight = Math.min(currentHeight + delta, targetHeight);
        } else if (currentHeight > targetHeight) {
            currentHeight = Math.max(currentHeight - delta, targetHeight);
        }

        // Log every 20 ticks (~1 sec) to show disable animation
        if (tickCounter++ % 20 == 0) {
            System.out.println("[CialloCamera] tick: enabled=" + enabled
                + " curH=" + String.format("%.3f", currentHeight)
                + " targetH=" + String.format("%.3f", targetHeight));
        }

        // When target is 0 and animation completes, fully disable
        if (targetHeight <= 0.001f && currentHeight <= 0.001f) {
            System.out.println("[CialloCamera] tick: animation complete → finishDisable()");
            finishDisable();
        }
    }

    /** Called by the disable path when animation completes. */
    public static void finishDisable() {
        System.out.println("[CialloCamera] finishDisable() — restoring HUD");
        enabled = false;
        currentHeight = 0f;
        targetHeight = 0f;
        hideHand = false;
    }

    private static long tickCounter = 0;

    /** Send feedback to the local player's chat (client-side only). */
    public static void logFeedback(String msg) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(net.minecraft.text.Text.literal("§6[CialloCamera] §r" + msg), false);
        }
    }
}
