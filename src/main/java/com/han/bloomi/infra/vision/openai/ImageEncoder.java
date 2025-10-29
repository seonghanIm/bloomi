package com.han.bloomi.infra.vision.openai;

import com.han.bloomi.common.error.ErrorCode;
import com.han.bloomi.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

/**
 * 이미지를 Base64로 인코딩하는 유틸리티
 */
@Slf4j
@Component
public class ImageEncoder {
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB

    public String encodeToBase64(MultipartFile file) {
        validateImage(file);

        try {
            byte[] bytes = file.getBytes();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            log.error("Failed to encode image to base64", e);
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FORMAT, "Failed to read image file");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.MISSING_REQUIRED_FIELD, "Image file is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PAYLOAD_TOO_LARGE,
                    String.format("Image size %d bytes exceeds limit %d bytes", file.getSize(), MAX_FILE_SIZE));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                    "Only image files are supported");
        }
    }
}