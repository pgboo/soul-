# Soul 风格年轻人社交系统一期 — API 接口文档

| 项 | 内容 |
|---|---|
| 文档名称 | Soul 社交系统一期 API 接口文档 |
| 文档版本 | v1.0 |
| 编写日期 | 2026-10-14 |
| 编写人 | 高见远（架构师） |
| 依据 | `docs/01-需求文档.md` §10、`docs/02-架构设计.md`、`sql/schema.sql` |
| 规范 | Controller 不接收/不返回 Entity；Req/Resp 分离；路径 `/api/mini/**` 与 `/api/admin/**` |

---

## 一、通用约定

### 1.1 Base URL

| 端 | 前缀 |
|---|---|
| 用户端 | `http://localhost:8080/api/mini` |
| 后台 | `http://localhost:8080/api/admin` |

### 1.2 Header 约定

| Header | 值 | 说明 |
|---|---|---|
| `Content-Type` | `application/json` | JSON 请求体（上传接口用 `multipart/form-data`） |
| `Authorization` | `Bearer <token>` | 登录后所有受保护接口必带 |

### 1.3 统一返回结构

```json
{ "code": 200, "message": "操作成功", "data": { } }
```

| 字段 | 类型 | 说明 |
|---|---|---|
| code | Integer | 200 成功，其它为失败 |
| message | String | 提示信息 |
| data | T | 业务数据，可为 null / 对象 / 数组 |

### 1.4 分页结构

请求参数：`pageNum`（默认 1）、`pageSize`（默认 10）。
响应结构（`data` 内）：

```json
{
  "code": 200, "message": "操作成功",
  "data": {
    "total": 100, "pageNum": 1, "pageSize": 10,
    "records": [ { } ]
  }
}
```

### 1.5 错误码表

| code | 说明 |
|---|---|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未登录或登录已过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 405 | 请求方法不支持 |
| 500 | 系统繁忙，请稍后重试 |
| 1001 | 验证码错误 |
| 1002 | 验证码已过期 |
| 1003 | 账号已被封禁 |
| 1004 | 账号已被禁言 |
| 1005 | 资料尚未完善 |
| 1006 | 需要获取验证码 |
| 1007 | 验证码发送过于频繁 |
| 2001 | 标签数量须为 3-10 个 |
| 2002 | 标签不可用 |
| 3001 | 请勿重复提交灵魂测试 |
| 3002 | 灵魂测试题目为空 |
| 4001 | 内容含违规信息 |
| 4002 | 瞬间不存在或已删除 |
| 4003 | 仅可操作自己的内容 |
| 4004 | 内容不能为空 |
| 5001 | 请勿重复操作 |
| 5002 | 内容已下架，不可操作 |
| 6001 | 对方已拉黑你或你已拉黑对方 |
| 6002 | 对方未开启陌生人私信 |
| 6003 | 不可对自己操作 |
| 7001 | 打招呼已达今日上限 |
| 7002 | 今日已向该用户打过招呼 |
| 8001 | 对方已关闭被推荐 |
| 9001 | 通知不存在或无权操作 |
| 9002 | 会话不存在或无权操作 |

---

## 二、用户端接口（`/api/mini`）

### 2.1 发送验证码

- **路径**：`POST /api/mini/auth/code`
- **说明**：开发模式下不真正发短信，固定验证码见 `system_config.sms_enable=0` 时日志打印 `123456`。
- **鉴权**：否

| 参数 | 类型 | 必填 | 说明 | 校验 |
|---|---|---|---|---|
| phone | String | 是 | 手机号 | 11 位数字 |

**请求示例**
```json
{ "phone": "13800000001" }
```
**响应示例**
```json
{ "code": 200, "message": "验证码已发送", "data": null }
```
**涉及表**：`user_sensitive_info`（存在性）、`CacheService`（验证码）。
**规则**：60s 内重复发送 → `1007`。

---

### 2.2 登录

- **路径**：`POST /api/mini/auth/login`
- **说明**：手机号 + 验证码登录，新用户自动注册。
- **鉴权**：否

| 参数 | 类型 | 必填 | 说明 | 校验 |
|---|---|---|---|---|
| phone | String | 是 | 手机号 | 11 位数字 |
| code | String | 是 | 验证码 | 非空 |

**请求示例**
```json
{ "phone": "13800000001", "code": "123456" }
```
**响应参数**

