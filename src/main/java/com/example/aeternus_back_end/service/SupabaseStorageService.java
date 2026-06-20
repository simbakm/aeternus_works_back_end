package com.example.aeternus_back_end.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;

    @Value("${supabase.s3.bucket:aeternus}")
    private String bucketName;

    @Value("${supabase.s3.endpoint:}")
    private String s3Endpoint;

    @Value("${supabase.s3.region:}")
    private String s3Region;

    @Value("${supabase.s3.access-key:}")
    private String s3AccessKey;

    @Value("${supabase.s3.secret-key:}")
    private String s3SecretKey;

    @Value("${supabase.s3.default-path:}")
    private String s3DefaultPath;

    public String uploadFile(MultipartFile file) throws IOException {
        ensureS3Configured();

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
        String key = (s3DefaultPath != null ? s3DefaultPath : "") + filename;

        AwsBasicCredentials credentials = AwsBasicCredentials.create(s3AccessKey, s3SecretKey);
        try (S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(s3Endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(s3Region == null || s3Region.isBlank() ? "us-east-1" : s3Region))
                .forcePathStyle(true)
                .build()) {

            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .build();

            s3Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + key;
    }

    public String uploadBase64Image(String base64Image) {
        if (base64Image == null || !base64Image.startsWith("data:image/")) {
            return base64Image; // Return original if not a base64 image or if it's already a URL
        }

        try {
            // Extract MIME type and extension
            int commaIndex = base64Image.indexOf(',');
            String prefix = base64Image.substring(0, commaIndex);
            String mimeType = prefix.substring(prefix.indexOf(':') + 1, prefix.indexOf(';'));
            String extension = mimeType.split("/")[1];
            
            if (extension == null || extension.isEmpty()) {
                extension = "png";
            }

            // Decode base64 data
            String base64Data = base64Image.substring(commaIndex + 1);
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);

            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "." + extension;

            ensureS3Configured();

            String key = (s3DefaultPath != null ? s3DefaultPath : "") + filename;

            AwsBasicCredentials credentials = AwsBasicCredentials.create(s3AccessKey, s3SecretKey);
            try (S3Client s3Client = S3Client.builder()
                    .endpointOverride(URI.create(s3Endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of(s3Region == null || s3Region.isBlank() ? "us-east-1" : s3Region))
                    .forcePathStyle(true)
                    .build()) {

                PutObjectRequest putReq = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(mimeType)
                        .build();

                s3Client.putObject(putReq, RequestBody.fromBytes(imageBytes));
            }

            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + key;
        } catch (Exception e) {
            throw new RuntimeException("Exception while uploading to Supabase S3: " + e.getMessage(), e);
        }
    }

    private void ensureS3Configured() {
        if (s3AccessKey == null || s3AccessKey.isBlank() || s3SecretKey == null || s3SecretKey.isBlank() || s3Endpoint == null || s3Endpoint.isBlank()) {
            throw new IllegalStateException("Supabase S3 credentials (supabase.s3.access-key / secret-key / endpoint) are not configured in application.properties");
        }

        if (supabaseUrl == null || supabaseUrl.isBlank()) {
            // Try to derive a usable supabase base URL from the S3 endpoint when not explicitly provided.
            try {
                URI uri = URI.create(s3Endpoint);
                StringBuilder sb = new StringBuilder();
                sb.append(uri.getScheme()).append("://").append(uri.getHost());
                if (uri.getPort() != -1) {
                    sb.append(":").append(uri.getPort());
                }
                supabaseUrl = sb.toString();
            } catch (Exception e) {
                throw new IllegalStateException("supabase.url must be configured to construct public URLs");
            }
        }
    }
}
