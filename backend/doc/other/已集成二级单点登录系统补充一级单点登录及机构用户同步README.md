# 已集成二级单点登录系统补充一级单点登录及机构用户同步 README

> 适用场景：业务系统已集成二级单点登录（政务中台/南威统一身份认证），现需补充集成一级单点登录（福建省数字身份管理服务平台），并开放机构、用户同步接口供二级单点登录推送。

---

## 一、整体架构与数据流转

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              用户访问层                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│    ┌─────────────┐                     ┌─────────────┐                     │
│    │  直接访问   │                     │  总门户访问  │                     │
│    │  业务系统   │                     │  (带参数)    │                     │
│    └──────┬──────┘                     └──────┬──────┘                     │
│           │                                    │                           │
│           ▼                                    ▼                           │
│    ┌─────────────────────────────────────────────────────────────┐        │
│    │                    业务系统（SSO网关层）                      │        │
│    │  ┌─────────────────────────────────────────────────────┐   │        │
│    │  │  登录策略判断：                                       │   │        │
│    │  │  1. URL参数 yjsso=true → 一级单点登录（中转模式）   │   │        │
│    │  │  2. 默认 → 二级单点登录                              │   │        │
│    │  └─────────────────────────────────────────────────────┘   │        │
│    └─────────────────────────────────────────────────────────────┘        │
│           │                                        │                        │
│           │ 1.一级单点登录                         │ 2.二级单点登录          │
│           ▼                                        ▼                        │
│    ┌─────────────────────┐              ┌─────────────────────┐            │
│    │  一级单点登录       │              │  二级单点登录       │            │
│    │  (省平台/IAM)       │              │  (政务中台)         │            │
│    └──────────┬──────────┘              └──────────┬──────────┘            │
│               │                                     │                       │
│               │ 授权码(code)                        │ 机构/用户同步        │
│               │ redirect_uri → 中转系统             │                      │
│               │ state=systemCode                    │                      │
│               ▼                                     ▼                      │
│    ┌─────────────────────────────────────────────────────────────┐        │
│    │              一级机构用户系统（中转系统）                       │        │
│    │  1. 接收一级单点回调的授权码                                   │        │
│    │  2. 根据 state 参数识别目标业务系统                            │        │
│    │  3. 将授权码转发给对应业务系统                                 │        │
│    └─────────────────────────────────────────────────────────────┘        │
│                                                                             │
│  机构/用户推送链：                                                          │
│  一级 IAM → 一级机构用户系统（中转） → 二级单点登录 → 各业务系统            │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.1 一级单点登录“中转模式”关键点

- 所有业务系统使用**同一套** `client_id`、`client_secret`、`redirect_uri`。
- `redirect_uri` 固定指向**一级机构用户系统（中转系统）**。
- 业务系统通过 `state` 参数（格式：`systemCode_timestamp`）被中转系统识别。
- 中转系统根据 `state` 中的 `systemCode` 将授权码 **转发** 到对应业务系统的 `/yjsso/ssoCode`。

### 1.2 机构/用户同步推送链

后续一级 IAM 平台把组织机构、用户数据推送到**一级机构用户系统（中转系统）**，中转系统再将数据推送到**二级单点登录系统**，最后由二级单点登录系统调用各业务系统开放的同步接口完成数据落地。

