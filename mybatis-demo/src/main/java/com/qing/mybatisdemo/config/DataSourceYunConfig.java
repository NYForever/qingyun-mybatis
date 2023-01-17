package com.qing.mybatisdemo.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * @author wenan.ren
 * @date 2023/1/17 10:27
 * @Description
 */
@Configuration
@MapperScan(value = "com.qing.mybatisdemo.mapper.qingyun", sqlSessionTemplateRef="yunSqlSessionTemplate")
public class DataSourceYunConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.qingyun")
    public DataSource yunDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean
    public SqlSessionFactory yunSqlSessionFactory(DataSource yunDataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(yunDataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate yunSqlSessionTemplate(SqlSessionFactory yunSqlSessionFactory){
        return new SqlSessionTemplate(yunSqlSessionFactory);
    }

}
