package com.campuspe.soc2_readiness_manager.controller;

import com.campuspe.soc2_readiness_manager.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File Attachments", description = "Endpoints for uploading and downloading evidence files")
public class FileAttachmentController {

    private final FileStorageService fileStorageService;

    public FileAttachmentController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "Upload a file", description = "Uploads a file and returns its generated UUID filename")
    @ApiResponse(responseCode = "200", description = "File successfully uploaded")
    @ApiResponse(responseCode = "400", description = "Invalid file type or size exceeded")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);
        
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Download a file", description = "Downloads a previously uploaded file by its UUID filename")
    @ApiResponse(responseCode = "200", description = "File successfully downloaded")
    @ApiResponse(responseCode = "404", description = "File not found")
    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(id);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Default content type if type could not be determined
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