因此业务系统需要暴露以下两个接口给二级单点登录：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/sso/ssoSyncOrg` | POST | 接收机构同步数据 |
| `/sso/ssoSyncUser` | POST | 接收用户同步数据 |

---

## 二、一级单点登录接入（与二级单点登录类似）

一级单点登录接入流程与二级单点登录高度相似，区别主要在于：

| 对比项 | 二级单点登录 | 一级单点登录 |
|--------|-------------|-------------|
| 跳转授权页 | `/sso/oauth/authorize` | `/idp/authCenter/authenticate` |
| 回调地址 | 业务系统自己的 `/sso/ssoCode` | 固定到中转系统 `/yjsso/ssoCode`，再由中转系统转发 |
| 状态参数 | 无特殊 state | `state=systemCode_timestamp` |
| 获取 Token | `/sso/oauth/token` | `/ebus/ability_sszsfpt_zw/idp/oauth2/getTokenV2` |
| 获取用户信息 | `/sso/user/current` | `/ebus/ability_sszsfpt_zw/idp/oauth2/getUserInfoV2` |
| 手机号 | 明文 | SM4 加密，需解密 |
| 签名头 | 二级自有签名 | 一体化网关签名（x-rio-*） |

### 2.1 前端改造

#### 2.1.1 环境变量配置

在 `.env.development` / `.env.production` / `.env.jnw` 中新增一级单点配置：

```properties
# 一级单点登录设置
VITE_APP_YJSSO_LOGIN = 'true'
VITE_APP_YJSSO_BASE_URL = 'https://allinone-public.e-govt.cn:9080'
VITE_APP_YJSSO_CLIENT_ID = 'fht'
VITE_APP_YJSSO_REDIRECT_URI = 'http://your-domain/yjsso/ssoCode'
VITE_APP_YJSSO_SYSTEM_CODE = 'your_system_code'
```

> `VITE_APP_YJSSO_SYSTEM_CODE` 需与中转系统配置的目标系统编码一致。

#### 2.1.2 路由改造 `src/router/index.js`

在 `router.beforeEach` 的 `if (!token)` 逻辑中，**二级单点登录判断之前**增加一级单点登录判断：

```javascript
// ========== 一级单点登录判断 ==========
const urlParams = new URLSearchParams(window.location.search);
let yjssoParam = urlParams.get('yjsso');
if (!yjssoParam && window.location.hash.includes('?')) {
    const hashParams = new URLSearchParams(window.location.hash.split('?')[1]);
    yjssoParam = hashParams.get('yjsso');
}

if (import.meta.env.VITE_APP_YJSSO_LOGIN === 'true' && yjssoParam === 'true') {
    if (to.name === 'yjSsoLogin') {
        next();
        return false;
    }
    const state = `${import.meta.env.VITE_APP_YJSSO_SYSTEM_CODE}_${Date.now()}`;
    const authorizeUrl = `${import.meta.env.VITE_APP_YJSSO_BASE_URL}/idp/authCenter/authenticate` +
        `?client_id=${import.meta.env.VITE_APP_YJSSO_CLIENT_ID}` +
        `&response_type=code` +
        `&redirect_uri=${import.meta.env.VITE_APP_YJSSO_REDIRECT_URI}` +
        `&state=${state}`;
    window.location.href = authorizeUrl;
    return false;
}
// ========== 一级单点登录判断结束 ==========
```

> 必须把一级单点判断放在二级单点判断之前，并且允许 `yjSsoLogin` 页面直接通过，避免回调后再次跳转授权页导致死循环。

#### 2.1.3 新增回调页面 `src/views/common/YjSsoLogin.vue`

参照已有的 `SsoLogin.vue`，新增 `YjSsoLogin.vue`：

- 从 URL 的 `search` 或 `hash` 中取出 `st`（ticket）。
- 调用后端 `/yjsso/ssoLogin?ticket=xxx`。
- 登录成功保存 token、用户信息、菜单权限，跳转首页。
- 登录失败由 `httpRequest.js` 401 拦截器统一处理，回跳到 `/login?yjsso=true`，再走一级授权流程。

示例核心代码：

```javascript
mounted() {
    this.yjSsoLogin()
},
methods: {
    async yjSsoLogin() {
        let st = this.getTicket()
        this.$http({
            url: '/yjsso/ssoLogin',
            method: 'get',
            params: { 'ticket': st }
        }).then(async (data) => {
            this.$TOOL.data.set(config.TOKEN, data.token);
            this.$cookies.set(config.TOKEN, data.token, -1);
            // ... 保存用户信息、菜单、权限
            this.$router.replace({ path: "/home/home" });
        }).catch((error) => {
            console.log('yjSsoLogin 请求失败，已由全局拦截器处理', error)
        })
    },
    getTicket() {
        let url = new URL(document.location.toString())
        let st = url.searchParams.get('st')
        if (!st && url.hash.includes('?')) {
            const hashParams = new URLSearchParams(url.hash.split('?')[1])
            st = hashParams.get('st')
        }
        return st
    }
}
```

#### 2.1.4 路由注册

在 `src/router/systemRouter.js` 或其他路由注册文件中新增：

```javascript
{
    path: '/yjSsoLogin',
    component: () => import('@/views/common/YjSsoLogin'),
    name: 'yjSsoLogin',
    meta: { title: '一级单点登录' }
}
```

#### 2.1.5 接口失败回跳 `src/utils/httpRequest.js`

确保 401 拦截器中区分一级/二级单点登录失败：

```javascript
} else if (error.response.status === 401) {
    ElNotification.error({ title: "登录失败", message: error.response.data });
    clearLoginInfo();
    if (error.config.url && error.config.url.includes('/yjsso/ssoLogin')) {
        router.replace({path: "/login", query: {yjsso: 'true'}});
    } else if (error.config.url && error.config.url.includes('/sso/ssoLogin')) {
        router.replace({path: "/login", query: {comsso: 'true'}});
    } else {
        router.replace({path: "/login"});
    }
}
```

### 2.2 后端改造

#### 2.2.1 引入 `zhhy-fj-sso-1.0.0.jar`

在 `pom.xml` 中新增依赖：

```xml
<dependency>
    <groupId>com.linewell</groupId>
    <artifactId>zhhy-fj-sso</artifactId>
    <version>1.0.0</version>
    <systemPath>${basedir}/src/main/webapp/WEB-INF/lib/zhhy-fj-sso-1.0.0.jar</systemPath>
    <scope>system</scope>
