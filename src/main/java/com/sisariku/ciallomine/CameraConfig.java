package com.sisariku.ciallomine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.Path;

public class CameraConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("ciallo-mine-camera.json");

    public float defaultHeight = 5.0f;
    public float defaultSpeed = 5.0f;
    public float defaultDisableSpeed = 5.0f;
    public boolean defaultHideHand = false;

    public float defaultHeadX = 0f;
    public float defaultHeadY = 0f;
    public float defaultHeadZ = 0f;

    private static CameraConfig INSTANCE;

    public static CameraConfig get() {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    private static CameraConfig load() {
        File file = CONFIG_PATH.toFile();
        if (file.exists()) {
            try (Reader r = new FileReader(file)) { return GSON.fromJson(r, CameraConfig.class); }
            catch (Exception e) { System.err.println("[CialloCamera] Read error: " + e.getMessage()); }
        }
        CameraConfig c = new CameraConfig();
        c.save();
        return c;
    }

    public void save() {
        CONFIG_PATH.toFile().getParentFile().mkdirs();
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) { GSON.toJson(this, w); }
        catch (Exception e) { System.err.println("[CialloCamera] Save error: " + e.getMessage()); }
    }
}
