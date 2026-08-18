package com.file_storage.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.file_storage.file.entity.FileMetadataEntity;
import com.file_storage.file.repository.FileMetadataRepository;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class FileMetadataRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer(
                    DockerImageName.parse("postgres:18.4")
            )
            .withDatabaseName("file_storage_test")
            .withUsername("test")
            .withPassword("test")
            .withEnv("TZ", "UTC");

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @BeforeEach
    void setUp() {

        fileMetadataRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindFileMetadata() {

        UUID id = UUID.randomUUID();

        FileMetadataEntity entity =
                FileMetadataEntity.builder()
                        .id(id)
                        .originalName("document.pdf")
                        .objectName(id + ".pdf")
                        .contentType("application/pdf")
                        .size(1024L)
                        .bucketName("files")
                        .sha256("test-sha256")
                        .createdAt(Instant.now())
                        .build();

        FileMetadataEntity saved =
                fileMetadataRepository.save(entity);

        Optional<FileMetadataEntity> result =
                fileMetadataRepository.findById(saved.getId());

        assertTrue(result.isPresent());

        FileMetadataEntity found = result.get();

        assertEquals(id, found.getId());
        assertEquals("document.pdf", found.getOriginalName());
        assertEquals(id + ".pdf", found.getObjectName());
        assertEquals("application/pdf", found.getContentType());
        assertEquals(1024L, found.getSize());
        assertEquals("files", found.getBucketName());
        assertEquals("test-sha256", found.getSha256());
    }

    @Test
    void shouldReturnEmptyWhenFileDoesNotExist() {

        UUID id = UUID.randomUUID();

        Optional<FileMetadataEntity> result = fileMetadataRepository.findById(id);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldDeleteFileMetadata() {

        UUID id = UUID.randomUUID();

        FileMetadataEntity entity =
                FileMetadataEntity.builder()
                        .id(id)
                        .originalName("document.txt")
                        .objectName(id + ".txt")
                        .contentType("text/plain")
                        .size(100L)
                        .bucketName("files")
                        .sha256("test-sha256")
                        .createdAt(Instant.now())
                        .build();

        fileMetadataRepository.save(entity);

        assertTrue(fileMetadataRepository.findById(id).isPresent());

        fileMetadataRepository.delete(entity);

        assertFalse(fileMetadataRepository.findById(id).isPresent());
    }
}