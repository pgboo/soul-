# 部署与内测操作手册

面向「找人试用 / 收集反馈」阶段。所有命令都在 **项目根目录** 下用 Git Bash 执行。

---

## 0. 一图看懂

```
                 ┌─────────────────────────────────────────┐
   测试者 ─────► │  公网地址（内网穿透给的 URL）              │
   （网页/APK）  │            ↓                            │
                 │   你电脑的 8090 端口                      │
                 │   ┌───────────────────────────────┐     │
                 │   │  一个 Spring Boot jar          │     │
                 │   │  ├─ /             → H5 页面    │     │
                 │   │  ├─ /api/**       → 接口       │     │
                 │   │  └─ /upload/**    → 上传的图   │     │
                 │   └───────────────────────────────┘     │
                 │            ↓                            │
                 │   MySQL: soul_social_test（独立测试库）   │
                 └─────────────────────────────────────────┘
```

**关键点：前端已打包进后端 jar，只暴露一个端口**。所以穿透工具只需要映射 8090，不用开两个隧道。
APK 只是一个 WebView 外壳，指向同一个地址，数据同样在你这台电脑上。

---

## 1. 目录里都有什么

| 文件 | 作用 |
|---|---|
| `deploy/build-all.sh` | 一键构建：前端 `npm run build` → 后端打包（H5 产物自动进 jar） |
| `deploy/run.sh` | 启动单端口服务，默认 `test` profile + 8090 端口 |
| `deploy/init-test-db.sh` | 建一个**只有表结构**的空测试库 |
| `deploy/seed-test-db.sh` | 把演示库数据复制到测试库（每轮内测前重置用） |
| `deploy/backup-db.sh` | 数据库备份，自动保留最近 14 份 |
| `deploy/setup-android-sdk.sh` | 一次性安装 Android SDK 命令行工具链（打 APK 用） |
| `deploy/build-apk.sh` | 一键打签名 APK，需传入后端公网地址 |
| `deploy/_mvn.sh` | Maven 包装脚本（本机 Git Bash 直接跑 `mvn` 会失败，见文末） |

---

## 2. 标准流程

### 2.1 首次准备（只做一次）

```bash
# 1) 建测试库（二选一）
bash deploy/init-test-db.sh      # A. 空库，让测试者从零走注册流程
bash deploy/seed-test-db.sh      # B. 灌入演示数据（10 用户 / 20 动态 / 6 会话）← 推荐

# 2) 构建单端口部署包
bash deploy/build-all.sh
```

### 2.2 每次开始内测

**推荐：一条命令搞定（后端 + 公网隧道一起起）**

```bash
bash deploy/start-public.sh
```

它会在**你自己的终端**里把后端和隧道一起拉起来，并打印当前公网地址。
保持窗口打开即可；按 `Ctrl+C` 停止。窗口被关了想收尾就执行 `bash deploy/stop-public.sh`。

> ⚠️ 为什么强调「你自己的终端」：由 AI 会话启动的后台服务绑在会话上，
> 会话一结束就可能被回收，测试者会突然报 network error。

**手动方式（分两个窗口）**

```bash
# 1) 重置测试环境（可选，想让测试者从干净状态开始时执行）
bash deploy/seed-test-db.sh

# 2) 窗口一：启动服务
bash deploy/run.sh test 8090

# 3) 窗口二：把 8090 暴露到公网（见第 3 节）
```

### 2.3 每轮结束

```bash
# 备份两个库
bash deploy/backup-db.sh all
```

---

## 3. 内网穿透：把 8090 暴露到公网

### 方案 A：Cloudflare Tunnel（最快，免注册，自带 HTTPS）

```bash
# 1) 下载 cloudflared（Windows 版，单文件 exe）
#    https://github.com/cloudflare/cloudflared/releases/latest
#    下载 cloudflared-windows-amd64.exe，重命名为 cloudflared.exe

# 2) 一条命令启动
cloudflared.exe tunnel --url http://localhost:8090
```

控制台会打印一个形如 `https://xxxx-yyyy.trycloudflare.com` 的地址，**直接发给测试者**。
关掉窗口隧道就断，域名下次会变。

### 方案 B：cpolar（国内，速度更好，需注册）

```bash
# 1) 到 https://www.cpolar.com/ 注册并下载 Windows 客户端
# 2) 登录后在后台复制 authtoken，执行一次
cpolar authtoken <你的token>
# 3) 启动
cpolar http 8090
```

免费版给随机域名，速度比 Cloudflare 在国内更好。

### 方案 C：ngrok / natapp / 花生壳

思路一样：注册 → 装客户端 → 把**本地 8090** 映射出去。免费版普遍是随机域名 + 带宽限制。

> ⚠️ **安全红线**
> 1. **只暴露 8090**。5174 是后台管理端，有删用户、改配置的权限，绝对不能开出去。
> 2. 8080（dev 环境）也不要暴露，它连的是你的演示库。
> 3. 穿透期间你的电脑必须开机、不能休眠。
> 4. 建议设一个环境变量再启动，避免用仓库里的默认 JWT 密钥：
>    ```bash
>    SOUL_JWT_SECRET="换成你自己的32位以上随机串" bash deploy/run.sh test 8090
>    ```

