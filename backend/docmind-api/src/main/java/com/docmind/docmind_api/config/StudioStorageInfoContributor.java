package com.docmind.docmind_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class StudioStorageInfoContributor implements InfoContributor {

    private final String provider;
    private final String bucket;
    private final String endpoint;

    public StudioStorageInfoContributor(
            @Value("${docmind.studio.storage-provider}") String provider,
            @Value("${docmind.r2.bucket:}") String bucket,
            @Value("${docmind.r2.endpoint:}") String endpoint
    ) {

        this.provider = provider;
        this.bucket = bucket;
        this.endpoint = endpoint;
    }

    @Override
    public void contribute(
            Info.Builder builder
    ) {

        builder.withDetail(
                "studioStorage",
                StudioStorageInfo.of(
                        provider,
                        hasText(bucket),
                        hasText(endpoint)
                )
        );
    }

    private boolean hasText(
            String value
    ) {

        return value != null && !value.isBlank();
    }

    private record StudioStorageInfo(
            String provider,
            boolean r2BucketConfigured,
            boolean r2EndpointConfigured
    ) {

        static StudioStorageInfo of(
                String provider,
                boolean r2BucketConfigured,
                boolean r2EndpointConfigured
        ) {

            return new StudioStorageInfo(
                    provider,
                    r2BucketConfigured,
                    r2EndpointConfigured
            );
        }
    }
}
