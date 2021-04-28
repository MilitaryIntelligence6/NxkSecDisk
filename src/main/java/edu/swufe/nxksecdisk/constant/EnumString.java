package edu.swufe.nxksecdisk.constant;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 */
public enum EnumString {

    /**
     * string池;
     */
    EMPTY(""),

    ;

    private final String value;

    EnumString(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
