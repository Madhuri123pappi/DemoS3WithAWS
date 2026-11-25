package com.example.DemoS3WithAWS.controller;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.DemoS3WithAWS.service.s3Service;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequestMapping("/api/s3")
public class s3Controller {

    private final s3Service s3Service;

    public s3Controller(s3Service s3Service) {
        this.s3Service = s3Service;
    }

    // Upload file
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file) throws Exception {

        String key = s3Service.uploadFile(file);

        return ResponseEntity.ok(
                Map.of(
                        "message", "File uploaded successfully",
                        "s3Key", key
                )
        );
    }

    // Download file
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(
            @RequestParam String key) {

        ResponseBytes<GetObjectResponse> object =
                s3Service.downloadFile(key);

        byte[] fileContent = object.asByteArray();

        String contentType = object.response().contentType();

        MediaType mediaType = (contentType == null || contentType.isBlank())
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(contentType);

        String fileName = key.substring(key.lastIndexOf('/') + 1);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(fileContent.length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileName, StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(fileContent);
    }
}