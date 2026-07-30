package com.sisariku.ciallomine;

public class CameraZoom {
    private static boolean enabled = false;
    private static boolean inMode = true;
    private static float magnification = 1.0f;
    private static float targetZoomFactor = 1.0f;
    private static float currentZoomFactor = 1.0f;
    private static boolean smooth = false;
    private static float smoothSpeed = 1.0f;

    // Last-set parameters for C-key toggle
    private static boolean lastIn = true;
    private static float lastMag = 2f;
    private static float lastSpeed = 1f;
    private static boolean lastSmooth = false;
    private static boolean lastValid = false;

    public static boolean isEnabled() { return enabled; }
    public static float getZoomFactor() { return currentZoomFactor; }

    public static void set(boolean in, float mag, boolean sm, float smSpd) {
        enabled = true;
        inMode = in;
        magnification = Math.max(mag, 0.01f);
        smooth = sm;
        smoothSpeed = Math.max(smSpd, 0.01f);
        float f = in ? magnification : (1.0f / magnification);
        targetZoomFactor = f;
        if (!smooth) currentZoomFactor = f;
        // Save for C-key toggle
        lastIn = in; lastMag = mag; lastSpeed = smSpd; lastSmooth = sm; lastValid = true;
    }

    public static void reset() {
        enabled = false;
        magnification = 1.0f;
        targetZoomFactor = 1.0f;
        currentZoomFactor = 1.0f;
        smooth = false;
    }

    /** Toggle on/off using last valid parameters. */
    public static boolean toggle() {
        if (!lastValid) return false;
        if (enabled) { reset(); return false; }
        else { set(lastIn, lastMag, lastSmooth, lastSpeed); return true; }
    }

    public static void tick() {
        if (!enabled || !smooth) return;
        float step = smoothSpeed * 0.05f;
        if (currentZoomFactor < targetZoomFactor)
            currentZoomFactor = Math.min(currentZoomFactor + step, targetZoomFactor);
        else if (currentZoomFactor > targetZoomFactor)
            currentZoomFactor = Math.max(currentZoomFactor - step, targetZoomFactor);
    }
}
