# Ciallo Mine Camera

为 Minecraft Fabric 1.21.1 添加电影视角功能的客户端模组。

## 功能

- 🎬 **电影模式** — 上下 + 左右黑边遮幅，支持多比例构图，隐藏 HUD，平滑动画过渡
- 📝 **电影字幕** — 在黑边上直接渲染 title / subtitle / actionbar，可调位置和持续时长
- 📷 **越肩视角** — 第三人称相机偏移到角色侧后方
- 🔍 **镜头缩放** — FOV 缩放，支持平滑 / 直接两种模式，C 键快速切换
- 🧑 **头部锁定** — 锁定玩家模型头部朝向，支持轴独立 + 死区范围
- 📍 **航点系统** — 保存当前位置，平滑移动到指定航点
- 🖐️ **隐藏手持物品** — 电影模式下可选隐藏
- 🌐 **服务端同步** — OP 可推送电影模式给玩家
- ⚙️ **JSON 配置** — 全部默认参数可自定义

## 使用方法

1. 安装 Fabric Loader（≥ 0.19.3）和 Fabric API
2. 将模组 `.jar` 放入 `mods` 文件夹
3. 启动游戏，使用指令控制

## 指令

### 电影模式

```
/ciallo camera movie enable [<horizontal> <vertical>] [<speed>] [<hideHand>]
/ciallo camera movie disable [<speed>]
```

| 参数 | 说明 | 范围 | 默认值 |
|---|---|---|---|
| `horizontal` | 左右黑边厚度 | 0 ~ 16 | 0 |
| `vertical` | 上下黑边厚度 | 0 ~ 16 | 5.0 |
| `speed` | 动画速度 | 0.1 ~ 99 | 5.0 |
| `hideHand` | 隐藏手中物品 | true / false | false |

### 电影字幕

```
/ciallo camera movie title <x> <y> <duration> <text>
/ciallo camera movie subtitle <x> <y> <duration> <text>
/ciallo camera movie actionbar <x> <y> <duration> <text>
```

| 参数 | 说明 | 范围 |
|---|---|---|
| `x` | 水平偏移（像素，0=居中，正=右，负=左） | -999 ~ 999 |
| `y` | 垂直偏移（像素，正=下，负=上） | -999 ~ 999 |
| `duration` | 显示时长（秒，0=永久） | ≥ 0 |
| `text` | 文本内容（greedyString，支持中文和空格） | — |

### 越肩视角

```
/ciallo camera overshoulder enable [<distance>] [<offset>] [<height>]
/ciallo camera overshoulder disable
```

| 参数 | 说明 | 范围 | 默认值 |
|---|---|---|---|
| `distance` | 相机与玩家距离 | -99 ~ 99 | 2.5 |
| `offset` | 水平偏移（+ 右侧，- 左侧） | -99 ~ 99 | 0.8 |
| `height` | 垂直偏移 | -99 ~ 99 | 0.5 |

### 头部锁定

```
/ciallo camera head lock [<yaw>] [<pitch>] [<yawRange>] [<pitchRange>]
/ciallo camera head unlock
```

| 参数 | 说明 | 范围 |
|---|---|---|
| `yaw` | 锁定偏航角 | -180 ~ 180 |
| `pitch` | 锁定俯仰角 | -90 ~ 90 |
| `yawRange` | 偏航允许偏差（0=锁死，>0=±范围，-1=不锁） | -1 ~ 180 |
| `pitchRange` | 俯仰允许偏差（0=锁死，>0=±范围，-1=不锁） | -1 ~ 90 |

### 镜头缩放

```
/ciallo camera zoom in <magnification> [smooth [<speed>] | direct]
/ciallo camera zoom out <magnification> [smooth [<speed>] | direct]
/ciallo camera zoom reset
```

| 参数 | 说明 | 范围 |
|---|---|---|
| `magnification` | 放大 / 缩小倍率 | ≥ 0.01 |
| `speed` | 平滑过渡速度 | ≥ 0.01 |

### 航点

```
/ciallo waypoint add [<name>]
/ciallo waypoint list
/ciallo waypoint remove <name>
/ciallo waypoint clear
/ciallo waypoint goto <name> [<speed>]
```

航点保存在 `.minecraft/config/ciallo-mine-camera-waypoints.json`。

### 快捷键

| 按键 | 功能 |
|---|---|
| **C** | 切换镜头缩放 |

## 配置文件

`.minecraft/config/ciallo-mine-camera.json`：

```json
{
  "defaultVertical": 5.0,
  "defaultHorizontal": 0.0,
  "defaultSpeed": 5.0,
  "defaultDisableSpeed": 5.0,
  "defaultHideHand": false,
  "defaultShoulderDistance": 2.5,
  "defaultShoulderOffset": 0.8,
  "defaultShoulderHeight": 0.5,
  "defaultWaypointSpeed": 3.0,
  "actionbarOffset": 4.0
}
```

## 服务端同步

OP 可通过服务端指令推送电影模式（不指定玩家 = 全体）：

```
/ciallo camera movie enable <horizontal> <vertical> <speed> <hideHand>
/ciallo camera movie disable [<speed>]
```

## 更新日志

### v0.1.0 → v0.1.1

**新增**
- 镜头缩放 (`/ciallo camera zoom`) + C 键切换
- 越肩视角 (`/ciallo camera overshoulder`)
- 航点系统 (`/ciallo waypoint`) — 保存 / 列表 / 删除 / 平滑移动
- 双轴遮幅 — 电影模式支持上下 + 左右黑边
- 头部锁定轴独立 + 死区范围 (`yawRange` / `pitchRange`)
- 电影字幕 — 在黑边上直接渲染文本，支持位置和时长
- 服务端同步 (CustomPayload S2C)
- JSON 配置文件

**修复**
- `/gamerule sendCommandFeedback false` 现在正确静默反馈
- 黑边最小值从 0.1 改为 0
- 字幕不再依赖原版 `/title` timer，自管理倒计时
- 大量 Tab 补全缺失（服务端 stub 补齐参数节点）
- Camera Mixin 崩溃 `0xFFFFFFFF` → 改用安全的 `ClientPlayerEntityMixin`

## 开发

AI 辅助开发。

### 环境要求

- Java 21
- Minecraft 1.21.1
- Fabric Loader ≥ 0.19.3

### 构建

```bash
./gradlew build
```

构建产物位于 `build/libs/`。
