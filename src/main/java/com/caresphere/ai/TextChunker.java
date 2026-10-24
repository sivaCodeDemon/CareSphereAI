package com.caresphere.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits text into fixed-size character chunks with an overlap so that passages spanning
 * a chunk boundary still appear together in at least one chunk. Character-based chunking is
 * a simple, model-agnostic baseline; a production system might chunk by tokens or sentences.
 */
public final class TextChunker {

    private TextChunker() {
    }

    public static List<String> chunk(String text, int size, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }
        int step = Math.max(1, size - overlap);
        int length = text.length();
        for (int start = 0; start < length; start += step) {
            int end = Math.min(start + size, length);
            String chunk = text.substring(start, end).strip();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            if (end == length) {
                break;
            }
        }
        return chunks;
    }
}
