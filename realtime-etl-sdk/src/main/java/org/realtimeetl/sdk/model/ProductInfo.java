/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.realtimeetl.sdk.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ProductsInfo
 *
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2024-05-19 10:35
 */
public class ProductInfo {

    /**
     * Machine code
     */
    private String licenseKey;

    /**
     * SystemName
     */
    private String osName;

    /**
     * Company name
     */
    private String company;

    /**
     * Head
     */
    private String owner;

    /**
     * Cell phone number
     */
    private String phone;

    /**
     * Mailbox(Multiple MailboxesComm put it together)
     */
    private String email;

    /**
     * RemarksInfo
     */
    private String remark;

    /**
     * Mandate Time
     */
    private long createTime;

    /**
     * VersionNo. No.
     */
    private long version;

    /**
     * AuthorizationProductsListDetails
     */
    private List<Product> products = new ArrayList<>();

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
