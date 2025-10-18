
# EL-Admin 后台管理系统

## 项目简介

EL-Admin 是一个基于 Spring Boot + elenent UI 开发的轻量级后台管理系统脚手架，开箱即用,集成了用户管理、权限控制、操作日志等常用功能。系统采用 RBAC（基于角色的访问控制）模型，支持 Google Authenticator 双因素认证

## 技术栈

### 后端技术
- **Spring Boot 2.3.3** - 核心框架
- **Sa-Token 1.41.0** - 认证授权框架
- **MyBatis-Plus 3.5.2** - ORM框架
- **MySQL 5.5+** - 数据库
- **Redis** - Token缓存
- **Google Authenticator** - 双因素认证
- **AOP** - 操作日志切面

### 前端技术
- **Vue.js 2.x** - 前端框架
- **Element UI** - UI组件库
- **Axios** - HTTP请求库
- **ECharts** - 图表库

## 功能特性

### 🔐 用户认证
- 用户注册/登录（密码AES加密）
- Google Authenticator 双因素认证
- Token认证（Sa-Token）
- 单设备登录限制
- Session超时管理（10分钟）
- 登录IP记录

### 👥 用户管理
- 用户信息增删改查
- 批量冻结/解冻用户
- 用户角色分配
- 密码加密存储
- 注册IP和登录IP追踪
- 封禁原因记录

### 🔑 权限管理
- RBAC权限模型（用户-角色-权限）
- 动态角色分配
- 细粒度权限控制
- 注解式权限验证
- 角色权限批量配置
- 权限继承机制

### 📝 操作日志
- AOP自动记录操作日志
- 操作描述注解支持（@OperationDesc）
- 支持排除特定方法（@NotLog）
- 日志分类（用户操作/钱包操作/其他）
- IP地址记录
- 执行时间统计
- 请求参数记录

### 🛡️ 系统安全
- AES密码加密
- 统一异常处理
- 参数校验（JSR-303）
- XSS防护
- SQL注入防护
- 敏感信息脱敏

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 5.5+
- Redis 3.0+
- Node.js 12+ (前端开发)

### 安装步骤

#### 1. 克隆项目
```bash
git clone [项目地址]
cd eladmin
```

#### 2. 创建数据库
```sql
-- 创建数据库
CREATE DATABASE `eladmin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 导入数据结构
USE eladmin;
SOURCE src/main/resources/eladmin.sql;
```

#### 3. 修改配置文件
编辑 `src/main/resources/application.yml`：
```yaml
server:
  port: 8081

spring:
  # 数据库配置
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/eladmin?serverTimezone=Asia/Shanghai
    username: eladmin
    password: eladmin
    
  # Redis配置
  redis:
    host: 127.0.0.1
    port: 6379
    database: 1
    # password: your_password  # 如有密码请取消注释

# Sa-Token配置
sa-token:
  token-name: satoken
  timeout: 600  # token有效期（秒）
  active-timeout: 300  # token最低活跃时间（秒）
  is-concurrent: false  # 不允许多地同时登录

# AES加密密钥
aes:
  secret:
    key: evan123456789012  # 生产环境请更换
