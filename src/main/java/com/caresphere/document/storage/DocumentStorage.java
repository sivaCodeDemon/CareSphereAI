package com.caresphere.document.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Abstraction over document storage so the local filesystem can be swapped for S3 in the
 * deployment milestone without touching the service layer.
 */
public interface DocumentStorage {

    /** Persists the uploaded file and returns a storage key (relative path / S3 key). */
    String store(Long patientId, MultipartFile file) throws IOException;

    /** Loads the raw bytes for a previously stored file by its storage key. */
    byte[] load(String storageKey) throws IOException;
}
