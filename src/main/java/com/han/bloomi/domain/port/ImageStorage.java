package com.han.bloomi.domain.port;

import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 저장소 포트
 * S3, GCS, Azure Blob 등 다양한 구현체로 교체 가능
 */
public interface ImageStorage {
    /**
     * 이미지 업로드
     *
     * @param image 업로드할 이미지 파일
     * @param path  저장 경로 (예: "meals/2025/11/03/uuid.jpg")
     * @return 업로드된 이미지의 공개 URL
     */
    String upload(MultipartFile image, String path);

    /**
     * 이미지 삭제
     *
     * @param imageUrl 삭제할 이미지 URL
     */
    void delete(String imageUrl);
}