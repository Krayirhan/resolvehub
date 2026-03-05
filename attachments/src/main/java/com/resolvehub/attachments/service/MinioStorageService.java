package com.resolvehub.attachments.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class MinioStorageService {
    private final StorageProperties storageProperties;

    public MinioStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public void upload(String objectKey, MultipartFile file) {
        if (!isConfigured()) {
            return;
        }
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(storageProperties.getEndpoint())
                    .credentials(storageProperties.getAccessKey(), storageProperties.getSecretKey())
                    .build();
            boolean bucketExists = client.bucketExists(BucketExistsArgs.builder().bucket(storageProperties.getBucket()).build());
            if (!bucketExists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(storageProperties.getBucket()).build());
            }
            try (InputStream inputStream = file.getInputStream()) {
                client.putObject(PutObjectArgs.builder()
                        .bucket(storageProperties.getBucket())
                        .object(objectKey)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Attachment upload failed: " + ex.getMessage(), ex);
        }
    }

    public boolean isConfigured() {
        return storageProperties.getEndpoint() != null && !storageProperties.getEndpoint().isBlank()
                && storageProperties.getAccessKey() != null && !storageProperties.getAccessKey().isBlank()
                && storageProperties.getSecretKey() != null && !storageProperties.getSecretKey().isBlank();
    }
}
