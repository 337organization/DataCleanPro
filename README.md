# DataCleanPro - 自动化数据处理与清洗系统

## 项目简介

DataCleanPro 是一个基于 Java 的自动化数据处理与清洗系统，用于处理 Excel、CSV 格式的数据文件。系统提供数据导入、清洗、验证、查询、统计和导出等功能。

## 功能特性

### 核心功能
- **文件导入**：支持 Excel (.xlsx, .xls) 和 CSV 文件导入
- **数据清洗**：自动去重、空值处理、格式标准化
- **数据验证**：支持自定义验证规则（手机号、邮箱、必填字段等）
- **数据查询**：支持数据搜索、筛选和分页显示
- **报表导出**：支持导出清洗后的数据到 Excel
- **历史记录**：记录所有操作历史，支持审计追踪

### 技术特性
- **MVC 架构**：清晰的分层架构设计
- **多线程处理**：使用 ExecutorService 实现异步任务
- **网络编程**：支持 Socket 客户端/服务器模式
- **连接池管理**：自定义数据库连接池
- **日志系统**：完整的日志记录和审计功能

## 技术栈

- **Java 17**
- **Maven**：项目构建和管理
- **MySQL 8.0**：数据存储
- **Swing**：图形用户界面
- **Apache POI**：Excel 文件处理
- **OpenCSV**：CSV 文件处理
- **Jackson**：JSON 处理
- **SLF4J + Logback**：日志框架

## 项目结构

```
src/main/java/com/datacleanpro/
├── App.java                    # 应用程序入口
├── model/                      # 数据模型层
│   ├── DataRow.java
│   ├── DataFile.java
│   ├── CleanTask.java
│   ├── ValidationRule.java
│   ├── ValidationResult.java
│   ├── QueryCondition.java
│   ├── StatResult.java
│   └── TaskHistory.java
├── parser/                     # 文件解析层
│   ├── FileParser.java         # 抽象类
│   ├── ExcelParser.java        # Excel 解析器
│   ├── CsvParser.java          # CSV 解析器
│   └── ParserFactory.java      # 解析器工厂
├── cleaner/                    # 数据清洗层
│   ├── DataCleaner.java        # 接口
│   ├── DuplicateCleaner.java   # 去重清洗器
│   ├── EmptyValueCleaner.java  # 空值清洗器
│   ├── FormatCleaner.java      # 格式清洗器
│   └── CleanPipeline.java      # 清洗管道
├── validator/                  # 数据验证层
│   ├── ValidateRule.java       # 接口
│   ├── PhoneRule.java          # 手机号验证
│   ├── EmailRule.java          # 邮箱验证
│   ├── RequiredRule.java       # 必填验证
│   ├── NumberRangeRule.java    # 数字范围验证
│   ├── RegexRule.java          # 正则验证
│   └── ValidationEngine.java   # 验证引擎
├── service/                    # 业务逻辑层
│   ├── DataImportService.java
│   ├── ValidationService.java
│   ├── DatabaseService.java
│   ├── ReportService.java
│   └── TaskHistoryService.java
├── dao/                        # 数据访问层
│   ├── DBConnection.java       # 连接池管理
│   ├── DataFileDAO.java
│   ├── DataRowDAO.java
│   ├── CleanTaskDAO.java
│   ├── ValidationRuleDAO.java
│   ├── ValidationResultDAO.java
│   └── TaskHistoryDAO.java
├── controller/                 # 控制器层
│   └── ImportController.java
├── view/                       # 视图层
│   ├── MainFrame.java
│   ├── ImportPanel.java
│   ├── CleanPanel.java
│   ├── ValidationPanel.java
│   ├── QueryPanel.java
│   ├── ReportPanel.java
│   ├── HistoryPanel.java
│   └── StatusBar.java
├── network/                    # 网络层
│   ├── Server.java
│   ├── Client.java
│   ├── TaskRequest.java
│   ├── TaskResponse.java
│   ├── ProtocolUtil.java
│   └── TaskManager.java
├── exception/                  # 异常处理
│   ├── DataCleanException.java
│   ├── FileFormatException.java
│   ├── DataValidateException.java
│   ├── DatabaseImportException.java
│   └── NetworkException.java
├── util/                       # 工具类
│   ├── LogUtil.java
│   ├── ConfigUtil.java
│   ├── FileUtil.java
│   ├── DateUtil.java
│   └── StringUtil.java
└── config/                     # 配置类
```

## 数据库设计

### 主要表结构

