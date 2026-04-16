package com.tixy.core.config;

import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JooqConfig {

    @Bean
    public DefaultDSLContext dslContext(DataSource dataSource) {
        // JOOQ 에서 tixy.EVENTS 라고 저장되던걸
        // events 로 조회할 수 있도록 설정
        Settings settings = new Settings()
                .withRenderSchema(false)
                .withRenderNameCase(RenderNameCase.LOWER);

        return (DefaultDSLContext) DSL.using(dataSource, SQLDialect.MYSQL, settings);
    }
}