| 字段 | 类型 | 说明 |
|---|---|---|
| token | String | JWT |
| userId | Long | 用户 ID |
| nickname | String | 昵称（可能为空） |
| avatar | String | 头像 URL |
| profileComplete | Boolean | 资料是否完善 |
| tagComplete | Boolean | 标签是否完成 |
| testComplete | Boolean | 测试是否完成 |

**响应示例**
```json
{ "code": 200, "message": "登录成功",
  "data": { "token": "eyJhbGci...", "userId": 1001, "nickname": "小白", "avatar": "",
            "profileComplete": false, "tagComplete": false, "testComplete": false } }
```
**涉及表**：`user`、`user_sensitive_info`、`user_login_log`。
**规则**：封禁 → `1003`；验证码错/过期 → `1001`/`1002`。

---

### 2.3 退出登录

- **路径**：`POST /api/mini/auth/logout`
- **说明**：将当前 token 加入黑名单缓存直至过期。
- **鉴权**：是
- **请求参数**：无
- **响应示例**：`{ "code": 200, "message": "退出成功", "data": null }`
- **涉及表**：`CacheService`（token 黑名单）。

---

### 2.4 初始化状态

- **路径**：`GET /api/mini/user/initStatus`
- **说明**：登录后分流查询。
- **鉴权**：是
- **响应参数**

| 字段 | 类型 | 说明 |
|---|---|---|
| profileComplete | Boolean | 资料完善 |
| tagComplete | Boolean | 标签完成 |
| testComplete | Boolean | 测试完成 |
| allCompleted | Boolean | 三项是否均完成 |
| nextStep | String | 下一步：`profile`/`tag`/`test`/`square` |

**响应示例**
```json
{ "code": 200, "message": "success",
  "data": { "profileComplete": false, "tagComplete": false, "testComplete": false,
            "allCompleted": false, "nextStep": "profile" } }
```
**涉及表**：`user`。

---

### 2.5 我的资料

- **路径**：`GET /api/mini/user/profile`
- **鉴权**：是
- **响应参数**：见 2.7 `UserProfileResp`（含 `completePercent`、`nicknameModifyCount`）。

**响应示例**
```json
{ "code": 200, "message": "success",
  "data": { "userId": 1001, "nickname": "小白", "avatar": "", "gender": 1, "birthday": "2000-05-01",
            "city": "深圳", "sign": "你好世界", "intro": "", "emotionStatus": 0, "friendGoal": 0,
            "completePercent": 80, "nicknameModifyCount": 1 } }
```
**涉及表**：`user_profile`（**不返回任何敏感字段**）。

---

### 2.6 完善资料

- **路径**：`POST /api/mini/user/profile/complete`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 | 校验 |
|---|---|---|---|---|
| avatar | String | 是 | 头像 URL | 非空 |
| nickname | String | 是 | 昵称 | 1-16 字、敏感词 |
| gender | Integer | 是 | 性别 | 1/2 |
| birthday | String | 是 | 生日 | `yyyy-MM-dd`、≤今天 |
| city | String | 是 | 城市 | 非空 |
| sign | String | 否 | 签名 | ≤50 字、敏感词 |

**请求示例**
```json
{ "avatar": "/upload/avatar/202610/a.png", "nickname": "小白", "gender": 1,
  "birthday": "2000-05-01", "city": "深圳", "sign": "你好世界" }
```
**响应**：`UserProfileResp`（同 2.5）。
**涉及表**：`user_profile`、`user`（置 `profile_complete_flag=1`）、`audit_record`（预留）。
**规则**：敏感词 → `4001`；保存后计算完整度。

---

### 2.7 编辑资料

- **路径**：`POST /api/mini/user/profile/update`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 | 校验 |
|---|---|---|---|---|
| avatar | String | 否 | 头像 | — |
| nickname | String | 否 | 昵称 | 敏感词；修改次数 ≤3 |
| city | String | 否 | 城市 | — |
| sign | String | 否 | 签名 | ≤50 字、敏感词 |
| intro | String | 否 | 简介 | ≤200 字、敏感词 |
| emotionStatus | Integer | 否 | 情感状态 | 0/1/2 |
| friendGoal | Integer | 否 | 交友目的 | 0/1/2/3 |

**请求示例**
```json
{ "nickname": "小鱼", "intro": "喜欢咖啡和猫", "emotionStatus": 1 }
```
**响应**：`UserProfileResp`。
**涉及表**：`user_profile`、`audit_record`。
**规则**：昵称超 3 次不可改 → `4003`（或专用码）。

