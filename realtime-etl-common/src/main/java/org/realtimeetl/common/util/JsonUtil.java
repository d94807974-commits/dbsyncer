package org.realtimeetl.common.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONValidator;
import com.alibaba.fastjson2.TypeReference;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class JsonUtil {

    public static String objToJson(Object obj) {
        return JSON.toJSONString(obj);
    }

    /**
     * 将对象序列化为 JSON 字符串，对 FastJSON2 难以序列化的类型做转换，避免 JSONException。
     * ForgetBinlogData Waiting for possible Inspectionbyte[]、ByteBuffer、Original array, etc.TypeYes.Map。
     * <p>
     * Convert27. Rules:byte[]/ByteBuffer -> Base64；int/long/short/float/double/boolean/char[] -> List；
     * OtherTypeNo change;Map/List Recursive processing.
     */
    public static String objToJsonSafe(Object obj) {
        if (obj == null) {
            return "null";
        }
        Object sanitized = sanitizeForJson(obj);
        try {
            return JSON.toJSONString(sanitized);
        } catch (Exception e) {
            return "{\"error\":\"serialization failed\",\"message\":\"" + (e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "") + "\"}";
        }
    }

    private static Object sanitizeForJson(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Map) {
            Map<String, Object> out = new HashMap<>(((Map<?, ?>) o).size());
            for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
                Object k = e.getKey();
                Object v = e.getValue();
                out.put(String.valueOf(k), sanitizeForJson(v));
            }
            return out;
        }
        if (o instanceof Collection) {
            Collection<?> c = (Collection<?>) o;
            List<Object> out = new ArrayList<>(c.size());
            for (Object v : c) {
                out.add(sanitizeForJson(v));
            }
            return out;
        }
        if (o instanceof byte[]) {
            byte[] b = (byte[]) o;
            if (b.length > 512) {
                return "[bytes, length=" + b.length + "]";
            }
            return Base64.getEncoder().encodeToString(b);
        }
        if (o instanceof ByteBuffer) {
            ByteBuffer buf = ((ByteBuffer) o).duplicate();
            byte[] b = new byte[buf.remaining()];
            buf.get(b);
            return sanitizeForJson(b);
        }
        if (o instanceof int[]) {
            int[] a = (int[]) o;
            List<Integer> list = new ArrayList<>(a.length);
            for (int x : a)
                list.add(x);
            return list;
        }
        if (o instanceof long[]) {
            long[] a = (long[]) o;
            List<Long> list = new ArrayList<>(a.length);
            for (long x : a)
                list.add(x);
            return list;
        }
        if (o instanceof short[]) {
            short[] a = (short[]) o;
            List<Integer> list = new ArrayList<>(a.length);
            for (short x : a)
                list.add((int) x);
            return list;
        }
        if (o instanceof float[]) {
            float[] a = (float[]) o;
            List<Double> list = new ArrayList<>(a.length);
            for (float x : a)
                list.add((double) x);
            return list;
        }
        if (o instanceof double[]) {
            double[] a = (double[]) o;
            List<Double> list = new ArrayList<>(a.length);
            for (double x : a)
                list.add(x);
            return list;
        }
        if (o instanceof boolean[]) {
            boolean[] a = (boolean[]) o;
            List<Boolean> list = new ArrayList<>(a.length);
            for (boolean x : a)
                list.add(x);
            return list;
        }
        if (o instanceof char[]) {
            char[] a = (char[]) o;
            List<String> list = new ArrayList<>(a.length);
            for (char x : a)
                list.add(String.valueOf(x));
            return list;
        }
        if (o.getClass().isArray()) {
            Object[] a = (Object[]) o;
            List<Object> list = new ArrayList<>(a.length);
            for (Object x : a)
                list.add(sanitizeForJson(x));
            return list;
        }
        if (o instanceof Double) {
            double d = (Double) o;
            if (Double.isNaN(d) || Double.isInfinite(d))
                return String.valueOf(d);
        }
        if (o instanceof Float) {
            float f = (Float) o;
            if (Float.isNaN(f) || Float.isInfinite(f))
                return String.valueOf(f);
        }
        if (o instanceof java.util.Date) {
            return String.valueOf(((java.util.Date) o).getTime());
        }
        if (o instanceof String) {
            return sanitizeStringForJson((String) o);
        }
        if (o instanceof Number || o instanceof Boolean) {
            return o;
        }
        return String.valueOf(o);
    }

    /**
     * Cleans up illegal in a stringUTF-16（Not pairedsurrogate Wait, avoid.FastJSON WritingJSON is the wrong time.
     */
    private static String sanitizeStringForJson(String s) {
        if (s == null || s.isEmpty())
            return s;
        StringBuilder sb = null;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isSurrogate(c)) {
                if (sb == null)
                    sb = new StringBuilder(s.length()).append(s, 0, i);
                sb.append('\uFFFD');
            } else if (sb != null) {
                sb.append(c);
            }
        }
        return sb == null ? s : sb.toString();
    }

    public static <T> T jsonToObj(String json, Class<T> valueType) {
        return JSON.parseObject(json, valueType);
    }

    /**
     * Map Switch object Observations (dismantling)minColumn storage line returns scene.
     *
     * @param map       Store line or middleMap
     * @param valueType Objective CombinedType
     * @param <T>       Objective CombinedType
     * @return Inverse sequenceResult，map is emptyBack null
     */
    public static <T> T mapToObj(Map<?, ?> map, Class<T> valueType) {
        if (map == null || valueType == null) {
            return null;
        }
        return JSON.parseObject(JSON.toJSONString(map), valueType);
    }

    /**
     * Inverse sequences into objects of a general nature (e.g.{@code Map<String, Xxx>}）。
     *
     * @param json          JSON String
     * @param typeReference PanTypeReferences
     * @param <T>           Objective CombinedType
     * @return Inverse sequenceResult，json is emptyBack null
     */
    public static <T> T jsonToObj(String json, TypeReference<T> typeReference) {
        if (json == null || typeReference == null) {
            return null;
        }
        return JSON.parseObject(json, typeReference);
    }

    public static <T> List<T> jsonToArray(String json, Class<T> valueType) {
        return JSON.parseArray(json, valueType);
    }

    public static Map parseMap(Object obj) {
        return parseMap(objToJson(obj));
    }

    public static Map parseMap(String json) {
        return JSON.parseObject(json);
    }

    public static List parseList(String json) {
        return JSON.parseArray(json).toList(Map.class);
    }

    /**
     * Judgement ConfirmationJSONStringType
     */
    public static JsonType getJsonType(String jsonStr) {
        if (jsonStr == null) {
            return JsonType.NULL;
        }

        String trimmed = jsonStr.trim();

        // Quick Check
        if (trimmed.isEmpty()) {
            return JsonType.INVALID;
        }

        // InspectionYesNoYesIt works.JSON
        JSONValidator validator = JSONValidator.from(jsonStr);
        if (!validator.validate()) {
            return JsonType.INVALID;
        }

        // According to firstCharacterJudgement Confirmation
        char firstChar = trimmed.charAt(0);

        switch (firstChar) {
            case '{':
                return JsonType.OBJECT;
            case '[':
                return JsonType.ARRAY;
            case '"':
                return JsonType.STRING;
            case 't': // true
            case 'f': // false
                if ("true".equals(trimmed) || "false".equals(trimmed)) {
                    return JsonType.BOOLEAN;
                }
                break;
            case 'n': // null
                if ("null".equals(trimmed)) {
                    return JsonType.NULL;
                }
                break;
            default:
                // InspectionYesNoYesNumbers
                if (trimmed.matches("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?")) {
                    return JsonType.NUMBER;
                }
        }

        return JsonType.UNKNOWN;
    }

    public enum JsonType {
        OBJECT, // JSONObject {key: value}
        ARRAY, // JSONArray [element1, element2]
        STRING, // JSONString"text"
        NUMBER, // JSONNumbers123 or 123.45
        BOOLEAN, // JSONBooleanValue true or false
        NULL, // JSON null
        INVALID, // NoneActiveJSON
        UNKNOWN // UnknownType
    }
}
