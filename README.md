
#Mybatis相关

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

		@AutoConfigureAfter：在加载其value配置的类之后再加载当前类
			确定了线程池管理对象PooledDataSourceConfiguration，在一个list中存放，for循环，Class对象加载类，获取到就返回，第一个是HikariDataSource，所以springboot2.0以上版本，会默认使用HikariDataSource作为数据库连接池管理对象
			"com.zaxxer.hikari.HikariDataSource",
			"org.apache.tomcat.jdbc.pool.DataSource", 
			"org.apache.commons.dbcp2.BasicDataSource"

		sqlSessionFactory 通过DataSource生成factory，factory中设置了configuration配置的所有属性，包括plugins、typeHandlers等
		sqlSessionTemplate 在包 mybatis-spring中，是spring封装的sqlSession对象，可以调用查询等方法

		MapperScannerRegistrarNotFoundConfiguration静态类引入了AutoConfiguredMapperScannerRegistrar
		该类实现了InitializingBean，注入了MapperScanner对象，该对象会扫描指定包路径下的Mapper文件

