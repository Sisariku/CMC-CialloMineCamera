package com.sisariku.ciallomine;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;

public class CialloMineCameraClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var cfg = CameraConfig.get();
            dispatcher.register(
                    ClientCommandManager.literal("ciallo")

                            // ── /ciallo camera ──
                            .then(ClientCommandManager.literal("camera")
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
                                                                HeadLockState.lock(y, cfg.defaultHeadY);
                                                                feedback(ctx.getSource(), "§a头部已锁定 — yaw:" + f(y));
                                                                return 1;
                                                            })
                                                            .then(ClientCommandManager.argument("pitch", FloatArgumentType.floatArg(-90f, 90f))
                                                                    .executes(ctx -> {
                                                                        float y = FloatArgumentType.getFloat(ctx, "yaw");
                                                                        float p = FloatArgumentType.getFloat(ctx, "pitch");
                                                                        HeadLockState.lock(y, p);
                                                                        feedback(ctx.getSource(), "§a头部已锁定 — yaw:" + f(y) + " pitch:" + f(p));
                                                                        return 1;
                                                                    })
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
                            )

                            // ── /ciallo movie ──
                            .then(ClientCommandManager.literal("movie")
                                    .then(ClientCommandManager.literal("enable")
                                            .executes(ctx -> {
                                                CinemaMode.enable(cfg.defaultHeight, cfg.defaultSpeed, cfg.defaultHideHand);
                                                feedback(ctx.getSource(), "§a电影模式已开启 (默认值)");
                                                return 1;
                                            })
                                            .then(ClientCommandManager.argument("height", FloatArgumentType.floatArg(0.1f, 16f))
                                                    .executes(ctx -> {
                                                        float h = FloatArgumentType.getFloat(ctx, "height");
                                                        CinemaMode.enable(h, cfg.defaultSpeed, cfg.defaultHideHand);
                                                        feedback(ctx.getSource(), "§a电影模式已开启 — 高度:" + f(h));
                                                        return 1;
                                                    })
                                                    .then(ClientCommandManager.argument("speed", FloatArgumentType.floatArg(0.1f, 99f))
                                                            .executes(ctx -> {
                                                                float h = FloatArgumentType.getFloat(ctx, "height");
                                                                float s = FloatArgumentType.getFloat(ctx, "speed");
                                                                CinemaMode.enable(h, s, cfg.defaultHideHand);
                                                                feedback(ctx.getSource(), "§a电影模式已开启 — 高度:" + f(h) + " 速度:" + f(s));
                                                                return 1;
                                                            })
                                                            .then(ClientCommandManager.argument("hideHand", BoolArgumentType.bool())
                                                                    .executes(ctx -> {
                                                                        float h = FloatArgumentType.getFloat(ctx, "height");
                                                                        float s = FloatArgumentType.getFloat(ctx, "speed");
                                                                        boolean hh = BoolArgumentType.getBool(ctx, "hideHand");
                                                                        CinemaMode.enable(h, s, hh);
                                                                        feedback(ctx.getSource(), "§a电影模式已开启 — 高度:" + f(h) + " 速度:" + f(s) + " 隐藏手臂:" + hh);
                                                                        return 1;
                                                                    })
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
            );
        });

        // 网络包监听注册
        ClientPlayNetworking.registerGlobalReceiver(CinemaModePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.enabled())
                    CinemaMode.enable(payload.height(), payload.speed(), payload.hideHand());
                else
                    CinemaMode.disable(payload.speed());
            });
        });

        // 客户端 Tick 监听注册
        ClientTickEvents.END_CLIENT_TICK.register(client -> CinemaMode.tick());
    }

    private static void feedback(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src, String msg) {
        src.sendFeedback(Text.literal("§6[CialloCamera] §r" + msg));
    }

    private static String f(float v) {
        return v == (int) v ? String.valueOf((int) v) : String.format("%.1f", v);
    }
}
