/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.sqlserver.schema;

import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.sdk.schema.CustomData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Geometric DataTypeValueConvert
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-01-18 13:03
 */
public class SqlServerGeometryData extends CustomData {

    public SqlServerGeometryData(Object value) {
        super(value);
    }

    @Override
    public Collection<?> apply() {
        List<Object> args = new ArrayList<>();
        Object value = getValue();

        // SQL Just...2 individualParameter！
        // Parameter1: WKT（NULLIF FirstParameter）
        // Parameter2: SRID
        if (value == null || value.toString().trim().isEmpty()) {
            args.add("");
            args.add(0);
            return args;
        }

        // ParsingWKT andSRID
        GeometryData data = parseGeometry(String.valueOf(value));
        if (StringUtil.isBlank(data.wkt)) {
            args.add("");
            args.add(0);
        } else {
            args.add(data.wkt);
            args.add(data.srid);
        }
        return args;
    }

    /**
     * Pars empowerSRID Yes.WKT String
     * Format: "SRID=4326;POINT(133.4 38.5)" or"POINT(133.4 38.5) | 4326"
     */
    private GeometryData parseGeometry(String input) {
        GeometryData data = new GeometryData();
        if (input == null || input.trim().isEmpty()) {
            return data;
        }

        input = input.trim();

        try {
            // Format: "POINT (133.4 38.5) | 4326"
            if (input.contains("|")) {
                String[] parts = input.split("\\|");
                data.wkt = parts[0].trim();
                data.srid = Integer.parseInt(parts[1].trim());
            }
            // Format: "SRID=4326;POINT(133.4 38.5)"
            else if (input.toUpperCase().startsWith("SRID=")) {
                int semicolonIdx = input.indexOf(';');
                if (semicolonIdx > 0) {
                    String sridPart = input.substring(0, semicolonIdx);
                    data.wkt = input.substring(semicolonIdx + 1).trim();
                    data.srid = Integer.parseInt(sridPart.replaceAll("[^0-9]", ""));
                }
            }
            // Format: PureWKT
            else {
                data.wkt = input;
                data.srid = 0;
            }
        } catch (Exception e) {
            // ParsingFailed，BackEmpty
            data.wkt = "";
            data.srid = 0;
        }
        return data;
    }

    static class GeometryData {

        String wkt = "";
        int srid = 0;
    }
}
