package com.sisariku.ciallomine;

import net.minecraft.client.MinecraftClient;

public class HeadLockState {
    private static boolean locked = false;
    private static float lockYaw = 0f;
    private static float lockPitch = 0f;
    /// 允许的偏航偏差角度：0=完全锁定，<0=不锁偏航，>0=允许±range
    private static float yawRange = 0f;
    /// 允许的俯仰偏差角度：0=完全锁定，<0=不锁俯仰，>0=允许±range
    private static float pitchRange = 0f;

    public static boolean isLocked() { return locked; }
    public static float getLockYaw() { return lockYaw; }
    public static float getLockPitch() { return lockPitch; }
    public static float getYawRange() { return yawRange; }
    public static float getPitchRange() { return pitchRange; }

    public static void lock() {
        var player = MinecraftClient.getInstance().player;
        if (player != null) lock(player.getYaw(), player.getPitch(), 0f, 0f);
    }

    public static void lock(float yaw, float pitch, float yr, float pr) {
        locked = true;
        lockYaw = yaw;
        lockPitch = Math.clamp(pitch, -90f, 90f);
        yawRange = yr;
        pitchRange = pr;
    }

    public static void unlock() { locked = false; }
}
