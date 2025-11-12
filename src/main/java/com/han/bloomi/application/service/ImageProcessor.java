package com.han.bloomi.application.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 이미지 최적화 서비스
 * - 리사이즈: 최대 1920x1080 (비율 유지)
 * - 압축: JPEG 품질 85%
 * - 목표: S3 저장 비용 절감 + Vision API 속도/비용 최적화
 */
@Slf4j
@Service
public class ImageProcessor {
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;
    private static final double QUALITY = 0.85;
    private static final long MAX_FILE_SIZE = 1024 * 1024; // 1MB

    /**
     * 이미지를 최적화합니다 (리사이즈 + 압축)
     * @param originalFile 원본 이미지
     * @return 최적화된 이미지
     */
    public MultipartFile optimize(MultipartFile originalFile) throws IOException {
        long originalSize = originalFile.getSize();

        // 1. 원본 이미지 읽기
        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(originalFile.getInputStream());
            if (originalImage == null) {
                log.warn("Failed to read image, returning original: {}", originalFile.getOriginalFilename());
                return originalFile;
            }
        } catch (Exception e) {
            log.error("Error reading image, returning original", e);
            return originalFile;
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // 2. 최적화 필요 여부 확인
        if (originalWidth <= MAX_WIDTH && originalHeight <= MAX_HEIGHT
            && originalSize <= MAX_FILE_SIZE) {
            log.info("Image already optimized: {}x{}, size: {}KB",
                    originalWidth, originalHeight, originalSize / 1024);
            return originalFile;
        }

        // 3. 리사이즈 + 압축
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(originalImage)
                    .size(MAX_WIDTH, MAX_HEIGHT)  // 최대 크기 (비율 유지)
                    .outputFormat("jpg")
                    .outputQuality(QUALITY)
                    .toOutputStream(outputStream);

            byte[] optimizedBytes = outputStream.toByteArray();
            long optimizedSize = optimizedBytes.length;

            log.info("Image optimized: {}x{} ({}KB) -> optimized ({}KB), reduction: {}%",
                    originalWidth, originalHeight,
                    originalSize / 1024, optimizedSize / 1024,
                    (int) (100 - (optimizedSize * 100.0 / originalSize)));

            // 4. MultipartFile로 변환
            String originalFilename = originalFile.getOriginalFilename();
            String optimizedFilename = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(0, originalFilename.lastIndexOf(".")) + ".jpg"
                    : "optimized.jpg";

            return new OptimizedMultipartFile(
                    originalFile.getName(),
                    optimizedFilename,
                    "image/jpeg",
                    optimizedBytes
            );
        } catch (Exception e) {
            log.error("Error optimizing image, returning original", e);
            return originalFile;
        }
    }
}