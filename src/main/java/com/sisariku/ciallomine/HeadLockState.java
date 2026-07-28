package com.sisariku.ciallomine;

import net.minecraft.client.MinecraftClient;

/**
 * Client-side head-lock state. When locked, the local player's head
 * stays at a fixed yaw/pitch regardless of camera movement.
 */
public class HeadLockState {
    private static boolean locked = false;
    private static float lockYaw = 0f;    // degrees
    private static float lockPitch = 0f;  // degrees

    public static boolean isLocked() { return locked; }
    public static float getLockYaw() { return lockYaw; }
    public static float getLockPitch() { return lockPitch; }

    /** Lock head at current player rotation. */
    public static void lock() {
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            lock(player.getYaw(), player.getPitch());
        }
    }

    /** Lock head at specified angles (degrees). */
    public static void lock(float yaw, float pitch) {
        locked = true;
        lockYaw = yaw;
        lockPitch = Math.clamp(pitch, -90f, 90f);
        System.out.println("[CialloCamera] Head locked: yaw=" + yaw + " pitch=" + pitch);
    }

    public static void unlock() {
        locked = false;
        System.out.println("[CialloCamera] Head unlocked");
    }

    /** Get yaw in radians for model rendering. */
    public static float getYawRadians() { return lockYaw * (float) Math.PI / 180f; }
    public static float getPitchRadians() { return lockPitch * (float) Math.PI / 180f; }
}
