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
@MapperScan(value = "com.qing.mybatisdemo.mapper.qingwan", sqlSessionTemplateRef="wanSqlSessionTemplate")
public class DataSourceWanConfig {

//    @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.qingwan")
    public DataSource wanDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean
    public SqlSessionFactory wanSqlSessionFactory(DataSource wanDataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(wanDataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate wanSqlSessionTemplate(SqlSessionFactory wanSqlSessionFactory){
        return new SqlSessionTemplate(wanSqlSessionFactory);
    }

}
