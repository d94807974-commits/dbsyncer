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
package org.realtimeetl.connector.mongodb.constant;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-06 20:00
 */
public final class MongoDBConstant {

    public static final String SOURCE_COLLECTION = "_source_collection";

    public static final String TARGET_COLLECTION = "_target_collection";

    public static final String DATABASE = "_database";

    public static final String ID_FIELD = "_id";

    public static final String RESUME_TOKEN_PREFIX = "resume_";

    private MongoDBConstant() {
    }
}
