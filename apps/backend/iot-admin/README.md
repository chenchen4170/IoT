# IoT Admin

管理平台后端

## 用户接口

- `POST /api/admin/user/register`：提交 `username`、`password`、`confirmPassword`，返回用户 ID、用户名和创建/更新时间。
- `POST /api/admin/user/login`：提交 `username`、`password`，返回 `id`、`username`、`token`、`refreshToken`。

登录使用 Redis 保存随机令牌，需配置可用的 `REDIS_HOST`、`REDIS_PORT`（及按需配置 `REDIS_PASSWORD`）。
access token 默认有效期 15 分钟，refresh token 默认有效期 7 天，可通过
`AUTH_ACCESS_TOKEN_TTL` 和 `AUTH_REFRESH_TOKEN_TTL` 调整。
