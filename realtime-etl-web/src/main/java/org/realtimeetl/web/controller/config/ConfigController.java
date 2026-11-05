/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.web.controller.config;

import org.realtimeetl.biz.ConfigExportService;
import org.realtimeetl.biz.SystemConfigService;
import org.realtimeetl.biz.vo.RestResult;
import org.realtimeetl.web.log.OperLog;
import org.realtimeetl.web.log.OperLogType;
import org.realtimeetl.common.config.AppConfig;
import org.realtimeetl.common.model.VersionInfo;
import org.realtimeetl.parser.LogService;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.storage.impl.SnowflakeIdWorker;
import org.realtimeetl.web.Version;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.OutputStream;
import java.time.Instant;

@Controller
@RequestMapping("/config")
public class ConfigController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private ConfigExportService configExportService;

    @Resource
    private LogService logService;

    @Resource
    private AppConfig appConfig;

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @RequestMapping("")
    public String index(ModelMap model) {
        model.put("config", systemConfigService.getConfigModelAll());
        model.put("fileSize", systemConfigService.estimateExportSize());
        return "config/list";
    }

    @OperLog(type = OperLogType.CACHE_IMPORT)
    @PostMapping(value = "/upload")
    @ResponseBody
    public RestResult upload(MultipartFile[] files) {
        try {
            if (files != null) {
                for (MultipartFile file : files) {
                    if (file == null) {
                        continue;
                    }
                    String filename = file.getOriginalFilename();
                    systemConfigService.checkFileSuffix(filename);
                    String tmpdir = System.getProperty("java.io.tmpdir");
                    File dest = new File(tmpdir, filename);
                    FileUtils.deleteQuietly(dest);
                    FileUtils.copyInputStreamToFile(file.getInputStream(), dest);
                    systemConfigService.refreshConfig(dest);
                    String msg = String.format("Organisation", filename);
                    logger.info(msg);
                    logService.log(LogType.CacheLog.IMPORT, msg);
                }
            }
            return RestResult.restSuccess("ok");
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response) {
        String fileName = String.format("%s-%s-%s.zip", appConfig.getName(), appConfig.getVersion(), snowflakeIdWorker.nextId());
        response.setHeader("content-type", "application/octet-stream");
        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", fileName));
        response.setContentType("application/zip");
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            VersionInfo info = new VersionInfo();
            info.setVersion(Version.CURRENT.getVersion());
            info.setAppName(appConfig.getName());
            info.setCreateTime(Instant.now().toEpochMilli());
            configExportService.exportZip(outputStream, info);
            String msg = String.format("Export profile %s", fileName);
            logger.info(msg);
            logService.log(LogType.CacheLog.EXPORT, msg);
            outputStream.flush();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }
}
