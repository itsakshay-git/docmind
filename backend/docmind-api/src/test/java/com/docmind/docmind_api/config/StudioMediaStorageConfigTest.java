package com.docmind.docmind_api.config;

import com.docmind.docmind_api.studio.service.FileSystemStudioMediaStorage;
import com.docmind.docmind_api.studio.service.R2StudioMediaStorage;
import com.docmind.docmind_api.studio.service.StudioMediaStorage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

class StudioMediaStorageConfigTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(
                            StudioMediaStorageConfig.class
                    )
                    .withPropertyValues(
                            "docmind.studio.audio-storage-dir=storage/studio-audio",
                            "docmind.studio.image-storage-dir=storage/studio-images"
                    );

    @Test
    void usesFileSystemStorageByDefault() {
        contextRunner.run(context -> {
            assertThat(context)
                    .hasSingleBean(StudioMediaStorage.class);
            assertThat(context.getBean(StudioMediaStorage.class))
                    .isInstanceOf(FileSystemStudioMediaStorage.class);
            assertThat(context)
                    .doesNotHaveBean(S3Client.class);
        });
    }

    @Test
    void usesR2StorageWhenConfigured() {
        contextRunner
                .withPropertyValues(
                        "docmind.studio.storage-provider=r2",
                        "docmind.r2.endpoint=https://example-account.r2.cloudflarestorage.com",
                        "docmind.r2.bucket=docmind-studio",
                        "docmind.r2.access-key-id=test-key",
                        "docmind.r2.secret-access-key=test-secret"
                )
                .run(context -> {
                    assertThat(context)
                            .hasSingleBean(StudioMediaStorage.class);
                    assertThat(context.getBean(StudioMediaStorage.class))
                            .isInstanceOf(R2StudioMediaStorage.class);
                    assertThat(context)
                            .hasSingleBean(S3Client.class);
                });
    }
}