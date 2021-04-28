package edu.swufe.nxksecdisk.system;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 */
public interface IOutputStream {

    /**
     * println;
     *
     * @param context
     */
    void println(final String context);

    /**
     * printf;
     *
     * @param context
     * @param args
     */
    void printf(final String context, Object... args);
}
