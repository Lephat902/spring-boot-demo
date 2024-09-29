package com.phatle.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.phatle.demo.service.FirebaseStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FirebaseStorageService storageService;

    @GetMapping("/download-url/{fileName}")
    public String getDownloadUrl(@PathVariable String fileName) {
        return storageService.generateDownloadUrl(fileName);
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadFile(@RequestParam MultipartFile file) {
        String newFileName = storageService.uploadFile(file);
        return newFileName;
    }
}
