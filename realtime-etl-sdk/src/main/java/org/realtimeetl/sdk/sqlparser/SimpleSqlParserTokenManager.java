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
package org.realtimeetl.sdk.sqlparser;

import net.sf.jsqlparser.parser.CCJSqlParserTokenManager;
import net.sf.jsqlparser.parser.SimpleCharStream;

import net.sf.jsqlparser.parser.CCJSqlParserTokenManager;
import net.sf.jsqlparser.parser.SimpleCharStream;

/**
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2025-03-02 16:07
 */
public class SimpleSqlParserTokenManager extends CCJSqlParserTokenManager {

    int curLexState = 0;
    int defaultLexState = 0;

    public SimpleSqlParserTokenManager(SimpleCharStream stream) {
        super(stream);
    }

    public SimpleSqlParserTokenManager(SimpleCharStream stream, int lexState) {
        super(stream, lexState);
    }

    @Override
    public void ReInit(SimpleCharStream stream) {
        curLexState = defaultLexState;
        super.ReInit(stream);
    }

    /**
     * Switch to specified lex state.
     */
    public void SwitchTo(int lexState) {
        curLexState = lexState;
        super.SwitchTo(lexState);
    }
}
