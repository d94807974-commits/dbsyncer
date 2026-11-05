/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.web.controller.license;

import org.apache.commons.io.FileUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.realtimeetl.biz.UserConfigService;
import org.realtimeetl.biz.vo.EditionInfoVO;
import org.realtimeetl.biz.vo.ProductStatusVO;
import org.realtimeetl.biz.vo.RestResult;
import org.realtimeetl.common.model.HttpResult;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.HttpClientUtil;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.LogService;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.parser.model.UserInfo;
import org.realtimeetl.sdk.model.Product;
import org.realtimeetl.sdk.model.ProductInfo;
import org.realtimeetl.sdk.spi.LicenseService;
import org.realtimeetl.web.Version;
import org.realtimeetl.web.controller.BaseController;
import org.realtimeetl.web.log.OperLog;
import org.realtimeetl.web.log.OperLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/license")
public class LicenseController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private LogService logService;

    @Resource
    private LicenseService licenseService;

    @Resource
    private UserConfigService userConfigService;

    @Value("${realtime-etl.web.license.server.address:https://dbsyncer.cn/api/license/create}")
    private String serverAddress;

    public static final Integer SUCCESS = 200;
    public static final String STATUS = "status";
    public static final String DATA = "data";
    public static final String MSG = "msg";
    // DueOrganisation10daysInternal
    private static final long TEN_DAYS_MILLIS = 864000000L;
    // AllNormal
    private static final String PRODUCT_STATUS_NORMAL = "normal";
    // NoneNormal、Yes.Expiring soon
    private static final String PRODUCT_STATUS_EXPIRING = "expiring";
    // AllExpiry
    private static final String PRODUCT_STATUS_EXPIRED = "expired";
    // Yes.Normal + Yes.Error
    private static final String PRODUCT_STATUS_PARTIAL = "partial";
    // NoneProductsList
    private static final String PRODUCT_STATUS_INACTIVE = "inactive";

    @RequestMapping("")
    public String index(ModelMap model) {
        model.put("key", licenseService.getKey());
        model.put("productInfo", licenseService.getProductInfo());
        initEditionInfo(model);
        return "license/list";
    }

    @OperLog(type = OperLogType.USER_UPLOAD_LICENSE)
    @PostMapping(value = "/upload")
    @ResponseBody
    public RestResult upload(MultipartFile[] files) {
        try {
            requirePermission("license:edit");
            if (files != null && files[0] != null) {
                String filename = "license";
                File dest = new File(licenseService.getLicensePath() + filename);
                FileUtils.deleteQuietly(dest);
                FileUtils.copyInputStreamToFile(files[0].getInputStream(), dest);
                licenseService.updateLicense();
                logger.info("{}:{}", LogType.UserLog.UPLOAD_LICENSE_FILE.getMessage(), filename);
                logService.log(LogType.UserLog.UPLOAD_LICENSE_FILE);
            }
            return RestResult.restSuccess("ok");
        } catch (Exception e) {
            logService.log(LogType.UserLog.UPLOAD_LICENSE_FILE_ERROR);
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.USER_ACTIVATE_LICENSE)
    @PostMapping(value = "/activate")
    @ResponseBody
    public RestResult activate(HttpServletRequest request) {
        try {
            requirePermission("license:edit");
            String content = getLicenseContent(getParams(request));
            if (StringUtil.isNotBlank(content)) {
                String filename = "license";
                File dest = new File(licenseService.getLicensePath() + filename);
                FileUtils.writeStringToFile(dest, content, Charset.defaultCharset());
                licenseService.updateLicense();
                logger.info("{}:{}", LogType.UserLog.ACTIVATE_FREE_LICENSE_FILE.getMessage(), filename);
                logService.log(LogType.UserLog.ACTIVATE_FREE_LICENSE_FILE);
            }
            return RestResult.restSuccess("ok");
        } catch (Exception e) {
            logService.log(LogType.UserLog.ACTIVATE_FREE_LICENSE_FILE_ERROR);
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.USER_DELETE_LICENSE)
    @PostMapping(value = "/remove")
    @ResponseBody
    public RestResult remove() {
        try {
            String filename = "license";
            File dest = new File(licenseService.getLicensePath() + filename);
            FileUtils.deleteQuietly(dest);
            licenseService.updateLicense();
            logger.info("{}:{}", LogType.UserLog.DELETE_LICENSE_FILE.getMessage(), filename);
            logService.log(LogType.UserLog.DELETE_LICENSE_FILE);
            return RestResult.restSuccess("ok");
        } catch (Exception e) {
            logService.log(LogType.UserLog.DELETE_LICENSE_FILE_ERROR);
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @GetMapping("/query.json")
    @ResponseBody
    public RestResult query() {
        EditionInfoVO infoVo = new EditionInfoVO();
        infoVo.setEdition(licenseService.getEditionEnum().getCode());
        infoVo.setEditionName(licenseService.getEditionEnum().getMessage());
        ProductInfo productInfo = licenseService.getProductInfo();
        if (productInfo == null || CollectionUtils.isEmpty(productInfo.getProducts())) {
            // ProfessionalSoftware deployed, but not yet availableNot activatedAny productAuthorization
            infoVo.setEditionStatus(PRODUCT_STATUS_INACTIVE);
            return RestResult.restSuccess(infoVo);
        }

        long currentTime = infoVo.getCurrentTime();
        List<ProductStatusVO> productStatuses = productInfo.getProducts().stream()
                .map(product -> toProductStatus(product, currentTime))
                .collect(Collectors.toList());
        infoVo.setProducts(productStatuses);
        fillAggregateStatus(infoVo, productStatuses);
        return RestResult.restSuccess(infoVo);
    }

    /**
     * PressAllProduct aggregationVersionStatus。
     */
    private void fillAggregateStatus(EditionInfoVO infoVo, List<ProductStatusVO> products) {
        long normalCount = products.stream()
                .filter(product -> PRODUCT_STATUS_NORMAL.equals(product.getStatus()))
                .count();
        long expiringCount = products.stream()
                .filter(product -> PRODUCT_STATUS_EXPIRING.equals(product.getStatus()))
                .count();
        long expiredCount = products.stream()
                .filter(product -> PRODUCT_STATUS_EXPIRED.equals(product.getStatus()))
                .count();

        // Needing attention.Errorproducts
        List<ProductStatusVO> attentionProducts = products.stream()
                .filter(product -> !PRODUCT_STATUS_NORMAL.equals(product.getStatus()))
                .collect(Collectors.toList());
        infoVo.setAttentionCount(attentionProducts.size());
        infoVo.setAttentionSummary(buildAttentionSummary(attentionProducts));

        if (normalCount == products.size()) {
            infoVo.setEditionStatus(PRODUCT_STATUS_NORMAL);
            return;
        }
        if (expiredCount == products.size()) {
            infoVo.setEditionStatus(PRODUCT_STATUS_EXPIRED);
            return;
        }
        if (normalCount > 0) {
            infoVo.setEditionStatus(PRODUCT_STATUS_PARTIAL);
            return;
        }
        if (expiringCount > 0) {
            infoVo.setEditionStatus(PRODUCT_STATUS_EXPIRING);
            products.stream()
                    .filter(product -> PRODUCT_STATUS_EXPIRING.equals(product.getStatus()))
                    .min(Comparator.comparingLong(ProductStatusVO::getEffectiveTime))
                    .ifPresent(product -> {
                        infoVo.setEffectiveTime(product.getEffectiveTime());
                        formatEffectiveTimeContent(infoVo);
                    });
            return;
        }
        infoVo.setEditionStatus(PRODUCT_STATUS_EXPIRED);
    }

    private ProductStatusVO toProductStatus(Product product, long currentTime) {
        ProductStatusVO statusVo = new ProductStatusVO();
        statusVo.setName(product.getName());
        statusVo.setEffectiveTime(product.getEffectiveTime());
        statusVo.setStatus(resolveProductStatus(product.getEffectiveTime(), currentTime));
        return statusVo;
    }

    private String resolveProductStatus(long effectiveTime, long currentTime) {
        if (effectiveTime <= 0 || currentTime > effectiveTime) {
            return PRODUCT_STATUS_EXPIRED;
        }
        if (effectiveTime - TEN_DAYS_MILLIS <= currentTime) {
            return PRODUCT_STATUS_EXPIRING;
        }
        return PRODUCT_STATUS_NORMAL;
    }

    private String buildAttentionSummary(List<ProductStatusVO> attentionProducts) {
        if (CollectionUtils.isEmpty(attentionProducts)) {
            return null;
        }
        if (attentionProducts.size() == 1) {
            ProductStatusVO product = attentionProducts.get(0);
            return product.getName() + " " + statusLabel(product.getStatus());
        }
        return attentionProducts.size() + " Item for attention";
    }

    private String statusLabel(String status) {
        if (PRODUCT_STATUS_EXPIRING.equals(status)) {
            return "Expiring soon";
        }
        if (PRODUCT_STATUS_EXPIRED.equals(status)) {
            return "Expired";
        }
        return "";
    }

    private void formatEffectiveTimeContent(EditionInfoVO infoVo) {
        LocalDateTime before = LocalDateTime.ofInstant(Instant.ofEpochMilli(infoVo.getCurrentTime()), ZoneId.systemDefault());
        LocalDateTime after = LocalDateTime.ofInstant(Instant.ofEpochMilli(infoVo.getEffectiveTime()), ZoneId.systemDefault());
        // 1. know the whole year.
        long years = ChronoUnit.YEARS.between(before, after);
        before = before.plusYears(years);
        // 2. The whole month.
        long months = ChronoUnit.MONTHS.between(before, after);
        if (years > 0) {
            infoVo.setEffectiveContent(String.format("%d %d months remaining", years, months));
            return;
        }
        // 3. Rewinding.days
        before = before.plusMonths(months);
        long days = ChronoUnit.DAYS.between(before, after);
        days = days <= 0 ? 0 : days;
        if (months > 0) {
            infoVo.setEffectiveContent(String.format("%d months %d days remaining", months, days));
            return;
        }
        // 4. Finally.hours
        before = before.plusDays(days);
        long hours = ChronoUnit.HOURS.between(before, after);
        hours = hours <= 0 ? 0 : hours;
        infoVo.setEffectiveContent(String.format("Peaceday %d hours left", days, hours));
    }

    private String getLicenseContent(Map<String, String> params) throws IOException {
        ProductInfo info = JsonUtil.jsonToObj(JsonUtil.objToJson(params), ProductInfo.class);
        UserInfo userInfo = getUserInfo();
        Assert.notNull(userInfo, "Acting expired session, please re-entry");
        info.setLicenseKey(licenseService.getKey());
        info.setOsName(System.getProperty("os.name"));
        info.setVersion(Version.CURRENT.getVersion());
        return invoke(info);
    }

    public String invoke(ProductInfo info) throws IOException {
        // AuthorizationService history agreements:body YesURL EncodedJSON，Content-Type Stillapplication/json
        String data = URLEncoder.encode(JsonUtil.objToJson(info), "UTF-8");
        try {
            HttpResult response = HttpClientUtil.post(serverAddress, data, ContentType.APPLICATION_JSON);
            if (response.isOk()) {
                Map<String, String> result = JsonUtil.jsonToObj(response.getBody(), Map.class);
                if (result != null && result.containsKey(DATA)) {
                    String status = String.valueOf(result.get(STATUS));
                    if (Integer.parseInt(status) == SUCCESS) {
                        return result.get(DATA);
                    }
                }
                throw new IllegalArgumentException(result == null ? "Authorized service returns abnormal." : result.get(MSG));
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (HttpHostConnectException e) {
            throw new IllegalArgumentException("Network connection is abnormal. Could not activate");
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
        throw new IllegalArgumentException("Authorized 287 Address Unable to Activate");
    }

    private UserInfo getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userConfigService.getUserInfo(authentication.getName());
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
}
