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
package org.realtimeetl.connector.oracle.logminer;

import java.math.BigInteger;
import java.util.Objects;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2023-12-09 20:20
 */
public class LogFile {

    private final String fileName;
    private final BigInteger firstScn;
    private final BigInteger nextScn;
    private final boolean current;

    public LogFile(String fileName, BigInteger firstScn, BigInteger nextScn, boolean current) {
        this.fileName = fileName;
        this.firstScn = firstScn;
        this.nextScn = nextScn;
        this.current = current;
    }

    public String getFileName() {
        return fileName;
    }

    public BigInteger getFirstScn() {
        return firstScn;
    }

    public BigInteger getNextScn() {
        return nextScn;
    }

    public boolean isCurrent() {
        return current;
    }

    public boolean isSameRange(LogFile other) {
        return Objects.equals(firstScn, other.getFirstScn()) && Objects.equals(nextScn, other.getNextScn());
    }
}
