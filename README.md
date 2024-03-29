
# Mybatis相关

QA：
- 1.为什么引入mybatis-spring-boot-starter之后还要引入mysql-connector-java？为什么starter不直接引入该包呢？

  mybatis本身可以链接很多数据库，比如mysql、sqlserver、oracle、clickhouse等，但是链接不同的数据库需要不同的驱动Driver，即不同的数据库厂商需要实现自己的驱动包
  所以mybatis-spring-boot-starter中并没有指定驱动包，而是根据需要看要连接的数据库为什么，再引入对应的驱动包
- 2.druid作用是什么？Hikari又是啥

  都是数据库连接池，用于管理和数据库建立的链接，减少频繁创建销毁链接的成本

  springboot2.0之后默认的连接池是 Hikari，是该包引入的
  mybatis-spring-boot-starter
  spring-boot-starter-jdbc
- 3.一级缓存、二级缓存

  一级缓存：是sqlsession级别的缓存，原理是根据sqlid和参数生成对应的key，value放查询结果，下次查询会先检查该map中是否有数据，有直接返回；
  增删改等操作会清空缓存；
  一级缓存默认是开启的
  二级缓存：是namespace级别的；默认关闭，不推荐开启
  多个sqlsession可以共享该缓存，如果一个表的操作，不只存在单一的mapper中，会导致该表的数据在两个mapper中查询的结果是不一致的
- 4.引入mybatis-spring-boot-starter，不配置datasource，启动会报错 `Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.`

  因为spring-boot-autoconfigurition中自动注入了类DataSourceAutoConfiguration，其启动依赖DataSource类
  当没有引入starter时，DataSource只是一个接口，没有实现类，故DataSourceAutoConfiguration不会加载
  引入starter后，引入了DataSource的实现类，该配置类将会加载，其依赖配置类DataSourceProperties，需要指定url，name等属性，所以报错

## springboot整合mabtis（mysql）

