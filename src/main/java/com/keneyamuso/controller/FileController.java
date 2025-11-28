package com.keneyamuso.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller pour servir les fichiers uploadés (vidéos, images, etc.)
 */
@RestController
@RequestMapping("/uploads")
public class FileController {

    /**
     * Sert un fichier uploadé
     * Permet la lecture en streaming des vidéos et l'affichage des images
     */
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get("./uploads").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Déterminer le type MIME basé sur l'extension
                String contentType = determineContentType(fileName);
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes") // Support du streaming pour les vidéos
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Détermine le type MIME basé sur l'extension du fichier
     */
    private String determineContentType(String fileName) {
        String extension = fileName.toLowerCase();
        
        if (extension.endsWith(".mp4")) {
            return "video/mp4";
        } else if (extension.endsWith(".mpeg") || extension.endsWith(".mpg")) {
            return "video/mpeg";
        } else if (extension.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (extension.endsWith(".mov")) {
            return "video/quicktime";
        } else if (extension.endsWith(".wmv")) {
            return "video/x-ms-wmv";
        } else if (extension.endsWith(".flv")) {
            return "video/x-flv";
        } else if (extension.endsWith(".webm")) {
            return "video/webm";
        } else if (extension.endsWith(".mkv")) {
            return "video/x-matroska";
        } else if (extension.endsWith(".m4v")) {
            return "video/x-m4v";
        } else if (extension.endsWith(".3gp")) {
            return "video/3gpp";
        } else if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (extension.endsWith(".png")) {
            return "image/png";
        } else if (extension.endsWith(".gif")) {
            return "image/gif";
        } else if (extension.endsWith(".webp")) {
            return "image/webp";
        } else if (extension.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (extension.endsWith(".wav")) {
            return "audio/wav";
        } else if (extension.endsWith(".m4a")) {
            return "audio/mp4";
        } else {
            return "application/octet-stream";
        }
    }
}

