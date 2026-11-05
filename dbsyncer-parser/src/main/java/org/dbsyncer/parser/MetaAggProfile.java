/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser;

import org.dbsyncer.parser.model.MetaAgg;

import java.util.List;

/**
 * Sync task Meta Day summary reading and writing.
 *
 * @author wuji
 * @version 1.0.0
 */
public interface MetaAggProfile {

    /**
     * Query by statistical day.
     */
    MetaAgg getByStatDate(String statDate);

    /**
     * Some recently.days（PressSTAT_DATE Raised, maximum{@code days} (Article Concerning Article).
     */
    List<MetaAgg> listRecent(int days);

    /**
     * InsertOr pressSTAT_DATE Updates the summary of the day.
     */
    void upsert(MetaAgg daily);

    /**
     * DeleteLines earlier than the date specified (excludingcutoff）。
     */
    void removeBefore(String cutoffStatDate);
}