- 1.引入 `mybatis-spring-boot-starter` 包
- 2.引入mysql驱动包 `mysql-connector-java`
- 3.springboot 与 MyBatis-Spring-Boot-Starter 版本对应关系
  [http://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/](http://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
tips：
  - MyBatis-Spring-Boot-Starter 2.1.4版本有问题，启动会报错，本项目选择2.1.3版本


## mybatis-spring-boot-starter源码解读

自动配置类
MybatisAutoConfiguration(在包mybatis-spring-boot-autoconfigure中)
> 在spring.factories文件中指定了该类，spring启动即会加载

> @AutoConfigureAfter：在加载其value配置的类之后再加载当前类
> 	确定了线程池管理对象PooledDataSourceConfiguration，在一个list中存放，for循环，Class对象加载类，获取到就返回，第一个是HikariDataSource，所以springboot2.0以上版本，会默认使用HikariDataSource作为数据库连接池管理对象
> 	"com.zaxxer.hikari.HikariDataSource",
> 	"org.apache.tomcat.jdbc.pool.DataSource", 
> 	"org.apache.commons.dbcp2.BasicDataSource"
>
> sqlSessionFactory 通过DataSource生成factory，factory中设置了configuration配置的所有属性，包括plugins、typeHandlers等
> sqlSessionTemplate 在包 mybatis-spring中，是spring封装的sqlSession对象，可以调用查询等方法
>
> MapperScannerRegistrarNotFoundConfiguration静态类引入了AutoConfiguredMapperScannerRegistrar
> 该类实现了InitializingBean，注入了MapperScanner对象，该对象会扫描指定包路径下的Mapper文件


## 源码解读


### 一、@MapperScan，扫描mybatis要用到的类，解析为BeanDefinition，最终加载到spring容器中

- 1.spring启动`AbstractApplicationContext` refresh()方法中`invokeBeanFactoryPostProcessors`中会执行`BeanDefinitionRegistryPostProcessor`的`postProcessBeanDefinitionRegistry`方法
- 2.其中优先执行实现了`PriorityOrdered`接口的`ConfigurationClassPostProcessor`类，该类中加载配置类，full、lite配置
- 3.对于其中的`@ComponentScan`注解，会进行包扫描，`@Import`相关的注解也会注入对应的BeanDefinition
- 4.`MapperScan`注解Import了`MapperScannerRegistrar`，该类注入了`MapperScannerConfigurer`
- 5.`MapperScannerConfigurer`类实现了`BeanDefinitionRegistryPostProcessor`，会执行`postProcessBeanDefinitionRegistry`方法，该方法会扫描mybatis需要用到的类
- 6.`MapperScannerConfigurer`没有实现任何优先级的接口，故执行顺序要次于`ConfigurationClassPostProcessor`
- 7.`ConfigurationClassPostProcessor`中会进行包扫描，`MapperScannerConfigurer`中也会进行包扫描，优于先后顺序，spring的包扫描要优于mybatis的执行


### 二、拦截器Interceptor

- 1.`MybatisAutoConfiguration`配置类，启动会注入`Interceptor`类型的数组，将spring中所有的拦截器加载进来，并设置为`SqlSessionFactoryBean`的属性
- 2.在`SqlSessionFactoryBean`的`buildSqlSessionFactory`方法中设置为`Configuration`的拦截器属性，添加到`interceptorChain`拦截器调用链中
- 3.在`Configuration`的`newExecutor`等方法，创建各个模块（Executor、ParameterHandler、ResultSetHandler、StatementHandler）的最后一步，执行`pluginAll`方法
- 4.在`Plugin`的`wrap`方法中解析`Intercepts`注解的属性，根据type生成其对应的代理对象
- 5.`Plugin`的`invoke`方法（代理对象最终要执行的方法），会判断当前方法是否是需要拦截的方法，如果是，直接调用`Interceptor`的`intercept`方法，执行自定义逻辑，如果不是，通过反射调用对应方法



> Interceptor接口的方法
>   
>   修改方法的执行逻辑
> 
>   Object intercept(Invocation invocation) throws Throwable;
> 
> 
>  根据要拦截的信息，生成代理对象
> 
>   default Object plugin(Object target) {
>     return Plugin.wrap(target, this);
>   }


> MyBatis 允许你在映射语句执行过程中的某一点进行拦截调用。默认情况下，MyBatis 允许使用插件来拦截的方法调用包括：
>
> Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
> 
> ParameterHandler (getParameterObject, setParameters)
> 
> ResultSetHandler (handleResultSets, handleOutputParameters)
> 
> StatementHandler (prepare, parameterize, batch, update, query)


> Executor：代表执行器，由它调度StatementHandler、ParameterHandler、ResultSetHandler等来执行对应的SQL，其中StatementHandler是最重要的。executor最先执行
> 
> StatementHandler：作用是使用数据库的Statement（PreparedStatement）执行操作，它是四大对象的核心，起到承上启下的作用，许多重要的插件都是通过拦截它来实现的。
> 
> ParameterHandler：是用来处理SQL参数的。
> 
> ResultSetHandler：是进行数据集（ResultSet）的封装返回处理的。
> 
> 各模块执行顺序 Executor StatementHandler ParameterHandler ResultSetHandler


### 三、多数据源配置（核心：根据不同的数据库配置，加载不同的`DataSource`对象，通过`DataSource`创建`SqlSessionFactory`，通过`SqlSessionFactory`创建`SqlSessionTemplate`）

- 1.`MybatisAutoConfiguration`中`@ConditionalOnSingleCandidate(DataSource.class)`表示当只有一个`DataSource`时加载该类，即创建默认的`SqlSessionFactory`和`sqlSessionTemplate`
- 2.当引入多数据源时，`MybatisAutoConfiguration`类不再加载，走自定义的配置类
- 3.`MapperScan`注解指定数据源要扫描的路径，当有多个数据源时，**通过`sqlSessionTemplateRef`指定template的别名**
- 4.通过`@ConfigurationProperties`注解指定`DataSource`的配置值，注入对应的`DataSource`
- 5.通过`DataSource`创建`SqlSessionFactory`，通过`SqlSessionFactory`创建`sqlSessionTemplate`

> 多数据源配置类：
> 
> DataSourceWanConfig
> 
> DataSourceYunConfig


![img.png](img.png)

### 四、串联所有流程

- 1.入口`MapperScan`，`@Import(MapperScannerRegistrar.class)`
- 2.`MapperScannerRegistrar`实现了`ImportBeanDefinitionRegistrar`，register BeanDefinition `MapperScannerConfigurer`
- 3.`MapperScannerConfigurer`实现了`BeanDefinitionRegistryPostProcessor`，执行其`postProcessBeanDefinitionRegistry`方法注入bean `ClassPathMapperScanner`
- 4.`ClassPathMapperScanner`对象为包扫描，根据配置的路径扫描mapper对应的接口
- 5.其扫描逻辑重写了spring的doScan方法，修改扫描到mapper的BeanDefinition信息，修改BeanClass为`MapperFactoryBean`，自动注入方式为ByType，即spring启动会注入`MapperFactoryBean`
- 6.`MapperFactoryBean`继承`DaoSupport`，其实现了`InitializingBean`接口，会执行`afterPropertiesSet`，最终执行`checkDaoConfig`方法，将mapper接口信息设置到`configuration`对象中
- 7.`MapperFactoryBean`同时实现了`FactoryBean`接口，在spring容器启动过程中会调用其`getObject`方法注入对象
- 8.跟踪`getObject`方法会发现，其最终是根据mapper接口创建了`MapperProxy`代理对象，交由spring管理，当调用mapper接口对应的方法时，会执行`MapperProxy`的`invoke`方法

====================自此启动完成，以下执行业务逻辑时触发====================

- 9.业务侧通过注入mapper接口，进行方法调用时，会执行`MapperProxy`的`invoke`方法，找到接口对应的代理类，真正执行业务方法
- 10.继续执行，会走到`PlainMethodInvoker`的`invoke`方法，`MapperMethod`的`executor`方法会根据当前操作的type，来走增、删、改、查不同的逻辑
- 11.比如执行`selectList`操作，会走到`SqlSessionTemplate`的`selectList`方法
- 12.`SqlSessionTemplate`的`sqlSessionProxy`，在其构造方法中会发现其也是通过代理实现
- 13.真正获取`sqlSession`会执行`SqlSessionInterceptor`的`invoke`方法
- 14.其中`getSqlSession`是真正获取`sqlSession`对象，其会先从当前ThreadLocal中拿，拿不到再创建
- 15.其中`openSessionFromDataSource`获取`sqlSession`对象会创建`Executor`对象
- 16.`newExecutor`方法最后一步会执行`interceptorChain.pluginAll(executor);`，即对`executor`对象进行拦截处理，执行拦截器`Interceptor`逻辑
- 17.拿着`executor`执行`selectList`,到`SimpleExecutor`的`doQuery`方法，`configuration.newStatementHandler`创建`StatementHandler`对象，同样在`newStatementHandler`的最后一步也会执行拦截器逻辑
- 18.其中创建`StatementHandler`的过程中，会创建`RoutingStatementHandler`，在其父构造方法中，会创建`ParameterHandler`、`ResultSetHandler`，同样在创建的最后一步也会执行拦截器逻辑
- 19.所以拦截器的执行顺序为`Executor>StatementHandler>ParameterHandler>ResultSetHandler`
- 20.最终会用生成的四大对象，调用jdbc，预处理sql，执行sql，封装返回结果，执行完毕



### 五、自动装配类 MybatisAutoConfiguration

