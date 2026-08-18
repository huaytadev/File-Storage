package com.file_storage.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.file_storage.common.exception.GlobalExceptionHandler;
import com.file_storage.file.controller.FileController;
import com.file_storage.file.dto.FileResponse;
import com.file_storage.file.service.FileService;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    private UUID fileId;

    private FileResponse fileResponse;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(fileController)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();

        fileId = UUID.randomUUID();

        fileResponse = new FileResponse(
                fileId,
                "document.txt",
                "text/plain",
                12L,
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }

    @Test
    void shouldUploadFile() throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "document.txt",
                        "text/plain",
                        "Hello World!".getBytes()
                );

        when(fileService.upload(any()))
                .thenReturn(fileResponse);

        mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(fileId.toString()))
                .andExpect(jsonPath("$.data.originalName").value("document.txt"));

        verify(fileService).upload(any());
    }

    @Test
    void shouldGetFileMetadata() throws Exception {

        when(fileService.getMetadata(fileId))
                .thenReturn(fileResponse);

        mockMvc.perform(get("/api/v1/files/{id}", fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(fileId.toString()))
                .andExpect(jsonPath("$.data.originalName").value("document.txt"))
                .andExpect(jsonPath("$.data.contentType").value("text/plain"));

        verify(fileService).getMetadata(fileId);
    }

    @Test
    void shouldDownloadFile() throws Exception {

        byte[] content = "Hello World!".getBytes();

        InputStream inputStream = new ByteArrayInputStream(content);

        when(fileService.getMetadata(fileId)).thenReturn(fileResponse);

        when(fileService.download(fileId)).thenReturn(inputStream);

        mockMvc.perform(get("/api/v1/files/{id}/download", fileId))
                .andExpect(status().isOk())
                .andExpect(header().string(
                		"Content-Disposition",
                		"attachment; filename=\"document.txt\""
                		)
                	)
                .andExpect(header().string("Content-Type", "text/plain"))
                .andExpect(header().longValue("Content-Length", 12L))
                .andExpect(content().bytes(content));

        verify(fileService).getMetadata(fileId);

        verify(fileService).download(fileId);
    }

    @Test
    void shouldDeleteFile() throws Exception {

        doNothing().when(fileService).delete(fileId);

        mockMvc.perform(delete("/api/v1/files/{id}", fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(fileService).delete(fileId);
    }

    @Test
    void shouldReturnNotFoundWhenFileDoesNotExist()
            throws Exception {

        when(fileService.getMetadata(fileId))
                .thenThrow(
                        new com.file_storage.common.exception
                                .ResourceNotFoundException(
                                        "File not found with id: " + fileId
                                )
                );

        mockMvc.perform(
                get("/api/v1/files/{id}", fileId)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                		"File not found with id: " + fileId));
    }
}