package com.file_storage.file.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.file_storage.common.exception.FileStorageException;
import com.file_storage.common.exception.ResourceNotFoundException;
import com.file_storage.file.dto.FileResponse;
import com.file_storage.file.entity.FileMetadataEntity;
import com.file_storage.file.mapper.FileMapper;
import com.file_storage.file.repository.FileMetadataRepository;
import com.file_storage.file.validation.FileContentValidator;
import com.file_storage.file.validation.FileNameValidator;
import com.file_storage.security.FileHashService;
import com.file_storage.security.FileIntegrityInputStream;
import com.file_storage.storage.StorageService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileServiceImpl implements FileService{
	
    private final FileMetadataRepository fileMetadataRepository;
    private final FileMapper fileMapper;
    private final StorageService storageService;
    private final FileContentValidator fileContentValidator;
    private final FileHashService fileHashService;
    private final FileNameValidator fileNameValidator;

    @Override
    @Transactional
    public FileResponse upload(MultipartFile file) {
    	
    	fileNameValidator.validate(file);

    	String detectedContentType = fileContentValidator.validate(file);
    	
    	String sha256 = calculateHash(file);

        UUID fileId = UUID.randomUUID();

        String objectName = buildObjectName(fileId, file.getOriginalFilename());
        
        storageService.upload(objectName, file, detectedContentType);

        try {
	        FileMetadataEntity fileMetadata = FileMetadataEntity.builder()
	        		.id(fileId)
	                .originalName(file.getOriginalFilename())
	                .objectName(objectName)
	                .contentType(detectedContentType)
	                .size(file.getSize())
	                .bucketName(storageService.getBucketName())
	                .sha256(sha256)
	                .createdAt(Instant.now())
	                .build();

	        FileMetadataEntity savedFile = fileMetadataRepository.save(fileMetadata);
	
	        return fileMapper.toResponse(savedFile);
	        
        } catch (Exception exception) {

            rollbackStorage(objectName, exception);

            throw new FileStorageException(
                    "Failed to save file metadata.",
                    exception
            );
        }
    }

    @Override
    public FileResponse getMetadata(UUID id) {

        FileMetadataEntity fileMetadata = findFile(id);

        return fileMapper.toResponse(fileMetadata);
    }

    @Override
    public InputStream download(UUID id) {

        FileMetadataEntity fileMetadata = findFile(id);

        InputStream inputStream = storageService.download(fileMetadata.getObjectName());
        
        return new FileIntegrityInputStream(inputStream, fileMetadata.getSha256());
    }

    @Override
    @Transactional
    public void delete(UUID id) {

        FileMetadataEntity fileMetadata = findFile(id);

        fileMetadataRepository.delete(fileMetadata);
        fileMetadataRepository.flush();

        try {

            storageService.delete(fileMetadata.getObjectName());

        } catch (Exception exception) {

            throw new FileStorageException(
                    "Failed to delete file from storage.",
                    exception
            );
        }
    }

    private FileMetadataEntity findFile(UUID id) {

        return fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "File not found with id: " + id
                ));
    }
    
    private String calculateHash(MultipartFile file) {

        try (InputStream inputStream = file.getInputStream()) {

            return fileHashService.calculateSha256(inputStream);

        } catch (IOException exception) {

            throw new FileStorageException(
                    "Failed to read file for hash calculation.",
                    exception
            );
        }
    }
    
    private void rollbackStorage(String objectName, Exception originalException) {

        try {

            storageService.delete(objectName);

        } catch (Exception rollbackException) {

            originalException.addSuppressed(rollbackException);
        }
    }
    
//    private void restoreMetadata(FileMetadataEntity fileMetadata, Exception originalException) {
//
//        try {
//
//            fileMetadataRepository.save(fileMetadata);
//
//        } catch (Exception rollbackException) {
//
//            originalException.addSuppressed(rollbackException);
//        }
//    }

    private String buildObjectName(UUID fileId, String originalFilename) {

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
}
