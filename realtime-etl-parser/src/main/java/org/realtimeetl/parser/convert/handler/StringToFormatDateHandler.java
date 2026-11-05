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
package org.realtimeetl.parser.convert.handler;

import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.parser.convert.AbstractHandler;

import java.text.ParseException;
import java.time.format.DateTimeFormatter;

/**
 * String rotationCustomFormat
 *
 * @author Realtime ETL
 */
public class StringToFormatDateHandler extends AbstractHandler {

    @Override
    public Object convert(String args, Object value) throws ParseException {

        if (value instanceof String) {
            String s = (String) value;
            return DateFormatUtil.stringToDate(s, DateTimeFormatter.ofPattern(args));
        }
        return value;
    }
}
