package com.stackademy.proje.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class CloudStorageService {

    // Kendi Bucket ismini buraya tekrar yazmayı unutma!
    private final String BUCKET_NAME = "stackfile";

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

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

        BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        return "https://storage.googleapis.com/" + BUCKET_NAME + "/" + fileName;
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
}