---

### 2.8 用户主页

- **路径**：`GET /api/mini/user/home/{userId}`
- **鉴权**：是

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| userId | path | Long | 是 | 目标用户 ID |

**响应参数**

| 字段 | 类型 | 说明 |
|---|---|---|
| userId | Long | 用户 ID |
| nickname | String | 昵称 |
| avatar | String | 头像 |
| gender | Integer | 性别 |
| age | Integer | 年龄（隐私关闭则为 null） |
| city | String | 城市（隐私关闭则为 null） |
| sign | String | 签名 |
| intro | String | 简介 |
| soulId | String | 灵魂号 |
| tags | Array\<TagItem\> | 标签 |
| soulResult | SoulResultItem | 灵魂测试结果（无则 null） |
| dynamicCount | Integer | 瞬间数 |
| blacklisted | Boolean | 我是否已拉黑对方 |
| greetedToday | Boolean | 今日是否已打招呼 |

其中 `TagItem{tagId, name}`，`SoulResultItem{resultCode, name, description, socialAdvice}`。

**响应示例**
```json
{ "code": 200, "message": "success",
  "data": { "userId": 1002, "nickname": "小明", "avatar": "", "gender": 2, "age": 24,
            "city": "深圳", "sign": "hi", "intro": "", "soulId": "S1002",
            "tags": [ { "tagId": 9, "name": "音乐" } ],
            "soulResult": { "resultCode": "S", "name": "温柔倾听者", "description": "...", "socialAdvice": "..." },
            "dynamicCount": 3, "blacklisted": false, "greetedToday": false } }
```
**涉及表**：`user_profile`、`user_tag`、`user_soul_test_record`、`dynamic_content`、`user_privacy_setting`、`user_blacklist`。
**规则**：被拉黑 → `6001`；隐私开关控制 `age`/`city` 是否返回。

---

### 2.9 更新隐私设置

- **路径**：`POST /api/mini/user/privacy/update`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| showAge | Boolean | 否 | 展示年龄 |
| showCity | Boolean | 否 | 展示城市 |
| showOnline | Boolean | 否 | 展示在线状态 |
| allowRecommend | Boolean | 否 | 允许被推荐 |
| allowStrangerMsg | Boolean | 否 | 允许陌生人私信 |

**请求示例**
```json
{ "showCity": false, "allowRecommend": false }
```
**响应**：`{ "code": 200, "message": "保存成功", "data": null }`
**涉及表**：`user_privacy_setting`。

---

### 2.10 拉黑用户

- **路径**：`POST /api/mini/user/blacklist/add`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 | 校验 |
|---|---|---|---|---|
| targetUserId | Long | 是 | 目标用户 | 不可为自己 |

**请求示例**：`{ "targetUserId": 1002 }`
**响应**：`{ "code": 200, "message": "已拉黑", "data": null }`
**涉及表**：`user_blacklist`。
**规则**：自己 → `6003`。

---

### 2.11 取消拉黑

- **路径**：`POST /api/mini/user/blacklist/remove`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| targetUserId | Long | 是 | 目标用户 |

**响应**：`{ "code": 200, "message": "已取消", "data": null }`
**涉及表**：`user_blacklist`。

---

### 2.12 黑名单列表

- **路径**：`GET /api/mini/user/blacklist/list`
- **鉴权**：是
- **响应**：分页 `records` 项 `{ userId, nickname, avatar, blacklistTime }`。
**涉及表**：`user_blacklist`、`user_profile`。

---

### 2.13 标签列表

- **路径**：`GET /api/mini/tag/list`
- **鉴权**：是
- **响应参数**：`data` 为分类数组。

```
[{ "categoryId":1, "name":"生活方式", "tags":[ {"tagId":1,"name":"咖啡","hot":true} ] }]
```
**响应示例**
```json
{ "code": 200, "message": "success",
  "data": [ { "categoryId": 1, "name": "生活方式",
              "tags": [ { "tagId": 1, "name": "咖啡", "hot": true } ] } ] }
```
**涉及表**：`tag_category`、`tag`（仅 `status=1`）。

---

### 2.14 保存我的标签

- **路径**：`POST /api/mini/user/tag/save`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 | 校验 |
|---|---|---|---|---|
| tagIds | Array\<Long\> | 是 | 标签 ID 列表 | 3-10 个、均启用 |

