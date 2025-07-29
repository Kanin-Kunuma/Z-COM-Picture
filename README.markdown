# 矩阵云端素材库

欢迎体验我的“矩阵云端素材库”！这是一个由我独立打造的编程学习与素材分享平台，旨在通过云端技术，帮你探索编程世界、收集灵感素材，伴你成长为编程达人！

## 项目概述

- **核心理念**：我希望打造一个独一无二的云端素材库，结合我的创意，融合学习资源和个人项目经验，让编程之旅更有趣。
- **技术栈**：前端用 Vue3 构建，后端基于 SpringBoot，数据库选用 MySQL 和 Redis，Nginx 优化服务器性能。

**目标**：不仅仅是素材存储，更是我对编程学习与分享的独特诠释，未来计划加入更多互动功能。\
\
**项目架构图**

## 功能特性

- **云端分享**：提供个人素材上传和浏览功能，打造属于你的知识宝库。
- **团队空间**：企业可开通团队空间，可邀请其它用户一起共享和实时编辑图片
- **高效管理**：通过缓存和数据库优化，素材访问更流畅。

## 安装与运行

### 前置条件

- Node.js (v16 或以上)
- Java JDK (v17 或以上)
- MySQL (v8.0 或以上)
- Redis (v6.0 或以上)
- Nginx (已配置)

### 安装步骤

1. **克隆我的仓库**：

   ```
   git clone https://github.com/Kanin-Kunuma/Z-COM-Picture.git
   cd Z-COM-Picture
   ```

2. **安装前端依赖**：

   ```
   cd "Z-COM PictureFrontkend"
   npm install
   # 或者使用 
   yarn install
   npm run build
   ```

3. **配置后端**：

   - 编辑 `src/main/resources/application.properties`，设置数据库和 Redis：

     ```
     spring.datasource.url=jdbc:mysql://localhost:3306/最好是自己的数据库名字
     spring.datasource.username=root
     spring.datasource.password=root
     spring.redis.host=localhost
     spring.redis.port=6379
     ```

4. **运行项目**：

   - 启动 MySQL 和 Redis。
   - 运行 SpringBoot：

     ```
     mvn spring-boot:run
     ```
   - 部署前端到 Nginx。

5. **访问**：

   - 打开浏览器，访问 `http://localhost` 或我的域名。

## 文件结构

```
MatrixCloudResource/
├── UserFrontEnd/         # 我的 Vue3 前端代码
├── BackEnd/             # SpringBoot 后端代码
├── README.md            # 项目说明
└── resources/           # 素材和文档
```

## 

“矩阵云端素材库”是我从编程热情出发，结合云端技术的个人项目。我希望它像一个“矩阵”般连接知识点，为你提供无限可能。未来，我计划优化前端页面，加入云上画板和AI搜图功能，敬请期待！

## 贡献指南

1. Fork 我的仓库。
2. 创建功能分支 (`git checkout -b feature/`)。
3. 提交代码 (`git commit -m ""`)。
4. 推送分支 (`git push origin feature/`)。
5. 提交 Pull Request，告诉我你的想法！

## 