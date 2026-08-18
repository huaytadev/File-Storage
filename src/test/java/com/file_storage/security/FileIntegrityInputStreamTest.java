package com.file_storage.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

import com.file_storage.common.exception.FileStorageException;

class FileIntegrityInputStreamTest {

    @Test
    void shouldReadFileWhenHashIsValid()
            throws IOException {

        byte[] content = "Hello World".getBytes(StandardCharsets.UTF_8);

        String expectedHash = calculateSha256(content);

        try (InputStream inputStream =
                     new FileIntegrityInputStream(
                             new ByteArrayInputStream(content),
                             expectedHash
                     )) {

            byte[] result = inputStream.readAllBytes();

            assertArrayEquals(content, result);
        }
    }

    @Test
    void shouldThrowExceptionWhenHashIsInvalid()
            throws IOException {

        byte[] content = "Hello World".getBytes(StandardCharsets.UTF_8);

        String invalidHash =
                "0000000000000000000000000000000000000000000000000000000000000000";

        assertThrows(FileStorageException.class,
                () -> {try (InputStream inputStream = 
                				new FileIntegrityInputStream(
                						new ByteArrayInputStream(content),
                                         invalidHash
                                         )) {
                		inputStream.readAllBytes();
                    }
                }
        );
    }

    @Test
    void shouldVerifyIntegrityWhenUsingSingleByteRead()
            throws IOException {

        byte[] content = "Hello World".getBytes(StandardCharsets.UTF_8);

        String expectedHash = calculateSha256(content);

        try (InputStream inputStream =
                     new FileIntegrityInputStream(
                             new ByteArrayInputStream(content),
                             expectedHash
                     )) {

            for (byte expectedByte : content) {

                int actualByte = inputStream.read();

                assertEquals(expectedByte & 0xFF, actualByte);
            }

            assertEquals(-1, inputStream.read());
        }
    }

    @Test
    void shouldReadFileUsingByteArrayRead()
            throws IOException {

        byte[] content = "Hello World".getBytes(StandardCharsets.UTF_8);

        String expectedHash = calculateSha256(content);

        try (InputStream inputStream =
                     new FileIntegrityInputStream(
                             new ByteArrayInputStream(content),
                             expectedHash
                     )) {

            byte[] result = new byte[content.length];

            int bytesRead = inputStream.read(result);

            assertEquals(content.length, bytesRead);

            assertArrayEquals(content, result);

            int endOfStream = inputStream.read(result);

            assertEquals(-1, endOfStream);
        }
    }

    @Test
    void shouldReadFileUsingOffsetAndLengthRead()
            throws IOException {

        byte[] content = "Hello World".getBytes(StandardCharsets.UTF_8);

        String expectedHash = calculateSha256(content);

        try (InputStream inputStream =
                     new FileIntegrityInputStream(
                             new ByteArrayInputStream(content),
                             expectedHash
                     )) {

            byte[] result =
                    new byte[content.length + 10];

            int bytesRead = inputStream.read(result, 5, content.length);

            assertEquals(content.length,bytesRead);

            byte[] extracted = new byte[content.length];

            System.arraycopy(result, 5,extracted, 0, content.length);

            assertArrayEquals(content, extracted);

            int endOfStream =
                    inputStream.read(result, 0, result.length);

            assertEquals(-1, endOfStream);
        }
    }

    @Test
    void shouldThrowExceptionWhenSingleByteReadDetectsInvalidHash()
            throws IOException {

        byte[] content = "Hello World".getBytes(StandardCharsets.UTF_8);

        String invalidHash =
                "0000000000000000000000000000000000000000000000000000000000000000";

        try (InputStream inputStream =
                     new FileIntegrityInputStream(
                             new ByteArrayInputStream(content),
                             invalidHash
                     )) {

            for (int i = 0; i < content.length; i++) {

                inputStream.read();
            }

            assertThrows(FileStorageException.class, inputStream::read);
        }
    }

    private String calculateSha256(byte[] content) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(content);

            StringBuilder result = new StringBuilder(hash.length * 2);

            for (byte value : hash) {

                result.append(String.format("%02x", value));
            }

            return result.toString();

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is not available.",
                    exception
            );
        }
    }
}