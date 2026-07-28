一个为 Minecraft Fabric 1.21.1 添加电影视角功能的模组。

## 功能

- 🎬 指令一键切换电影视角模式
- ⬛ 上下黑边遮幅
- 📦 隐藏 HUD 和玩家的手

## 使用方法

1. 安装 Fabric Loader 和 Fabric API
2. 将模组放入 `mods` 文件夹

## 指令
/ciallo camera movie enable <Height> <Speed> <HideHand>	开启电影模式
/ciallo camera movie disable <Speed>	关闭电影模式
/ciallo head lock <Yaw> <Pitch>	锁定头部
/ciallo head unlock	解锁头部


## 开发
AI写的。
API：DeepSeek-V4 Pro - Med

### 环境要求
- Java 21
- Gradle 8.x
- Minecraft 1.21.1
- Fabric Loader 0.16.7+

### 构建

```bash
./gradlew build
