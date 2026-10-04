package com.fooddelivery.common.service;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CloudflareR2DocumentSafetyTest {
    private S3Presigner presigner() {
        // Offline SigV4 with explicitly synthetic credentials; never contacts storage.
        return S3Presigner.builder().endpointOverride(URI.create("https://unit-test.r2.cloudflarestorage.com"))
                .region(Region.of("auto")).credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("EXAMPLE", "synthetic-unit-test-secret"))).build();
    }

    private CloudflareR2Service service(S3Client client, S3Presigner presigner) {
        var service = new CloudflareR2Service(client, presigner);
        ReflectionTestUtils.setField(service, "bucketName", "kyc-unit");
        return service;
    }

    @Test void actualPresignerSignsTypeLengthAndTenMinuteExpiry() {
        try (var presigner = presigner()) {
            var url = service(mock(S3Client.class), presigner).generatePresignedUploadUrl(
                    "documents/owned/document.pdf", "application/pdf", 128, Duration.ofMinutes(10));
            String query = URLDecoder.decode(url.getQuery(), StandardCharsets.UTF_8);
            assertTrue(query.contains("X-Amz-Expires=600"));
            assertTrue(query.contains("X-Amz-SignedHeaders=content-length;content-type;host"), query);
            assertTrue(url.getPath().endsWith("documents/owned/document.pdf"));
        }
    }

    @Test void invalidDeclaredLengthsCannotProduceAnUploadCredential() {
        var signer = mock(S3Presigner.class);
        var service = service(mock(S3Client.class), signer);
        for (long size : new long[]{-1, 0, 5L * 1024 * 1024 + 1}) {
            assertThrows(IllegalArgumentException.class, () -> service.generatePresignedUploadUrl(
                    "documents/owned/document.pdf", "application/pdf", size, Duration.ofMinutes(10)));
        }
        verifyNoInteractions(signer);
    }

    @Test void referenceRequiresMatchingActualStorageSizeAndType() {
        S3Client client = mock(S3Client.class);
        var service = service(client, mock(S3Presigner.class));
        when(client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder()
                .contentLength(128L).contentType("application/pdf").eTag("document-etag").build());
        assertEquals(new CloudflareR2Service.UploadedObject(128, "application/pdf", "document-etag"),
                service.requireUploadedObject("documents/owned/document.pdf", "application/pdf", 128));
        assertThrows(IllegalArgumentException.class, () -> service.requireUploadedObject(
                "documents/owned/document.pdf", "application/pdf", 129));
        assertThrows(IllegalArgumentException.class, () -> service.requireUploadedObject(
                "documents/owned/document.pdf", "text/html", 128));
    }

    @Test void missingObjectAndStorageOutageNeverBecomeVerifiedReferences() {
        S3Client client = mock(S3Client.class);
        var service = service(client, mock(S3Presigner.class));
        when(client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(404).message("Missing").build());
        assertThrows(IllegalArgumentException.class, () -> service.requireUploadedObject(
                "documents/owned/document.pdf", "application/pdf", 128));
        when(client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(503).message("provider details must stay private").build());
        var error = assertThrows(CloudflareR2Service.DocumentStorageUnavailableException.class,
                () -> service.requireUploadedObject("documents/owned/document.pdf", "application/pdf", 128));
        assertNull(error.getCause());
        assertFalse(error.getMessage().contains("provider details"));
    }

    @Test void finalizationCopiesOnlyTheConfirmedVersionAndReportsRacesAsRefusals() {
        S3Client client = mock(S3Client.class);
        var service = service(client, mock(S3Presigner.class));
        service.finalizeDocument("documents/owned/uploads/document.pdf", "documents/owned/accepted/document.pdf", "confirmed-etag");
        var request = org.mockito.ArgumentCaptor.forClass(software.amazon.awssdk.services.s3.model.CopyObjectRequest.class);
        verify(client).copyObject(request.capture());
        assertEquals("kyc-unit/documents/owned/uploads/document.pdf", request.getValue().copySource());
        assertEquals("confirmed-etag", request.getValue().copySourceIfMatch());
        assertEquals("documents/owned/accepted/document.pdf", request.getValue().destinationKey());
        when(client.copyObject(any(software.amazon.awssdk.services.s3.model.CopyObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(412).build());
        assertThrows(IllegalArgumentException.class, () -> service.finalizeDocument(
                "documents/owned/uploads/document.pdf", "documents/owned/accepted/document.pdf", "confirmed-etag"));
    }
}
