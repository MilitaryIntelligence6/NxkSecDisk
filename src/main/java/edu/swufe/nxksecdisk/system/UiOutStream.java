package edu.swufe.nxksecdisk.system;

import edu.swufe.nxksecdisk.server.util.ServerTimeUtil;
import edu.swufe.nxksecdisk.ui.module.ServerUiModule;

/**
 * @author Administrator
 */
final class UiOutStream implements IOutputStream
{
    private static UiOutStream instance = null;

    private static ServerUiModule serverUi;

    private UiOutStream()
    {
        try
        {
            UiOutStream.serverUi = ServerUiModule.getInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.printf("ERROR: 错误：无法以UI模式输出信息，自动切换至命令模式输出。详细信息：%s%n",
                    e.getMessage());
        }
    }

    public static UiOutStream getInstance()
    {
        if (instance == null)
        {
            synchronized (UiOutStream.class)
            {
                if (instance == null)
                {
                    instance = new UiOutStream();
                }
            }
        }
        return instance;
    }

    @Override
    public void println(final String context)
    {
        serverUi.println(context);
    }

    @Override
    public void printf(final String context, Object... args)
    {
        serverUi.printf(context, args);
    }
}
