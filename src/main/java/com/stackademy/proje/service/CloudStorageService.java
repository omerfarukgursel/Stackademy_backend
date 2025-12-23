package com.stackademy.proje.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CloudStorageService {

    // Kendi Bucket ismini buraya tekrar yazmayı unutma!
    private final String BUCKET_NAME = "stackfile";
    private final FFmpegService ffmpegService;

    public CloudStorageService(FFmpegService ffmpegService) {
        this.ffmpegService = ffmpegService;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        boolean isVideo = isVideoFile(file);

        Storage storage;

        // 1. Dosya yollarını kontrol et (Local Development)
        // Bazen 'user.dir' beklediğimiz yer olmayabilir, bu yüzden kesin yolları da
        // deneyelim
        Path keyPath = Paths.get("gcp-key.json");
        Path absoluteKeyPath = keyPath.toAbsolutePath();

        System.out.println("GCP Key aranıyor: " + absoluteKeyPath);

        if (Files.exists(keyPath)) {
            System.out.println("GCP Key BULUNDU! Dosyadan okunuyor...");
            storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(keyPath.toFile())))
                    .build()
                    .getService();
        } else {
            // 2. Dosya yoksa "Default Credentials" kullan (Cloud Run / Production)
            System.out.println("GCP Key yerelde BULUNAMADI. Default Credentials deneniyor...");
            storage = StorageOptions.getDefaultInstance().getService();
        }

        byte[] fileBytes = file.getBytes();

        // Process video files with FFmpeg
        if (isVideo && ffmpegService.isFfmpegAvailable()) {
            System.out.println("Video detected, processing with FFmpeg...");
            fileBytes = processVideoWithFFmpeg(file);
            if (fileBytes == null) {
                System.err.println("FFmpeg processing failed, uploading original file");
                fileBytes = file.getBytes();
            } else {
                System.out.println("FFmpeg processing successful");
            }
        }

        BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, fileBytes);

        return "https://storage.googleapis.com/" + BUCKET_NAME + "/" + fileName;
    }

    /**
     * Generate a Signed URL for direct client-side upload to GCS.
     * This bypasses the 32MB Cloud Run limit.
     */
    public Map<String, String> generateSignedUploadUrl(String originalFileName, String contentType) throws IOException {
        String fileName = UUID.randomUUID().toString() + "-" + originalFileName;

        // Credentials'i al
        GoogleCredentials credentials = getCredentials();
        Storage storage = getStorageClient();

        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, fileName))
                .setContentType(contentType)
                .build();

        URL signedUrl;

        // ServiceAccountCredentials ile imzala (Cloud Run için gerekli)
        if (credentials instanceof ServiceAccountCredentials) {
            signedUrl = storage.signUrl(
                    blobInfo,
                    15,
                    TimeUnit.MINUTES,
                    Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                    Storage.SignUrlOption.withContentType(),
                    Storage.SignUrlOption.signWith((ServiceAccountCredentials) credentials));
        } else {
            // Default credentials - bu Cloud Run'da çalışmayabilir ama deneyeceğiz
            signedUrl = storage.signUrl(
                    blobInfo,
                    15,
                    TimeUnit.MINUTES,
                    Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                    Storage.SignUrlOption.withContentType());
        }

        Map<String, String> response = new HashMap<>();
        response.put("signedUrl", signedUrl.toString());
        response.put("publicUrl", "https://storage.googleapis.com/" + BUCKET_NAME + "/" + fileName);
        response.put("fileName", fileName);

        return response;
    }

    /**
     * Get credentials
     */
    private GoogleCredentials getCredentials() throws IOException {
        Path keyPath = Paths.get("gcp-key.json");
        if (Files.exists(keyPath)) {
            return GoogleCredentials.fromStream(new FileInputStream(keyPath.toFile()));
        } else {
            return GoogleCredentials.getApplicationDefault();
        }
    }

    /**
     * Helper to get Storage client (DRY)
     */
    private Storage getStorageClient() throws IOException {
        Path keyPath = Paths.get("gcp-key.json");
        if (Files.exists(keyPath)) {
            return StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(keyPath.toFile())))
                    .build()
                    .getService();
        } else {
            return StorageOptions.getDefaultInstance().getService();
        }
    }

    // Dosya silme metodu
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // URL'den dosya adını çıkar
            // Format: https://storage.googleapis.com/BUCKET_NAME/filename
            String prefix = "https://storage.googleapis.com/" + BUCKET_NAME + "/";
            if (!fileUrl.startsWith(prefix)) {
                System.out.println("Geçersiz dosya URL'si: " + fileUrl);
                return;
            }

            String fileName = fileUrl.substring(prefix.length());

            Storage storage;
            Path keyPath = Paths.get("gcp-key.json");

            if (Files.exists(keyPath)) {
                storage = StorageOptions.newBuilder()
                        .setCredentials(GoogleCredentials.fromStream(new FileInputStream(keyPath.toFile())))
                        .build()
                        .getService();
            } else {
                storage = StorageOptions.getDefaultInstance().getService();
            }

            BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                System.out.println("Dosya silindi: " + fileName);
            } else {
                System.out.println("Dosya bulunamadı veya silinemedi: " + fileName);
            }
        } catch (Exception e) {
            System.out.println("Dosya silme hatası: " + e.getMessage());
        }
    }

    /**
     * Check if uploaded file is a video
     */
    private boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        if (contentType != null && contentType.startsWith("video/")) {
            return true;
        }

        if (fileName != null) {
            String lowerFileName = fileName.toLowerCase();
            return lowerFileName.endsWith(".mp4") ||
                    lowerFileName.endsWith(".mov") ||
                    lowerFileName.endsWith(".avi") ||
                    lowerFileName.endsWith(".mkv") ||
                    lowerFileName.endsWith(".webm");
        }

        return false;
    }

    /**
     * Process video with FFmpeg - fix metadata
     * Returns processed file bytes or null if failed
     */
    private byte[] processVideoWithFFmpeg(MultipartFile file) {
        Path tempDir = null;
        try {
            // Create temp directory
            tempDir = Files.createTempDirectory("video-processing-");
            Path inputPath = tempDir.resolve("input-" + file.getOriginalFilename());
            Path outputPath = tempDir.resolve("output-" + file.getOriginalFilename());

            // Save uploaded file to temp input
            Files.write(inputPath, file.getBytes());

            // Process with FFmpeg
            boolean success = ffmpegService.processVideo(
                    inputPath.toFile(),
                    outputPath.toFile());

            if (!success) {
                return null;
            }

            // Read processed file
            byte[] processedBytes = Files.readAllBytes(outputPath);
            return processedBytes;

        } catch (Exception e) {
            System.err.println("Video processing error: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            // Clean up temp files
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                            .sorted((a, b) -> -a.compareTo(b)) // Delete files before dirs
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    System.err.println("Failed to delete: " + path);
                                }
                            });
                } catch (IOException e) {
                    System.err.println("Temp cleanup error: " + e.getMessage());
                }
            }
        }
    }
}