# Ciallo Mine Camera

为 Minecraft Fabric 1.21.1 添加电影视角功能的客户端模组。

## 功能

- 🎬 **电影模式** — 上下 + 左右黑边遮幅，支持多比例构图，隐藏 HUD，平滑动画过渡
- 📷 **越肩视角** — 第三人称相机偏移到角色侧后方，适合拍摄角色特写
- 🔍 **镜头缩放** — FOV 缩放，支持平滑 / 直接两种模式，可按 C 键快速切换
- 🧑 **头部锁定** — 锁定玩家模型头部朝向，独立于相机旋转
- 📍 **航点系统** — 保存当前位置，平滑移动 / 列表 / 删除
- 🖐️ **隐藏手持物品** — 电影模式下可选隐藏手中物品
- 🌐 **服务端同步** — 管理员可通过服务端指令推送电影模式给玩家
- ⚙️ **JSON 配置文件** — 自定义默认参数

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
| `horizontal` | 左右黑边厚度 | 0.1 ~ 16 | 0 |
| `vertical` | 上下黑边厚度 | 0.1 ~ 16 | 5.0 |
| `speed` | 动画速度 | 0.1 ~ 99 | 5.0 |
| `hideHand` | 是否隐藏手中物品 | true / false | false |

> 不指定参数时使用配置文件中的默认值。`horizontal` 和 `vertical` 配合可实现任意画面比例遮罩（如 2.35:1 宽银幕、方形构图等）。

### 越肩视角

```
/ciallo camera overshoulder enable [<distance>] [<offset>] [<height>]
/ciallo camera overshoulder disable
```

| 参数 | 说明 | 范围 | 默认值 |
|---|---|---|---|
| `distance` | 相机与玩家距离 | ≥ 0.1 | 2.5 |
| `offset` | 水平偏移（+ 右侧，- 左侧） | -10 ~ 10 | 0.8 |
| `height` | 垂直偏移 | -10 ~ 10 | 0.5 |

> 开启时自动切换到第三人称背面视角。

### 头部锁定

```
/ciallo camera head lock [<yaw>] [<pitch>]
/ciallo camera head unlock
```

| 参数 | 说明 | 范围 |
|---|---|---|
| `yaw` | 水平朝向 | -180 ~ 180 |
| `pitch` | 垂直朝向 | -90 ~ 90 |

不指定参数时锁定当前视角。

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

- `smooth`：平滑过渡到目标缩放
- `direct`：立即切换（默认）

### 航点

```
/ciallo waypoint add [<name>]
/ciallo waypoint list
/ciallo waypoint remove <name>
/ciallo waypoint clear
/ciallo waypoint goto <name> [<speed>]
```

| 命令 | 说明 |
|---|---|
| `add` | 保存当前位置为航点（不指定名称时自动生成） |
| `list` | 显示所有已保存的航点 |
| `remove` | 删除指定航点 |
| `clear` | 清除全部航点 |
| `goto` | 平滑移动相机到指定航点 |

航点保存在 `.minecraft/config/ciallo-mine-camera-waypoints.json`。

### 快捷键

| 按键 | 功能 |
|---|---|
| **C** | 切换镜头缩放（开启 / 关闭上次参数） |

> 可在「选项 → 按键设置 → 你好，我的相机！」中修改按键。

## 配置文件

主配置文件位于 `.minecraft/config/ciallo-mine-camera.json`：

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
  "defaultWaypointSpeed": 3.0
}
```

| 字段 | 说明 |
|---|---|
| `defaultVertical` | 默认上下黑边厚度 |
| `defaultHorizontal` | 默认左右黑边厚度 |
| `defaultSpeed` | 默认动画速度 |
| `defaultDisableSpeed` | 关闭动画的默认速度 |
| `defaultHideHand` | 默认是否隐藏手持物品 |
| `defaultShoulderDistance` | 越肩视角默认距离 |
| `defaultShoulderOffset` | 越肩视角默认水平偏移 |
| `defaultShoulderHeight` | 越肩视角默认垂直偏移 |
| `defaultWaypointSpeed` | 航点移动默认速度 |

## 服务端同步

服务端可通过相同指令结构将电影模式推送给玩家（需要 OP 权限）：

```
/ciallo camera movie enable <horizontal> <vertical> <speed> <hideHand>
/ciallo camera movie disable [<speed>]
```

不指定玩家时推送给全体玩家。

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
