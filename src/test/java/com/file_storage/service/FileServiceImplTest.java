package com.file_storage.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.file_storage.common.exception.FileStorageException;
import com.file_storage.common.exception.ResourceNotFoundException;
import com.file_storage.file.dto.FileResponse;
import com.file_storage.file.entity.FileMetadataEntity;
import com.file_storage.file.mapper.FileMapper;
import com.file_storage.file.repository.FileMetadataRepository;
import com.file_storage.file.service.FileServiceImpl;
import com.file_storage.file.validation.FileContentValidator;
import com.file_storage.file.validation.FileNameValidator;
import com.file_storage.security.FileHashService;
import com.file_storage.storage.StorageService;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private FileMapper fileMapper;

    @Mock
    private StorageService storageService;

    @Mock
    private FileContentValidator fileContentValidator;

    @Mock
    private FileNameValidator fileNameValidator;

    @Mock
    private FileHashService fileHashService;

    @InjectMocks
    private FileServiceImpl fileService;

    private MultipartFile file;

    @BeforeEach
    void setUp() {

        file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "Hello World".getBytes()
        );
    }

    @Test
    void shouldUploadFileSuccessfully() {

        UUID id = UUID.randomUUID();

        FileMetadataEntity entity =
                FileMetadataEntity.builder()
                        .id(id)
                        .originalName("document.txt")
                        .objectName(id + ".txt")
                        .contentType("text/plain")
                        .size(file.getSize())
                        .bucketName("files")
                        .sha256("hash")
                        .createdAt(Instant.now())
                        .build();

        FileResponse response =
                new FileResponse(
                        id,
                        "document.txt",
                        "text/plain",
                        file.getSize(),
                        entity.getCreatedAt()
                );

        when(fileContentValidator.validate(file))
                .thenReturn("text/plain");

        when(fileHashService.calculateSha256(any(InputStream.class)))
                .thenReturn("hash");

        when(storageService.getBucketName())
                .thenReturn("files");

        when(fileMetadataRepository.save(any(FileMetadataEntity.class)))
                .thenReturn(entity);

        when(fileMapper.toResponse(entity))
                .thenReturn(response);

        FileResponse result =
                fileService.upload(file);

        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals("document.txt", result.originalName());

        verify(fileNameValidator).validate(file);

        verify(fileContentValidator).validate(file);

        verify(fileHashService).calculateSha256(any(InputStream.class));

        verify(storageService)
                .upload(
                        anyString(),
                        any(MultipartFile.class),
                        anyString()
                );

        verify(fileMetadataRepository).save(any(FileMetadataEntity.class));
    }

    @Test
    void shouldRollbackStorageWhenMetadataSaveFails() {

        when(fileContentValidator.validate(file))
                .thenReturn("text/plain");

        when(fileHashService.calculateSha256(any(InputStream.class)))
                .thenReturn("hash");

        when(storageService.getBucketName())
                .thenReturn("files");

        when(fileMetadataRepository.save(any(FileMetadataEntity.class)))
                .thenThrow(
                        new RuntimeException("Database error")
                );

        FileStorageException exception =
                assertThrows(
                        FileStorageException.class,
                        () -> fileService.upload(file)
                );

        assertEquals(
                "Failed to save file metadata.",
                exception.getMessage()
        );

        verify(storageService)
                .upload(
                        anyString(),
                        any(MultipartFile.class),
                        anyString()
                );

        verify(storageService)
                .delete(anyString());
    }

    @Test
    void shouldThrowFileStorageExceptionWhenHashCalculationFails() {

        when(fileContentValidator.validate(file))
                .thenReturn("text/plain");

        when(fileHashService.calculateSha256(any(InputStream.class)))
                .thenThrow(
                        new FileStorageException(
                                "Failed to calculate file hash."
                        )
                );

        FileStorageException exception =
                assertThrows(
                        FileStorageException.class,
                        () -> fileService.upload(file)
                );

        assertEquals(
                "Failed to calculate file hash.",
                exception.getMessage()
        );

        verify(storageService, never())
                .upload(
                        anyString(),
                        any(MultipartFile.class),
                        anyString()
                );

        verify(fileMetadataRepository, never())
                .save(any(FileMetadataEntity.class));
    }

    @Test
    void shouldReturnFileMetadata() {

        UUID id = UUID.randomUUID();

        FileMetadataEntity entity =
                FileMetadataEntity.builder()
                        .id(id)
                        .originalName("document.txt")
                        .objectName(id + ".txt")
                        .contentType("text/plain")
                        .size(100L)
                        .bucketName("files")
                        .sha256("hash")
                        .createdAt(Instant.now())
                        .build();

        FileResponse response =
                new FileResponse(
                        id,
                        "document.txt",
                        "text/plain",
                        100L,
                        entity.getCreatedAt()
                );

        when(fileMetadataRepository.findById(id))
                .thenReturn(Optional.of(entity));

        when(fileMapper.toResponse(entity))
                .thenReturn(response);

        FileResponse result =
                fileService.getMetadata(id);

        assertEquals(id, result.id());

        assertEquals(
                "document.txt",
                result.originalName()
        );

        verify(fileMetadataRepository)
                .findById(id);

        verify(fileMapper)
                .toResponse(entity);
    }

    @Test
    void shouldThrowExceptionWhenFileDoesNotExist() {

        UUID id = UUID.randomUUID();

        when(fileMetadataRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> fileService.getMetadata(id)
        );

        verify(fileMetadataRepository)
                .findById(id);

        verify(fileMapper, never())
                .toResponse(any(FileMetadataEntity.class));

        verify(storageService, never())
                .download(anyString());
    }

    @Test
    void shouldDownloadFileSuccessfully() {

        UUID id = UUID.randomUUID();

        FileMetadataEntity entity =
                FileMetadataEntity.builder()
                        .id(id)
                        .originalName("document.txt")
                        .objectName(id + ".txt")
                        .contentType("text/plain")
                        .size(100L)
                        .bucketName("files")
                        .sha256("hash")
                        .createdAt(Instant.now())
                        .build();

        InputStream inputStream =
                new ByteArrayInputStream(
                        "Hello World".getBytes()
                );

        when(fileMetadataRepository.findById(id))
                .thenReturn(Optional.of(entity));

        when(storageService.download(
                entity.getObjectName()
        )).thenReturn(inputStream);

        InputStream result = fileService.download(id);

        assertNotNull(result);

        verify(fileMetadataRepository).findById(id);

        verify(storageService).download(entity.getObjectName());
    }

    @Test
    void shouldThrowExceptionWhenDownloadingNonExistingFile() {

        UUID id = UUID.randomUUID();

        when(fileMetadataRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> fileService.download(id)
        );

        verify(fileMetadataRepository).findById(id);

        verify(storageService, never()).download(anyString());
    }

    @Test
    void shouldDeleteFileSuccessfully() {

        UUID id = UUID.randomUUID();

        FileMetadataEntity entity =
                FileMetadataEntity.builder()
                        .id(id)
                        .originalName("document.txt")
                        .objectName(id + ".txt")
                        .contentType("text/plain")
                        .size(100L)
                        .bucketName("files")
                        .sha256("hash")
                        .createdAt(Instant.now())
                        .build();

        when(fileMetadataRepository.findById(id)).thenReturn(Optional.of(entity));

        doNothing().when(fileMetadataRepository).delete(entity);

        doNothing().when(storageService).delete(entity.getObjectName());

        fileService.delete(id);

        verify(fileMetadataRepository).findById(id);

        verify(fileMetadataRepository).delete(entity);

        verify(fileMetadataRepository).flush();

        verify(storageService).delete(entity.getObjectName());
    }

    @Test
    void shouldThrowFileStorageExceptionWhenStorageDeleteFails() {

        UUID id = UUID.randomUUID();

        FileMetadataEntity entity =
                FileMetadataEntity.builder()
                        .id(id)
                        .originalName("document.txt")
                        .objectName(id + ".txt")
                        .contentType("text/plain")
                        .size(100L)
                        .bucketName("files")
                        .sha256("hash")
                        .createdAt(Instant.now())
                        .build();

        when(fileMetadataRepository.findById(id)).thenReturn(Optional.of(entity));

        doNothing().when(fileMetadataRepository).delete(entity);

        doNothing().when(fileMetadataRepository).flush();

        doThrow(new RuntimeException("MinIO error"))
                .when(storageService)
                .delete(entity.getObjectName());

        FileStorageException exception =
                assertThrows(
                        FileStorageException.class,
                        () -> fileService.delete(id)
                );

        assertEquals(
                "Failed to delete file from storage.",
                exception.getMessage()
        );

        verify(fileMetadataRepository).delete(entity);

        verify(fileMetadataRepository).flush();

        verify(storageService).delete(entity.getObjectName());
    }
}
