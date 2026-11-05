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
package org.realtimeetl.web.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Operation logs：Mark inWeb ChangeInterfaceGo, by{@link OperLogInterceptor} UnifiedLibrary。
 * <p>
 * {@code type} Use{@link OperLogType} Constants, corresponding{@link org.realtimeetl.parser.LogType} Typecode.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /**
     * LogType TypeYeah, see you.{@link OperLogType}
     */
    String type();

    /**
     * ListYeah Limits. Summary; empty「Actions+ModuleFirst Name」，If there is.Request parameters name Add{@code :name}
     */
    String message() default "";
}
