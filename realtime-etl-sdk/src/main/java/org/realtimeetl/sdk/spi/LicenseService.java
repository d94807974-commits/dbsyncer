/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.sdk.spi;

import org.realtimeetl.sdk.enums.EditionEnum;
import org.realtimeetl.sdk.model.ProductInfo;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2024-05-13 01:17
 */
public interface LicenseService {

    /**
     * AccessVersionNo. No.
     */
    EditionEnum getEditionEnum();

    /**
     * AccessLicenseUploadPathrealtime-etl/conf/
     */
    String getLicensePath();

    /**
     * AccessAuthorizationKEY
     */
    String getKey();

    /**
     * AccessAuthorizationInfo
     */
    ProductInfo getProductInfo();

    /**
     * UpdateAuthorization
     */
    void updateLicense();
}
