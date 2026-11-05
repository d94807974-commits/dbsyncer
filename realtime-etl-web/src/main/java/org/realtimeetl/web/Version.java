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
package org.realtimeetl.web;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2025-02-09 15:06
 */
public class Version {

    /**
     * LargeVersion-SmallVersion-Number-year-month-day-yearNo.
     */
    private final long version;

    public static final Version V_2_0_5 = new Version(20_00_05_2025_02_19_00L);
    public static final Version V_2_0_6 = new Version(20_00_06_2025_00_00_00L);
    public static final Version V_2_0_7 = new Version(20_00_07_2025_07_08_00L);
    public static final Version V_2_0_8 = new Version(20_00_08_2026_01_30_00L);
    public static final Version V_2_0_9 = new Version(20_00_09_2026_02_26_00L);
    public static final Version V_2_1_0 = new Version(20_01_00_2026_05_13_00L);
    public static final Version V_2_1_1 = new Version(20_01_01_2026_05_21_00L);
    public static final Version V_2_1_2 = new Version(20_01_02_2026_06_22_00L);
    public static final Version V_2_1_3 = new Version(20_01_03_2026_08_06_00L);
    public static final Version V_2_1_4 = new Version(20_01_04_2026_09_20_00L);
    public static final Version V_2_1_5 = new Version(20_01_05_2026_10_30_01L);
    public static final Version CURRENT = new Version(20_01_06_2026_11_04_00L);

    public Version(long version) {
        this.version = version;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return String.valueOf(version);
    }
}
