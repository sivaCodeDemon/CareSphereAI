package com.caresphere.document.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Filesystem-backed implementation used for local development. In production this is replaced
 * by an S3 implementation (deployment milestone); the service layer depends only on
 * {@link DocumentStorage}.
 */
@Component
public class LocalDocumentStorage implements DocumentStorage {

    private final Path root;

    public LocalDocumentStorage(@Value("${caresphere.storage.dir:./caresphere-uploads}") String dir) throws IOException {
        this.root = Paths.get(dir).toAbsolutePath();
        Files.createDirectories(root);
    }

    @Override
    public String store(Long patientId, MultipartFile file) throws IOException {
        Path patientDir = root.resolve(String.valueOf(patientId));
        Files.createDirectories(patientDir);
        String safeName = sanitize(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "-" + safeName;
        file.transferTo(patientDir.resolve(filename).toFile());
        return patientId + "/" + filename;
    }

    @Override
    public byte[] load(String storageKey) throws IOException {
        return Files.readAllBytes(root.resolve(storageKey));
    }

    private String sanitize(String filename) {
        if (filename == null) {
            return "upload";
        }
        return filename.replaceAll("[^a-zA-Z0-9.\\-]", "_");
    }
}