**请求示例**：`{ "tagIds": [1,2,9] }`
**响应**：`{ "code": 200, "message": "保存成功", "data": null }`
**涉及表**：`user_tag`、`tag`、`user`（置 `tag_flag=1`）。
**规则**：数量非 3-10 → `2001`；禁用标签 → `2002`。

---

### 2.15 灵魂测试题目

- **路径**：`GET /api/mini/soulTest/questions`
- **鉴权**：是
- **响应参数**：题目数组，每项含选项。

```
{ "questions":[ { "questionId":1,"title":"...","sort":1,
    "options":[ {"optionId":1,"content":"...","sort":1} ] } ] }
```
> 选项**不返回** `result_code`/`score`（防作弊）。

**涉及表**：`soul_test_question`、`soul_test_option`（`status=1`）。

---

### 2.16 提交灵魂测试

- **路径**：`POST /api/mini/soulTest/submit`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| answers | Array\<AnswerItem\> | 是 | 答案 |

`AnswerItem{questionId:Long, optionId:Long}`。

**请求示例**
```json
{ "answers": [ { "questionId": 1, "optionId": 3 }, { "questionId": 2, "optionId": 4 } ] }
```
**响应**：`SoulTestResultResp`（同 2.17）。
**涉及表**：`soul_test_question`、`soul_test_option`、`soul_test_result`、`user_soul_test_record`、`user`（置 `test_flag=1`）。
**规则**：必答全部 → 未答完 `400`；已提交可覆盖（保留最后一次）或首次锁定（默认**允许重测并覆盖**）。

---

### 2.17 我的测试结果

- **路径**：`GET /api/mini/soulTest/result`
- **鉴权**：是
- **响应参数**：`{ resultCode, name, icon, description, socialAdvice }`。
**响应示例**
```json
{ "code": 200, "message": "success",
  "data": { "resultCode": "S", "name": "温柔倾听者", "icon": "",
            "description": "...", "socialAdvice": "..." } }
```
**涉及表**：`soul_test_result`、`user_soul_test_record`。

---

### 2.18 发布瞬间

- **路径**：`POST /api/mini/dynamic/publish`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 | 校验 |
|---|---|---|---|---|
| content | String | 否 | 正文 | ≤500 字、敏感词 |
| mediaUrls | Array\<String\> | 否 | 图片 URL 列表 | ≤9 张 |
| topicId | Long | 否 | 话题 ID | 启用 |
| anonymousFlag | Boolean | 否 | 匿名 | 默认 false |
| visibleScope | Integer | 否 | 可见范围 | 1公开/2仅好友/3仅自己 |

**校验**：正文与图片不可同时为空 → `4004`。
**请求示例**
```json
{ "content": "今天天气真好", "mediaUrls": ["/upload/dynamic/202610/a.png"], "topicId": 2,
  "anonymousFlag": false, "visibleScope": 1 }
```
**响应**：`{ "code": 200, "message": "发布成功", "data": { "dynamicId": 5001 } }`
**涉及表**：`dynamic_content`、`dynamic_media`、`dynamic_topic_relation`。
**规则**：禁言 → `1004`；敏感词 → `4001`；风控观察 → `audit_status=1`。

---

### 2.19 瞬间列表

- **路径**：`GET /api/mini/dynamic/list`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| channel | String | 否 | `recommend`（默认）/`latest`/`city` |
| topicId | Long | 否 | 话题筛选 |
| pageNum | Integer | 否 | 页码（默认 1） |
| pageSize | Integer | 否 | 每页（默认 10） |

**响应参数**（`records` 每项）

| 字段 | 类型 | 说明 |
|---|---|---|
| dynamicId | Long | 动态 ID |
| author | AuthorItem | `{userId, nickname, avatar}` |
| content | String | 正文 |
| mediaUrls | Array\<String\> | 图片 |
| topicName | String | 话题名（无则空） |
| likeCount / commentCount / collectCount | Integer | 计数 |
| liked / collected | Boolean | 我是否点赞/收藏 |
| auditStatus | Integer | 审核状态（本人可见） |
| createTime | String | 时间 |

**响应示例**
```json
{ "code": 200, "message": "success",
  "data": { "total": 1, "pageNum": 1, "pageSize": 10,
    "records": [ { "dynamicId": 5001, "author": { "userId": 1001, "nickname": "小白", "avatar": "" },
        "content": "今天天气真好", "mediaUrls": [], "topicName": "周末去哪儿",
        "likeCount": 3, "commentCount": 1, "collectCount": 0,
        "liked": true, "collected": false, "auditStatus": 0, "createTime": "2026-10-14 10:00:00" } ] } }
```

