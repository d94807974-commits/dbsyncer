/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.common.util;

import org.apache.commons.io.IOUtils;
import org.realtimeetl.common.config.PackageFormatConfig;
import org.realtimeetl.common.model.VersionInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * ConfigurePackageZIP Read-and-write aidsformatVersion=2）。
 *
 * @author wuji
 * @version 1.0.0
 */
public final class PackageZipUtil {

    private PackageZipUtil() {
    }

    /**
     * ReadZIP Entries (non-existent)null）。
     */
    public static String readOptionalEntry(ZipFile zip, String entryName) throws IOException {
        ZipEntry entry = zip.getEntry(entryName);
        if (entry == null) {
            return null;
        }
        return readEntryAsString(zip, entry);
    }

    /**
     * Verifymanifest.json And parseVersionInfo。
     */
    public static VersionInfo validateManifest(ZipFile zip) throws IOException {
        String json = readOptionalEntry(zip, PackageFormatConfig.MANIFEST);
        if (StringUtil.isBlank(json)) {
            throw new IllegalArgumentException("Importing a low version or configuration incomplete is not supported: missing least.json");
        }
        Map map = JsonUtil.parseMap(json);
        if (map == null) {
            throw new IllegalArgumentException("Invalid.json");
        }
        Object formatVersion = map.get("formatVersion");
        if (formatVersion == null) {
            throw new IllegalArgumentException("Importing a low version or configuration incomplete is not supported");
        }
        VersionInfo info = new VersionInfo();
        Object version = map.get("version");
        if (version instanceof Number) {
            info.setVersion(((Number) version).longValue());
        }
        Object appName = map.get("appName");
        if (appName != null) {
            info.setAppName(String.valueOf(appName));
        }
        Object createTime = map.get("createTime");
        if (createTime instanceof Number) {
            info.setCreateTime(((Number) createTime).longValue());
        }
        return info;
    }

    /**
     * Throughtable_group/*.ndjson Central African Airline.
     */
    public static void pageScanTableGroupNdjsonLines(ZipFile zip, Consumer<String> lineConsumer) throws IOException {
        if (zip == null || lineConsumer == null) {
            return;
        }
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (entry.isDirectory() || !name.startsWith(PackageFormatConfig.TABLE_GROUP_DIR)
                    || !name.endsWith(PackageFormatConfig.NDJSON_SUFFIX)) {
                continue;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (StringUtil.isNotBlank(line)) {
                        lineConsumer.accept(line);
                    }
                }
            }
        }
    }

    private static String readEntryAsString(ZipFile zip, ZipEntry entry) throws IOException {
        try (InputStream in = zip.getInputStream(entry)) {
            return new String(IOUtils.toByteArray(in), StandardCharsets.UTF_8);
        }
    }
}
