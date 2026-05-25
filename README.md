BlindRunApp（助盲跑系统）
一款连接视障人士与陪跑志愿者的无障碍陪跑平台，实现招募发布、实时匹配、位置共享与安全求助。

📌 项目简介
BlindRunApp 致力于解决视障人士户外跑步时方向识别困难、安全风险高、缺少陪跑人员等问题。
盲人用户可一键发布陪跑招募，志愿者（陪跑员）通过附近列表接单，双方匹配后在地图上实时共享位置，全程支持语音播报和紧急求助。

本项目为 MVP 版本，已验证核心业务闭环。

🚀 已实现功能
盲人端
注册/登录：角色选择与模拟认证

发布陪跑招募

自动定位当前地点为起点

同一地图界面选择起点/终点（支持搜索、长按选点）

自动规划步行路线并显示距离

语音引导发布流程

我的匹配：查看历史陪跑会话

实时跑步页面

显示路线、起点（绿）、终点（红）

自身位置带方向箭头（蓝），队友位置为红点

SOS 紧急求助

陪跑员端
附近招募列表：每 3 秒自动刷新

一键接单：点击后创建匹配会话，自动通知盲人端进入跑步页面

实时位置共享：双方在地图上互相可见

我的匹配：查看参与过的陪跑记录

通用功能
TTS 语音播报（页面切换、关键操作）

SOS 一键求助（模拟发送位置）

全局返回导航

退出登录

🧠 技术栈
Android 客户端
技术	用途
Kotlin	主要开发语言
Jetpack Compose	响应式 UI 框架
MVVM + Hilt	架构模式与依赖注入
Retrofit + OkHttp	HTTP 网络请求
WebSocket (OkHttp)	实时位置同步
高德地图 3D SDK	地图显示、定位、路线规划
高德 Web API	步行路线规划（步行）
TextToSpeech (TTS)	无障碍语音播报
Coroutines + Flow	异步与状态流
SharedPreferences	本地用户信息存储
后端（可选，已提供 Spring Boot 实现）
技术	用途
Spring Boot 3.1.5	后端框架
Spring WebSocket	实时双向通信
ConcurrentHashMap	内存数据存储
Maven	项目构建
🏗 项目架构
遵循 MVVM 模式，各层职责清晰：

text
UI (Compose) → ViewModel → Repository → DataSource (API / WebSocket / Local)
客户端结构
text
com.blindrun.app
├── di/               # Hilt 依赖注入模块
├── model/            # 数据类
├── network/          # Retrofit API, WebSocket 管理, Mock 拦截器
├── repository/       # 数据仓库
├── ui/               # Compose 界面（屏幕、主题）
├── viewmodel/        # 业务逻辑 ViewModel
├── location/         # 高德定位封装
├── tts/              # TTS 语音服务
└── utils/            # 工具类
后端结构（Spring Boot）
text
com.blindrun
├── config/           # CORS、WebSocket 配置
├── controller/       # REST API 与 WebSocket 处理器
├── model/            # 实体类
└── service/          # 业务服务与内存存储
📂 核心模块说明
模块	说明
发布招募	地图选点 + 路线规划 + 语音引导
附近招募	自动刷新列表，展示盲人发布的活跃招募
接单匹配	陪跑员接单后，后端生成匹配会话并推送通知
实时跑步	WebSocket 位置同步，地图路线绘制，双人位置显示
我的匹配	查询用户参与过的匹配记录
SOS	模拟发送位置报警（可扩展为真实短信/电话）
📈 开发进度与计划
✅ 已完成
Android 项目初始化（Kotlin + Compose + Hilt）

高德地图 SDK 集成（地图、定位、路径规划）

登录 / 注册界面（模拟密码验证）

盲人发布招募（完整的地图选点、路线规划）

陪跑员查看附近招募（自动刷新）

接单匹配与 WebSocket 通知（后端支持）

跑步页面（路线、自身方向、队友位置、SOS）

TTS 语音播报集成

本地用户状态存储（SharedPreferences）

后端 Spring Boot 完整实现（API + WebSocket）

Git 版本管理（dev 分支）

🚧 后续计划
真实后端部署（云端服务器）

增加跑步数据记录（配速、里程、时长）

双方互评与信用体系

更精确的路线跟随与偏离提醒

增强无障碍支持（TalkBack 深度优化）

🛠 开发环境
环境	版本
Android Studio	Hedgehog 及更高
Kotlin	1.9.20
Gradle	8.2
Min SDK	API 24 (Android 7.0)
Target SDK	API 34
JDK (后端)	17
Spring Boot	3.1.5
Maven	3.8+
🚀 快速开始
1. 克隆项目
   bash
   git clone https://github.com/czx220288/BlindRunApp.git
   cd BlindRunApp
2. 配置高德地图 Key
   前往高德开放平台申请 Android SDK Key 和 Web API Key。

在 app/local.properties 中添加：

properties
AMAP_KEY=你的AndroidSDKKey
AMAP_WEB_KEY=你的WebAPIKey
3. 运行 Android 客户端
   使用 Android Studio 打开项目，同步 Gradle。

连接真机（推荐）或 ARM 模拟器。

点击运行按钮。

4. 启动后端（可选）
   bash
   cd backend
   mvn spring-boot:run
   修改 Android 端 NetworkModule.kt 和 WebSocketManager.kt 中的 BASE_URL 为你的电脑局域网 IP。

确保手机与电脑在同一 Wi-Fi，防火墙放行 8080 端口。

📸 界面预览
（可添加应用截图，例如发布招募页、附近列表、跑步页等）

🤝 贡献指南
欢迎提交 Issue 和 Pull Request。请确保代码风格一致，并添加必要的注释。

📄 许可证
本项目仅供学习交流使用，未经许可不得用于商业用途。

📧 联系方式
作者：陈梓轩

GitHub：czx220288

助盲跑 —— 让奔跑不再孤单，让出行更有温度。