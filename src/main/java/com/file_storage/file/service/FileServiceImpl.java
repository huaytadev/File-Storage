package com.file_storage.file.service;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.file_storage.common.exception.BadRequestException;
import com.file_storage.common.exception.ResourceNotFoundException;
import com.file_storage.file.dto.FileResponse;
import com.file_storage.file.entity.FileMetadataEntity;
import com.file_storage.file.mapper.FileMapper;
import com.file_storage.file.repository.FileMetadataRepository;
import com.file_storage.storage.StorageService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileServiceImpl implements FileService{
	
    private final FileMetadataRepository fileMetadataRepository;
    private final FileMapper fileMapper;
    private final StorageService storageService;

    @Override
    @Transactional
    public FileResponse upload(MultipartFile file) {

        validateFile(file);

        UUID fileId = UUID.randomUUID();

        String objectName = buildObjectName(fileId, file.getOriginalFilename());
        
        storageService.upload(objectName, file);

        FileMetadataEntity fileMetadata = FileMetadataEntity.builder()
                .originalName(file.getOriginalFilename())
                .objectName(objectName)
                .contentType(resolveContentType(file))
                .size(file.getSize())
                .bucketName(storageService.getBucketName())
                .createdAt(Instant.now())
                .build();

        FileMetadataEntity savedFile = fileMetadataRepository.save(
                fileMetadata
        );

        return fileMapper.toResponse(savedFile);
    }

    @Override
    public FileResponse getMetadata(UUID id) {

        FileMetadataEntity fileMetadata = findFile(id);

        return fileMapper.toResponse(fileMetadata);
    }

    @Override
    public InputStream download(UUID id) {

        FileMetadataEntity fileMetadata = findFile(id);

        return storageService.download(
                fileMetadata.getObjectName()
        );
    }

    @Override
    @Transactional
    public void delete(UUID id) {

        FileMetadataEntity fileMetadata = findFile(id);

        storageService.delete(
                fileMetadata.getObjectName()
        );

        fileMetadataRepository.delete(fileMetadata);
    }

    private FileMetadataEntity findFile(UUID id) {

        return fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "File not found with id: " + id
                ));
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException(
                    "File must not be empty."
            );
        }
    }

    private String buildObjectName(
            UUID fileId,
            String originalFilename
    ) {

        String extension = extractExtension(originalFilename);

        return extension.isBlank()
                ? fileId.toString()
                : fileId + "." + extension;
    }

    private String extractExtension(String filename) {

        if (filename == null || filename.isBlank()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');

        if (lastDotIndex <= 0 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1);
    }

    private String resolveContentType(MultipartFile file) {

        String contentType = file.getContentType();

        return contentType == null || contentType.isBlank()
                ? "application/octet-stream"
                : contentType;
    }
}
