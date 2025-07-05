package com.csjbot.coshandler.taskscheduler;

import android.os.Looper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 任务调度器，用于执行后台任务
 */
public class TaskScheduler {
    /**
     * 日志TAG
     */
    public static final String TAG = "TaskScheduler";
    /**
     * 日志开关，方便调试
     */
    public static final boolean DEBUG = false;
    /**
     * copy from AsyncTask
     * We want at least 2 threads and at most 4 threads in the core pool,
     * preferring to have 1 less than the CPU count to avoid saturating the CPU with background work
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CORE_POOL_SIZE * 2 + 1;
    private static final long KEEP_ALIVE = 60L;
    /**
     * 任务队列
     */
    private static final BlockingQueue<Runnable> TASK_QUEUE = new ArrayBlockingQueue<>(256);

    /**
     * UI线程的Handler
     */
    private static final UIHandler mUIHandler = new UIHandler(Looper.getMainLooper());

    private static final ExecutorService mParallelExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS, TASK_QUEUE, TaskThreadFactory.TASK_SCHEDULER_FACTORY);

    /**
     * 执行一个无回调的任务，使用Runnable
     *
     * @param action Runnable
     */
    public static void execute(Runnable action) {
        if (mParallelExecutor != null) {
            try {
                mParallelExecutor.execute(action);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 在UI线程执行一个任务，使用Runnable
     *
     * @param action Runnable
     */
    public static void runOnUiThread(Runnable action) {
        if (mUIHandler != null) {
            mUIHandler.post(action);
        }
    }

    /**
     * 在UI线程延迟执行一个任务，使用Runnable
     *
     * @param runnable Runnable
     * @param delayed  延迟时间，单位毫秒
     */
    public static void runOnUiThread(Runnable runnable, long delayed) {
        if (mUIHandler != null) {
            mUIHandler.postDelayed(runnable, delayed);
        }
    }


    /**
     * 取消UI线程的Runnable
     *
     * @param action Runnable
     */
    public static void removeUiCallback(Runnable action) {
        if (mUIHandler != null) {
            mUIHandler.removeCallbacks(action);
        }
    }

    /**
     * 当前线程是否为主线程
     *
     * @return true-是；false-否
     */
    public static boolean isMainThread() {
        return Thread.currentThread() == mUIHandler.getLooper().getThread();
    }

}
