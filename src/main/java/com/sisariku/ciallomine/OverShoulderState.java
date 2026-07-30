package com.sisariku.ciallomine;

import net.minecraft.client.MinecraftClient;

/// 越肩视角状态 —— 第三人称下相机偏移到角色侧后方
public class OverShoulderState {
    private static boolean enabled = false;
    private static float distance = 2.5f;   // 相机离玩家的距离
    private static float offset  = 0.8f;    // 水平偏移：+ 右侧后方，- 左侧后方
    private static float height  = 0.5f;    // 垂直偏移

    public static boolean isEnabled() { return enabled; }
    public static float getDistance() { return distance; }
    public static float getOffset()  { return offset; }
    public static float getHeight()  { return height; }

    public static void enable(float dist, float off, float h) {
        enabled = true;
        distance = Math.max(dist, -10f);
        offset   = off;
        height   = h;
        // 自动切到第三人称背面
        var client = MinecraftClient.getInstance();
        if (client.options != null)
            client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
    }

    public static void disable() { enabled = false; }

    /// C 键切换（与 zoom 互斥）
    public static boolean toggle(float dist, float off, float h) {
        if (enabled) { disable(); return false; }
        enable(dist, off, h);
        return true;
    }
}
