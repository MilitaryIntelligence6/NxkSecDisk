package edu.swufe.nxksecdisk.thread;

import java.util.concurrent.*;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 * @ClassName ThreadPool
 * @Description TODO
 * @CreateTime 2021年03月28日 20:02:00
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
        this(ThreadPoolConfig.corePoolSize,
                ThreadPoolConfig.maximumPoolSize,
                ThreadPoolConfig.keepAliveTime,
                ThreadPoolConfig.unit,
                ThreadPoolConfig.workQueue);
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
