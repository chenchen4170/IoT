# iot-parent

IoT 后端项目的 Maven 父工程。

## 模块

- `iot-parent`：配置管理及公共依赖，不聚合业务模块
- `iot-admin`：管理平台后端，默认端口 `8081`
- `iot-app`：App 后端，默认端口 `8082`

## 构建

三个项目为同级目录。先安装父项目：

```bash
cd iot-parent
mvn clean install
```

再分别启动业务项目：

```bash
cd ../iot-admin
mvn spring-boot:run

cd ../iot-app
mvn spring-boot:run
```
