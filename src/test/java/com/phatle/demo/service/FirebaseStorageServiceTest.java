package com.phatle.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@ExtendWith(MockitoExtension.class)
public class FirebaseStorageServiceTest {

    @Mock
    private Environment env;

    @Mock
    private Storage storage;

    @Mock
    private Bucket bucket;

    @Mock
    private Blob blob;

    private MockedStatic<GoogleCredentials> credentialsSM;
    private MockedStatic<StorageOptions> storageOptionsSM;
    private MockedConstruction<FileInputStream> fileInputStreamCM;

    private FirebaseStorageService firebaseStorageService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(env.getProperty("FIREBASE_BUCKET_NAME")).thenReturn("test-bucket");
        when(env.getProperty("PRESIGNED_URL_DURATION_IN_MIN")).thenReturn("15");
        when(env.getProperty("GOOGLE_APPLICATION_CREDENTIALS")).thenReturn("path/to/credentials.json");

        var credentials = mock(GoogleCredentials.class);
        var storageOptions = mock(StorageOptions.class);
        // Mock construction for not to call the real constructor
        fileInputStreamCM = mockConstruction(FileInputStream.class);
        credentialsSM = mockStatic(GoogleCredentials.class);
        storageOptionsSM = mockStatic(StorageOptions.class);

        credentialsSM
                .when(() -> GoogleCredentials.fromStream(any(FileInputStream.class)))
                .thenReturn(credentials);

        var builder = mock(StorageOptions.Builder.class);
        storageOptionsSM.when(StorageOptions::newBuilder).thenReturn(builder);
        when(builder.setCredentials(credentials)).thenReturn(builder);
        when(builder.build()).thenReturn(storageOptions);
        when(storageOptions.getService()).thenReturn(storage);

        firebaseStorageService = new FirebaseStorageService(env);
    }

    @AfterEach
    public void tearDown() {
        credentialsSM.close();
        storageOptionsSM.close();
        fileInputStreamCM.close();
    }

    @Test
    public void testGenerateDownloadUrl() {
        var uniqueFileName = "test-file.jpg";

        when(storage.get("test-bucket")).thenReturn(bucket);
        when(bucket.get(uniqueFileName)).thenReturn(blob);
        when(blob.signUrl(15, TimeUnit.MINUTES)).thenReturn(mock(URL.class));

        var url = firebaseStorageService.generateDownloadUrl(uniqueFileName);

        assertNotNull(url);
        verify(storage).get("test-bucket");
        verify(bucket).get(uniqueFileName);
        verify(blob).signUrl(15, TimeUnit.MINUTES);
    }

    @Test
    public void testGenerateDownloadUrl_FileNotFound() {
        var uniqueFileName = "non-existent-file.jpg";

        when(storage.get("test-bucket")).thenReturn(bucket);
        when(bucket.get(uniqueFileName)).thenReturn(null);

        var exception = assertThrows(ResponseStatusException.class, () -> {
            firebaseStorageService.generateDownloadUrl(uniqueFileName);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("File not found: " + uniqueFileName, exception.getReason());
    }
}
