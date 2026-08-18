package com.file_storage.security;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.file_storage.common.exception.FileStorageException;

public class FileIntegrityInputStream extends FilterInputStream{
	private final MessageDigest digest;
    private final String expectedSha256;

    private boolean verified;

    public FileIntegrityInputStream(InputStream inputStream, String expectedSha256) {

        super(inputStream);

        this.expectedSha256 = expectedSha256;

        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new FileStorageException(
                    "SHA-256 algorithm is not available.",
                    exception
            );
        }
    }

    @Override
    public int read() throws IOException {

        int value = super.read();

        if (value == -1) {
            verifyIntegrity();
            return -1;
        }

        digest.update((byte) value);

        return value;
    }

//    @Override
//    public int read(byte[] buffer) throws IOException {
//
//        int bytesRead = super.read(buffer);
//
//        if (bytesRead == -1) {
//            verifyIntegrity();
//            return -1;
//        }
//
//        digest.update(buffer, 0, bytesRead);
//
//        return bytesRead;
//    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {

        int bytesRead = super.read(buffer, offset, length);

        if (bytesRead == -1) {
            verifyIntegrity();
            return -1;
        }

        digest.update(buffer, offset, bytesRead);

        return bytesRead;
    }

    private void verifyIntegrity() {

        if (verified) {
            return;
        }

        verified = true;

        String actualSha256 = toHex(digest.digest());

        if (!actualSha256.equalsIgnoreCase(expectedSha256)) {
            throw new FileStorageException(
                    "File integrity verification failed."
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
