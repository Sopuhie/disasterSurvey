## 1.项目简介

**disaster-survey** 是一款数据共享平台运维系统的软件。本项目基于JDK1.8，采用Java语言，springboot框架进行开发实现，数据库操作框架基于JDBC封装的自有依赖包实现，若需要开源框架，可自行引入。


## 2.代码架构
``` 
disaster-survey  
    ├── disaster-survey-admin                     // 启动模块
    ├── disaster-survey-core                      // 共用核心模块
    ├── disaster-survey-dependencies              // 项目版本依赖模块
    ├── disaster-survey-framework                 // 功能模块
    │       └── disaster-survey-exception         // 自定义异常模块
    │       └── disaster-survey-global            // 全局变量及响应包装类
    │       └── disaster-survey-utils             // 工具类模块
    ├── disaster-survey-module                    // 个人业务相关
    │       └── disaster-survey-system-biz        // 系统管理业务模块
    │       └── disaster-survey-common-biz        // 通用公共业务模块
    │       └── disaster-survey-authentication-biz// 认证权限业务模块    
    │       └── disaster-survey-resource-biz      // 资源管理业务模块    
    │       └── disaster-survey-gis-biz           // GIS相关业务模块          
    ├── disaster-survey-console                   // 算法输出模块
    │       └── disaster-survey-model-console     // 大数据量算法分析（如质检）工具包（预留）
```

## 3.启动步骤

### 3.1 后台JAVA代码运行步骤

**环境配置**

**运行环境**：JDK1.8

**开发工具：** Eclipse、Maven

**默认已启动中间件：**[Redis](https://redis.io/))


<table>
    <tr>
        <td>第一步：下载disaster-survey项目并导入Eclipse IDE中</td>
    </tr>
    <tr>
        <td>第二步：导入数据库文件到达梦（Dm）数据库中，并修改项目配置文件中数据库、redis以及文件根目录等配置信息</td>
    </tr>
    <tr>
        <td>第三步：刷新Maven,再进行Complie编译,启动项目</td>
    </tr>
</table>
  
### 3.2 自有数据库操作组件的使用

**组件引入**：引入依赖包santech-dbcomponent-0.0.1.jar
**数据库配置**：该数据库框架目前支持最多三个数据源的接入，如下为引入两个数据源的配置方式：

``` 
databse:
  dbconfig:
    providers:
      db1:
        dbtype: PostgreSQL #支持Dm(达梦),Mysql,Oracle,PostgreSQL,Highgo,Kingbase,Sqlite等数据库类型
        url: jdbc:postgresql://localhost:5432/db_ywk_updcyzt
        username: postgres
        password: xxxx
        datasource_type: com.alibaba.druid.pool.DruidDataSource
      db2:
        dbtype: Dm
        url: jdbc:dm://localhost:5236?schema="FISHERY"
        username: FISHERY
        password: xxxx
        datasource_type: com.alibaba.druid.pool.DruidDataSource
    debug: true
    default_db: db1
```
**数据库操作代码示例**：
**数据源注入**：

```
	@Autowired
    @Qualifier("db1")
    DatabaseHandler basicDatabase;

	@Autowired
    @Qualifier("db2")
	DatabaseHandler fisheryDatabase;
```

**增删改查示例**：

```
可参考com.santech.core.framework.dao.BaseDao.class及相关子类的使用
```

