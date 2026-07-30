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

/// 航点系统 —— 保存 / 列表 / 删除 / 顺序播放
public class WaypointState {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("ciallo-mine-camera-waypoints.json");
    private static final Type LIST_TYPE = new TypeToken<List<Waypoint>>(){}.getType();

    private static final List<Waypoint> waypoints = new ArrayList<>();
    private static boolean loaded = false;

    // ── 顺序播放状态 ──
    private static boolean playing = false;
    /// 每段起点：播放初始 = 玩家位置，后续 = 上一航点
    private static double segFromX, segFromY, segFromZ;
    private static float  segFromYaw, segFromPitch;
    /// 每段终点
    private static double segToX, segToY, segToZ;
    private static float  segToYaw, segToPitch;
    /// 0..1 当前段进度
    private static float segProgress = 0f;
    /// 当前正在前往的航点下标
    private static int playIndex = 0;
    /// 播放速度
    private static float playSpeed = 3f;
    /// 玩家起始位置（播放结束后返回）
    private static double returnX, returnY, returnZ;
    private static float  returnYaw, returnPitch;

    public static boolean isPlaying() { return playing; }

    public static Vec3d getPlayPos() {
        if (!playing) return null;
        return new Vec3d(
            MathHelper.lerp(segProgress, segFromX, segToX),
            MathHelper.lerp(segProgress, segFromY, segToY),
            MathHelper.lerp(segProgress, segFromZ, segToZ));
    }
    public static float getPlayYaw()   { return MathHelper.lerp(segProgress, segFromYaw,   segToYaw); }
    public static float getPlayPitch() { return MathHelper.lerp(segProgress, segFromPitch, segToPitch); }

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

    // ── 顺序播放 ──

    /// 从玩家当前位置开始，按顺序依次飞过所有航点，结束后返回玩家
    public static boolean play(float spd) {
        ensureLoaded();
        if (waypoints.isEmpty()) return false;
        var p = MinecraftClient.getInstance().player;
        if (p == null) return false;

        // 记录返回位置
        returnX = p.getX(); returnY = p.getY(); returnZ = p.getZ();
        returnYaw = p.getYaw(); returnPitch = p.getPitch();

        // 第一段：玩家 → waypoints[0]
        startSegment(returnX, returnY, returnZ, returnYaw, returnPitch,
                     waypoints.get(0).x, waypoints.get(0).y, waypoints.get(0).z,
                     waypoints.get(0).yaw, waypoints.get(0).pitch);
        playIndex = 0;
        playSpeed = spd;
        playing = true;
        return true;
    }

    public static void stopPlay() { playing = false; }

    private static void startSegment(double fx, double fy, double fz, float fyaw, float fpitch,
                                      double tx, double ty, double tz, float tyaw, float tpitch) {
        segFromX = fx; segFromY = fy; segFromZ = fz;
        segFromYaw = fyaw; segFromPitch = fpitch;
        segToX = tx; segToY = ty; segToZ = tz;
        segToYaw = tyaw; segToPitch = tpitch;
        segProgress = 0f;
    }

    public static void tick() {
        if (!playing) return;
        segProgress += playSpeed * 0.05f;
        if (segProgress < 1f) return;

        // 当前段完成，进入下一段
        playIndex++;
        if (playIndex < waypoints.size()) {
            // 上一航点 → 下一航点
            Waypoint prev = waypoints.get(playIndex - 1);
            Waypoint next = waypoints.get(playIndex);
            startSegment(prev.x, prev.y, prev.z, prev.yaw, prev.pitch,
                         next.x, next.y, next.z, next.yaw, next.pitch);
            segProgress = 0f;
        } else {
            // 最后一段：最后航点 → 返回玩家
            Waypoint last = waypoints.get(waypoints.size() - 1);
            startSegment(last.x, last.y, last.z, last.yaw, last.pitch,
                         returnX, returnY, returnZ, returnYaw, returnPitch);
            segProgress = 0f;
            playIndex = Integer.MAX_VALUE; // 标记为返回段
        }

        // 返回段也完成后停止
        if (playIndex > waypoints.size() && segProgress >= 1f)
            playing = false;
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
