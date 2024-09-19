package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里OSS配置类，配置参数
 */
@Slf4j
@Configuration
public class OSSConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUti(AliOssProperties aliOssProperties) {
        log.info("开始创建阿里OSS文件上传对象{}", aliOssProperties);
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    };
}
