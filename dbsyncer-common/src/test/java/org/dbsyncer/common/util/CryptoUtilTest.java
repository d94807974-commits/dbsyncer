/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.util;

import org.dbsyncer.common.model.AesData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Encryption Tool ClassTest
 *
 * @author The clouds.
 * @version 1.0.0
 */
public class CryptoUtilTest {

    private static final Logger logger = LoggerFactory.getLogger(CryptoUtilTest.class);

    @Before
    public void setUp() {
        // Generate RSA KeyYeah.
        Map<String, Object> testData = new HashMap<>();
        testData.put("name", "Test Data");
        testData.put("value", 12345);
        testData.put("timestamp", System.currentTimeMillis());
        logger.info("Test data: {", testData);
    }

    @Test
    public void testGenerateAESKey() {
        logger.info("== sync, corrected by elderman ==");
        String aesKey = CryptoUtil.generateAESKeyPair();
        Assert.assertNotNull("AES keys should not be empty", aesKey);
        logger.info("Generated AES key: }", aesKey);
    }

    @Test
    public void testLargeDataEncryption() {
        logger.info("== sync, corrected by elderman ==");
        // GenerateLargeTest Data（Approximately100KB）
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeData.append("Here's the test data.").append(i).append(",");
        }
        String largeDataStr = largeData.toString();

        String aesKey = CryptoUtil.generateAESKeyPair();

        long startTime = System.currentTimeMillis();
        AesData encryptedResult = CryptoUtil.encryptData(largeDataStr, aesKey);
        long encryptTime = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        String decryptedData = CryptoUtil.decryptData(encryptedResult.getEncryptedData(), encryptedResult.getIv(), aesKey);
        long decryptTime = System.currentTimeMillis() - startTime;

        Assert.assertEquals("Big data decryption should be identical.", largeDataStr, decryptedData);
        logger.info("Big Data Encryption Time - Encryption: {ms, Decrypt: {ms", encryptTime, decryptTime);
    }
}
