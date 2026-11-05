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
package org.realtimeetl.common.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {
    @Test
    public void hexStringToByteArrayAcceptsValidValues() {
        Assert.assertNull(StringUtil.hexStringToByteArray(null));
        Assert.assertArrayEquals(new byte[0], StringUtil.hexStringToByteArray(""));
        Assert.assertArrayEquals(new byte[]{0, 15, (byte) 0xab, (byte) 0xff},
                StringUtil.hexStringToByteArray("000faBFF"));
    }

    @Test
    public void hexStringToByteArrayRejectsInvalidValues() {
        for (String value : new String[]{"0g", "g0"}) {
            try {
                StringUtil.hexStringToByteArray(value);
                Assert.fail("Expected IllegalArgumentException for: " + value);
            } catch (IllegalArgumentException expected) {
                // Invalid input must never silently produce a byte value.
            }
        }
    }

    @Test
    public void hexStringToByteArrayRejectsOddLength() {
        try {
            StringUtil.hexStringToByteArray("123");
            Assert.fail("Expected IllegalArgumentException for odd length");
        } catch (IllegalArgumentException expected) {
            // Odd-length input is not a sequence of complete bytes.
        }
    }
}
