package com.file_storage.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.file_storage.common.exception.BadRequestException;
import com.file_storage.file.validation.FileContentValidator;

class FileContentValidatorTest {

    private FileContentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileContentValidator();
    }

    @Test
    void shouldAcceptValidTextFile() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "Hello World".getBytes(StandardCharsets.UTF_8)
        );

        String contentType = validator.validate(file);

        assertEquals("text/plain", contentType);
    }

    @Test
    void shouldRejectEmptyFile() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                new byte[0]
        );

        assertThrows(
                BadRequestException.class,
                () -> validator.validate(file)
        );
    }

    @Test
    void shouldRejectUnsupportedContentType() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "installer.exe",
                "application/x-msdownload",
                "MZ".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(
                BadRequestException.class,
                () -> validator.validate(file)
        );
    }

    @Test
    void shouldRejectExtensionThatDoesNotMatchContent() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "text/plain",
                "Hello World".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(
                BadRequestException.class,
                () -> validator.validate(file)
        );
    }

    @Test
    void shouldRejectFileLargerThan10MB() {

        byte[] content =
                new byte[10 * 1024 * 1024 + 1];

        Arrays.fill(content, (byte) 'A');

        MultipartFile file = new MockMultipartFile(
                "file",
                "large.txt",
                "text/plain",
                content
        );

        assertThrows(
                BadRequestException.class,
                () -> validator.validate(file)
        );
    }

    @Test
    void shouldAcceptJpegImage() {

        byte[] jpegHeader = new byte[]{
                (byte) 0xFF,
                (byte) 0xD8,
                (byte) 0xFF,
                (byte) 0xE0,
                0x00,
                0x10,
                0x4A,
                0x46,
                0x49,
                0x46,
                0x00
        };

        MultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                jpegHeader
        );

        assertDoesNotThrow(
                () -> validator.validate(file)
        );
    }
}