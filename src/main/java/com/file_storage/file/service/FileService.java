package com.file_storage.file.service;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.file_storage.file.dto.FileResponse;

public interface FileService {

    FileResponse upload(MultipartFile file);

    FileResponse getMetadata(UUID id);

    InputStream download(UUID id);

    void delete(UUID id);
}
