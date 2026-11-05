/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.util;

import org.realtimeetl.common.util.StringUtil;

/**
 * Meta Ensure:FullTogether{@code incrementMeta} and snapshot flush TotalUse to avoid line writing backOverwriteExclusive Refer POLICY Continuity Accommodation Moratorium
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-08-11
 */
public abstract class MetaLockUtil {

    /**
     * PresstaskId Accusation locks within the acquisition process ({@link String#intern()}）。
     *
     * @param taskId Task ID
     * @return Lock/42;
     */
    public static Object lock(String taskId) {
        String id = StringUtil.isBlank(taskId) ? StringUtil.EMPTY : taskId;
        return ("meta-write-" + id).intern();
    }
}
