# UserManagement 项目

欢迎来到 UserManagement 项目！这是一个基于 Vue3 前端和 SpringBoot 后端的用户管理系统，集成了 MySQL 数据库和 Redis 缓存，使用 Nginx 作为服务器代理。

## 项目概述

- **前端**：使用 Vue3 框架，负责用户界面和交互。
- **后端**：基于 SpringBoot 开发，提供 API 接口和业务逻辑。
- **数据库**：MySQL 用于存储用户数据，Redis 用于缓存提升性能。
- **服务器**：Nginx 作为反向代理，优化请求处理。

## 功能特性

- 用户登录和认证。
- 通过 API 访问数据库查询用户信息。
- 支持缓存加速登录态管理。

## 安装与运行

### 前置条件

- Node.js (v16 或以上)
- Java JDK (v17 或以上)
- MySQL (v8.0 或以上)
- Redis (v6.0 或以上)
- Nginx (已配置)

### 安装步骤

1. **克隆仓库**：
   ```
   git clone https://github.com/Kanin-Kunuma/UserManagement.git
   cd UserManagement
   ```

2. **安装前端依赖**：
   ```
   cd UserFrontEnd
   npm install
   npm run build
   ```

3. **配置后端**：
   - 修改 `src/main/resources/application.properties` 文件，设置 MySQL 和 Redis 连接信息。
   - 示例：
     ```
     spring.datasource.url=jdbc:mysql://localhost:3306/usermanagement
     spring.datasource.username=root
     spring.datasource.password=yourpassword
     spring.redis.host=localhost
     spring.redis.port=6379
     ```

4. **运行项目**：
   - 启动 Redis 和 MySQL 服务。
   - 运行 SpringBoot 应用：
     ```
     mvn spring-boot:run
     ```
   - 部署前端到 Nginx（参考 Nginx 配置）。

5. **访问**：
   - 打开浏览器，访问 `http://localhost` 或 `https://www.z-com.icu`。

## 文件结构

```
UserManagement/
├── UserFrontEnd/         # Vue3 前端代码
├── BackEnd/             # SpringBoot 后端代码
├── README.md            # 项目说明
└── LICENSE              # 许可证（可选）
```

## 贡献指南

1. Fork 本仓库。
2. 创建你的功能分支 (`git checkout -b feature/xxx`)。
3. 提交代码 (`git commit -m "添加新功能"`)。
4. 推送分支 (`git push origin feature/xxx`)。
5. 提交 Pull Request。

## 许可证

[MIT License](LICENSE)（可选，替换为实际许可证）。

## 联系方式

有问题或建议？欢迎通过 [GitHub Issues](https://github.com/Kanin-Kunuma/UserManagement/issues) 联系我！