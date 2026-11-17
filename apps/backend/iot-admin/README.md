# IoT Admin

管理平台后端


基于 Spring Boot、MyBatis-Plus 和 PostgreSQL。
Supabase 已提供 `postgres` 数据库。

```bash
DB_PASSWORD='your_supabase_database_password' mvn spring-boot:run
```

密码通过 `DB_PASSWORD` 环境变量提供，不要写进仓库；可通过 `DB_URL`、`DB_USERNAME` 覆盖其他连接参数。运行时不自动改动表结构。
测试使用 H2 的 PostgreSQL 兼容模式。
