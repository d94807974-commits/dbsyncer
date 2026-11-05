/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser;

import org.dbsyncer.parser.model.Group;

import java.util.List;

/**
 * Business groupRead and write.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-14 18:00
 */
public interface GroupProfile {

    /**
     * Pressid QuestionGroup。
     */
    Group getGroup(String id);

    /**
     * AllGroup（PressNameRaise.
     */
    List<Group> listGroups();

    /**
     * PressNameAccurate query (for unique verification).
     */
    Group getByName(String name);

    /**
     * Add Group。
     */
    String addGroup(Group group);

    /**
     * UpdateGroup。
     */
    String updateGroup(Group group);

    /**
     * Delete group（Caller for cascade.rel，Or byService It's organized.
     */
    void removeGroup(String id);

}
