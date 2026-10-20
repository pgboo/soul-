# CLAUDE.md — Soul 风格年轻人社交系统一期

> 本文件是本项目的**最高优先级上下文约束**。所有 AI 编码工具（Cursor / Claude Code / WorkBuddy）在动任何代码前必须先读完本文件。

## 1. 项目身份

| 项 | 值 |
|---|---|
| 项目名称 | Soul 风格年轻人社交系统一期 |
| 一二期定位 | 匿名/弱熟人身份 + 兴趣标签 + 灵魂测试 + 广场瞬间 + 匹配 + IM 单聊 + 站内通知 |
| 开发原则 | **不是 Demo**。所有功能要能正常运行、能闭环、能被后台管理、能被治理、能被 OpenSpec 追踪 |
| 权威输入 | `D:\github项目案例\一期需求.xlsx`、`Soul社交项目完整排期.xlsx` |

## 2. 技术底座（硬约束，禁止擅自更换）

| 层 | 选型 |
|---|---|
| JDK | **Java 21**（`C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`） |
| 后端 | **Spring Boot 3.2.x** + Spring Web + Validation |
| ORM | **MyBatis-Plus 3.5.x** |
| 数据库 | **MySQL 8.0**（本机 `localhost:3306`，root/123456，库名 `soul_social`） |
| 鉴权 | JWT（`jjwt`），Token 存 Redis 或内存兜底 |
| 文件存储 | 本地磁盘 + 静态资源映射（一期不引入 MinIO/OSS） |
| 后台前端 | **Vue 3 + Vite + Element Plus** |
| 用户端前端 | **Vue 3 + Vite + Vant**（H5，模拟小程序） |
| 构建 | Maven 3.9.16（`D:\BaiduNetdiskDownload\maven\apache-maven-3.9.16`） |
| AI | 阿里云百炼 DashScope（**本期只留接口位，不实现**） |

## 3. 编码铁律（违反即返工）

1. **依赖注入用 `@Resource` 字段注入**，禁止使用构造器注入、禁止 `@Autowired`。
2. **Controller 不接收 Entity、不返回 Entity**。入参一律 `XxxReq`，出参一律 `XxxResp` / `XxxDTO`。
3. **业务表必须含四个字段**：`id`、`create_time`、`update_time`、`is_delete`。缺一不可。
4. **敏感信息分表**：手机号/openid/unionid 只存在 `user_sensitive_info`。主表 `user` 与**所有** Resp 都不得返回。
5. **IM 统一使用既有表** `im_conversation` / `im_message`。**严禁新建** `chat`、`chat_conversation`、`chat_message` 等任何同义表。
6. **逻辑删除**：删除一律 `is_delete = 1`，禁止物理 `DELETE`。
7. **统一返回体**：所有接口返回 `Result<T>`（`code` / `message` / `data`）。
8. **接口路径分离**：用户端 `/api/mini/**`，后台 `/api/admin/**`。
9. **接口路径中禁止出现动词化 REST 例外以外的自定义路径**；分页统一 `pageNum` / `pageSize`。
10. **状态判断不得漏判**：封禁、禁言、拉黑、审核、隐私、可见性必须按 `docs/01-需求文档.md` 的治理规则逐条落库判断。

## 4. 开发流程铁律（顺序不可跳过）

```
T00 项目初始化 → T01 需求确认 → T02 UI页面说明 → T03 UI出图 → T04 OpenSpec
→ T05 数据库确认 → T06 接口确认 → T07 后端框架 → T08/T09 后端模块
→ T13 用户端前端 → T14 后台前端 → T15 联调验收
```

- **T00–T06 属于「不允许编码」阶段**。未完成前置阶段，禁止写业务代码。
- 每个任务只交给 AI 一个阶段或一个模块，禁止一次性生成全部。
- 产物落盘位置见《§5 目录约定》。

## 5. 目录约定

```
soul社交/
├── CLAUDE.md                     # 本文件
├── docs/
│   ├── 01-需求文档.md             # T01
│   ├── 02-架构设计.md             # T04
│   ├── 03-API接口文档.md          # T06
│   ├── 04-数据库文档.md           # T05
│   └── 05-UI页面说明.md           # T02
├── openspec/changes/init-social-soul-mvp/   # T04
│   ├── proposal.md
│   ├── design.md
│   ├── tasks.md
│   └── specs/<能力>/spec.md
├── design/ui/                    # T03 UI 图
├── sql/                          # T05 DDL
├── backend/                      # T07-T12 Java 后端
├── frontend-admin/               # T14 Vue3 后台
└── frontend-h5/                  # T13 Vue3 H5 用户端
```

## 6. 本期范围（T00–T09 主链路批次）

**In Scope**：账号登录、用户资料、隐私安全、兴趣标签、灵魂测试、广场瞬间、广场互动、话题、匹配推荐、打招呼、IM 单聊、站内通知。

**Out of Scope（后续批次）**：AI 陪伴 / AI 帮聊、举报治理、审核与敏感词后台、用户管理后台、数据看板、足迹中心、WebSocket 实时推送、群聊、图片/语音消息、会员体系、假繁荣系统。

## 7. 禁止事项清单

- ❌ 禁止新建任何 chat / conversation 同义表（IM 沿用既有表）
- ❌ 禁止在用户端 Resp 中返回手机号、openid、unionid
- ❌ 禁止 Controller 直接暴露 Entity
- ❌ 禁止使用构造器注入
- ❌ 禁止物理删除数据
- ❌ 禁止前端长期使用 Mock 数据（联调阶段必须替换为真实接口）
- ❌ 禁止跳过 OpenSpec / 表结构 / 接口确认直接编码
- ❌ 禁止把 AI 陪伴写成"客服话术"（后续批次实现时注意）
