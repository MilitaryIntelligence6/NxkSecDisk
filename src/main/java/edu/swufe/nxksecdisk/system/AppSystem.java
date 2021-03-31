package edu.swufe.nxksecdisk.system;

import edu.swufe.nxksecdisk.config.DynamicConfig;
import edu.swufe.nxksecdisk.server.util.LogUtil;
import edu.swufe.nxksecdisk.thread.ThreadPool;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 * @ClassName Logger
 * @Description TODO
 * @CreateTime 2021年03月27日 22:22:00
 */
public final class AppSystem {

    public static final IOutputStream out = requireOutStream();

    public static final ThreadPool pool = ThreadPool.getInstance();

    public static final LogUtil log = null;

    private AppSystem() {}

    private static IOutputStream requireOutStream() {
        switch (DynamicConfig.getLauncherMode()) {
            case CONSOLE: {
                return StdOutStream.getInstance();
            }
            case UI: {
                return UiOutStream.getInstance();
            }
            default: {
                throw new RuntimeException("unknown launch mode");
            }
        }
    }

    public void exit(int status) {
        System.exit(status);
    }
}
