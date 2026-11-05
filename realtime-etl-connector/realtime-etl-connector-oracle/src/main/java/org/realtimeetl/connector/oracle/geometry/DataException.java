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
package org.realtimeetl.connector.oracle.geometry;

import java.io.PrintStream;
import java.io.PrintWriter;

public class DataException extends Exception {

    public String description = "";
    public Throwable detail = null;

    public DataException(String var1) {
        super(var1);
    }

    public String toString() {
        String var1 = "Message:" + this.getMessage() + "\n";
        var1 = var1 + "Description: " + this.description;
        if (this.detail != null) {
            var1 = var1 + "Nested exception is:\n" + this.detail.toString();
        }

        return var1;
    }

    public void printStackTrace(PrintStream var1) {
        if (this.detail == null) {
            super.printStackTrace(var1);
        } else {
            synchronized (var1) {
                var1.println(this);
                this.detail.printStackTrace(var1);
            }
        }
    }

    public void printStackTrace() {
        this.printStackTrace(System.err);
    }

    public void printStackTrace(PrintWriter var1) {
        if (this.detail == null) {
            super.printStackTrace(var1);
        } else {
            synchronized (var1) {
                var1.println(this);
                this.detail.printStackTrace(var1);
            }
        }
    }
}
