package com.sisariku.ciallomine;

import net.minecraft.client.MinecraftClient;

public class HeadLockState {
    private static boolean locked = false;
    private static float lockYaw = 0f;
    private static float lockPitch = 0f;

    public static boolean isLocked() { return locked; }
    public static float getLockYaw() { return lockYaw; }
    public static float getLockPitch() { return lockPitch; }

    public static void lock() {
        var player = MinecraftClient.getInstance().player;
        if (player != null) lock(player.getYaw(), player.getPitch());
    }

    public static void lock(float yaw, float pitch) {
        locked = true;
        lockYaw = yaw;
        lockPitch = Math.clamp(pitch, -90f, 90f);
    }

    public static void unlock() { locked = false; }

    public static float getYawRadians() { return lockYaw * (float) Math.PI / 180f; }
    public static float getPitchRadians() { return lockPitch * (float) Math.PI / 180f; }
}
