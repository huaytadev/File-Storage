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

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(
        name = "Files",
        description = "File management endpoints"
)
public class FileController {
	private final FileService fileService;

	@Operation(summary = "Upload a file")
	@ApiResponses({
	    @io.swagger.v3.oas.annotations.responses.ApiResponse(
	        responseCode = "201",
	        description = "File uploaded successfully."
	    ),
	    @io.swagger.v3.oas.annotations.responses.ApiResponse(
	        responseCode = "400",
	        description = "Invalid file."
	    ),
	    @io.swagger.v3.oas.annotations.responses.ApiResponse(
	        responseCode = "413",
	        description = "File exceeds the maximum allowed size."
	    )
	})
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

    @Operation(summary = "Get file metadata")
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

    @Operation(summary = "Download a file")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "File downloaded successfully."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "File not found."
            )
    })
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

    @Operation(summary = "Delete a file")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "File deleted successfully."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "File not found."
            )
    })
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