</dependency>
<dependency>
    <groupId>commons-lang</groupId>
    <artifactId>commons-lang</artifactId>
    <version>2.6</version>
</dependency>
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.8.26</version>
</dependency>
```

#### 2.2.2 配置文件 `application-xxx.yml`

在已有二级单点配置旁新增一级单点配置：

```yaml
yjsso:
  # 是否启用一级单点登录
  enabled: true
  # 基础地址
  baseUrl: https://allinone-public.e-govt.cn:9080
  # 前端地址（登录成功后跳转）
  frontUrl: http://your-domain/yjsso-ui
  # === 闽政务OAuth配置 ===
  clientId: fht
  clientSecret: your_client_secret
  tokenUrl: ${yjsso.baseUrl}/ebus/ability_sszsfpt_zw/idp/oauth2/getTokenV2
  userInfoUrl: ${yjsso.baseUrl}/ebus/ability_sszsfpt_zw/idp/oauth2/getUserInfoV2
  refreshTokenUrl: ${yjsso.baseUrl}/ebus/ability_sszsfpt_zw/idp/oauth2/refreshTokenV2
  tokenInfoUrl: ${yjsso.baseUrl}/ebus/ability_sszsfpt_zw/idp/oauth2/getTokenInfoV2
  checkTokenValidUrl: ${yjsso.baseUrl}/ebus/ability_sszsfpt_zw/idp/oauth2/checkTokenValidV2
  revokeTokenUrl: ${yjsso.baseUrl}/ebus/ability_sszsfpt_zw/idp/oauth2/revokeTokenV2
  globalLogoutUrl: ${yjsso.baseUrl}/idp/authCenter/GLO
  # === 登录 + 同步共用配置 ===
  sm4key: your_sm4_key_16bit
  ythPassid: your_yth_passid
  ythToken: your_yth_token
  # 是否跳过签名（生产环境必须 false）
  skipSign: false
