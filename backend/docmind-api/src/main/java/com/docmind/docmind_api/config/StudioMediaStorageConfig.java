package com.docmind.docmind_api.config;

import com.docmind.docmind_api.studio.service.FileSystemStudioMediaStorage;
import com.docmind.docmind_api.studio.service.R2StudioMediaStorage;
import com.docmind.docmind_api.studio.service.StudioMediaStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class StudioMediaStorageConfig {

    @Bean
    @ConditionalOnProperty(
            name = "docmind.studio.storage-provider",
            havingValue = "filesystem",
            matchIfMissing = true
    )
    public StudioMediaStorage fileSystemStudioMediaStorage(
            @Value("${docmind.studio.audio-storage-dir}") String audioStorageDir,
            @Value("${docmind.studio.image-storage-dir}") String imageStorageDir
    ) {

        return new FileSystemStudioMediaStorage(
                audioStorageDir,
                imageStorageDir
        );
    }

    @Bean
    @ConditionalOnProperty(
            name = "docmind.studio.storage-provider",
            havingValue = "r2"
    )
    public StudioMediaStorage r2StudioMediaStorage(
            S3Client r2S3Client,
            @Value("${docmind.r2.bucket}") String bucket
    ) {

        return new R2StudioMediaStorage(
                r2S3Client,
                bucket
        );
    }

    @Bean
    @ConditionalOnProperty(
            name = "docmind.studio.storage-provider",
            havingValue = "r2"
    )
    public S3Client r2S3Client(
            @Value("${docmind.r2.endpoint}") String endpoint,
            @Value("${docmind.r2.access-key-id}") String accessKeyId,
            @Value("${docmind.r2.secret-access-key}") String secretAccessKey
    ) {

        return S3Client.builder()
                .endpointOverride(
                        URI.create(endpoint.trim())
                )
                .region(
                        Region.of("auto")
                )
                .httpClient(
                        UrlConnectionHttpClient.create()
                )
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        accessKeyId.trim(),
                                        secretAccessKey.trim()
                                )
                        )
                )
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build()
                )
                .build();
    }
}