**可见性规则**

| 动态归属 | 进入广场的条件 |
|---|---|
| 他人 | `status=1` **且** `audit_status=0`（已过审）**且** `visible_scope=1`（公开）**且** 双向无拉黑 |
| 自己 | `status=1` 即可，**不受** `audit_status` / `visible_scope` 限制 |

> 即：作者总能在广场看到自己发布的瞬间（与 `GET /api/mini/dynamic/detail/{id}` 对作者的放行规则一致），
> 避免「刚发布却在广场找不到」。
**涉及表**：`dynamic_content`、`dynamic_media`、`dynamic_like`、`dynamic_collect`、`dynamic_topic_relation`、`dynamic_topic`、`user_profile`、`user_blacklist`。
**规则**：过滤下架/删除/拉黑/待审（非本人）。

---

### 2.20 瞬间详情

- **路径**：`GET /api/mini/dynamic/detail/{id}`
- **鉴权**：是
- **响应**：`DynamicDetailResp`，比列表项增加 `visibility`、`canDelete`、`isAuthor`。
**涉及表**：`dynamic_content`、`dynamic_media`、`dynamic_comment`、`dynamic_like`、`dynamic_collect`、`user_profile`。
**规则**：删除/下架不可见 → `4002`。

---

### 2.21 删除瞬间

- **路径**：`POST /api/mini/dynamic/delete/{id}`
- **鉴权**：是
- **响应**：`{ "code": 200, "message": "删除成功", "data": null }`
**涉及表**：`dynamic_content`（`is_delete=1`）。
**规则**：仅作者 → 否则 `4003`。

---

### 2.22 点赞/取消点赞

- **路径**：`POST /api/mini/dynamic/like`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| dynamicId | Long | 是 | 动态 ID |
| action | String | 是 | `like` / `cancel` |

**请求示例**：`{ "dynamicId": 5001, "action": "like" }`
**响应**：`{ "code": 200, "message": "success", "data": { "liked": true, "likeCount": 4 } }`
**涉及表**：`dynamic_like`、`dynamic_content`、`user_notice`。
**规则**：幂等；自己赞自己不通知。

---

### 2.23 收藏/取消收藏

- **路径**：`POST /api/mini/dynamic/collect`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| dynamicId | Long | 是 | 动态 ID |
| action | String | 是 | `collect` / `cancel` |

**响应**：`{ "code": 200, "message": "success", "data": { "collected": true, "collectCount": 1 } }`
**涉及表**：`dynamic_collect`、`dynamic_content`。
**规则**：下架不可收藏 → `5002`。

---

### 2.24 发表评论/回复

- **路径**：`POST /api/mini/dynamic/comment`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| dynamicId | Long | 是 | 动态 ID |
| content | String | 是 | 内容（≤200 字、敏感词） |
| parentId | Long | 否 | 父评论 ID（0=一级） |
| replyUserId | Long | 否 | 被回复人 ID |

**请求示例**
```json
{ "dynamicId": 5001, "content": "好看！", "parentId": 0, "replyUserId": 0 }
```
**响应**：`{ "code": 200, "message": "评论成功", "data": { "commentId": 9001 } }`
**涉及表**：`dynamic_comment`、`dynamic_content`（评论数+1）、`user_notice`。
**规则**：禁言 → `1004`；敏感词 → `4001`；通知作者/被回复人（不自通知）。

---

### 2.25 评论列表

- **路径**：`GET /api/mini/dynamic/comment/list`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| dynamicId | Long | 是 | 动态 ID |
| pageNum / pageSize | Integer | 否 | 分页 |

**响应**（`records` 项）：`{ commentId, user:{userId,nickname,avatar}, content, parentId, replyUser:{userId,nickname}, createTime }`。
**涉及表**：`dynamic_comment`、`user_profile`。
**规则**：`status=2` 或已删不展示。

---

### 2.26 话题列表

- **路径**：`GET /api/mini/topic/list`
- **鉴权**：是
- **响应**：`[{ topicId, name, description, cover, hotValue }]`。
**涉及表**：`dynamic_topic`（`status=1`，按 `hot_value`/`sort` 排序）。

---

### 2.26.1 自拟话题（用户自建话题）

