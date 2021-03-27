package edu.swufe.nxksecdisk.printer;

import edu.swufe.nxksecdisk.server.util.ServerTimeUtil;
import edu.swufe.nxksecdisk.ui.module.ServerUiModule;

/**
 * @author Administrator
 */
public final class Out
{
    private static boolean uiModel;

    private static ServerUiModule uiModule;

    private Out() {}

    public static void putModel(final boolean uiModel)
    {
        if (uiModel)
        {
            try
            {
                Out.uiModule = ServerUiModule.getInstance();
                Out.uiModel = uiModel;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.printf("错误：无法以UI模式输出信息，自动切换至命令模式输出。详细信息：%s%n",
                        e.getMessage());
            }
        }
    }

    public static void println(final String context)
    {
        if (uiModel)
        {
            uiModule.println(context);
        }
        else
        {
            System.out.printf("[%s]%s\r\n%n",
                    ServerTimeUtil.accurateToSecond(),
                    context);
        }
    }

//    public static void printf(final String context, Object... args)
//    {
//        if (uiModel)
//        {
//            uiModule.println(context);
//        }
//        else
//        {
//            System.out.printf("[%s]%s\r\n%n",
//                    ServerTimeUtil.accurateToSecond(),
//                    context);
//        }
//    }
}
