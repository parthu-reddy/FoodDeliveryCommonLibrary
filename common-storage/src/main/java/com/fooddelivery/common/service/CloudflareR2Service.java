package com.fooddelivery.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@ConditionalOnExpression("!'${r2.endpoint:}'.isEmpty()")
@lombok.extern.slf4j.Slf4j
@lombok.RequiredArgsConstructor
public class CloudflareR2Service {
    private final S3Client s3Client;
    private final software.amazon.awssdk.services.s3.presigner.S3Presigner s3Presigner;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.document-bucket-name:}")
    private String documentBucketName;

    @Value("${r2.public-url}")
    private String publicUrlBase;

    /**
     * Uploads an image to Cloudflare R2
     *
     * @param imageBytes The image byte array
     * @param folder The folder name (e.g., brandId or userId)
     * @param fileName The generated file name (e.g., uuid.png)
     * @param contentType The MIME type (e.g., image/png)
     * @return The public URL to access the uploaded image
     */
    public String uploadImage(
            byte[] imageBytes, String folder, String fileName, String contentType) {
        String key = folder + "/" + fileName;
        log.info(
                "Uploading image to R2: bucket={}, key={}, size={}, contentType={}",
                bucketName,
                key,
                imageBytes.length,
                contentType);
        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType)
                        .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));
        // Construct the public URL
        String publicUrl =
                publicUrlBase.endsWith("/") ? publicUrlBase + key : publicUrlBase + "/" + key;
        log.info("Successfully uploaded image to R2. Public URL: {}", publicUrl);
        return publicUrl;
    }

    /** KYC uploads bind the declared length as well as MIME type into the actual SDK request. */
    public java.net.URL generatePresignedUploadUrl(
            String objectKey,
            String contentType,
            long contentLength,
            java.time.Duration expiration) {
        if (contentLength <= 0 || contentLength > 5L * 1024 * 1024) {
            throw new IllegalArgumentException("Document size must be between 1 byte and 5 MB");
        }
        PutObjectRequest objectRequest =
                PutObjectRequest.builder()
                        .bucket(requireDocumentBucket())
                        .key(objectKey)
                        .contentType(contentType)
                        .contentLength(contentLength)
                        .build();
        var request =
                software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .putObjectRequest(objectRequest)
                        .build();
        return s3Presigner.presignPutObject(request).url();
    }

    /** Always verify storage metadata before accepting a private document reference. */
    public UploadedObject requireUploadedObject(
            String objectKey, String expectedContentType, long expectedLength) {
        String documentBucket = requireDocumentBucket();
        final software.amazon.awssdk.services.s3.model.HeadObjectResponse object;
        try {
            object =
                    s3Client.headObject(
                            software.amazon.awssdk.services.s3.model.HeadObjectRequest.builder()
                                    .bucket(documentBucket)
                                    .key(objectKey)
                                    .build());
        } catch (software.amazon.awssdk.services.s3.model.S3Exception failure) {
            if (failure.statusCode() == 404) {
                throw new IllegalArgumentException(
                        "Upload the document before submitting its reference");
            }
            throw new DocumentStorageUnavailableException();
        } catch (software.amazon.awssdk.core.exception.SdkClientException failure) {
            throw new DocumentStorageUnavailableException();
        }
        if (object == null || object.contentLength() == null || object.eTag() == null) {
            throw new DocumentStorageUnavailableException();
        }
        if (expectedLength <= 0
                || expectedLength > 5L * 1024 * 1024
                || object.contentLength() != expectedLength
                || !java.util.Objects.equals(object.contentType(), expectedContentType)) {
            throw new IllegalArgumentException(
                    "Uploaded document type or size does not match the upload request");
        }
        return new UploadedObject(object.contentLength(), object.contentType(), object.eTag());
    }

    public record UploadedObject(long contentLength, String contentType, String eTag) {}

    /**
     * Finalized KYC objects never have a browser PUT capability; replay cannot replace a reviewed
     * file.
     */
    public void finalizeDocument(String uploadKey, String finalKey, String expectedEtag) {
        String documentBucket = requireDocumentBucket();
        try {
            s3Client.copyObject(
                    software.amazon.awssdk.services.s3.model.CopyObjectRequest.builder()
                            .copySource(documentBucket + "/" + uploadKey)
                            .copySourceIfMatch(expectedEtag)
                            .destinationBucket(documentBucket)
                            .destinationKey(finalKey)
                            .build());
        } catch (software.amazon.awssdk.services.s3.model.S3Exception failure) {
            if (failure.statusCode() == 412) {
                throw new IllegalArgumentException("Document changed; request a new upload");
            }
            throw new DocumentStorageUnavailableException();
        } catch (software.amazon.awssdk.core.exception.SdkClientException failure) {
            throw new DocumentStorageUnavailableException();
        }
    }

    /** Omits the SDK exception/response: it may contain credentials or provider request details. */
    public static class DocumentStorageUnavailableException extends RuntimeException {
        public DocumentStorageUnavailableException() {
            super("Document storage is temporarily unavailable");
        }
    }

    /** KYC storage must be configured independently of the public asset bucket. */
    private String requireDocumentBucket() {
        if (documentBucketName == null
                || documentBucketName.isBlank()
                || documentBucketName.equals(bucketName)) {
            throw new DocumentStorageUnavailableException();
        }
        return documentBucketName;
    }

    public java.net.URL generatePresignedDocumentDownloadUrl(
            String objectKey, java.time.Duration expiration) {
        var objectRequest =
                software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                        .bucket(requireDocumentBucket())
                        .key(objectKey)
                        .build();
        var request =
                software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .getObjectRequest(objectRequest)
                        .build();
        return s3Presigner.presignGetObject(request).url();
    }

    public java.net.URL generatePresignedDownloadUrl(
            String objectKey, java.time.Duration expiration) {
        log.info(
                "Generating presigned download URL for bucket: {}, key: {}", bucketName, objectKey);
        software.amazon.awssdk.services.s3.model.GetObjectRequest objectRequest =
                software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();
        software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest presignRequest =
                software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .getObjectRequest(objectRequest)
                        .build();
        return s3Presigner.presignGetObject(presignRequest).url();
    }
}