1. **data_file** - 数据文件表
2. **data_row** - 数据行表
3. **clean_task** - 清洗任务表
4. **validation_rule** - 验证规则表
5. **validation_result** - 验证结果表
6. **task_history** - 任务历史表

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd DataCleanPro
   ```

2. **配置数据库**
   - 创建 MySQL 数据库
   - 执行 `src/main/resources/db/schema.sql` 创建表结构
   - 修改 `src/main/resources/application.properties` 中的数据库配置

3. **编译项目**
   ```bash
   mvn clean compile
   ```

4. **运行项目**
   ```bash
   mvn exec:java -Dexec.mainClass="com.datacleanpro.App"
   ```

### 数据库配置

在 `application.properties` 中配置数据库连接：

```properties
db.url=jdbc:mysql://localhost:3306/datacleanpro?useSSL=false&serverTimezone=Asia/Shanghai
db.username=root
db.password=root
db.driver=com.mysql.cj.jdbc.Driver
```

## 测试文件

项目提供了多种测试文件，位于 `src/main/resources/test/` 目录：

| 文件名 | 类型 | 说明 |
|--------|------|------|
| employees.csv | CSV | 员工数据，测试基本导入 |
| customers.xlsx | Excel | 客户数据，测试Excel导入 |
| products.csv | CSV | 产品数据，测试不同列结构 |
| dirty_data.csv | CSV | 脏数据，测试格式清洗 |
| empty_fields.csv | CSV | 空值数据，测试空值处理 |
| duplicate_data.csv | CSV | 重复数据，测试去重功能 |
| invalid_phone.csv | CSV | 错误手机号，测试验证 |
| invalid_email.csv | CSV | 错误邮箱，测试验证 |
| missing_required.csv | CSV | 缺少必填字段，测试验证 |
| large_data.csv | CSV | 1000行数据，测试性能 |

## 课程设计文档

项目包含完整的课程设计文档：

| 文档 | 说明 |
|------|------|
| 课程设计报告.docx | 完整的课程设计报告 |
| 组员任务分工说明.docx | 4人团队任务分工 |
| 组员报告-张三.docx | 项目组长/后端开发报告 |
| 组员报告-李四.docx | 后端开发/数据库设计报告 |
| 组员报告-王五.docx | 前端开发/GUI设计报告 |
| 组员报告-赵六.docx | 测试/文档编写报告 |

## 使用说明

### 1. 数据导入
- 点击"选择文件"按钮选择 Excel 或 CSV 文件
- 点击"导入数据"按钮开始导入
- 系统自动解析文件并显示数据预览

### 2. 数据清洗
- 选择需要清洗的数据文件
- 选择清洗类型（去重、空值处理、格式化）
- 点击执行清洗操作

### 3. 数据验证
- 配置验证规则（手机号、邮箱、必填字段等）
- 选择需要验证的数据文件
- 执行验证并查看结果

### 4. 数据查询
- 使用搜索框搜索数据
- 支持按文件、列、关键字筛选
- 支持分页显示

### 5. 报表导出
- 选择需要导出的数据
- 点击导出按钮生成 Excel 报表
- 报表保存在 `storage/reports/` 目录

## 项目亮点

1. **完整的 OOP 设计**
   - 继承：FileParser 抽象类和子类
   - 接口：DataCleaner、ValidateRule 接口
   - 多态：解析器工厂模式

2. **异步任务处理**
   - 使用 ExecutorService 线程池
   - SwingWorker 实现 GUI 异步操作
   - 进度条显示任务进度

3. **网络编程**
   - Socket 客户端/服务器模式
   - JSON 序列化通信
   - 支持远程任务处理

4. **连接池管理**
   - 自定义数据库连接池
   - 连接复用和回收
   - 事务管理支持

5. **完整的日志系统**
   - SLF4J + Logback 日志框架
   - 审计日志记录
   - 日志文件滚动和归档

## 答辩说明

### 技术要点

1. **Java 类继承**
   - FileParser 抽象类
   - ExcelParser 和 CsvParser 子类

2. **Java 接口**
   - DataCleaner 接口及其实现类
   - ValidateRule 接口及其实现类

3. **Java 多态**
   - ParserFactory 工厂模式
   - CleanPipeline 管道模式

4. **Java 图形界面**
   - Swing 组件
   - 卡片布局
   - JTable 数据展示

5. **Java 多线程**
   - ExecutorService 线程池
   - SwingWorker 异步任务
   - 后台任务处理

6. **Java 网络编程**
   - Socket 通信
   - 客户端/服务器模式
   - JSON 数据交换

7. **文件永久化存储**
   - 上传文件存储
   - 导出报表存储
   - 日志文件存储

8. **自定义异常处理**
   - FileFormatException
   - DataValidateException
   - DatabaseImportException
   - NetworkException

### 演示流程

1. **启动应用程序**
2. **导入示例数据文件**
3. **执行数据清洗**
4. **配置验证规则并验证**
5. **查询和筛选数据**
6. **导出报表**
7. **查看历史记录**
8. **网络功能演示**

## 常见问题

### 1. 数据库连接失败
- 检查 MySQL 服务是否启动
- 验证数据库配置是否正确
- 确认数据库用户权限

### 2. 文件导入失败
- 检查文件格式是否支持
- 确认文件编码是否正确
- 查看日志文件获取详细错误信息

### 3. 导出报表失败
- 检查存储目录权限
- 确认磁盘空间是否充足
- 查看日志文件获取详细错误信息

## 开发团队

DataCleanPro 开发团队

## 许可证

本项目仅供学习和研究使用。
