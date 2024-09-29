package com.phatle.demo.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Service
public class FirebaseStorageService {
    private final Storage storage;

    private static String BUCKET_NAME;
    private static int DURATION;
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("image/jpeg", "image/png", "application/pdf");

    public FirebaseStorageService(Environment env) {
        BUCKET_NAME = env.getProperty("FIREBASE_BUCKET_NAME");
        DURATION = Integer.parseInt(env.getProperty("PRESIGNED_URL_DURATION_IN_MIN"));
        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new FileInputStream(env.getProperty("GOOGLE_APPLICATION_CREDENTIALS")));
            storage = StorageOptions.newBuilder().setCredentials(credentials)
                    .build().getService();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String generateDownloadUrl(String uniqueFileName) {
        Bucket bucket = storage.get(BUCKET_NAME);
        Blob blob = bucket.get(uniqueFileName);

        if (blob == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found: " + uniqueFileName);
        }

        URL url = blob.signUrl(DURATION, TimeUnit.MINUTES);
        return url.toString();
    }

    public String uploadFile(MultipartFile file) {
        if (!isFileTypeAllowed(file.getContentType())) {
            throw new IllegalArgumentException("File type not allowed.");
        }

        String fileName = file.getOriginalFilename();
        String uniqueFileName = generateUniqueFileName(fileName);

        BlobId blobId = BlobId.of(BUCKET_NAME, uniqueFileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();

        try {
            storage.create(blobInfo, file.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed file");
        }

        return uniqueFileName;
    }

    private boolean isFileTypeAllowed(String contentType) {
        return ALLOWED_FILE_TYPES.contains(contentType);
    }

    private String generateUniqueFileName(String fileName) {
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex);
            fileName = fileName.substring(0, dotIndex);
        }
        return fileName + "_" + System.currentTimeMillis() + extension;
    }
}
