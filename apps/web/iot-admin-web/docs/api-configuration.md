# 后端接口地址

开发环境默认读取 `.env.development`，将 `/api` 请求代理到
`http://127.0.0.1:8081`。启动时的环境变量优先于配置文件：

```sh
npm run dev
VITE_API_BASE_URL=http://127.0.0.1:8082 npm run dev
```

也可以创建 `.env.staging`（写入 `VITE_API_BASE_URL=后端地址`），
再通过 `npm run dev -- --mode staging` 选择配置。修改配置后需重启开发服务器。

生产构建可在 `.env.production` 或构建命令的环境变量中设置
`VITE_API_BASE_URL`；未设置时请求同源 `/api`，需要部署服务器转发该路径。
此地址会编入前端构建产物，请勿在其中放密钥。

当前页面请求 `GET /api/admin/product-categories`，读取统一响应的 `data`
字段并展示产品分类。
