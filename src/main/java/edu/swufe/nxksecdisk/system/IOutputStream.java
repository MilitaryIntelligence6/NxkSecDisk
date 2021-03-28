package edu.swufe.nxksecdisk.system;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 * @ClassName IOutputStream
 * @Description TODO
 * @CreateTime 2021年03月28日 12:35:00
 */
public interface IOutputStream
{
    /**
     * println;
     * @param context
     */
    void println(final String context);

    /**
     * printf;
     * @param context
     * @param args
     */
    void printf(final String context, Object... args);
}
