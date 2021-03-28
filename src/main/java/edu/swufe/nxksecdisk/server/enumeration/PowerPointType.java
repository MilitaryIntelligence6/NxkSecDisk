package edu.swufe.nxksecdisk.server.enumeration;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * <h2>用于识别两种PPT格式的枚举</h2>
 * <p>该枚举用于在PPT转换PDF过程中作为参数传入来告知转换工具需转换PPT的格式类型。</p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
public enum PowerPointType {

    /**
     * ppt;
     */
    PPT(".ppt"),

    PPTX(".pptx"),
    ;

    private final String literal;

    PowerPointType(String literal) {
        this.literal = literal;
    }

    public String literal() {
        return literal;
    }

    private static final Map<String, PowerPointType> lookup = new HashMap<>(count());

    static {
        for (PowerPointType powerPointType : EnumSet.allOf(PowerPointType.class)) {
            lookup.put(powerPointType.literal, powerPointType);
        }
    }

    public static PowerPointType selectByLiteral(String literal) {
        return lookup.get(literal);
    }

    public static int count() {
        return values().length;
    }
}
