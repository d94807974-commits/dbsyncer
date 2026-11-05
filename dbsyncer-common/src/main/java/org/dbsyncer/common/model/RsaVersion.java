/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.model;

/**
 * RSAConfigure
 *
 * @version 1.0.0
 * @Author The clouds.
 * @Date 2026-02-06 08:30
 */
public class RsaVersion extends ConfigVersion {

    /**
     * RSA Public Key
     */
    private String publicKey;

    /**
     * RSA private key
     */
    private String privateKey;

    /**
     * KeyLength
     */
    private int keyLength = 2048;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }
}
