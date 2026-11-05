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
package org.realtimeetl.parser;

import org.realtimeetl.parser.model.Group;

import java.util.List;

/**
 * Business groupRead and write.
 *
 * @author Realtime ETL
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
