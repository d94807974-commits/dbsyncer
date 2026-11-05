/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.util;

import org.dbsyncer.common.util.DataChunkUtil.DataChunk;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Data FactminSnippetsTest
 *
 * @author The clouds.
 * @version 1.0.0
 */
public class DataChunkUtilTest {

    private static final Logger logger = LoggerFactory.getLogger(DataChunkUtilTest.class);

    @Test
    public void testChunkData_SmallData() {
        logger.info("== sync, corrected by elderman == @elder man");
        String smallData = "Here's the test data.";

        List<DataChunk> chunks = DataChunkUtil.chunkData(smallData);
        Assert.assertNotNull("Shard list must not be empty", chunks);
        Assert.assertEquals("There should be only one fraction of the small data.", 1, chunks.size());

        DataChunk chunk = chunks.get(0);
        Assert.assertNotNull("Part ID should not be empty.", chunk.getChunkId());
        Assert.assertEquals("The fraction index should read 0", 0, chunk.getChunkIndex());
        Assert.assertEquals("The total number of fractions should read 1", 1, chunk.getTotalChunks());
        Assert.assertEquals("The fraction data should be the same.", smallData, chunk.getData());

        logger.info("Small data fraction tested, fraction: {", chunks.size());
    }

    @Test
    public void testChunkData_LargeData() {
        logger.info("== sync, corrected by elderman == @elder man");
        // GenerateApproximately2MBData (UseASCIICharacters, avoid multibytesminQuestion
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 200000; i++) {
            largeData.append("test_data_").append(i).append(",");
        }
        String largeDataStr = largeData.toString();

        // Use500KBminBlock Size
        int chunkSize = 500 * 1024;
        List<DataChunk> chunks = DataChunkUtil.chunkData(largeDataStr, chunkSize);

        Assert.assertNotNull("Shard list must not be empty", chunks);
        Assert.assertTrue("Big data should be divided into multiple fractions", chunks.size() > 1);

        // Verify AllminPieces.chunkIdSame
        String firstChunkId = chunks.get(0).getChunkId();
        for (DataChunk chunk : chunks) {
            Assert.assertEquals("All pieces of chunkId should be the same.", firstChunkId, chunk.getChunkId());
            Assert.assertEquals("Total fractions should be consistent", chunks.size(), chunk.getTotalChunks());
        }

