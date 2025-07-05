package com.csjbot.coshandler.taskscheduler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂
 */
public class TaskThreadFactory {
    static final ThreadFactory TASK_SCHEDULER_FACTORY = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "TaskScheduler Thread #" + mCount.getAndIncrement());
        }
    };

}
