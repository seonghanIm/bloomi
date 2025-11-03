package com.han.bloomi.application.service;

import com.han.bloomi.domain.port.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 이미지 업로드 공통 서비스
 * S3 업로드 로직을 캡슐화하여 재사용 가능하게 함
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {
    private final ImageStorage imageStorage;

    /**
     * 식단 이미지 업로드
     *
     * @param image    업로드할 이미지
     * @param userId   사용자 ID
     * @param category 카테고리 (예: "meals", "profile", "etc")
     * @return 업로드된 이미지 URL
     */
    public String uploadImage(MultipartFile image, String userId, String category) {
        String imagePath = generateImagePath(userId, category, image.getOriginalFilename());
        String imageUrl = imageStorage.upload(image, imagePath);
        log.info("Image uploaded - userId: {}, category: {}, url: {}", userId, category, imageUrl);
        return imageUrl;
    }

    /**
     * 식단 이미지 업로드 (간편 메서드)
     */
    public String uploadMealImage(MultipartFile image, String userId) {
        return uploadImage(image, userId, "meals");
    }

    /**
     * 프로필 이미지 업로드
     */
    public String uploadProfileImage(MultipartFile image, String userId) {
        return uploadImage(image, userId, "profiles");
    }

    /**
     * 이미지 삭제
     *
     * @param imageUrl 삭제할 이미지 URL
     */
    public void deleteImage(String imageUrl) {
        imageStorage.delete(imageUrl);
        log.info("Image deleted - url: {}", imageUrl);
    }

    /**
     * 이미지 경로 생성
     * 패턴: {category}/{userId}/{yyyy/MM/dd}/{uuid}.{ext}
     * 예: meals/user-123/2025/11/03/uuid-abc.jpg
     */
    private String generateImagePath(String userId, String category, String originalFilename) {
        LocalDateTime now = LocalDateTime.now();
        String datePrefix = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String filename = UUID.randomUUID() + getFileExtension(originalFilename);
        return String.format("%s/%s/%s/%s", category, userId, datePrefix, filename);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null) return ".jpg";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : ".jpg";
    }
}