```

#### 2.2.3 新增一级单点登录控制器

新建 `com.jeeplus.yjsso.YjSsoController`，至少实现以下接口：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/yjsso/ssoCode` | GET | 接收授权码，换取 access_token，生成 ticket，重定向前端 `/yjsso/ssoLogin?st=xxx` |
| `/yjsso/ssoLogin` | GET | 根据 ticket 从 Redis 取出 access_token，获取用户信息，完成本地登录 |
| `/yjsso/ssoLogout` | GET | 清除本地会话，可再重定向到 IAM 全局退出页 |

核心逻辑：

```java
@Controller
public class YjSsoController {

    @Autowired
    private YjSsoUtil yjSsoUtil;

    @Value("${yjsso.frontUrl}")
    protected String frontUrl;

    /**
     * 接收授权码回调
     */
    @RequestMapping(value = "/yjsso/ssoCode", method = RequestMethod.GET)
    public String ssoCode(@RequestParam String code,
                          @RequestParam(required = false) String state) throws Exception {
        String accessToken = yjSsoUtil.getAccessToken(code);
        if (StringUtils.isBlank(accessToken)) {
            throw new Exception("获取 accessToken 失败!");
        }
        String ticket = accessToken + System.currentTimeMillis();
        RedisUtils.set(CacheNames.USER_CACHE_OTHER, ticket, accessToken);
        return "redirect:" + frontUrl + "/#/yjSsoLogin?st=" + ticket;
    }

    /**
     * 根据 ticket 登录
     */
    @ResponseBody
    @RequestMapping("/yjsso/ssoLogin")
    public ResponseEntity ssoLogin(@RequestParam(name = "ticket") String ticket,
                                   HttpServletRequest request) throws Exception {
        // 1. 取 accessToken
        String accessToken = String.valueOf(RedisUtils.get(CacheNames.USER_CACHE_OTHER, ticket));
        RedisUtils.delete(CacheNames.USER_CACHE_OTHER, ticket);
        if (StringUtils.isBlank(accessToken) || "null".equals(accessToken)) {
            throw new Exception("ticket 无效或已过期!");
        }

        // 2. 获取并解析用户信息
        String userJsonStr = yjSsoUtil.getUserInfo(accessToken);
        JSONObject json = JSONUtil.parseObj(userJsonStr);
        String mobileStr = json.getStr("mobile", "");

        // 3. 解密手机号
        String mobile = yjSsoUtil.decryptMobile(mobileStr);
        if (StringUtils.isBlank(mobile)) {
            throw new Exception("无法获取用户手机号!");
        }

        // 4. 根据手机号查询本地用户并登录
        User user = userService.getByMobile(mobile);
        if (user == null) {
            throw new UsernameNotFoundException(ErrorConstants.LOGIN_ERROR_NOTFOUND);
        }
        // ... 校验状态、生成 token、更新登录信息
        return responseUtil.ok();
    }
}
```

> 注意：中转模式下 `/yjsso/ssoCode` 可能被中转系统直接调用，因此**不需要**在业务系统前端路由里注册该路径。

---

## 三、二级单点登录机构/用户同步接口补充

### 3.1 为什么需要补充

后续数据推送链路为：

```
一级 IAM 平台
    ↓ 推送
一级机构用户系统（中转系统）
    ↓ 推送
二级单点登录系统（政务中台）
    ↓ 推送
各业务系统 /sso/ssoSyncOrg、/sso/ssoSyncUser
```

因此业务系统必须暴露机构、用户同步接口，供二级单点登录调用。

### 3.2 新增同步控制器

新建或复制 `com.jeeplus.sso.SsoSyncDataToBusiness.java`，实现以下两个接口：

#### 3.2.1 机构同步接口 `/sso/ssoSyncOrg`

