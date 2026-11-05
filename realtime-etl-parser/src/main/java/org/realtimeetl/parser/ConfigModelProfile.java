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

import org.realtimeetl.common.model.ConfigModel;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-09-22 21:02
 */
public interface ConfigModelProfile {

    ConfigModel getCache(String id);

    void removeCacheAndNotice(String id);

    void removeCache(String id);

    void pushMessage(String id);

}