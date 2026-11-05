/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.common.model;

/**
 * OpenAPIUnified data model
 *
 * @author The clouds.
 * @version 1.0.0
 */
public class OpenApiData {

    /**
     * TimePoke
     */
    private Long timestamp;

    /**
     * Random Six
     */
    private String nonce;

    /**
     * Encrypted DataBase64Encoding)
     */
    private String encryptedData;

    /**
     * Initialization vector (%1)Base64Encoding)
     */
    private String iv;

    /**
     * RSAEncryptedAESKeyBase64Encoding)
     */
    private String encryptedKey;

    /**
     * Signature
     */
    private String signature;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
