package com.file_storage.file.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_metadata")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataEntity {
	@Id
//    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false, unique = true)
    private String objectName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String bucketName;
    
    @Column(nullable = false, length = 64)
    private String sha256;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
