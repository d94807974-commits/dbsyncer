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

import org.realtimeetl.common.util.StringUtil;

import java.util.regex.Pattern;

/**
 * Plugin JAR DocumentationDescriptionFilename、Size, summary).
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-09
 */
public class PluginFile {

    /**
     * PluginFilenameRequest head.
     */
    public static final String FILE_NAME_HEADER = "X-Plugin-File-Name";

    /**
     * Continue after receivingminLet's go.ValueYes{@code 1} Entered into force.
     */
    public static final String RELAY_HEADER = "X-Plugin-Relay";

    /**
     * SinglePluginDocumentationceiling,andPageNoodles.UploadRestrictions are consistent.
     */
    public static final long MAX_BYTES = 128L * 1024 * 1024;

    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9._-]+\\.jar");

    private String name;

    private long size;

    private String sha256;

    /**
     * FilenameLetters, numbers, dots, underlineds, short lines only, and in the{@code .jar} End.
     *
     * @param name Filename
     * @return LegalBack true
     */
    public static boolean isSafeName(String name) {
        if (StringUtil.isBlank(name) || name.indexOf("..") >= 0) {
            return false;
        }
        return SAFE_NAME.matcher(name).matches();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }
}
