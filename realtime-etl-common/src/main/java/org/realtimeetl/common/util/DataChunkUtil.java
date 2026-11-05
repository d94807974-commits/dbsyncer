/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Data FactminSnippets
 * For large data volume transferminPiece processing
 *
 * @author The clouds.
 * @version 1.0.0
 */
public class DataChunkUtil {

    private static final Logger logger = LoggerFactory.getLogger(DataChunkUtil.class);

    // DefaultminArea size:1MB（bytes)
    private static final int DEFAULT_CHUNK_SIZE = 1024 * 1024;

    // MaxminArea size:10MB
    private static final int MAX_CHUNK_SIZE = 10 * 1024 * 1024;

    // MinminArea size:100KB
    private static final int MIN_CHUNK_SIZE = 100 * 1024;

    /**
     * 将数据分片
     * 
     * @param data Source data (%2)JSONString)
     * @param chunkSize minArea size (bytes), if<=0UseDefault value
     * @return minSnippetsList
     */
    public static List<DataChunk> chunkData(String data, int chunkSize) {
        if (StringUtil.isBlank(data)) {
            return new ArrayList<>();
        }

        if (chunkSize <= 0) {
            chunkSize = DEFAULT_CHUNK_SIZE;
        }

        // LimitsminArea
        if (chunkSize > MAX_CHUNK_SIZE) {
            chunkSize = MAX_CHUNK_SIZE;
            logger.warn("The fraction size organises more than the maximum, using the maximum: {MB", MAX_CHUNK_SIZE / 1024 / 1024);
        }
        if (chunkSize < MIN_CHUNK_SIZE) {
            chunkSize = MIN_CHUNK_SIZE;
            logger.warn("The fraction size is less than the minimum, using the minimum: {KB", MIN_CHUNK_SIZE / 1024);
        }

        byte[] dataBytes = data.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int totalSize = dataBytes.length;
        int totalChunks = (int) Math.ceil((double) totalSize / chunkSize);

        List<DataChunk> chunks = new ArrayList<>();
        String chunkId = generateChunkId();

        for (int i = 0; i < totalChunks; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, totalSize);
            byte[] chunkBytes = new byte[end - start];
            System.arraycopy(dataBytes, start, chunkBytes, 0, end - start);

            DataChunk chunk = new DataChunk();
            chunk.setChunkId(chunkId);
            chunk.setChunkIndex(i);
            chunk.setTotalChunks(totalChunks);
            chunk.setData(new String(chunkBytes, java.nio.charset.StandardCharsets.UTF_8));
            chunk.setSize(chunkBytes.length);

            chunks.add(chunk);
        }

        logger.info("Data fraction completed, total size: }KB, fraction: {, fraction size: }KB", totalSize / 1024, totalChunks, chunkSize / 1024);

        return chunks;
    }

    /**
     * 将数据分片（使用默认分片大小）
     * 
     * @param data Source data (%2)JSONString)
     * @return minSnippetsList
     */
    public static List<DataChunk> chunkData(String data) {
        return chunkData(data, DEFAULT_CHUNK_SIZE);
    }

    /**
     * MergeminSnippets
     * 
     * @param chunks minSnippetsList
     * @return Combined data
     */
    public static String mergeChunks(List<DataChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "";
        }

        // PressIndexSort
        chunks.sort((a, b)->Integer.compare(a.getChunkIndex(), b.getChunkIndex()));

        // AuthenticationminArea Integrity
        String chunkId = chunks.get(0).getChunkId();
        int totalChunks = chunks.get(0).getTotalChunks();

        if (chunks.size() != totalChunks) {
            throw new RuntimeException(String.format("Part incomplete, expected: %d, actual: %d", totalChunks, chunks.size()));
        }

        for (int i = 0; i < chunks.size(); i++) {
            DataChunk chunk = chunks.get(i);
            if (!chunkId.equals(chunk.getChunkId())) {
                throw new RuntimeException(String.format("Division ID is inconsistent, index: %d", i));
            }
            if (chunk.getChunkIndex() != i) {
                throw new RuntimeException(String.format("Part index is inconsistent, expected: %d, actual: %d", i, chunk.getChunkIndex()));
            }
            if (chunk.getTotalChunks() != totalChunks) {
                throw new RuntimeException(String.format("Total number of fractions inconsistent, index: %d", i));
            }
        }

        // Merge Data
        StringBuilder sb = new StringBuilder();
        for (DataChunk chunk : chunks) {
            sb.append(chunk.getData());
        }

        logger.info("Split merge completed, fractions: {, total size: }KB", totalChunks, sb.length() / 1024);

        return sb.toString();
    }

    /**
     * GenerateminSnippetsID
     */
    private static String generateChunkId() {
        return UUIDUtil.getUUID();
    }

    /**
     * Data FactminSnippets
     */
    public static class DataChunk {

        private String chunkId; // minSnippetsID（Same one.minThe tablet is the sameID）
        private int chunkIndex; // minSnippetsIndex（From0Start
        private int totalChunks; // TotalminNumber of pieces
        private String data; // minSnippets
        private int size; // minArea Size (bytes)

        public String getChunkId() {
            return chunkId;
        }

        public void setChunkId(String chunkId) {
            this.chunkId = chunkId;
        }

        public int getChunkIndex() {
            return chunkIndex;
        }

        public void setChunkIndex(int chunkIndex) {
            this.chunkIndex = chunkIndex;
        }

        public int getTotalChunks() {
            return totalChunks;
        }

        public void setTotalChunks(int totalChunks) {
            this.totalChunks = totalChunks;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