- **路径**：`POST /api/mini/topic/create`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 | 校验 |
|---|---|---|---|---|
| name | String | 是 | 话题名，可带前导 `#`，服务端自动清理 | 非空、≤20 字、过敏感词 |

**请求示例**：`{ "name": "我的周末计划" }`

**响应**：`{ "code": 200, "message": "话题已创建",
  "data": { "topicId": 9, "name": "我的周末计划", "description": "", "cover": "", "hotValue": 0 } }`

**规则**：
- 同名**启用**话题已存在时直接复用，不重复创建（幂等）。
- 新建话题 `status=1`、`hot_value=0`、`sort=已有最大 sort+1`（列表按 `hot_value` 倒序，故新话题排在末尾）。
- 禁言用户 → `R002`；命中敏感词 → `R010`。

**涉及表**：`dynamic_topic`。
**典型用法**：发布瞬间前先调本接口拿到 `topicId`，再带 `topicId` 调 `POST /api/mini/dynamic/publish`。

---

### 2.27 推荐用户

- **路径**：`GET /api/mini/match/recommend`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| pageNum / pageSize | Integer | 否 | 分页 |

**响应**（`records` 项）

| 字段 | 类型 | 说明 |
|---|---|---|
| userId | Long | 用户 ID |
| nickname | String | 昵称 |
| avatar | String | 头像 |
| age | Integer | 年龄 |
| city | String | 城市 |
| tags | Array\<String\> | 共同/热门标签 |
| matchScore | Integer | 匹配度 0-100 |
| reason | String | 推荐理由（如"都喜欢音乐"） |

**响应示例**
```json
{ "code": 200, "message": "success",
  "data": { "total": 20, "pageNum": 1, "pageSize": 10,
    "records": [ { "userId": 1002, "nickname": "小明", "avatar": "", "age": 24, "city": "深圳",
        "tags": ["音乐","电影"], "matchScore": 86, "reason": "都喜欢音乐" } ] } }
```
**涉及表**：`user_profile`、`user_privacy_setting`、`user_blacklist`、`user_dislike_record`、`user_tag`、`user_soul_test_record`、`match_record`。
**规则**：排除自己/拉黑/封禁/关闭推荐/不感兴趣。

---

### 2.28 不感兴趣

- **路径**：`POST /api/mini/match/dislike`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| targetUserId | Long | 是 | 目标用户 |
| reason | String | 否 | 原因 |

**请求示例**：`{ "targetUserId": 1002, "reason": "不合适" }`
**响应**：`{ "code": 200, "message": "已记录", "data": null }`
**涉及表**：`user_dislike_record`。
**规则**：自己 → `6003`。

---

### 2.29 发送打招呼

- **路径**：`POST /api/mini/greet/send`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| toUserId | Long | 是 | 目标用户 |
| content | String | 否 | 话术（默认话术则不传） |

**请求示例**：`{ "toUserId": 1002, "content": "嗨，看到你也喜欢音乐～" }`
**响应**：`{ "code": 200, "message": "打招呼成功", "data": { "sessionUuid": "1001_1002" } }`
**涉及表**：`greet_record`、`im_conversation`、`im_message`、`user_notice`。
**规则**：禁言 `1004`、拉黑 `6001`、敏感词 `4001`、每日上限 `7001`、同人当日重复 `7002`。

---

### 2.30 会话列表

- **路径**：`GET /api/mini/im/conversation/list`
- **鉴权**：是
- **分页参数**：`pageNum` / `pageSize`。
- **响应**（`records` 项）

| 字段 | 类型 | 说明 |
|---|---|---|
| sessionUuid | String | 会话标识 |
| targetUser | AuthorItem | `{userId,nickname,avatar}` |
| sessionType | Integer | 1普通 2打招呼 3AI |
| unreadCount | Integer | 未读数 |
| lastMsgPreview | String | 最后消息预览 |
| lastMsgTime | String | 最后消息时间 |
| topFlag | Boolean | 置顶 |

**响应示例**
```json
{ "code": 200, "message": "success",
  "data": { "total": 1, "pageNum": 1, "pageSize": 10,
    "records": [ { "sessionUuid": "1001_1002", "targetUser": { "userId": 1002, "nickname": "小明", "avatar": "" },
        "sessionType": 2, "unreadCount": 1, "lastMsgPreview": "嗨，看到你也喜欢音乐～",
        "lastMsgTime": "2026-10-14 10:01:00", "topFlag": false } ] } }
```
**涉及表**：`im_conversation`、`im_message`、`user_profile`、`user_blacklist`。
**规则**：过滤拉黑/隐藏。

