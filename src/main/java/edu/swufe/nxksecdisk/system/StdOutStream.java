package edu.swufe.nxksecdisk.system;

import edu.swufe.nxksecdisk.server.util.ServerTimeUtil;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 * @ClassName StdOutStream
 * @Description TODO
 * @CreateTime 2021年03月28日 13:05:00
 */
final class StdOutStream implements IOutputStream
{
    private static StdOutStream instance = null;

    private StdOutStream() {}

    public static StdOutStream getInstance()
    {
        if (instance == null)
        {
            synchronized (StdOutStream.class)
            {
                if (instance == null)
                {
                    instance = new StdOutStream();
                }
            }
        }
        return instance;
    }

    @Override
    public void println(String context)
    {
        System.out.printf("[%s]%s\r\n%n",
                ServerTimeUtil.accurateToSecond(),
                context);
    }

    @Override
    public void printf(String context, Object... args)
    {
        System.out.printf("[%s]%s\r\n%n",
                ServerTimeUtil.accurateToSecond(),
                context);
    }
}