```

#### 4. 启动项目
```bash
# Maven方式启动
mvn clean compile
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/springboot-USDT-TRC20-V3.jar
```

#### 5. 访问系统
- 访问地址：http://localhost:8081
- 登录页面：http://localhost:8081/login.html
- 默认管理员：admin / 123456

## 项目结构
```
eladmin/
├── src/main/java/com/itheima/
│   ├── config/                      # 配置类
│   │   ├── Operation/               # 操作日志注解
│   │   │   ├── NotLog.java         # 排除日志注解
│   │   │   ├── OperationDesc.java  # 操作描述注解
│   │   │   └── OperationLogAspect.java # 日志切面
│   │   ├── AESConfig.java          # AES加密配置
│   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   ├── MyBatisPlusConfig.java  # MyBatis-Plus配置
│   │   └── StpInterfaceImpl.java   # Sa-Token权限实现
│   ├── controller/                  # 控制器层
│   │   ├── admin/                   # 管理员功能
│   │   │   ├── UserManageController.java    # 用户管理
│   │   │   ├── RoleManageController.java    # 角色管理
│   │   │   └── PermissionController.java    # 权限管理
│   │   ├── UserController.java     # 用户功能
│   │   └── UserOperationLogController.java  # 操作日志
│   ├── mapper/                      # 数据访问层
│   │   ├── user/                    # 用户相关
│   │   │   ├── UserMapper.java
│   │   │   ├── RoleMapper.java
│   │   │   └── PermissionMapper.java
│   │   └── log/                     # 日志相关
│   │       └── UserOperationLogMapper.java
│   ├── pojo/                        # 实体类
│   │   ├── Users.java              # 用户实体
│   │   └── UserOperationLog.java   # 操作日志实体
│   ├── utils/                       # 工具类
│   │   ├── AESUtil.java            # AES加密工具
│   │   └── GoogleAuthe/            # Google认证
│   │       ├── GoogleAuthenticator.java
│   │       └── TOTP.java
│   └── SaTokenDemoApplication.java  # 启动类
├── src/main/resources/
│   ├── application.yml              # 配置文件
│   ├── eladmin.sql                 # 数据库脚本
│   ├── logback.xml                 # 日志配置
│   └── static/                      # 静态资源
│       ├── admin/                   # 管理页面
│       │   ├── users.html          # 用户管理
│       │   ├── role.html           # 角色管理
│       │   ├── permission.html     # 权限管理
│       │   └── userlog.html        # 操作日志
│       ├── user/                    # 用户页面
│       │   ├── user.html           # 个人资料
│       │   └── userHome.html       # 用户主页
│       ├── index.html              # 系统主页
│       ├── login.html              # 登录页面
│       └── register.html           # 注册页面
└── pom.xml                          # Maven配置
```

## 数据库设计

### 核心表结构

#### users - 用户表
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | INT | 用户ID（主键） |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(200) | 密码（AES加密） |
| email | VARCHAR(100) | 邮箱（唯一） |
| google_secret_key | VARCHAR(255) | Google认证密钥 |
| registration_ip | VARCHAR(50) | 注册IP |
| last_login_ip | VARCHAR(50) | 最后登录IP |
| created_at | TIMESTAMP | 创建时间 |
| last_login_at | TIMESTAMP | 最后登录时间 |

#### roles - 角色表
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | INT | 角色ID（主键） |
| name | VARCHAR(50) | 角色名称（唯一） |
| description | VARCHAR(255) | 角色描述 |
| created_at | TIMESTAMP | 创建时间 |

#### permissions - 权限表
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | INT | 权限ID（主键） |
| name | VARCHAR(50) | 权限标识（唯一） |
| description | VARCHAR(255) | 权限描述 |
| created_at | TIMESTAMP | 创建时间 |

#### user_operation_logs - 操作日志表
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 日志ID（主键） |
| user_id | BIGINT | 用户ID |
| category | VARCHAR(50) | 操作分类 |
| method_name | VARCHAR(100) | 方法名 |
| operation_desc | VARCHAR(255) | 操作描述 |
| ip_address | VARCHAR(50) | IP地址 |
| status_code | INT | 状态码 |
| execution_time_ms | BIGINT | 执行时间(ms) |
| create_time | TIMESTAMP | 创建时间 |

## 使用说明

### 用户登录
1. 访问系统登录页面
2. 输入用户名和密码
3. 如已绑定Google认证器，需输入验证码
4. 登录成功后跳转到系统主页

### 权限控制

#### 在Controller使用注解控制权限
```java
@RestController
@RequestMapping("/manage/user")
public class UserManageController {
    
    // 需要user:read:all权限
    @SaCheckPermission("user:read:all")
    @GetMapping("/list")
    public SaResult getAllUsers() {
        // ...
    }
    
    // 需要root或admin角色
    @SaCheckRole(value = {"root", "admin"}, mode = SaMode.OR)
    @PostMapping("/delete")
    public SaResult deleteUser() {
        // ...
    }
}
```

#### 操作日志记录
```java
// 添加操作描述
@OperationDesc("用户登录")
@RequestMapping("doLogin")
public SaResult doLogin() {
    // 自动记录操作日志
}

// 排除日志记录
@NotLog
@GetMapping("/list")
public SaResult listData() {
    // 不记录此操作
}
```

### Google认证器绑定

1. 登录系统后进入个人资料页面
2. 点击"绑定谷歌验证器"按钮
3. 使用Google Authenticator App扫描二维码
4. 输入App显示的6位验证码
5. 点击确认完成绑定

### 用户管理

#### 添加用户
1. 进入用户管理页面
2. 点击"添加用户"按钮
3. 填写用户名、邮箱、密码
4. 选择用户角色
5. 点击确认添加

#### 冻结用户
1. 选择要冻结的用户
2. 点击"冻结"按钮
3. 输入冻结天数（0为永久）
4. 输入冻结原因
5. 确认冻结

### 角色权限配置

1. 进入角色管理页面
2. 点击角色的"编辑"按钮
3. 勾选需要分配的权限
4. 点击保存

## 系统配置

### 预设角色和权限

#### 默认角色
| 角色 | 标识 | 说明 |
|------|------|------|
| 超级管理员 | root | 拥有系统所有权限 |
| 管理员 | admin | 用户管理、日志查看等权限 |
| 普通用户 | user | 基础功能权限 |

#### 权限列表
| 权限标识 | 说明 |
|---------|------|
| user:read | 查看用户信息 |
| user:read:all | 查看所有用户 |
| user:insert | 添加用户 |
| user:update | 修改用户 |
| user:delete | 删除用户 |
| role:read | 查看角色 |
| role:insert | 添加角色 |
| role:update | 修改角色 |
| role:delete | 删除角色 |
| permission:read | 查看权限 |
| permission:insert | 添加权限 |
| permission:update | 修改权限 |
| permission:delete | 删除权限 |
| log:query | 查看操作日志 |

### Token配置说明
```yaml
sa-token:
  # token名称
  token-name: satoken
  # token有效期，单位：秒，默认10分钟
  timeout: 600
  # token最低活跃频率，单位：秒
  active-timeout: 300
  # 是否允许同一账号多地同时登录
  is-concurrent: false
  # 在多人登录同一账号时，是否共用token
  is-share: true
  # token风格
  token-style: random-128
