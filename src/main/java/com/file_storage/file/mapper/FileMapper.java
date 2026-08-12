package com.file_storage.file.mapper;

import org.springframework.stereotype.Component;

import com.file_storage.file.dto.FileMetadataResponse;
import com.file_storage.file.dto.FileResponse;
import com.file_storage.file.entity.FileMetadataEntity;

@Component
public class FileMapper {
	
    public FileResponse toResponse(FileMetadataEntity entity) {
        return new FileResponse(
                entity.getId(),
                entity.getOriginalName(),
                entity.getContentType(),
                entity.getSize(),
                entity.getCreatedAt()
        );
    }

    public FileMetadataResponse toMetadataResponse(FileMetadataEntity entity) {
        return new FileMetadataResponse(
                entity.getId(),
                entity.getOriginalName(),
                entity.getObjectName(),
                entity.getContentType(),
                entity.getSize(),
                entity.getBucketName(),
                entity.getCreatedAt()
        );
    }
}
