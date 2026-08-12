package com.file_storage.file.dto;

import java.time.Instant;
import java.util.UUID;

public record FileResponse(
		UUID id,
        String originalName,
        String contentType,
        Long size,
        Instant createdAt
){}
