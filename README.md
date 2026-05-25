# BlindRunApp（助盲跑系统）

> **让奔跑不再孤单，让出行更有温度。**

BlindRunApp 是一款面向视障人士与陪跑志愿者的无障碍陪跑平台，致力于解决视障用户在户外跑步过程中面临的方向识别困难、安全风险高、缺少陪跑人员等问题。

用户可通过平台快速发布陪跑需求，附近志愿者可实时接单，双方完成匹配后进入共享跑步模式，实现实时位置同步、语音辅助引导与紧急求助，为视障人士提供更加安全、便捷、温暖的运动体验。

当前项目为 **MVP（Minimum Viable Product）版本**，已完成核心业务闭环验证。

---

# ✨ 项目亮点

* 🧑‍🦯 面向视障用户的无障碍跑步解决方案
* 🗺 基于高德地图实现实时定位与路线规划
* 🔊 全流程 TTS 语音播报辅助操作
* 🤝 志愿者实时接单与双向位置共享
* 🚨 SOS 一键紧急求助机制
* ⚡ WebSocket 实现低延迟实时通信
* 🧱 MVVM + Hilt 构建现代 Android 架构

---

# 📱 核心功能

## 👨‍🦯 盲人端功能

### 用户认证

* 注册 / 登录
* 用户角色选择
* 模拟身份认证

### 发布陪跑招募

* 自动定位当前位置作为起点
* 地图界面选择起点与终点
* 支持搜索地点与长按选点
* 自动规划步行路线并计算距离
* TTS 语音引导发布流程

### 我的匹配

* 查看历史陪跑记录
* 查看当前匹配状态

### 实时跑步页面

* 显示完整跑步路线
* 起点（绿色）/ 终点（红色）标识
* 自身位置方向箭头（蓝色）
* 队友实时位置显示（红点）

### SOS 紧急求助

* 一键发送当前位置
* 可扩展短信 / 电话报警能力

---

## 🏃‍♂️ 陪跑员端功能

### 附近招募列表

* 自动刷新附近陪跑需求（每 3 秒）
* 实时展示活跃招募信息

### 一键接单

* 接单后自动创建匹配会话
* 自动通知盲人端进入跑步页面

### 实时位置共享

* 双方地图实时同步
* 实时查看队友位置变化

### 我的匹配

* 查看参与过的陪跑记录

---

## 🌐 通用功能

* 🔊 TTS 语音播报
* 🚨 SOS 一键求助
* 🔙 全局返回导航
* 🔐 本地登录状态保存
* 🌍 实时地图定位与路线规划

---

# 🧠 技术栈

## Android 客户端

| 技术                 | 用途           |
| ------------------ | ------------ |
| Kotlin             | 主要开发语言       |
| Jetpack Compose    | 响应式 UI 框架    |
| MVVM + Hilt        | 架构模式与依赖注入    |
| Retrofit + OkHttp  | HTTP 网络请求    |
| WebSocket (OkHttp) | 实时位置同步       |
| 高德地图 3D SDK        | 地图显示、定位、路线规划 |
| 高德 Web API         | 步行路线规划       |
| TextToSpeech (TTS) | 无障碍语音播报      |
| Coroutines + Flow  | 异步与状态流       |
| SharedPreferences  | 本地用户信息存储     |

---

## Spring Boot 后端（可选）

| 技术                | 用途     |
| ----------------- | ------ |
| Spring Boot 3.1.5 | 后端框架   |
| Spring WebSocket  | 实时双向通信 |
| ConcurrentHashMap | 内存数据存储 |
| Maven             | 项目构建   |

---

# 🏗 项目架构

项目整体遵循现代 Android MVVM 架构设计：

```text
UI (Compose)
   ↓
ViewModel
   ↓
Repository
   ↓
DataSource (API / WebSocket / Local)
```

---

# 📂 客户端结构

```text
com.blindrun.app
├── di/             # Hilt 依赖注入模块
├── model/          # 数据模型
├── network/        # Retrofit API / WebSocket / Mock 拦截器
├── repository/     # 数据仓库
├── ui/             # Compose 页面与主题
├── viewmodel/      # ViewModel 业务逻辑
├── location/       # 高德定位封装
├── tts/            # TTS 语音服务
└── utils/          # 工具类
```

