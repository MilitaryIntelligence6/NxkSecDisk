package edu.swufe.nxksecdisk.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 */
public  final class PoolConfig {

    private static final int QUEUE_SIZE = 12;

    public static final int corePoolSize = 16;

    public static final int maximumPoolSize = 24;

    public static final long keepAliveTime = 60;

    public static final TimeUnit unit = TimeUnit.SECONDS;

    public static final BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<>(QUEUE_SIZE);

    private PoolConfig() {}
}
