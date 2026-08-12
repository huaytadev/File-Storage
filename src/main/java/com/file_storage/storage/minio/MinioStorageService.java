package com.file_storage.storage.minio;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.file_storage.common.exception.FileStorageException;
import com.file_storage.storage.StorageService;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.PostConstruct;

@Service
public class MinioStorageService implements StorageService{

	private static final long AUTO_PART_SIZE = -1L;
	private final MinioClient minioClient;
    private final String bucketName;

    public MinioStorageService(
            MinioClient minioClient,
            @Value("${minio.bucket-name}") String bucketName
    ) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @PostConstruct
    @Override
    public void initialize() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
            }

        } catch (Exception exception) {
            throw new FileStorageException(
                    "Failed to initialize MinIO storage.",
                    exception
            );
        }
    }
    
    @Override
    public String getBucketName() {
        return bucketName;
    }

    @Override
    public void upload(
            String objectName,
            MultipartFile file
    ) {
        try (InputStream inputStream = file.getInputStream()) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(
                                    inputStream,
                                    file.getSize(),
                                    AUTO_PART_SIZE
                            )
                            .contentType(file.getContentType())
                            .build()
            );

        } catch (Exception exception) {
            throw new FileStorageException(
                    "Failed to upload file to storage.",
                    exception
            );
        }
    }

    @Override
    public InputStream download(
            String objectName
    ) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

        } catch (Exception exception) {
            throw new FileStorageException(
                    "Failed to download file from storage.",
                    exception
            );
        }
    }

    @Override
    public void delete(
            String objectName
    ) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

        } catch (Exception exception) {
            throw new FileStorageException(
                    "Failed to delete file from storage.",
                    exception
            );
        }
    }
}