---

### 2.31 消息列表

- **路径**：`GET /api/mini/im/message/list`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| sessionUuid | String | 是 | 会话标识 |
| pageNum / pageSize | Integer | 否 | 分页 |

**响应**（`records` 项）：`{ messageUuid, fromUserId, toUserId, content, contentType, sendStatus, readFlag, sendTime }`。
**涉及表**：`im_message`。
**规则**：会话归属校验 → 否则 `9002`。

---

### 2.32 发送消息

- **路径**：`POST /api/mini/im/message/send`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| sessionUuid | String | 是 | 会话标识 |
| content | String | 是 | 内容（≤500 字、敏感词） |

**请求示例**：`{ "sessionUuid": "1001_1002", "content": "你好呀" }`
**响应**：`{ "code": 200, "message": "发送成功", "data": { "messageUuid": "m-abc", "sendTime": "2026-10-14 10:02:00" } }`
**涉及表**：`im_message`、`im_conversation`（last_msg_preview、unread+1、sort_time）。
**规则**：禁言 `1004`、拉黑 `6001`、陌生人私信关闭 `6002`、敏感词 `4001`。

---

### 2.33 标记会话已读

- **路径**：`POST /api/mini/im/conversation/read`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| sessionUuid | String | 是 | 会话标识 |

**响应**：`{ "code": 200, "message": "已读", "data": null }`
**涉及表**：`im_conversation`（unread=0）、`im_message`（read_flag=1）。
**规则**：会话归属 → 否则 `9002`。

---

### 2.34 通知列表

- **路径**：`GET /api/mini/notice/list`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| noticeType | Integer | 否 | 类型筛选 |
| pageNum / pageSize | Integer | 否 | 分页 |

**响应**（`records` 项）：`{ noticeId, noticeType, title, content, fromUser:{userId,nickname,avatar}, targetType, targetId, readFlag, createTime }`。
**涉及表**：`user_notice`、`user_profile`。

---

### 2.35 通知已读

- **路径**：`POST /api/mini/notice/read`
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| noticeIds | Array\<Long\> | 否 | 指定通知；不传则全部已读 |

**请求示例**：`{ "noticeIds": [1,2] }`
**响应**：`{ "code": 200, "message": "已读", "data": null }`
**涉及表**：`user_notice`（`read_flag=1`）。
**规则**：归属校验 → 否则 `9001`。

---

### 2.36 文件上传

- **路径**：`POST /api/mini/upload/image`
- **说明**：`multipart/form-data`，用于头像与动态图片。
- **鉴权**：是

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| file | File | 是 | 图片文件 |
| bizType | String | 是 | `avatar` / `dynamic` |

**响应**：`{ "code": 200, "message": "上传成功", "data": { "url": "/upload/avatar/202610/a.png" } }`
**涉及表**：无（文件落盘）。
**规则**：后缀白名单、大小 ≤5MB。

---

## 三、后台接口（`/api/admin`）

### 3.0 后台登录

- **路径**：`POST /api/admin/auth/login`
- **说明**：后台管理员账号密码登录。与用户端 Token 隔离（JWT claim `scope=admin`）。
- **鉴权**：否（**白名单放行路径**：`/api/admin/auth/login`）

| 参数 | 类型 | 必填 | 说明 | 校验 |
|---|---|---|---|---|
| account | String | 是 | 管理员账号 | 非空 |
| password | String | 是 | 密码 | 非空 |

**请求示例**
```json
{ "account": "admin", "password": "123456" }
```
**响应参数**

| 字段 | 类型 | 说明 |
|---|---|---|
| token | String | 后台 JWT（claim `scope=admin`） |
| account | String | 管理员账号 |
| nickname | String | 昵称 |

**响应示例**
```json
{ "code": 200, "message": "登录成功",
  "data": { "token": "eyJhbGci...", "account": "admin", "nickname": "系统管理员" } }
```
**失败响应示例**（不区分账号不存在/密码错误，防枚举）
```json
{ "code": 1002, "message": "账号或密码错误", "data": null }
```
**涉及表**：`user`（`user_no` 承载 admin 账号或 `user_sensitive_info.phone='admin'`）、`user_sensitive_info`（`password` BCrypt 校验）、`user_login_log`（`login_type` 标记后台登录）。
**规则**：
1. 账号不存在或 `user.status!=1` → 统一返回 `1002 账号或密码错误`；
2. `PasswordUtil.matches(password, user_sensitive_info.password)` 校验 BCrypt，失败同上；
3. 成功签发 Token，claim `scope=admin`（用户端为 `scope=mini`），由 `AdminAuthInterceptor` 校验，两端 Token 互不通用；
4. 写 `user_login_log`（`login_type=3` 表示后台登录）。

