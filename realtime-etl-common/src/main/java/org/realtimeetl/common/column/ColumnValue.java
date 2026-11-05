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
package org.realtimeetl.common.column;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2022-04-22 22:39
 */
public interface ColumnValue {

    boolean isNull();

    String asString();

    byte[] asByteArray();

    Byte asByte();

    Short asShort();

    Integer asInteger();

    Long asLong();

    Float asFloat();

    Double asDouble();

    Boolean asBoolean();

    BigDecimal asBigDecimal();

    Date asDate();

    Timestamp asTimestamp();

    Time asTime();
}
