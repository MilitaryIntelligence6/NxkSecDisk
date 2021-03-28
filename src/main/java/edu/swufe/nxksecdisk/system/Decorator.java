package edu.swufe.nxksecdisk.system;

import edu.swufe.nxksecdisk.server.util.ServerTimeUtil;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 * @ClassName Decorator
 * @Description TODO
 * @CreateTime 2021年03月27日 18:35:00
 */
final class Decorator
{
    private static final StringBuffer buffer = new StringBuffer();

    private Decorator() {}

    private static void clear()
    {
        // builder 的话要加锁;
//        lock.lock();
//        try
//        {
//            clearBuilder();
//            builderAppend();
//            System.out.printf("builder = %s%n", builder);
//        }
//        finally
//        {
//            lock.unlock();
//        }
        buffer.delete(0, buffer.length());
    }

    public static String decorateDate(String context)
    {
        clear();
        return buffer
                .append("[")
                .append(ServerTimeUtil.accurateToSecond())
                .append("]")
                .append(context)
                .append("\n")
                .toString();
    }
}
