package com.fooddelivery.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudflareR2Service {

    private final S3Client s3Client;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.public-url}")
    private String publicUrlBase;

    /**
     * Uploads an image to Cloudflare R2
     *
     * @param imageBytes  The image byte array
     * @param folder      The folder name (e.g., brandId or userId)
     * @param fileName    The generated file name (e.g., uuid.png)
     * @param contentType The MIME type (e.g., image/png)
     * @return The public URL to access the uploaded image
     */
    public String uploadImage(byte[] imageBytes, String folder, String fileName, String contentType) {
        String key = folder + "/" + fileName;

        log.info("Uploading image to R2: bucket={}, key={}, size={}, contentType={}", bucketName, key, imageBytes.length, contentType);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));

        // Construct the public URL
        String publicUrl = publicUrlBase.endsWith("/") ? publicUrlBase + key : publicUrlBase + "/" + key;
        log.info("Successfully uploaded image to R2. Public URL: {}", publicUrl);

        return publicUrl;
    }
}
