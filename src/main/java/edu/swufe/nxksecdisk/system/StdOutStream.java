package edu.swufe.nxksecdisk.system;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 */
final class StdOutStream implements IOutputStream {

    private volatile static StdOutStream instance = null;

    private StdOutStream() {
    }

    public static StdOutStream getInstance() {
        if (instance == null) {
            synchronized (StdOutStream.class) {
                if (instance == null) {
                    instance = new StdOutStream();
                }
            }
        }
        return instance;
    }

    @Override
    public void println(String context) {
        System.out.println(Decorator.decorateDate(context));
    }

    @Override
    public void printf(String context, Object... args) {
        System.out.printf(Decorator.decorateDate(context),
                args);
    }
}
