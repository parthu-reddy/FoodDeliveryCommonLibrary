package com.fooddelivery.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@ConditionalOnExpression("!\'${r2.endpoint:}\'.isEmpty()")
public class CloudflareR2Service {
    @java.lang.SuppressWarnings("all")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CloudflareR2Service.class);
    private final S3Client s3Client;
    private final software.amazon.awssdk.services.s3.presigner.S3Presigner s3Presigner;
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
        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(key).contentType(contentType).build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));
        // Construct the public URL
        String publicUrl = publicUrlBase.endsWith("/") ? publicUrlBase + key : publicUrlBase + "/" + key;
        log.info("Successfully uploaded image to R2. Public URL: {}", publicUrl);
        return publicUrl;
    }

    public java.net.URL generatePresignedUploadUrl(String objectKey, String contentType, java.time.Duration expiration) {
        log.info("Generating presigned upload URL for bucket: {}, key: {}", bucketName, objectKey);
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucketName).key(objectKey).contentType(contentType).build();
        software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest presignRequest = software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest.builder().signatureDuration(expiration).putObjectRequest(objectRequest).build();
        return s3Presigner.presignPutObject(presignRequest).url();
    }

    public java.net.URL generatePresignedDownloadUrl(String objectKey, java.time.Duration expiration) {
        log.info("Generating presigned download URL for bucket: {}, key: {}", bucketName, objectKey);
        software.amazon.awssdk.services.s3.model.GetObjectRequest objectRequest = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();
        software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest presignRequest = software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.builder().signatureDuration(expiration).getObjectRequest(objectRequest).build();
        return s3Presigner.presignGetObject(presignRequest).url();
    }

    @java.lang.SuppressWarnings("all")
    public CloudflareR2Service(final S3Client s3Client, final software.amazon.awssdk.services.s3.presigner.S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }
}