---

## 4. 两个环境互不干扰

| 环境 | 端口 | 数据库 | 上传目录 | 用途 |
|---|---|---|---|---|
| 开发 | 8080 + 5173(H5) + 5174(后台) | `soul_social` | `backend/upload` | 你自己开发调试 |
| 内测 | 8090 | `soul_social_test` | `backend/upload-test` | 对外给测试者 |

两边可以同时跑。测试者怎么折腾都不会影响你的演示库。

---

## 5. 常见问题

**Q：测试者说打不开链接**
1. 先确认自己电脑上 `http://localhost:8090/` 能打开 → 服务没问题
2. 再确认穿透窗口还开着 → 隧道没断
3. 最后看 Windows 防火墙是否拦了；穿透工具是出站连接，一般不受影响

**Q：构建报 `Failed to delete ...jar`**
8080/8090 的服务还在跑，Windows 锁住了 jar 文件。先停掉再构建：
```bash
netstat -ano | grep LISTENING | grep -E ":(8080|8090)\s"   # 看 PID
taskkill //PID <PID> //F
```

**Q：为什么不能直接 `mvn package`**
本机 Git Bash 的 MSYS 路径转换被禁用，`mvn` 会把 `/d/...` 这类路径直接传给 Windows JVM，报
「找不到或无法加载主类 `org.codehaus.plexus.classworlds.launcher.Launcher`」。
所以统一走 `deploy/_mvn.sh`（用 java 直接启动 classworlds）。
如果你的 `JAVA_BIN` / `MVN_HOME` 路径不一样，用环境变量覆盖即可：
```bash
JAVA_BIN="/c/你的jdk/bin/java.exe" MVN_HOME='D:\你的\maven' bash deploy/build-all.sh
```

**Q：测试者登录要验证码怎么办**
不用管，**开发模式是固定码 `123456`**，任意手机号都能登录，新号码自动注册。
注意验证码发送有 **60 秒限流（按手机号）**，同一个号连续发两次第二次会失败 —— 让每个测试者用自己的号即可。

**Q：想清空测试图片**
直接删 `backend/upload-test/` 目录下的文件即可，不影响正式环境。

---

## 6. 打包成 APK（可选）

> **先决条件**：APK 只是一个 WebView 外壳，数据全在你电脑上。
> **必须先完成第 3 节**（公网地址已能访问），APK 才有意义 —— 否则装上打开就是白屏。
>
> 内测阶段其实更推荐直接发**网页链接**：你改完 bug 测试者刷新即可，
> 而 APK 每次都要重新下载安装。APK 适合「地址固定后」或需要原生能力的场景。

### 6.1 首次准备（只做一次）

```bash
# 安装 Android SDK 命令行工具链（约 600MB，装到 %LOCALAPPDATA%\Android\Sdk）
bash deploy/setup-android-sdk.sh
```

Android 工程（`frontend-h5/android`）与签名密钥已经生成好了，无需再操作。

### 6.2 每次打包

```bash
bash deploy/build-apk.sh https://你的公网地址
```

产物在 `dist-apk/soul-social-<时间戳>.apk`，同目录还有一个 `.txt` 记录本次绑定的地址。

脚本会先校验地址是否真的写进了构建产物，写不进去会直接报错退出，不会产出「装上去白屏」的包。

> ⚠️ **后端地址是编译进 APK 的**。地址一变（比如 cloudflared 重启换了域名），
> 已发出去的 APK 就全部失效，必须用新地址重新执行本脚本。
> 想让地址固定，用 cpolar 付费版固定二级域名，或上云服务器绑自己的域名。

### 6.3 分发注意事项

1. **微信里不能直接下载 APK**（腾讯拦截 `.apk` 直链）。让测试者复制链接到浏览器打开。
2. 测试者需要在手机上允许「安装未知来源应用」。
3. **iPhone 装不了 APK**，只能走网页链接。
4. 签名密钥在 `frontend-h5/android/keystore/`，密码在 `keystore.properties`。
   **不要删** —— 删了就无法对已安装的 App 做覆盖升级（会报签名冲突，必须卸载重装）。
5. 当前签名的 SHA1 / SHA256 已记录在项目 memory 中，接微信登录等第三方 SDK 时会用到。

### 6.4 已知取舍

| 项 | 说明 |
|---|---|
| 应用名 | `Soul社交`，包名 `com.soul.social` |
| 最低系统 | Android 7.0（API 24） |
| 图标 | Capacitor 默认图标，未替换 |
| 明文流量 | 已允许（`usesCleartextTraffic="true"`），便于用局域网 IP 调试；正式上架前应关掉 |
| 版本号 | 每次打包都是 `versionCode 1 / versionName 1.0`，覆盖安装没问题，但应用商店上架前需递增 |
