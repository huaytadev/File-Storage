package com.file_storage.storage;

import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
	
    void initialize();
    
    String getBucketName();

    void upload(String objectName, MultipartFile file, String contentType);

    InputStream download(String objectName);

    void delete(String objectName);
}
