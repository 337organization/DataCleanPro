package com.datacleanpro.network;

import com.datacleanpro.util.ConfigUtil;
import com.datacleanpro.util.LogUtil;

import java.util.concurrent.*;

/**
 * 任务管理器
 * 管理异步任务执行
 */
public class TaskManager {
    
    private static TaskManager instance;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    
    private TaskManager() {
        this.executorService = Executors.newFixedThreadPool(ConfigUtil.getThreadPoolSize());
        this.scheduledExecutorService = Executors.newScheduledThreadPool(2);
    }
    
    /**
     * 获取单例实例
     * @return TaskManager实例
     */
    public static synchronized TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }
    
    /**
     * 提交任务
     * @param task 任务
     * @return Future对象
     */
    public Future<?> submitTask(Runnable task) {
        LogUtil.info("提交任务: " + task.getClass().getSimpleName());
        return executorService.submit(task);
    }
    
    /**
     * 提交带返回值的任务
     * @param task 任务
     * @param <T> 返回值类型
     * @return Future对象
     */
    public <T> Future<T> submitTask(Callable<T> task) {
        LogUtil.info("提交任务: " + task.getClass().getSimpleName());
        return executorService.submit(task);
    }
    
    /**
     * 延迟执行任务
     * @param task 任务
     * @param delay 延迟时间
     * @param unit 时间单位
     * @return ScheduledFuture对象
     */
    public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit unit) {
        LogUtil.info("延迟执行任务: " + task.getClass().getSimpleName() + ", 延迟: " + delay + " " + unit);
        return scheduledExecutorService.schedule(task, delay, unit);
    }
    
    /**
     * 定时执行任务
     * @param task 任务
     * @param initialDelay 初始延迟
     * @param period 周期
     * @param unit 时间单位
     * @return ScheduledFuture对象
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        LogUtil.info("定时执行任务: " + task.getClass().getSimpleName() + ", 周期: " + period + " " + unit);
        return scheduledExecutorService.scheduleAtFixedRate(task, initialDelay, period, unit);
    }
    
    /**
     * 关闭任务管理器
     */
    public void shutdown() {
        LogUtil.info("关闭任务管理器");
        executorService.shutdown();
        scheduledExecutorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            scheduledExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 获取线程池大小
     * @return 线程池大小
     */
    public int getThreadPoolSize() {
        return ConfigUtil.getThreadPoolSize();
    }
    
    /**
     * 获取活跃线程数
     * @return 活跃线程数
     */
    public int getActiveThreadCount() {
        return ((ThreadPoolExecutor) executorService).getActiveCount();
    }
    
    /**
     * 获取任务队列大小
     * @return 任务队列大小
     */
    public int getQueueSize() {
        return ((ThreadPoolExecutor) executorService).getQueue().size();
    }
}
