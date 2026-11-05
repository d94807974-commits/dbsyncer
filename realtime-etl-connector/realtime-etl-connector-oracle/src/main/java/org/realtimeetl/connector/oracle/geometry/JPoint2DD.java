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

import java.io.Serializable;

public class JPoint2DD implements Serializable {

    public double x;
    public double y;

    public JPoint2DD() {
        this.x = this.y = 0.0D;
    }

    public JPoint2DD(double var1, double var3) {
        this.x = var1;
        this.y = var3;
    }

    public JPoint2DD(JPoint2DD var1) {
        this.x = var1.x;
        this.y = var1.y;
    }

    public double[] getArray() {
        return new double[]{this.x, this.y};
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public void setX(double var1) {
        this.x = var1;
    }

    public void setY(double var1) {
        this.y = var1;
    }

    public String toString() {
        return new String(this.x + " " + this.y);
    }

    public boolean equals(JPoint2DD var1) {
        return var1.x == this.x && var1.y == this.y;
    }
}