        logger.info("Large data fraction tested, data size: }KB, fraction: {, fraction size: }KB", largeDataStr.length() / 1024, chunks.size(), chunkSize / 1024);
    }

    @Test
    public void testChunkData_CustomSize() {
        logger.info("== sync, corrected by elderman == @elder man");
        // GenerateApproximately500KBData
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < 50000; i++) {
            data.append("Test Data").append(i);
        }
        String dataStr = data.toString();

        // Use100KBminBlock Size
        int chunkSize = 100 * 1024;
        List<DataChunk> chunks = DataChunkUtil.chunkData(dataStr, chunkSize);

        Assert.assertTrue("It should be divided into separate pieces.", chunks.size() > 1);

        // AuthenticationminBlock Size
        for (int i = 0; i < chunks.size() - 1; i++) {
            DataChunk chunk = chunks.get(i);
            Assert.assertTrue("The fraction size should be within reasonable limits.", chunk.getSize() <= chunkSize && chunk.getSize() > 0);
        }

        logger.info("Custom fraction size tested, fraction: {", chunks.size());
    }

    @Test
    public void testMergeChunks() {
        logger.info("== sync, corrected by elderman ==");
        String originalData = "This is the test data. It needs to be split and merged.";

        // minSnippets
        List<DataChunk> chunks = DataChunkUtil.chunkData(originalData, 10); // 3.1.4 Use of smallminBlock Size

        // Merge
        String mergedData = DataChunkUtil.mergeChunks(chunks);
        Assert.assertEquals("The consolidated data should be the same", originalData, mergedData);

        logger.info("We're going to test the sub-section.");
    }

    @Test
    public void testMergeChunks_LargeData() {
        logger.info("== sync, corrected by elderman ==");
        // GenerateApproximately1MBData (UseASCIICharacters, avoid multibytesminQuestion
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            largeData.append("test_data_").append(i).append(",");
        }
        String originalData = largeData.toString();

        // minSnippets
        List<DataChunk> chunks = DataChunkUtil.chunkData(originalData, 200 * 1024); // 200KBminSnippets

        // Merge
        String mergedData = DataChunkUtil.mergeChunks(chunks);
        Assert.assertEquals("Big data should be the same after consolidation", originalData, mergedData);

        logger.info("Large data merge test passed, original size: {KB, fraction:{", originalData.length() / 1024, chunks.size());
    }

    @Test
    public void testMergeChunks_Incomplete() {
        logger.info("PARTICIPATION OF INSPECTIONS");
        // GenerateIt's big enough to make sure there' Institutions are multiple.minSnippets
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            data.append("test_data_").append(i).append(",");
        }
        List<DataChunk> chunks = DataChunkUtil.chunkData(data.toString(), 100); // Use smallminMake sure there' 1-3.minSnippets

        if (chunks.size() > 1) {
            // RemoveminFilm, simulation incomplete Lower
            chunks.remove(0);

            try {
                DataChunkUtil.mergeChunks(chunks);
                Assert.fail("The incomplete fraction should be thrown out of the ordinary.");
            } catch (RuntimeException e) {
                Assert.assertTrue("We should throw out the partial anomaly.", e.getMessage().contains("Split incomplete."));
                logger.info("Incomplete fraction test passed.", e.getMessage());
            }
        } else {
            logger.info("Data is too small to test incomplete fractions. Skip");
        }
    }

    @Test
    public void testMergeChunks_InvalidOrder() {
        logger.info("== sync, corrected by elderman ==");
        String data = "test_data_1234567890";
        List<DataChunk> chunks = DataChunkUtil.chunkData(data, 5);

        if (chunks.size() > 1) {
            // Break the order.
            DataChunk temp = chunks.get(0);
            chunks.set(0, chunks.get(1));
            chunks.set(1, temp);

            // mergeChunksIt's sorted automatically, so it should.NormalMerge
            String mergedData = DataChunkUtil.mergeChunks(chunks);
            // mergeChunksThe Experience will press.IndexSort so you can merge correctly
            Assert.assertEquals("The consolidated data should be the same", data, mergedData);
            logger.info("Serial test passed.");
        } else {
            logger.info("Data is too small to test order, skip");
        }
    }

    @Test
    public void testChunkData_Empty() {
        logger.info("== sync, corrected by elderman ==");
        List<DataChunk> chunks = DataChunkUtil.chunkData("");
        Assert.assertNotNull("EmptyEWS list should not be null", chunks);
        Assert.assertTrue("Empty data should return empty list", chunks.isEmpty());

        chunks = DataChunkUtil.chunkData(null);
        Assert.assertNotNull("Null data fraction list should not be nulla", chunks);
        Assert.assertTrue("Null data should return empty list", chunks.isEmpty());

        logger.info("45. Empty data fraction test passed.");
    }

    @Test
    public void testChunkData_SizeLimits() {
        logger.info("== sync, corrected by elderman ==");
        String data = "Test Data";

        // TestMore than maximumValueYes.minBlock Size
        List<DataChunk> chunks1 = DataChunkUtil.chunkData(data, 20 * 1024 * 1024); // 20MB
        Assert.assertNotNull("Shard list must not be empty", chunks1);

        // TestLess than MinimumValueYes.minBlock Size
        List<DataChunk> chunks2 = DataChunkUtil.chunkData(data, 10 * 1024); // 10KB
        Assert.assertNotNull("Shard list must not be empty", chunks2);

        logger.info("The size limit test passed.");
    }
}
