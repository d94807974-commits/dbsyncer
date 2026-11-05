/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.sdk.spi;

import org.dbsyncer.sdk.enums.EditionEnum;
import org.dbsyncer.sdk.model.ProductInfo;

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
     * AccessLicenseUploadPathdbsyncer/conf/
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