```java
@ResponseBody
@RequestMapping(value = "sso/ssoSyncOrg")
public boolean ssoSyncOrg(HttpServletRequest request, HttpServletResponse response) throws Exception {
    boolean result = true;
    try {
        String body = readBodyAsString(request);
        JSONObject bodyJson = JSONObject.parseObject(body);
        String type = bodyJson.getString("type");
        if ("ORG".equals(type)) {
            String operType = bodyJson.getString("operType");
            String object = bodyJson.getString("object");
            JSONArray objectArr = JSONObject.parseArray(object);
            if ("INSERT".equals(operType)) {
                for (int i = 0; i < objectArr.size(); i++) {
                    saveOrg(objectArr.getJSONObject(i));
                }
            } else if ("UPDATE".equals(operType) || "INSERT_UPDATE".equals(operType)) {
                for (int i = 0; i < objectArr.size(); i++) {
                    JSONObject json = objectArr.getJSONObject(i);
                    Office office = officeMapper.selectById(json.getString("id"));
                    if (null == office) {
                        saveOrg(json);
                    } else {
                        updateOrg(office, json);
                    }
                }
            } else if ("DELETE".equals(operType)) {
                for (int i = 0; i < objectArr.size(); i++) {
                    JSONObject json = objectArr.getJSONObject(i);
                    Office office = officeMapper.selectById(json.getString("id"));
                    if (null != office) {
                        officeMapper.deleteById(office);
                    }
                }
            }
        }
    } catch (Exception e) {
        log.error("同步 SSO 机构出现错误", e);
        result = false;
    }
    return result;
}
```

#### 3.2.2 用户同步接口 `/sso/ssoSyncUser`

```java
@ResponseBody
@RequestMapping(value = "sso/ssoSyncUser")
public boolean ssoSyncUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
    boolean result = true;
    try {
        String body = readBodyAsString(request);
        JSONObject bodyJson = JSONObject.parseObject(body);
        String type = bodyJson.getString("type");
        if ("USER".equals(type)) {
            String operType = bodyJson.getString("operType");
            String object = bodyJson.getString("object");
            JSONArray objectArr = JSONObject.parseArray(object);
            if ("INSERT".equals(operType)) {
                for (int i = 0; i < objectArr.size(); i++) {
                    saveUser(objectArr.getJSONObject(i));
                }
            } else if ("UPDATE".equals(operType) || "INSERT_UPDATE".equals(operType)) {
                for (int i = 0; i < objectArr.size(); i++) {
                    JSONObject json = objectArr.getJSONObject(i);
                    User user = userMapper.selectById(json.getString("id"));
                    if (null == user) {
                        saveUser(json);
                    } else {
                        updateUser(user, json);
                        UserUtils.deleteCache(user);
                    }
                }
            } else if ("DELETE".equals(operType)) {
                for (int i = 0; i < objectArr.size(); i++) {
                    JSONObject json = objectArr.getJSONObject(i);
                    User user = userMapper.selectById(json.getString("id"));
                    if (null != user) {
                        userMapper.deleteById(user);
                        UserUtils.deleteCache(user);
                    }
                }
            }
        }
    } catch (Exception e) {
        log.error("同步 SSO 用户出现错误", e);
        result = false;
    }
    return result;
}
```

### 3.3 请求报文格式

二级单点登录推送的请求体为 JSON，外层结构统一如下：

```json
{
    "type": "ORG" | "USER",
    "operType": "INSERT" | "UPDATE" | "INSERT_UPDATE" | "DELETE",
    "object": "[{}, {}, ...]"
}
```

#### 3.3.1 机构对象字段

| 字段 | 说明 |
|------|------|
| `id` | 机构ID |
| `parentId` | 父机构ID，根节点一般为 `ROOT` |
| `name` | 机构名称 |
| `shortName` | 机构简称 |
| `deptCode` | 机构编码 |
| `status` | 状态 |
| `regionCode` | 区域编码 |
| `sortNum` | 排序号 |

#### 3.3.2 用户对象字段

| 字段 | 说明 |
|------|------|
| `id` | 用户ID |
| `userName` | 用户名/登录名 |
| `realName` | 真实姓名 |
| `orgIds` | 所属机构ID，多个以逗号分隔 |
| `phone` | 手机号 |
| `email` | 邮箱 |
| `gender` | 性别 |
| `status` | 状态，`1` 通常表示启用 |
| `dataSrc` | 数据来源 |

