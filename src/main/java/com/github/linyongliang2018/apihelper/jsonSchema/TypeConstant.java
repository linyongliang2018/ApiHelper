package com.github.linyongliang2018.apihelper.jsonSchema;

import org.jetbrains.annotations.NonNls;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 40696
 */
public class TypeConstant {

    /**
     * 基础类型
     */
    @NonNls
    public static final Map<String, Object> BASE_TYPES = new HashMap<>();
    /**
     * 携带包名
     */
    public static final Map<String, Object> BASE_TYPES_BY_PACKAGES = new HashMap<>();
    /**
     * 集合类型
     */
    public static final Map<String, Object> COLLECT_TYPES = new HashMap<>();
    /**
     * 携带包名
     */
    public static final Map<String, Object> COLLECT_TYPES_BY_PACKAGES = new HashMap<>();
    /**
     * 支持的泛型列表
     */
    public static final List<String> GENERIC_LIST = new ArrayList<>();

    // 初始化 NORMAL_TYPES
    static {
        BASE_TYPES.put("boolean", false);
        BASE_TYPES.put("byte", 1);
        BASE_TYPES.put("short", 1);
        BASE_TYPES.put("int", 1);
        BASE_TYPES.put("long", 1L);
        BASE_TYPES.put("float", 1.0F);
        BASE_TYPES.put("double", 1.0D);
        BASE_TYPES.put("char", 'a');
        BASE_TYPES.put("Boolean", false);
        BASE_TYPES.put("Byte", 0);
        BASE_TYPES.put("Short", (short) 0);
        BASE_TYPES.put("Integer", 0);
        BASE_TYPES.put("Long", 0L);
        BASE_TYPES.put("Float", 0.0F);
        BASE_TYPES.put("Double", 0.0D);
        BASE_TYPES.put("BigDecimal", 0);
        BASE_TYPES.put("String", "String");
        BASE_TYPES.put("Date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        BASE_TYPES.put("Timestamp", new Timestamp(System.currentTimeMillis()));
    }

    // 初始化 NORMAL_TYPES_PACKAGES
    static {
        BASE_TYPES_BY_PACKAGES.put("java.lang.Boolean", false);
        BASE_TYPES_BY_PACKAGES.put("java.lang.Byte", 0);
        BASE_TYPES_BY_PACKAGES.put("java.lang.Short", (short) 0);
        BASE_TYPES_BY_PACKAGES.put("java.lang.Integer", 1);
        BASE_TYPES_BY_PACKAGES.put("java.lang.Long", 1L);
        BASE_TYPES_BY_PACKAGES.put("java.lang.Float", 1L);
        BASE_TYPES_BY_PACKAGES.put("java.lang.Double", 1.0D);
        BASE_TYPES_BY_PACKAGES.put("java.sql.Timestamp", new Timestamp(System.currentTimeMillis()));
        BASE_TYPES_BY_PACKAGES.put("java.util.Date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        BASE_TYPES_BY_PACKAGES.put("java.lang.String", "String");
        BASE_TYPES_BY_PACKAGES.put("java.math.BigDecimal", 1);
    }

    // 初始化 COLLECT_TYPES
    static {
        COLLECT_TYPES.put("Map", "Map");
        COLLECT_TYPES.put("HashMap", "HashMap");
        COLLECT_TYPES.put("LinkedHashMap", "LinkedHashMap");
    }

    // 初始化 COLLECT_TYPES_PACKAGES
    static {
        COLLECT_TYPES_BY_PACKAGES.put("java.util.Map", "Map");
        COLLECT_TYPES_BY_PACKAGES.put("java.util.HashMap", "HashMap");
        COLLECT_TYPES_BY_PACKAGES.put("java.util.LinkedHashMap", "LinkedHashMap");
    }

    static {
        GENERIC_LIST.add("T");
        GENERIC_LIST.add("E");
        GENERIC_LIST.add("A");
        GENERIC_LIST.add("B");
        GENERIC_LIST.add("K");
        GENERIC_LIST.add("V");
    }

    public static boolean isNormalType(String typeName) {
        return BASE_TYPES.containsKey(typeName);
    }
}