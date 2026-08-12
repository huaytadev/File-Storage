package com.file_storage.file.controller;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.file_storage.common.response.ApiResponse;
import com.file_storage.file.dto.FileResponse;
import com.file_storage.file.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
	private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileResponse>> upload(
            @RequestParam("file") MultipartFile file
    ) {

        FileResponse response = fileService.upload(file);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.success(
                        "File uploaded successfully.",
                        response
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileResponse>> getMetadata(
            @PathVariable UUID id
    ) {

        FileResponse response = fileService.getMetadata(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "File metadata retrieved successfully.",
                        response
                )
        );
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable UUID id
    ) {

        FileResponse metadata = fileService.getMetadata(id);

        InputStream inputStream = fileService.download(id);

        InputStreamResource resource = new InputStreamResource(inputStream);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(metadata.originalName())
                        .build()
        );

        headers.setContentType(
                MediaType.parseMediaType(metadata.contentType())
        );

        headers.setContentLength(metadata.size());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id
    ) {

        fileService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "File deleted successfully."
                )
        );
    }
	
}