---

# 📂 后端结构（Spring Boot）

```text
com.blindrun
├── config/         # CORS / WebSocket 配置
├── controller/     # REST API 与 WebSocket 处理器
├── model/          # 实体类
└── service/        # 业务服务与内存存储
```

---

# 📌 核心模块说明

| 模块     | 功能说明               |
| ------ | ------------------ |
| 发布招募   | 地图选点 + 路线规划 + 语音引导 |
| 附近招募   | 自动刷新附近活跃招募         |
| 接单匹配   | 创建匹配会话并推送通知        |
| 实时跑步   | WebSocket 实时位置同步   |
| 我的匹配   | 查询历史陪跑记录           |
| SOS 求助 | 模拟发送位置报警           |

---

# 📈 当前开发进度

## ✅ 已完成

* Android 项目初始化（Kotlin + Compose + Hilt）
* 高德地图 SDK 集成
* 登录 / 注册模块
* 盲人发布招募完整流程
* 附近招募自动刷新
* 接单匹配与 WebSocket 通知
* 实时跑步地图页面
* TTS 无障碍语音播报
* SharedPreferences 本地状态存储
* Spring Boot 后端实现
* Git 版本管理（`dev` 分支）

---

# 🚧 后续开发计划

* ☁️ 云端后端部署
* 📊 跑步数据记录（配速 / 里程 / 时长）
* ⭐ 双方互评与信用体系
* 📍 路线偏离检测与智能提醒
* ♿ TalkBack 深度无障碍优化
* 🧠 AI 智能语音辅助

---

# 🛠 开发环境

| 环境             | 版本                  |
| -------------- | ------------------- |
| Android Studio | Hedgehog 及以上        |
| Kotlin         | 1.9.20              |
| Gradle         | 8.2                 |
| Min SDK        | API 24（Android 7.0） |
| Target SDK     | API 34              |
| JDK            | 17                  |
| Spring Boot    | 3.1.5               |
| Maven          | 3.8+                |

---

# 🚀 快速开始

## 1️⃣ 克隆项目

```bash
git clone https://github.com/czx220288/BlindRunApp.git

cd BlindRunApp
```

---

## 2️⃣ 配置高德地图 Key

前往高德开放平台申请：

* Android SDK Key
* Web API Key

在 `app/local.properties` 中添加：

```properties
AMAP_KEY=你的AndroidSDKKey
AMAP_WEB_KEY=你的WebAPIKey
```

---

## 3️⃣ 运行 Android 客户端

* 使用 Android Studio 打开项目
* 同步 Gradle
* 连接真机（推荐）或 ARM 模拟器
* 点击运行按钮启动应用

---

## 4️⃣ 启动后端（可选）

```bash
cd backend

mvn spring-boot:run
```

修改：

* `NetworkModule.kt`
* `WebSocketManager.kt`

中的 `BASE_URL` 为本机局域网 IP。

确保：

* 手机与电脑处于同一 Wi-Fi
* 防火墙已放行 `8080` 端口

---

# 📸 界面预览

> 可在此处添加以下页面截图：

* 登录页
* 发布招募页
* 地图选点页
* 附近招募列表
* 实时跑步页面
* SOS 求助页面

---
## 📱 APK 下载

[点击下载调试版 APK](./apk/app-debug.apk)


---

# 🤝 贡献指南

欢迎提交：

* Issue
* Pull Request
* 功能建议
* 无障碍优化建议

请确保：

* 代码风格统一
* 提交内容清晰
* 添加必要注释与文档

---

# 📄 开源协议

本项目仅供学习与交流使用，未经授权不得用于商业用途。

---

# 📧 联系方式

作者：陈梓轩

GitHub：[czx220288](https://github.com/czx220288?utm_source=chatgpt.com)

项目地址：[BlindRunApp](https://github.com/czx220288/BlindRunApp?utm_source=chatgpt.com)

---

# ❤️ BlindRunApp

> **让科技成为连接温暖的桥梁。**
> **让每一次奔跑，都有人同行。**
