package com.sisariku.ciallomine;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CialloMineCamera implements ModInitializer {
    public static final String MOD_ID = "ciallo-mine-camera";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(CinemaModePayload.ID, CinemaModePayload.CODEC);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                CommandManager.literal("ciallo")
                    .then(CommandManager.literal("camera")
                        .then(CommandManager.literal("movie")
                            .then(buildEnable())
                            .then(buildDisable())
                        ).then(CommandManager.literal("head")
                            .then(buildLock())
                            .then(buildUnlock())
                        ).then(buildZoom())
                        .then(CommandManager.literal("overshoulder")
                            .then(CommandManager.literal("enable")
                                .executes(ctx -> fb(ctx, "§a越肩视角指令已发送"))
                                .then(CommandManager.argument("distance", FloatArgumentType.floatArg(-10f, 10f))
                                    .executes(ctx -> fb(ctx, "§a越肩视角指令已发送"))
                                    .then(CommandManager.argument("offset", FloatArgumentType.floatArg(-10f, 10f))
                                        .executes(ctx -> fb(ctx, "§a越肩视角指令已发送"))
                                        .then(CommandManager.argument("height", FloatArgumentType.floatArg(-10f, 10f))
                                            .executes(ctx -> fb(ctx, "§a越肩视角指令已发送"))
                                        )
                                    )
                                )
                            ).then(CommandManager.literal("disable")
                                .executes(ctx -> fb(ctx, "§a越肩视角指令已发送"))
                            )
                        )
                    ).then(CommandManager.literal("waypoint")
                        .then(CommandManager.literal("add")
                            .executes(ctx -> fb(ctx, "§a航点已保存"))
                            .then(CommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> fb(ctx, "§a航点已保存"))
                            )
                        ).then(CommandManager.literal("list")
                            .executes(ctx -> fb(ctx, "§a航点列表"))
                        ).then(CommandManager.literal("remove")
                            .then(CommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> fb(ctx, "§a航点已删除"))
                            )
                        ).then(CommandManager.literal("clear")
                            .executes(ctx -> fb(ctx, "§a全部航点已清除"))
                        ).then(CommandManager.literal("play")
                            .executes(ctx -> fb(ctx, "§a航点播放已发送"))
                            .then(CommandManager.argument("speed", FloatArgumentType.floatArg(0.1f))
                                .executes(ctx -> fb(ctx, "§a航点播放已发送"))
                            )
                        ).then(CommandManager.literal("stop")
                            .executes(ctx -> fb(ctx, "§a航点停止已发送"))
                        )
                    )
            );
        });
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<ServerCommandSource, ?> buildEnable() {
        var cfg = CameraConfig.get();
        var enable = CommandManager.literal("enable")
            .executes(ctx -> doEnable(ctx, cfg.defaultHorizontal, cfg.defaultVertical, cfg.defaultSpeed, cfg.defaultHideHand));

        var horizontal = CommandManager.argument("horizontal", FloatArgumentType.floatArg(0.1f, 16f));
        var vertical = CommandManager.argument("vertical", FloatArgumentType.floatArg(0.1f, 16f))
            .executes(ctx -> doEnable(ctx,
                FloatArgumentType.getFloat(ctx, "horizontal"), FloatArgumentType.getFloat(ctx, "vertical"), cfg.defaultSpeed, cfg.defaultHideHand));

        var speed = CommandManager.argument("speed", FloatArgumentType.floatArg(0.1f, 99f))
            .executes(ctx -> doEnable(ctx,
                FloatArgumentType.getFloat(ctx, "horizontal"), FloatArgumentType.getFloat(ctx, "vertical"),
                FloatArgumentType.getFloat(ctx, "speed"), cfg.defaultHideHand));

        var hideHand = CommandManager.argument("hideHand", BoolArgumentType.bool())
            .executes(ctx -> doEnable(ctx,
                FloatArgumentType.getFloat(ctx, "horizontal"), FloatArgumentType.getFloat(ctx, "vertical"),
                FloatArgumentType.getFloat(ctx, "speed"), BoolArgumentType.getBool(ctx, "hideHand")));

        speed.then(hideHand);
        vertical.then(speed);
        horizontal.then(vertical);
        enable.then(horizontal);
        return enable;
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<ServerCommandSource, ?> buildDisable() {
        var cfg = CameraConfig.get();
        return CommandManager.literal("disable")
            .executes(ctx -> doDisable(ctx, cfg.defaultDisableSpeed))
            .then(CommandManager.argument("speed", FloatArgumentType.floatArg(0.1f, 99f))
                .executes(ctx -> doDisable(ctx, FloatArgumentType.getFloat(ctx, "speed"))));
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<ServerCommandSource, ?> buildLock() {
        var cfg = CameraConfig.get();
        var lock = CommandManager.literal("lock")
            .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("§a头部锁定指令已发送"), true); return 1; });
        var yaw = CommandManager.argument("yaw", FloatArgumentType.floatArg(-180f, 180f))
            .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("§a头部锁定指令已发送"), true); return 1; });
        var pitch = CommandManager.argument("pitch", FloatArgumentType.floatArg(-90f, 90f))
            .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("§a头部锁定指令已发送"), true); return 1; });
        yaw.then(pitch); lock.then(yaw);
        return lock;
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<ServerCommandSource, ?> buildUnlock() {
        return CommandManager.literal("unlock")
            .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("§a头部解锁指令已发送"), true); return 1; });
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<ServerCommandSource, ?> buildZoom() {
        var magBranch = CommandManager.argument("magnification", FloatArgumentType.floatArg(0.01f))
            .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("§a镜头缩放"), true); return 1; })
            .then(CommandManager.literal("smooth")
                .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("§a镜头缩放(平滑)"), true); return 1; })
                .then(CommandManager.argument("smoothSpeed", FloatArgumentType.floatArg(0.01f))
                    .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("§a镜头缩放(平滑)"), true); return 1; })
                )
            )
            .then(CommandManager.literal("direct")
                .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("§a镜头缩放(直接)"), true); return 1; })
            );

        var zoom = CommandManager.literal("zoom");
        zoom.then(CommandManager.literal("in").then(magBranch));
        zoom.then(CommandManager.literal("out").then(magBranch));
        zoom.then(CommandManager.literal("reset")
            .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("§a镜头缩放已重置"), true); return 1; }));
        return zoom;
    }

    private static int doEnable(CommandContext<ServerCommandSource> ctx, float h, float v, float s, boolean hide) {
        var payload = new CinemaModePayload(h, v, s, hide, true);
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player != null) {
            ServerPlayNetworking.send(player, payload);
            ctx.getSource().sendFeedback(
                () -> Text.literal("§a电影模式已开启 — 左右:" + fmt(h) + " 上下:" + fmt(v) + " 速度:" + fmt(s) + " 隐藏手臂:" + hide), true);
        } else {
            for (var p : ctx.getSource().getServer().getPlayerManager().getPlayerList())
                ServerPlayNetworking.send(p, payload);
            ctx.getSource().sendFeedback(() -> Text.literal("§a已对全体玩家开启电影模式"), true);
        }
        return 1;
    }

    private static int doDisable(CommandContext<ServerCommandSource> ctx, float spd) {
        var payload = new CinemaModePayload(0f, 0f, spd, false, false);
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player != null) {
            ServerPlayNetworking.send(player, payload);
            ctx.getSource().sendFeedback(() -> Text.literal("§c电影模式已关闭"), true);
        } else {
            for (var p : ctx.getSource().getServer().getPlayerManager().getPlayerList())
                ServerPlayNetworking.send(p, payload);
            ctx.getSource().sendFeedback(() -> Text.literal("§c已对全体玩家关闭电影模式"), true);
        }
        return 1;
    }

    private static int fb(CommandContext<ServerCommandSource> ctx, String msg) {
        ctx.getSource().sendFeedback(() -> Text.literal(msg), true);
        return 1;
    }

    private static String fmt(float v) {
        return v == (int) v ? String.valueOf((int) v) : String.format("%.1f", v);
    }
}
