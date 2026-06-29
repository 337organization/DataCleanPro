# DataCleanPro - 自动化数据处理与清洗系统

## 项目简介

DataCleanPro 是一个基于 Java 17 的桌面应用程序，提供 Excel/CSV 数据的导入、清洗、验证、查询、报表导出和历史审计等完整数据处理流程。

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+（也可运行 `start.bat` 自动下载）

### 安装运行

```bash
# 1. 配置数据库（可选，默认自动降级为 H2 本地存储）
#    编辑 src/main/resources/application.properties 设置 MySQL 连接
#    MySQL 不可用时系统自动使用 H2 嵌入式数据库，无需任何配置

# 2. 编译项目
mvn clean compile

# 3. 运行项目
mvn exec:java -Dexec.mainClass="com.datacleanpro.App"

# 4. 或直接运行启动脚本（自动检测环境）
start.bat
```

### 数据库配置

```properties
db.url=jdbc:mysql://localhost:3306/datacleanpro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
db.username=root
db.password=your_password
db.driver=com.mysql.cj.jdbc.Driver
```

> **提示**：MySQL 不可用时系统自动切换 H2 嵌入式数据库（`storage/local/`），所有功能正常使用。

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Java 17 |
| 构建 | Maven |
| 数据库 | MySQL 8.0+ / H2（回退） |
| GUI | Swing |
| Excel | Apache POI 5.2.5 |
| CSV | OpenCSV 5.9 |
| JSON | Jackson 2.16.1 |
| 日志 | SLF4J + Logback |

## 功能特性

### 核心功能

- **文件导入**：支持 `.xlsx` / `.xls` / `.csv`，自动识别文件格式
- **数据清洗**：去重、空值填充、格式标准化（去空格/大小写/特殊字符）
- **数据验证**：手机号、邮箱、必填字段、数字范围、正则表达式规则
- **数据查询**：按文件筛选、关键字搜索、分页展示
- **报表导出**：导出数据或历史记录为 Excel 文件，保存至 `storage/reports/`
- **历史审计**：所有操作自动记录，支持查看、清空、导出

### 技术特性

- **MVC 架构**：`model` / `view` / `controller` / `service` / `dao` 分层清晰
- **异步处理**：`SwingWorker` + `ExecutorService` 线程池，GUI 不阻塞
- **网络通信**：Socket 服务器/客户端，Jackson JSON 序列化
- **连接池**：自定义 `BlockingQueue` 连接池，支持事务管理
- **异常体系**：5 种自定义异常，覆盖文件、数据库、网络、验证等场景
- **日志审计**：SLF4J + Logback，控制台/文件/审计日志三级输出

## 项目结构

```text
src/main/java/com/datacleanpro/
├── App.java                    # 入口：初始化DB、启动Server、加载GUI
├── model/                      # 数据模型（8个POJO）
├── parser/                     # 文件解析（抽象类 + Excel/CSV实现 + 工厂）
├── cleaner/                    # 数据清洗（接口 + 去重/空值/格式 + 管道）
├── validator/                  # 数据验证（接口 + 5种规则 + 引擎）
├── service/                    # 业务逻辑（5个Service）
├── dao/                        # 数据访问（连接池 + 6个DAO）
├── controller/                 # 控制器
├── view/                       # Swing视图（主窗口 + 6面板 + 状态栏）
├── network/                    # 网络层（Server/Client/协议/任务管理）
├── exception/                  # 异常（5种层次化异常）
└── util/                       # 工具类（配置/文件/字符串/日期/日志）
```

## 数据库设计

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `data_file` | 导入的文件元数据 | id, file_name, file_type, row_count, status |
| `data_row` | 数据行（JSON存储） | id, file_id, row_index, row_data, is_deleted |
| `clean_task` | 清洗任务记录 | id, file_id, task_type, status, rows_affected |
| `validation_rule` | 验证规则定义 | id, rule_type, target_column, expression, is_active |
| `validation_result` | 验证结果明细 | id, file_id, rule_id, row_index, cell_value, is_passed |
| `task_history` | 操作审计日志 | id, action, target, status, execution_time |

### 默认验证规则（系统初始化时自动插入）

| 规则名 | 类型 | 目标列 | 说明 |
|--------|------|--------|------|
| 手机号格式 | PHONE | phone | `^1[3-9]\d{9}$` |
| 邮箱格式 | EMAIL | email | 标准邮箱正则 |
| 必填字段 | REQUIRED | - | 非空校验 |
| 年龄范围 | NUMBER_RANGE | age | 0-150 |

## 测试数据

位于 `src/main/resources/test/`，覆盖各类场景：

| 文件 | 测试点 |
|------|--------|
| `employees.csv` | 15行标准员工数据，基础导入 |
| `customers.xlsx` | Excel格式导入兼容性 |
| `dirty_data.csv` | 前后空格、大小写不统一 |
| `empty_fields.csv` | 空值/缺失字段处理 |
| `duplicate_data.csv` | 重复行（21行含6组重复） |
| `invalid_phone.csv` | 手机号验证规则测试 |
| `invalid_email.csv` | 邮箱验证规则测试 |
| `missing_required.csv` | 必填字段缺失测试 |
| `large_data.csv` | 1000行数据，性能测试 |

## OOP 设计亮点

| 特性 | 实现 | 说明 |
|------|------|------|
| **继承** | `FileParser` → `ExcelParser` / `CsvParser` | 抽象类定义解析骨架 |
| **接口** | `DataCleaner` / `ValidateRule` | 定义清洗/验证契约 |
| **多态** | `ParserFactory` / `CleanPipeline` | 工厂模式 + 管道模式 |
| **异常体系** | `DataCleanException` → 4个子类 | 层次化异常处理 |
| **异步** | `SwingWorker` + `ExecutorService` | GUI异步不阻塞 |
| **网络** | Socket + Jackson JSON | C/S通信模式 |

## 测试指南

```bash
# 编译
mvn compile

# 打包
mvn package -DskipTests

# 运行 JAR
java -jar target/DataCleanPro-1.0-SNAPSHOT.jar

# 启动脚本（推荐，自动检测环境）
start.bat
```

启动脚本 `start.bat` 支持：

```bash
start.bat              # 检测环境 → 编译 → 运行
start.bat --stop       # 停止 DataCleanPro 进程
start.bat --uninstall  # 清除 Maven 缓存和 target
start.bat --version    # 显示版本
```

## 常见问题

| 问题 | 解决方法 |
|------|----------|
| 数据库连接失败 | 检查 MySQL 服务；系统会自动降级 H2 本地存储 |
| 文件导入失败 | 仅支持 `.xlsx` / `.xls` / `.csv`，确认 UTF-8 编码 |
| 导出报表失败 | 检查 `storage/reports/` 目录权限 |
| 查看详细日志 | `storage/logs/datacleanpro.log` 或 `storage/logs/audit.log` |
