# IoT Admin

管理平台后端


基于 Spring Boot、MyBatis-Plus 和 PostgreSQL。
Supabase 已提供 `postgres` 数据库。

```bash
DB_PASSWORD='your_supabase_database_password' mvn spring-boot:run
```

密码通过 `DB_PASSWORD` 环境变量提供，不要写进仓库；可通过 `DB_URL`、`DB_USERNAME` 覆盖其他连接参数。运行时不自动改动表结构。
测试使用 H2 的 PostgreSQL 兼容模式。

## 用户与认证

先在 PostgreSQL 中执行 `src/main/resources/db/schema.sql`。注册账号的 `role` 固定为 `0`；
首次管理员需要在可信的数据库管理环境中将指定账号的 `role` 改为 `1`：

```sql
UPDATE iot_user SET role = 1 WHERE email = 'admin@example.com';
```

Redis 用于保存 access token（默认 15 分钟）和一次性 refresh token（默认 7 天）。
可通过 `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD` 等环境变量配置连接，
通过 `AUTH_ACCESS_TOKEN_TTL`、`AUTH_REFRESH_TOKEN_TTL` 配置有效期。
客户端通过 `Authorization: Bearer <accessToken>` 访问需要认证的用户接口。

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| POST | `/api/auth/register` | 邮箱、密码注册；可选 `displayName` |
| POST | `/api/auth/login` | 邮箱、密码登录，返回 access/refresh token |
| POST | `/api/auth/refresh` | 使用 `refreshToken` 换取新的一对 token，旧 refresh token 失效 |
| GET | `/api/users/me` | 查询当前用户 |
| PATCH | `/api/users/me` | 修改当前用户 `displayName` |
| GET | `/api/admin/users?page=1&size=20` | 管理员分页查看用户 |
| GET | `/api/admin/users/{id}` | 管理员查看用户 |
| PATCH | `/api/admin/users/{id}/status` | 管理员启停账号，`status` 为 `0` 或 `1` |

密码只保存 BCrypt 哈希。禁用账号后，其已有 token 无法继续访问上述用户接口。
当前认证校验仅接入本节列出的用户接口，已有产品分类等接口尚未纳入权限控制。
