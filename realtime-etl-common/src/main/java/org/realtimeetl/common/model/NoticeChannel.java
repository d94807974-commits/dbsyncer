/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.common.model;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-03-06 01:01
 */
public abstract class NoticeChannel {

    /**
     * YesNoEnable
     */
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
