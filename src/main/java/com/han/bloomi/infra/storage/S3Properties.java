package com.han.bloomi.infra.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * S3 설정 프로퍼티
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bloomi.s3")
public class S3Properties {
    private String bucket;
    private String region = "ap-northeast-2";
    private String accessKey;
    private String secretKey;
    private String baseUrl;  // CloudFront URL 또는 S3 public URL
}