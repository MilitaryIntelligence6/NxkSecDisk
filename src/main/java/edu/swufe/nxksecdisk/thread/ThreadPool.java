package edu.swufe.nxksecdisk.thread;

import java.util.concurrent.*;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 */
public final class ThreadPool extends ThreadPoolExecutor {

    private volatile static ThreadPool instance = null;

    private ThreadPool(int corePoolSize,
                      int maximumPoolSize,
                      long keepAliveTime,
                      TimeUnit unit,
                      BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    private ThreadPool() {
        this(PoolConfig.corePoolSize,
                PoolConfig.maximumPoolSize,
                PoolConfig.keepAliveTime,
                PoolConfig.unit,
                PoolConfig.workQueue);
    }

    public static ThreadPool getInstance() {
        if (instance == null) {
            synchronized (ThreadPool.class) {
                if (instance == null) {
                    instance = new ThreadPool();
                }
            }
        }
        return instance;
    }
}
