package com.sisariku.ciallomine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

/// 航点系统 —— 保存 / 列表 / 删除 / 平滑移动到指定航点
public class WaypointState {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("ciallo-mine-camera-waypoints.json");
    private static final Type LIST_TYPE = new TypeToken<List<Waypoint>>(){}.getType();

    private static final List<Waypoint> waypoints = new ArrayList<>();
    private static boolean loaded = false;

    // ── 平滑移动状态 ──
    private static boolean moving = false;
    private static double fromX, fromY, fromZ;
    private static float  fromYaw, fromPitch;
    private static double toX, toY, toZ;
    private static float  toYaw, toPitch;
    private static float  progress = 0f;   // 0..1
    private static float  moveSpeed = 3f;

    public static boolean isMoving() { return moving; }

    public static Vec3d getMovePos() {
        if (!moving) return null;
        return new Vec3d(
            MathHelper.lerp(progress, fromX, toX),
            MathHelper.lerp(progress, fromY, toY),
            MathHelper.lerp(progress, fromZ, toZ));
    }
    public static float getMoveYaw()   { return MathHelper.lerp(progress, fromYaw,   toYaw); }
    public static float getMovePitch() { return MathHelper.lerp(progress, fromPitch, toPitch); }

    // ── 持久化 ──

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        if (FILE.toFile().exists()) {
            try (Reader r = new FileReader(FILE.toFile())) {
                List<Waypoint> list = GSON.fromJson(r, LIST_TYPE);
                if (list != null) waypoints.addAll(list);
            } catch (Exception e) { System.err.println("[CialloCamera] Waypoint load error: " + e.getMessage()); }
        }
    }

    private static void save() {
        FILE.toFile().getParentFile().mkdirs();
        try (Writer w = new FileWriter(FILE.toFile())) { GSON.toJson(waypoints, w); }
        catch (Exception e) { System.err.println("[CialloCamera] Waypoint save error: " + e.getMessage()); }
    }

    // ── CRUD ──

    public static boolean add(String name) {
        ensureLoaded();
        var p = MinecraftClient.getInstance().player;
        if (p == null) return false;
        var world = MinecraftClient.getInstance().world;
        String dim = world != null ? world.getRegistryKey().getValue().toString() : "minecraft:overworld";
        waypoints.removeIf(w -> w.name.equals(name));
        waypoints.add(new Waypoint(name, p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch(), dim));
        save();
        return true;
    }

    public static List<Waypoint> list() { ensureLoaded(); return Collections.unmodifiableList(waypoints); }

    public static boolean remove(String name) {
        ensureLoaded();
        boolean ok = waypoints.removeIf(w -> w.name.equals(name));
        if (ok) save();
        return ok;
    }

    public static void clear() { ensureLoaded(); waypoints.clear(); save(); }

    // ── 平滑移动到指定航点 ──

    public static boolean gotoWaypoint(String name, float spd) {
        ensureLoaded();
        var p = MinecraftClient.getInstance().player;
        if (p == null) return false;
        var wp = waypoints.stream().filter(w -> w.name.equals(name)).findFirst().orElse(null);
        if (wp == null) return false;
        fromX = p.getX(); fromY = p.getY(); fromZ = p.getZ();
        fromYaw = p.getYaw(); fromPitch = p.getPitch();
        toX = wp.x; toY = wp.y; toZ = wp.z;
        toYaw = wp.yaw; toPitch = wp.pitch;
        progress = 0f; moveSpeed = spd; moving = true;
        return true;
    }

    public static void stop() { moving = false; }

    public static void tick() {
        if (!moving) return;
        progress += moveSpeed * 0.05f;
        if (progress >= 1f) { progress = 1f; moving = false; }
    }

    // ── 数据类 ──
    public static class Waypoint {
        public String name;
        public double x, y, z;
        public float yaw, pitch;
        public String dimension;

        public Waypoint() {}
        public Waypoint(String name, double x, double y, double z, float yaw, float pitch, String dim) {
            this.name = name; this.x = x; this.y = y; this.z = z;
            this.yaw = yaw; this.pitch = pitch; this.dimension = dim;
        }
    }
}
