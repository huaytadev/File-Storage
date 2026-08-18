package com.file_storage.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileHashServiceTest {

    private FileHashService fileHashService;

    @BeforeEach
    void setUp() {
        fileHashService = new FileHashService();
    }

    @Test
    void shouldCalculateCorrectSha256Hash() {

        String content = "Hello World";

        InputStream inputStream = new ByteArrayInputStream(
        		content.getBytes(StandardCharsets.UTF_8));

        String result = fileHashService.calculateSha256(inputStream);

        assertEquals(
                "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e",
                result
        );
    }

    @Test
    void shouldCalculateCorrectHashForEmptyInput() {

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        String result = fileHashService.calculateSha256(inputStream);

        assertEquals(
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                result
        );
    }

    @Test
    void shouldGenerateDifferentHashesForDifferentContent() {

        InputStream firstInputStream =
                new ByteArrayInputStream(
                        "Hello World".getBytes(StandardCharsets.UTF_8)
                );

        InputStream secondInputStream =
                new ByteArrayInputStream(
                        "Hello Java".getBytes(StandardCharsets.UTF_8)
                );

        String firstHash = fileHashService.calculateSha256(firstInputStream);

        String secondHash = fileHashService.calculateSha256(secondInputStream);

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void shouldCalculateHashForLargeInput() {

        byte[] content = new byte[20_000];

        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }

        InputStream inputStream = new ByteArrayInputStream(content);

        String result = fileHashService.calculateSha256(inputStream);

        assertEquals(64, result.length());
    }
}