```

## 注意事项

### 🔒 安全建议

1. **生产环境配置**
   - 必须修改默认的AES密钥（`AESUtil.SECRET_KEY`）
   - 更换application.yml中的AES密钥配置
   - 启用Redis密码保护
   - 修改默认管理员密码
   - 配置HTTPS证书

2. **密码策略**
   - 密码长度限制：6-20字符
   - 用户名只能包含字母
   - 建议定期更换密码
   - 强制绑定Google认证器

3. **权限管理**
   - 遵循最小权限原则
   - 定期审查用户权限
   - 及时清理离职用户账号
   - 避免过度使用root权限

### ⚡ 性能优化

1. **数据库优化**
   - 操作日志表建议定期归档
   - 为高频查询字段建立索引
   - 定期清理过期的Token记录
   - 使用读写分离提高性能

2. **缓存优化**
   - 合理配置Redis连接池
   - 设置合适的Token超时时间
   - 缓存热点数据
   - 使用Redis集群提高可用性

3. **日志管理**
   - 定期清理日志文件
   - 配置日志级别为WARN或ERROR
   - 使用异步日志提高性能
   - 日志文件按日期分割

### 🚀 部署建议

1. **服务器配置**
```nginx
   # Nginx反向代理配置
   server {
       listen 80;
       server_name your-domain.com;
       
       location / {
           proxy_pass http://127.0.0.1:8081;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       }
   }
```

2. **Docker部署**
```dockerfile
   FROM openjdk:8-jdk-alpine
   VOLUME /tmp
   ADD target/springboot-USDT-TRC20-V3.jar app.jar
   ENTRYPOINT ["java","-jar","/app.jar"]
```

3. **监控告警**
   - 配置系统监控（CPU、内存、磁盘）
   - 设置异常登录告警
   - 监控数据库连接数
   - 定期备份数据

### ⚠️ 已知限制

1. **系统限制**
   - 不支持同一账号多设备同时登录
   - Token超时时间固定为10分钟
   - 用户名只支持英文字母
   - 单次批量操作最多100条记录

2. **功能限制**
   - 暂不支持找回密码功能
   - 不支持第三方登录
   - 权限不支持动态加载
   - 日志不支持导出功能

## 常见问题

### Q1: 忘记管理员密码怎么办？
直接在数据库中更新密码：
```sql
UPDATE users SET password = 'AIyxhTzc7ICpm9yjy2UhLQ==' WHERE username = 'admin';
-- 密码重置为: 123456
```

### Q2: 如何添加新的权限？
1. 在permissions表中插入新权限
2. 通过角色管理分配给相应角色
3. 在代码中使用@SaCheckPermission注解

### Q3: Token过期时间如何调整？
修改application.yml中的sa-token.timeout配置项（单位：秒）

### Q4: 如何禁用Google认证？
暂不支持全局禁用，可以不绑定即可

### Q5: 操作日志占用空间过大怎么办？
建议定期归档或清理超过3个月的日志记录：
```sql
DELETE FROM user_operation_logs WHERE create_time < DATE_SUB(NOW(), INTERVAL 3 MONTH);
```

## 开发指南

### 添加新模块

1. **创建Controller**
```java
@RestController
@RequestMapping("/new-module")
public class NewModuleController {
    @OperationDesc("新模块操作")
    @SaCheckPermission("module:operate")
    @PostMapping("/operate")
    public SaResult operate() {
        // 业务逻辑
        return SaResult.ok();
    }
}
```

2. **添加权限**
```sql
INSERT INTO permissions (name, description) 
VALUES ('module:operate', '新模块操作权限');
```

3. **分配权限**
通过角色管理界面将新权限分配给相应角色

### 自定义异常处理
```java
@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public SaResult handleCustomException(CustomException e) {
        return SaResult.error(e.getMessage());
    }
}
```

## 版本更新

### v1.0.0 (2024-01)
- 初始版本发布
- 基础用户管理功能
- RBAC权限系统
- 操作日志记录
- Google认证集成

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 许可证

本项目仅供学习参考使用，未经授权不得用于商业用途。

## 联系方式

- 项目Issues：[GitHub Issues](https://github.com/your-repo/issues)
- 邮箱：admin@example.com

---

**声明：** 本系统为演示项目，生产环境使用前请进行充分的安全审查和性能测试。
