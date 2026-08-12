package com.file_storage.file.dto;

import java.time.Instant;
import java.util.UUID;

public record FileMetadataResponse(
        UUID id,
        String originalName,
        String objectName,
        String contentType,
        Long size,
        String bucketName,
        Instant createdAt
) {}
