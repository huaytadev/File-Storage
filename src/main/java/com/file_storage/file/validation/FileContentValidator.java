package com.file_storage.file.validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.file_storage.common.exception.BadRequestException;

@Component
public class FileContentValidator {
	
	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "text/plain"
    );
	
    private static final Map<String, Set<String>> ALLOWED_EXTENSIONS_BY_CONTENT_TYPE = Map.of(
            "application/pdf", Set.of("pdf"),
            "image/jpeg", Set.of("jpg", "jpeg"),
            "image/png", Set.of("png"),
            "image/gif", Set.of("gif"),
            "image/webp", Set.of("webp"),
            "text/plain", Set.of("txt")
    );

    private final Tika tika;

    public FileContentValidator() {
        this.tika = new Tika();
    }

    public String validate(MultipartFile file) {

        validateNotEmpty(file);
        
        validateSize(file);

        String detectedContentType = detectContentType(file);

        validateContentType(detectedContentType);
        
        validateExtension(file.getOriginalFilename(),detectedContentType);

//        fileExtensionValidator.validate(
//                file.getOriginalFilename(),
//                detectedContentType
//        );

        return detectedContentType;
    }

    private void validateNotEmpty(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException(
                    "File must not be empty."
            );
        }
    }
    
    private void validateSize(MultipartFile file) {

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(
                    "File exceeds the maximum allowed size of 10 MB."
            );
        }
    }

    private String detectContentType(MultipartFile file) {

        try (InputStream inputStream = file.getInputStream()) {

            return tika.detect(inputStream, file.getOriginalFilename());

        } catch (IOException exception) {

            throw new BadRequestException(
                    "Unable to determine file content type."
            );
        }
    }

    private void validateContentType(String detectedContentType) {

        if (!ALLOWED_CONTENT_TYPES.contains(detectedContentType)) {

            throw new BadRequestException(
                    "File content type is not allowed: "
                            + detectedContentType
            );
        }
    }
    
    private void validateExtension(String filename,String detectedContentType) {

        String extension = extractExtension(filename);

        if (extension.isBlank()) {
            throw new BadRequestException(
                    "File must have a valid extension."
            );
        }

        Set<String> allowedExtensions =
                ALLOWED_EXTENSIONS_BY_CONTENT_TYPE.get(detectedContentType);

        if (allowedExtensions == null || !allowedExtensions.contains(extension)) {

            throw new BadRequestException(
                    "File extension does not match its actual content."
            );
        }
    }
    
    private String extractExtension(String filename) {

        if (filename == null || filename.isBlank()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');

        if (lastDotIndex <= 0 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}
