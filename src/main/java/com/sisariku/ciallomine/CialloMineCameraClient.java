package com.sisariku.ciallomine;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.GameRules;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CialloMineCameraClient implements ClientModInitializer {

    private static KeyBinding zoomToggleKey;

    @Override
    public void onInitializeClient() {
        // ── C key keybind ──
        zoomToggleKey = new KeyBinding("key.ciallo-mine-camera.zoom_toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.ciallo-mine-camera");
        net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(zoomToggleKey);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var cfg = CameraConfig.get();
            dispatcher.register(
                ClientCommandManager.literal("ciallo")
                    .then(ClientCommandManager.literal("camera")

                        // ── movie ──
                        .then(ClientCommandManager.literal("movie")

                            .then(ClientCommandManager.literal("enable")
                                .executes(ctx -> {
                                    CinemaMode.enable(cfg.defaultHorizontal, cfg.defaultVertical, cfg.defaultSpeed, cfg.defaultHideHand);
                                    feedback(ctx.getSource(), "§a电影模式已开启 (默认值)");
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("horizontal", FloatArgumentType.floatArg(0f, 16f))
                                    .then(ClientCommandManager.argument("vertical", FloatArgumentType.floatArg(0f, 16f))
                                        .executes(ctx -> {
                                            float h = FloatArgumentType.getFloat(ctx, "horizontal");
                                            float v = FloatArgumentType.getFloat(ctx, "vertical");
                                            CinemaMode.enable(h, v, cfg.defaultSpeed, cfg.defaultHideHand);
                                            feedback(ctx.getSource(), "§a电影模式已开启 — 左右:" + f(h) + " 上下:" + f(v));
                                            return 1;
                                        })
                                        .then(ClientCommandManager.argument("speed", FloatArgumentType.floatArg(0.1f, 99f))
                                            .executes(ctx -> {
                                                float h = FloatArgumentType.getFloat(ctx, "horizontal");
                                                float v = FloatArgumentType.getFloat(ctx, "vertical");
                                                float s = FloatArgumentType.getFloat(ctx, "speed");
                                                CinemaMode.enable(h, v, s, cfg.defaultHideHand);
                                                feedback(ctx.getSource(), "§a电影模式已开启 — 左右:" + f(h) + " 上下:" + f(v) + " 速度:" + f(s));
                                                return 1;
                                            })
                                            .then(ClientCommandManager.argument("hideHand", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    float h = FloatArgumentType.getFloat(ctx, "horizontal");
                                                    float v = FloatArgumentType.getFloat(ctx, "vertical");
                                                    float s = FloatArgumentType.getFloat(ctx, "speed");
                                                    boolean hh = BoolArgumentType.getBool(ctx, "hideHand");
                                                    CinemaMode.enable(h, v, s, hh);
                                                    feedback(ctx.getSource(), "§a电影模式已开启 — 左右:" + f(h) + " 上下:" + f(v) + " 速度:" + f(s) + " 隐藏手臂:" + hh);
                                                    return 1;
                                                })
                                            )
                                        )
                                    )
                                )
                            )

                            .then(ClientCommandManager.literal("disable")
                                .executes(ctx -> {
                                    CinemaMode.disable(cfg.defaultDisableSpeed);
                                    feedback(ctx.getSource(), "§c电影模式已关闭");
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("speed", FloatArgumentType.floatArg(0.1f, 99f))
                                    .executes(ctx -> {
                                        CinemaMode.disable(FloatArgumentType.getFloat(ctx, "speed"));
                                        feedback(ctx.getSource(), "§c电影模式已关闭");
                                        return 1;
                                    })
                                )
                            )
                        )

                        // ── head ──
                        .then(ClientCommandManager.literal("head")
                            .then(ClientCommandManager.literal("lock")
                                .executes(ctx -> {
                                    HeadLockState.lock();
                                    feedback(ctx.getSource(), "§a头部已锁定 (当前视角)");
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("yaw", FloatArgumentType.floatArg(-180f, 180f))
                                    .executes(ctx -> {
                                        float y = FloatArgumentType.getFloat(ctx, "yaw");
                                        var p = MinecraftClient.getInstance().player;
                                        float pt = p != null ? p.getPitch() : 0f;
                                        HeadLockState.lock(y, pt, 0f, 0f);
                                        feedback(ctx.getSource(), "§a头部已锁定 — yaw:" + f(y));
                                        return 1;
                                    })
                                    .then(ClientCommandManager.argument("pitch", FloatArgumentType.floatArg(-90f, 90f))
                                        .executes(ctx -> {
                                            float y = FloatArgumentType.getFloat(ctx, "yaw");
                                            float p = FloatArgumentType.getFloat(ctx, "pitch");
                                            HeadLockState.lock(y, p, 0f, 0f);
                                            feedback(ctx.getSource(), "§a头部已锁定 — yaw:" + f(y) + " pitch:" + f(p));
                                            return 1;
                                        })
                                        .then(ClientCommandManager.argument("yawRange", FloatArgumentType.floatArg(-1f, 180f))
                                            .executes(ctx -> {
                                                float y = FloatArgumentType.getFloat(ctx, "yaw");
                                                float p = FloatArgumentType.getFloat(ctx, "pitch");
                                                float yr = FloatArgumentType.getFloat(ctx, "yawRange");
                                                HeadLockState.lock(y, p, yr, 0f);
                                                feedback(ctx.getSource(), "§a头部已锁定 — yaw:" + f(y) + " pitch:" + f(p) + " yawRange:" + f(yr));
                                                return 1;
                                            })
                                            .then(ClientCommandManager.argument("pitchRange", FloatArgumentType.floatArg(-1f, 90f))
                                                .executes(ctx -> {
                                                    float y = FloatArgumentType.getFloat(ctx, "yaw");
                                                    float p = FloatArgumentType.getFloat(ctx, "pitch");
                                                    float yr = FloatArgumentType.getFloat(ctx, "yawRange");
                                                    float pr = FloatArgumentType.getFloat(ctx, "pitchRange");
                                                    HeadLockState.lock(y, p, yr, pr);
                                                    feedback(ctx.getSource(), "§a头部已锁定 — yaw:" + f(y) + " pitch:" + f(p) + " yR:" + f(yr) + " pR:" + f(pr));
                                                    return 1;
                                                })
                                            )
                                        )
                                    )
                                )
                            )
                            .then(ClientCommandManager.literal("unlock")
                                .executes(ctx -> {
                                    HeadLockState.unlock();
                                    feedback(ctx.getSource(), "§a头部已解锁");
                                    return 1;
                                })
                            )
                        )

                        // ── zoom ──
                        .then(ClientCommandManager.literal("zoom")
                            .then(ClientCommandManager.literal("in")
                                .then(ClientCommandManager.argument("magnification", FloatArgumentType.floatArg(0.01f))
                                    .executes(ctx -> {
                                        float mag = FloatArgumentType.getFloat(ctx, "magnification");
                                        CameraZoom.set(true, mag, false, 0f);
                                        feedback(ctx.getSource(), "§a镜头缩放(in) 倍率:" + f(mag));
                                        return 1;
                                    })
                                    .then(ClientCommandManager.literal("smooth")
                                        .executes(ctx -> {
                                            float mag = FloatArgumentType.getFloat(ctx, "magnification");
                                            CameraZoom.set(true, mag, true, 1f);
                                            feedback(ctx.getSource(), "§a镜头缩放(in) 倍率:" + f(mag) + " 平滑");
                                            return 1;
                                        })
                                        .then(ClientCommandManager.argument("smoothSpeed", FloatArgumentType.floatArg(0.01f))
                                            .executes(ctx -> {
                                                float mag = FloatArgumentType.getFloat(ctx, "magnification");
                                                float ss = FloatArgumentType.getFloat(ctx, "smoothSpeed");
                                                CameraZoom.set(true, mag, true, ss);
                                                feedback(ctx.getSource(), "§a镜头缩放(in) 倍率:" + f(mag) + " 平滑:" + f(ss));
                                                return 1;
                                            })
                                        )
                                    )
                                    .then(ClientCommandManager.literal("direct").executes(ctx -> {
                                        float mag = FloatArgumentType.getFloat(ctx, "magnification");
                                        CameraZoom.set(true, mag, false, 0f);
                                        feedback(ctx.getSource(), "§a镜头缩放(in) 倍率:" + f(mag) + " 直接");
                                        return 1;
                                    }))
                                )
                            )
                            .then(ClientCommandManager.literal("out")
                                .then(ClientCommandManager.argument("magnification", FloatArgumentType.floatArg(0.01f))
                                    .executes(ctx -> {
                                        float mag = FloatArgumentType.getFloat(ctx, "magnification");
                                        CameraZoom.set(false, mag, false, 0f);
                                        feedback(ctx.getSource(), "§a镜头缩放(out) 倍率:" + f(mag));
                                        return 1;
                                    })
                                    .then(ClientCommandManager.literal("smooth")
                                        .executes(ctx -> {
                                            float mag = FloatArgumentType.getFloat(ctx, "magnification");
                                            CameraZoom.set(false, mag, true, 1f);
                                            feedback(ctx.getSource(), "§a镜头缩放(out) 倍率:" + f(mag) + " 平滑");
                                            return 1;
                                        })
                                        .then(ClientCommandManager.argument("smoothSpeed", FloatArgumentType.floatArg(0.01f))
                                            .executes(ctx -> {
                                                float mag = FloatArgumentType.getFloat(ctx, "magnification");
                                                float ss = FloatArgumentType.getFloat(ctx, "smoothSpeed");
                                                CameraZoom.set(false, mag, true, ss);
                                                feedback(ctx.getSource(), "§a镜头缩放(out) 倍率:" + f(mag) + " 平滑:" + f(ss));
                                                return 1;
                                            })
                                        )
                                    )
                                    .then(ClientCommandManager.literal("direct").executes(ctx -> {
                                        float mag = FloatArgumentType.getFloat(ctx, "magnification");
                                        CameraZoom.set(false, mag, false, 0f);
                                        feedback(ctx.getSource(), "§a镜头缩放(out) 倍率:" + f(mag) + " 直接");
                                        return 1;
                                    }))
                                )
                            )
                            .then(ClientCommandManager.literal("reset")
                                .executes(ctx -> { CameraZoom.reset(); feedback(ctx.getSource(), "§a镜头缩放已重置"); return 1; })
                            )
                        )

                        // ── overshoulder ──
                        .then(ClientCommandManager.literal("overshoulder")
                            .then(ClientCommandManager.literal("enable")
                                .executes(ctx -> {
                                    OverShoulderState.enable(cfg.defaultShoulderDistance, cfg.defaultShoulderOffset, cfg.defaultShoulderHeight);
                                    feedback(ctx.getSource(), "§a越肩视角已开启 (默认值)");
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("distance", FloatArgumentType.floatArg(-99f, 99f))
                                    .executes(ctx -> {
                                        float d = FloatArgumentType.getFloat(ctx, "distance");
                                        OverShoulderState.enable(d, cfg.defaultShoulderOffset, cfg.defaultShoulderHeight);
                                        feedback(ctx.getSource(), "§a越肩视角已开启 — 距离:" + f(d));
                                        return 1;
                                    })
                                    .then(ClientCommandManager.argument("offset", FloatArgumentType.floatArg(-99f, 99f))
                                        .executes(ctx -> {
                                            float d = FloatArgumentType.getFloat(ctx, "distance");
                                            float o = FloatArgumentType.getFloat(ctx, "offset");
                                            OverShoulderState.enable(d, o, cfg.defaultShoulderHeight);
                                            feedback(ctx.getSource(), "§a越肩视角已开启 — 距离:" + f(d) + " 偏移:" + f(o));
                                            return 1;
                                        })
                                        .then(ClientCommandManager.argument("height", FloatArgumentType.floatArg(-99f, 99f))
                                            .executes(ctx -> {
                                                float d = FloatArgumentType.getFloat(ctx, "distance");
                                                float o = FloatArgumentType.getFloat(ctx, "offset");
                                                float h = FloatArgumentType.getFloat(ctx, "height");
                                                OverShoulderState.enable(d, o, h);
                                                feedback(ctx.getSource(), "§a越肩视角已开启 — 距离:" + f(d) + " 偏移:" + f(o) + " 高度:" + f(h));
                                                return 1;
                                            })
                                        )
                                    )
                                )
                            )
                            .then(ClientCommandManager.literal("disable")
                                .executes(ctx -> { OverShoulderState.disable(); feedback(ctx.getSource(), "§c越肩视角已关闭"); return 1; })
                            )
                        )
                    )
            );
        });

        // ── waypoint ──
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var cfg = CameraConfig.get();
            dispatcher.register(
                ClientCommandManager.literal("ciallo")
                    .then(ClientCommandManager.literal("waypoint")
                        .then(ClientCommandManager.literal("add")
                            .executes(ctx -> {
                                WaypointState.add("wp_" + System.currentTimeMillis() % 100000);
                                feedback(ctx.getSource(), "§a航点已保存");
                                return 1;
                            })
                            .then(ClientCommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .executes(ctx -> {
                                    String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name");
                                    WaypointState.add(name);
                                    feedback(ctx.getSource(), "§a航点已保存: " + name);
                                    return 1;
                                })
                            )
                        )
                        .then(ClientCommandManager.literal("list")
                            .executes(ctx -> {
                                var wps = WaypointState.list();
                                if (wps.isEmpty()) { feedback(ctx.getSource(), "§7暂无航点"); return 1; }
                                var sb = new StringBuilder("§6航点列表:");
                                for (var w : wps) sb.append("\n  §e").append(w.name).append(" §7(").append(w.dimension).append(")");
                                feedback(ctx.getSource(), sb.toString());
                                return 1;
                            })
                        )
                        .then(ClientCommandManager.literal("remove")
                            .then(ClientCommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .executes(ctx -> {
                                    String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name");
                                    boolean ok = WaypointState.remove(name);
                                    feedback(ctx.getSource(), ok ? "§a航点已删除: " + name : "§c未找到航点: " + name);
                                    return 1;
                                })
                            )
                        )
                        .then(ClientCommandManager.literal("clear")
                            .executes(ctx -> { WaypointState.clear(); feedback(ctx.getSource(), "§a全部航点已清除"); return 1; })
                        )
                        .then(ClientCommandManager.literal("goto")
                            .then(ClientCommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .executes(ctx -> {
                                    String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name");
                                    boolean ok = WaypointState.gotoWaypoint(name, cfg.defaultWaypointSpeed);
                                    feedback(ctx.getSource(), ok ? "§a正在移动到: " + name : "§c未找到航点: " + name);
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("speed", FloatArgumentType.floatArg(0.1f))
                                    .executes(ctx -> {
                                        String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name");
                                        float spd = FloatArgumentType.getFloat(ctx, "speed");
                                        boolean ok = WaypointState.gotoWaypoint(name, spd);
                                        feedback(ctx.getSource(), ok ? "§a正在移动到: " + name + " 速度:" + f(spd) : "§c未找到航点: " + name);
                                        return 1;
                                    })
                                )
                            )
                        )
                        .then(ClientCommandManager.literal("stop")
                            .executes(ctx -> { WaypointState.stop(); feedback(ctx.getSource(), "§a移动已停止"); return 1; })
                        )
                    )
            );
        });

        ClientPlayNetworking.registerGlobalReceiver(CinemaModePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.enabled())
                    CinemaMode.enable(payload.horizontal(), payload.vertical(), payload.speed(), payload.hideHand());
                else
                    CinemaMode.disable(payload.speed());
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CinemaMode.tick();
            CameraZoom.tick();
            WaypointState.tick();
            // C key toggle zoom
            if (zoomToggleKey.wasPressed()) {
                boolean on = CameraZoom.toggle();
                if (!shouldSuppressFeedback() && client.player != null)
                    client.player.sendMessage(Text.literal("§6[CialloCamera] §r缩放" + (on ? "§a开启" : "§c关闭")), true);
            }
        });
    }

    /// 检查 sendCommandFeedback gamerule：优先取集成服务器（单人），
    /// 回退到客户端世界同步值（多人）。
    private static boolean shouldSuppressFeedback() {
        var client = MinecraftClient.getInstance();
        // 单人：集成服务器有真实的 gamerule
        var server = client.getServer();
        if (server != null) {
            var ow = server.getOverworld();
            if (ow != null) return !ow.getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK);
        }
        // 多人：客户端世界的 gamerule 应由服务端同步
        var world = client.world;
        if (world != null) return !world.getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK);
        return false;
    }

    private static void feedback(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src, String msg) {
        if (shouldSuppressFeedback()) return;
        src.sendFeedback(Text.literal("§6[CialloCamera] §r" + msg));
    }

    private static String f(float v) {
        return v == (int) v ? String.valueOf((int) v) : String.format("%.1f", v);
    }
}