---

### 3.1 标签分类列表

- **路径**：`GET /api/admin/tag/category/list`
- **鉴权**：是（admin）
- **响应**：`[{ categoryId, name, icon, sort, status }]`
**涉及表**：`tag_category`。

### 3.2 新增/编辑分类

- **路径**：`POST /api/admin/tag/category/save`
- **入参**：`{ categoryId(可选), name, icon, sort, status }`
**涉及表**：`tag_category`。

### 3.3 标签列表（后台）

- **路径**：`GET /api/admin/tag/list`
- **参数**：`categoryId`、`status`、`pageNum`、`pageSize`
- **响应**：分页 `{ tagId, categoryId, categoryName, name, hotFlag, matchFlag, recommendFlag, sort, status }`
**涉及表**：`tag`、`tag_category`。

### 3.4 新增/编辑标签

- **路径**：`POST /api/admin/tag/save`
- **入参**：`{ tagId(可选), categoryId, name, hotFlag, matchFlag, recommendFlag, sort, status }`
**涉及表**：`tag`。

### 3.5 删除标签

- **路径**：`POST /api/admin/tag/delete`
- **入参**：`{ tagId }`；逻辑删除。
**涉及表**：`tag`。

### 3.6 灵魂测试题目列表

- **路径**：`GET /api/admin/soulTest/question/list`
- **参数**：`status`、`pageNum`、`pageSize`
- **响应**：分页，含选项。
**涉及表**：`soul_test_question`、`soul_test_option`。

### 3.7 保存题目（含选项）

- **路径**：`POST /api/admin/soulTest/question/save`
- **入参**：
```json
{ "questionId": 1, "title": "...", "description": "...", "sort": 1, "status": 1,
  "options": [ { "optionId": 1, "content": "...", "resultCode": "E", "score": 3, "sort": 1 } ] }
```
**涉及表**：`soul_test_question`、`soul_test_option`。

### 3.8 结果类型列表

- **路径**：`GET /api/admin/soulTest/result/list`
- **响应**：`[{ resultId, resultCode, name, icon, description, socialAdvice }]`
**涉及表**：`soul_test_result`。

### 3.9 保存结果类型

- **路径**：`POST /api/admin/soulTest/result/save`
- **入参**：`{ resultId(可选), resultCode, name, icon, description, socialAdvice }`
**涉及表**：`soul_test_result`。

---

## 四、附录：接口-表映射汇总（本期主链路）

| 接口 | 主要涉及表 |
|---|---|
| auth/code, auth/login | user, user_sensitive_info, user_login_log |
| user/initStatus | user |
| user/profile* | user, user_profile, audit_record |
| user/home/{id} | user_profile, user_tag, user_soul_test_record, dynamic_content, user_privacy_setting, user_blacklist |
| user/privacy/update | user_privacy_setting |
| user/blacklist/* | user_blacklist |
| tag/list, user/tag/save | tag_category, tag, user_tag |
| soulTest/* | soul_test_question, soul_test_option, soul_test_result, user_soul_test_record |
| dynamic/publish | dynamic_content, dynamic_media, dynamic_topic_relation |
| dynamic/list, detail | dynamic_content, dynamic_media, dynamic_like, dynamic_collect |
| dynamic/delete | dynamic_content |
| dynamic/like | dynamic_like, dynamic_content, user_notice |
| dynamic/collect | dynamic_collect, dynamic_content |
| dynamic/comment* | dynamic_comment, dynamic_content, user_notice |
| topic/list | dynamic_topic |
| match/recommend | match_record, user_dislike_record, user_profile |
| match/dislike | user_dislike_record |
| greet/send | greet_record, im_conversation, im_message, user_notice |
| im/conversation/list | im_conversation, im_message |
| im/message/* | im_message, im_conversation |
| im/conversation/read | im_conversation, im_message |
| notice/* | user_notice, system_notice |
| upload/image | — |

*文档结束 — v1.0*
