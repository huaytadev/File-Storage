package com.file_storage.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;

import com.file_storage.common.exception.FileStorageException;

@Service
public class FileHashService {
	private static final int BUFFER_SIZE = 8192;

    public String calculateSha256(InputStream inputStream) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[BUFFER_SIZE];

            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            return toHex(digest.digest());

        } catch (IOException | NoSuchAlgorithmException exception) {

            throw new FileStorageException(
                    "Failed to calculate file hash.",
                    exception
            );
        }
    }

    private String toHex(byte[] hash) {

        StringBuilder result = new StringBuilder(hash.length * 2);

        for (byte value : hash) {
            result.append(String.format("%02x", value));
        }

        return result.toString();
    }
}
