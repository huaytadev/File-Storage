package com.file_storage.file.validation;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.file_storage.common.exception.BadRequestException;

@Component
public class FileNameValidator {
	
	private static final int MAX_FILENAME_LENGTH = 255;
	
	public void validate(MultipartFile file) {

        if (file == null) {
            throw new BadRequestException("File must not be null.");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new BadRequestException("File name must not be empty.");
        }
        
        if (filename.length() > MAX_FILENAME_LENGTH) {
            throw new BadRequestException(
                    "File name exceeds the maximum allowed length of 255 characters."
            );
        }

        if (filename.contains("..")) {
            throw new BadRequestException(
                    "File name contains invalid path traversal characters."
            );
        }

        if (filename.contains("/") || filename.contains("\\")) {

            throw new BadRequestException(
                    "File name must not contain path separators."
            );
        }

        if (filename.contains("\0")) {
            throw new BadRequestException(
                    "File name contains invalid characters."
            );
        }
        
        if (containsControlCharacter(filename)) {
            throw new BadRequestException(
                    "File name contains invalid control characters."
            );
        }
    }
	
    private boolean containsControlCharacter(String filename) {

        for (int i = 0; i < filename.length(); i++) {

            char character = filename.charAt(i);

            if (Character.isISOControl(character)) {
                return true;
            }
        }

        return false;
    }
}
