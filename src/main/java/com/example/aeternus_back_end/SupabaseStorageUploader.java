package com.example.aeternus_back_end;
//test class
import java.io.File;
import java.net.URI;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class SupabaseStorageUploader {

    // Replace these variables with the details obtained from your Supabase Dashboard
    private static final String SUPABASE_ENDPOINT = "https://ahruhkivkdqrxttpbvaj.storage.supabase.co/storage/v1/s3";
    private static final String SUPABASE_REGION = "eu-west-1";
    private static final String ACCESS_KEY = "61b438d92396b5d6b35c8002d0582240";
    private static final String SECRET_KEY = "f7d88a76145f979c0fea6b4a409e4fc4096a3e2044f4b0eca670109fd60d74c9";

    private static final String BUCKET_NAME = "aeternus-image-video";
    private static final String LOCAL_IMAGE_PATH = "C:/Users/support/Pictures/Saved Pictures/face.jpg/"; // Path on your machine
    private static final String BUCKET_DESTINATION_PATH = "uploads/my_uploaded_pic.jpg";

    public static void main(String[] args) {

        // 1. Set up static credentials
        AwsBasicCredentials credentials = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);

        // 2. Build the S3 Client specifically configured for Supabase Custom Endpoint
        try (S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(SUPABASE_ENDPOINT))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(SUPABASE_REGION))
                // Crucial step: Supabase uses path-style URLs (endpoint/bucket/file)
                .forcePathStyle(true)
                .build()) {

            File fileToUpload = new File(LOCAL_IMAGE_PATH);
            if (!fileToUpload.exists()) {
                System.err.println("Error: Local file does not exist at path: " + LOCAL_IMAGE_PATH);
                return;
            }

            System.out.println("Starting image upload to Supabase...");

            // 3. Prepare the upload payload metadata
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(BUCKET_DESTINATION_PATH)
                    .contentType("image/jpeg") // Adjust MIME type depending on your file extension
                    .build();

            // 4. Send the file over to your Supabase bucket
            PutObjectResponse response = s3Client.putObject(putObjectRequest, fileToUpload.toPath());

            System.out.println("✅ Image uploaded successfully!");
            System.out.println("ETag identifier from Supabase: " + response.eTag());

        } catch (Exception e) {
            System.err.println("❌ Upload failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

