/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.web.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Operation logs：Mark inWeb ChangeInterfaceGo, by{@link OperLogInterceptor} UnifiedLibrary。
 * <p>
 * {@code type} Use{@link OperLogType} Constants, corresponding{@link org.dbsyncer.parser.LogType} Typecode.
 *
 * @author wuji
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
