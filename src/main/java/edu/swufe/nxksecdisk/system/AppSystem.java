package edu.swufe.nxksecdisk.system;

import edu.swufe.nxksecdisk.config.DynamicConfig;
import edu.swufe.nxksecdisk.server.util.LogUtil;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 * @ClassName Logger
 * @Description TODO
 * @CreateTime 2021年03月27日 22:22:00
 */
public final class AppSystem
{
    public static IOutputStream out = null;

    public static final LogUtil log = null;

    static
    {
        initOutStream();
    }

    private static void initOutStream()
    {
        switch (DynamicConfig.getLauncherMode())
        {
            case CONSOLE:
            {
                out = StdOutStream.getInstance();
                break;
            }
            case UI:
            {
                out = UiOutStream.getInstance();
                break;
            }
            default:
            {
                break;
            }
        }
    }

    public void exit(int status)
    {
        System.exit(status);
    }
}
