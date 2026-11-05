package org.realtimeetl.parser.util;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.parser.enums.ConvertEnum;
import org.realtimeetl.parser.model.Convert;

import java.util.List;
import java.util.Map;

public abstract class ConvertUtil {

    /**
     * ConvertParameter
     *
     * @param convert
     * @param data
     */
    public static void convert(List<Convert> convert, List<Map> data) {
        if (!CollectionUtils.isEmpty(convert) && !CollectionUtils.isEmpty(data)) {
            data.forEach(row->convert(convert, row));
        }
    }

    /**
     * ConvertParameter
     *
     * @param convert
     * @param row
     */
    public static void convert(List<Convert> convert, Map row) {
        if (!CollectionUtils.isEmpty(convert) && !CollectionUtils.isEmpty(row)) {
            // ReplacerowValue, Reuse stacking address to reduce costs
            final int size = convert.size();
            Convert c = null;
            String name = null;
            String code = null;
            String args = null;
            Object value = null;
            for (int i = 0; i < size; i++) {
                c = convert.get(i);
                name = c.getName();
                code = c.getConvertCode();
                args = c.getArgs();
                value = ConvertEnum.getHandler(code).handle(args, row.get(name));

                row.put(name, value);
            }
        }
    }
}
