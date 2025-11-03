package com.han.bloomi.infra.storage;

import com.han.bloomi.domain.port.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

/**
 * S3 기반 이미지 저장소 어댑터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3ImageStorageAdapter implements ImageStorage {
    private final S3Properties s3Properties;
    private final S3Client s3Client;

    @Override
    public String upload(MultipartFile image, String path) {
        try {
            log.info("Uploading image to S3: bucket={}, path={}", s3Properties.getBucket(), path);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(path)
                    .contentType(image.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(image.getInputStream(), image.getSize()));

            String imageUrl = s3Properties.getBaseUrl() + "/" + path;
            log.info("Image uploaded successfully: {}", imageUrl);

            return imageUrl;
        } catch (IOException e) {
            log.error("Failed to upload image to S3", e);
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    @Override
    public void delete(String imageUrl) {
        try {
            String key = imageUrl.replace(s3Properties.getBaseUrl() + "/", "");
            log.info("Deleting image from S3: bucket={}, key={}", s3Properties.getBucket(), key);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Image deleted successfully: {}", imageUrl);
        } catch (Exception e) {
            log.error("Failed to delete image from S3: {}", imageUrl, e);
            throw new RuntimeException("Failed to delete image", e);
        }
    }
}