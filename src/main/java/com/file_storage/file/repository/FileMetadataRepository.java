package com.file_storage.file.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.file_storage.file.entity.FileMetadataEntity;

public interface FileMetadataRepository extends JpaRepository<FileMetadataEntity, UUID>{

}
