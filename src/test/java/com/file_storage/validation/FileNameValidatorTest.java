package com.file_storage.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.file_storage.common.exception.BadRequestException;
import com.file_storage.file.validation.FileNameValidator;

class FileNameValidatorTest {

    private FileNameValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileNameValidator();
    }

    @Test
    void shouldAcceptValidFilename() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "my document.pdf",
                "application/pdf",
                new byte[]{1}
        );

        assertDoesNotThrow(
                () -> validator.validate(file)
        );
    }

    @Test
    void shouldRejectNullFile() {

        assertThrows(
                BadRequestException.class,
                () -> validator.validate(null)
        );
    }

    @Test
    void shouldRejectEmptyFilename() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "",
                "application/pdf",
                new byte[]{1}
        );

        assertThrows(
                BadRequestException.class,
                () -> validator.validate(file)
        );
    }

    @Test
    void shouldRejectPathTraversal() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "..document.pdf",
                "application/pdf",
                new byte[]{1}
        );

        assertThrows(
                BadRequestException.class,
                () -> validator.validate(file)
        );
    }

    @Test
    void shouldRejectPathSeparator() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "folder/document.pdf",
                "application/pdf",
                new byte[]{1}
        );

        assertThrows(
                BadRequestException.class,
                () -> validator.validate(file)
        );
    }

    @Test
    void shouldRejectWindowsPathSeparator() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "folder\\document.pdf",
                "application/pdf",
                new byte[]{1}
        );

        assertThrows(
                BadRequestException.class,
                () -> validator.validate(file)
        );
    }

    @Test
    void shouldRejectFilenameLongerThan255Characters() {

        String filename = "a".repeat(256) + ".pdf";

        MultipartFile file = new MockMultipartFile(
                "file",
                filename,
                "application/pdf",
                new byte[]{1}
        );

        assertThrows(
                BadRequestException.class,
                () -> validator.validate(file)
        );
    }

    @Test
    void shouldRejectControlCharacters() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document\u0000.pdf",
                "application/pdf",
                new byte[]{1}
        );

        assertThrows(
                BadRequestException.class,
                () -> validator.validate(file)
        );
    }
}