> 实际字段以二级单点登录推送为准，上述为常见字段。

### 3.4 在二级单点登录管理后台注册接口

登录二级单点登录管理后台，找到**同步管理**或**应用管理**模块，将以下地址录入到对应应用的同步配置中：

```
http://your-domain/sso/ssoSyncOrg
http://your-domain/sso/ssoSyncUser
```

并配置推送的数据格式（旧版/新版）、同步数据类型、加密方式等。

---

## 四、验证清单

### 4.1 一级单点登录验证

- [ ] 前端 `.env` 中 `VITE_APP_YJSSO_LOGIN=true` 且其他参数正确。
- [ ] 访问 `http://your-domain/#/?yjsso=true` 能重定向到一级授权页。
- [ ] 在一级授权页登录后，能回到中转系统，再由中转系统转发授权码到业务系统 `/yjsso/ssoCode`。
- [ ] 业务系统 `/yjsso/ssoCode` 能成功换取 access_token 并重定向前端 `/yjSsoLogin?st=xxx`。
- [ ] 前端 `/yjsso/ssoLogin` 调用成功，本地用户已登录。
- [ ] 登录失败时（如用户不存在）能回跳到 `/login?yjsso=true` 重新走一级授权，而不是走到二级授权。

### 4.2 机构/用户同步验证

- [ ] 二级单点登录管理后台已配置本系统的 `/sso/ssoSyncOrg` 和 `/sso/ssoSyncUser` 地址。
- [ ] 手动触发机构同步，业务系统能正确接收并保存机构数据。
- [ ] 手动触发用户同步，业务系统能正确接收并保存用户数据。
- [ ] 验证 INSERT / UPDATE / DELETE 三种操作类型都能正确处理。
- [ ] 验证一级 IAM → 中转系统 → 二级单点 → 业务系统 的全链路推送能正常流转。

---

## 五、常见问题

### 5.1 一级单点登录回调后死循环

- 检查 `router/index.js` 中是否允许 `yjSsoLogin` 页面直接通过。
- 检查 `state` 是否包含 `systemCode_timestamp` 格式。
- 检查中转系统是否正确识别 `systemCode` 并转发授权码。

### 5.2 手机号解密失败

- 确认 `yjsso.sm4key` 与 IAM 平台约定的一致（16位）。
- 确认 IAM 返回的 `mobile` 字段确实是 SM4 密文。部分环境可能返回明文，解密失败时会保留原值。

### 5.3 同步接口返回 true 但数据没更新

- 检查 `type` 字段是否为 `ORG` / `USER`。
- 检查 `operType` 是否匹配 INSERT / UPDATE / INSERT_UPDATE / DELETE。
- 检查 `object` 是否为 JSON 数组字符串。
- 查看业务系统日志是否有异常被吞掉。

### 5.4 一级登录失败跳到了二级授权页

- 确认部署的前端包包含 `httpRequest.js` 的 401 拦截器分支：
  - `/yjsso/ssoLogin` 失败 → `/login?yjsso=true`
  - `/sso/ssoLogin` 失败 → `/login?comsso=true`
- 确认后端 `/yjsso/ssoLogin` 返回的是 401 而不是 500。

---

## 六、相关文档

| 文档 | 路径 |
|------|------|
| 一级单点登录jar接入 | `E:\projects\海渔\zhhy_fj_sso\一级单点登录jar接入.docx` |
| 二级单点登录jar接入 | `E:\projects\海渔\zhhy_fj_sso\二级单点登录jar接入.docx` |
| 一二级单点登录集成方案 | `E:\idea-workspace\yjsso\docs\单点登录\一二级单点登录集成方案.md` |
| 一级单点登录集成改造计划 | `E:\idea-workspace\yjsso\docs\单点登录\一级单点登录集成改造计划.md` |
| 福建省数字身份管理服务平台技术规范 | `E:\idea-workspace\yjsso\docs\单点登录\一级单点\04 福建省数字身份管理服务平台技术规范...` |
