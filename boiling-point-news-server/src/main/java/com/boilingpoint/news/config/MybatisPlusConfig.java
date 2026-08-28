package com.boilingpoint.news.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.boilingpoint.news.mapper")
public class MybatisPlusConfig {
}
