# 一张图应用

## 运行时配置（区分测试 / 部署）

| 文件 | 作用 |
|------|------|
| `public/config.json` | 环境开关，只改这一处：`{ "env": "test" }` 或 `{ "env": "prod" }` |
| `public/config.test.json` | 测试环境完整配置（如 fzsqi 接口、公网天地图） |
| `public/config.prod.json` | 局里部署完整配置（如 172.26 接口、代理天地图） |

部署后改 JSON 即可，**无需重新打包**。  
例外：修改任一环境里的 **`prefix`** 后仍需重新打包（影响 Vite `base`）。

切到局里部署时，把 `config.json` 改为：

```json
{ "env": "prod" }
```

## 一级单点相关

- `one_base` / `one_client_id`：省平台授权地址与 client_id
- `one_system_code`：写入 `state=systemCode_timestamp`，须与中转系统配置一致
- `one_redirect_uri`：授权回调地址（中转模式应指向中转系统 `/yjsso/ssoCode`）
- 入口：`?yjsso=true`；回调页：`/yjSsoLogin?st=xxx`
