# IoT Admin

管理平台后端

## 用户接口

- `POST /api/admin/user/register`：提交 `username`、`password`、`confirmPassword`，返回用户 ID、用户名和创建/更新时间。
- `POST /api/admin/user/login`：提交 `username`、`password`，返回 `id`、`username`、`token`、`refreshToken`。
- `POST /api/admin/user/refreshToken`：提交 `refreshToken`，返回新的 `token` 和 `refreshToken`。

`token` 是 HS256 签名的 JWT，`refreshToken` 是随机令牌。Redis 为每个用户只保存当前会话；
再次登录或刷新会使旧 JWT 和旧 refresh token 失效；refresh token 只能刷新一次。除健康检查、注册、登录和刷新外，
`/api/admin/**` 请求需携带 `Authorization: Bearer <token>`，服务端会同时检查 JWT 和 Redis 会话。
登录和鉴权需要可用的 Redis（可配置 `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`）。
生产环境需配置 `AUTH_JWT_SECRET` 为至少 32 字节随机密钥的 Base64 编码；
未配置时使用进程内随机密钥，重启会使旧 JWT 失效。
access token 默认有效期 15 分钟，refresh token 默认有效期 7 天，可通过
`AUTH_ACCESS_TOKEN_TTL` 和 `AUTH_REFRESH_TOKEN_TTL` 调整。
