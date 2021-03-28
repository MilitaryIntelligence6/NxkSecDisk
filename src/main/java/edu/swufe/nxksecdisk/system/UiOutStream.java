package edu.swufe.nxksecdisk.system;

import edu.swufe.nxksecdisk.ui.module.ServerUiModule;

/**
 * @author Administrator
 */
final class UiOutStream implements IOutputStream {

    private volatile static UiOutStream instance = null;

    private static final ServerUiModule out = requireOutUi();

    private UiOutStream() {

    }

    public static UiOutStream getInstance() {
        if (instance == null) {
            synchronized (UiOutStream.class) {
                if (instance == null) {
                    instance = new UiOutStream();
                }
            }
        }
        return instance;
    }

    private static ServerUiModule requireOutUi() {
        try {
            return ServerUiModule.getInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.printf("ERROR: 错误：无法以UI模式输出信息，自动切换至命令模式输出。详细信息：%s%n",
                    e.getMessage());
        }
        throw new RuntimeException("ui init failed");
    }

    @Override
    public void println(final String context) {
        out.println(context);
    }

    @Override
    public void printf(final String context, Object... args) {
        out.printf(context, args);
    }
}
