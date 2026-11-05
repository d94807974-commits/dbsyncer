package org.realtimeetl.common.util;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public abstract class BatchTaskUtil {

    /**
     * DefaultThreadsPool Size
     */
    private static final int DEFAULT_THREAD_POOL_SIZE = 10;

    /**
     * DefaultTasksTimeoutTime（seconds）
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 300;

    /**
     * DefaultThreadsQueue Size
     */
    private static final int DEFAULT_QUEUE_SIZE = 1024;

    /**
     * CreateThreadsJi.
     */
    public static ExecutorService createExecutor(int threadPoolSize, int maximumPoolSize, int queueSize) {
        return new ThreadPoolExecutor(threadPoolSize, // CoreThreads
                maximumPoolSize, // MaxThreads
                60L, // ThreadsFreeTime
                TimeUnit.SECONDS, // Expiration timeunits
                new ArrayBlockingQueue<>(queueSize), // There's a line.
                Executors.defaultThreadFactory(), // ThreadsPlant
                new ThreadPoolExecutor.CallerRunsPolicy()); // Reject Policy, When queue full, callThreadsImplementationTasks
    }

    /**
     * Parallel processingColumnsTable data
     *
     * @param dataList       Data FactList
     * @param processor      Data processingFunctions
     * @param threadPoolSize ThreadsPool Size
     * @param <T>            Data FactType
     * @param <R>            ResultType
     * @return ProcessingResultList
     */
    public static <T, R> List<R> submit(Collection<T> dataList, Processor<T, R> processor, int threadPoolSize, Logger logger) {
        if (CollectionUtils.isEmpty(dataList)) {
            return Collections.emptyList();
        }

        ExecutorService executor = createExecutor(threadPoolSize, threadPoolSize, DEFAULT_QUEUE_SIZE);
        List<Future<R>> futures = new ArrayList<>();
        List<R> results = new ArrayList<>();

        try {
            // SubmitAllTasks
            for (T data : dataList) {
                futures.add(executor.submit(() -> {
                    try {
                        return processor.process(data);
                    } catch (Throwable e) {
                        logger.error("Task execution error", e);
                        return null;
                    }
                }));
            }

            // OwnershipTasksFinish
            for (Future<R> future : futures) {
                try {
                    R result = future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    if (result != null) {
                        results.add(result);
                    }
                } catch (TimeoutException e) {
                    logger.error("Task execution timeout", e);
                } catch (Exception e) {
                    logger.error("Task execution error", e);
                }
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return results;
    }

    /**
     * Parallel processingColumnsTable dataNoneBackValue）
     *
     * @param dataList Data FactList
     * @param consumer Data-processing consumers
     * @param logger   LogRecorder
     * @param <T>      Data FactType
     */
    public static <T> void execute(Collection<T> dataList, Consumer<T> consumer, Logger logger) {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }

        ExecutorService executor = createExecutor(DEFAULT_THREAD_POOL_SIZE, DEFAULT_THREAD_POOL_SIZE, DEFAULT_QUEUE_SIZE);
        List<Future<?>> futures = new ArrayList<>();

        try {
            // SubmitAllTasks
            for (T data : dataList) {
                futures.add(executor.submit(() -> {
                    try {
                        consumer.accept(data);
                    } catch (Throwable e) {
                        logger.error("Task execution error", e);
                    }
                }));
            }

            // OwnershipTasksFinish
            for (Future<?> future : futures) {
                try {
                    future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    logger.error("Task execution timeout", e);
                } catch (Exception e) {
                    logger.error("Task execution error", e);
                }
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Parallel processingColumnsTable dataNoneBackValue）
     *
     * @param executor CustomThreadsJi.
     * @param dataList Data FactList
     * @param consumer Data-processing consumers
     * @param logger   LogRecorder
     * @param <T>      Data FactType
     */
    public static <T> void execute(ExecutorService executor, Collection<T> dataList, Consumer<T> consumer, Logger logger) {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }

        List<Future<?>> futures = new ArrayList<>();
        // SubmitAllTasks
        for (T data : dataList) {
            futures.add(executor.submit(() -> {
                try {
                    consumer.accept(data);
                } catch (Throwable e) {
                    logger.error("Task execution error", e);
                }
            }));
        }

        try {
            // OwnershipTasksFinish
            for (Future<?> future : futures) {
                try {
                    future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    logger.error("Task execution timeout", e);
                }
            }
        } catch (InterruptedException e) {
            // ThreadsInterrupted, elegant.
            Thread.currentThread().interrupt();
            // CancelNoneFinishYes.Tasks
            for (Future<?> f : futures) {
                if (!f.isDone()) {
                    f.cancel(true);
                }
            }
        } catch (Exception e) {
            logger.error("Task execution error", e);
        }
    }

    public static <T> void executeBySlice(List<T> rows, int batchSize, int threadNum, Function<T> function, Logger logger) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);
        try {
            TaskSplitUtil.split(rows, batchSize, slice -> {
                try {
                    function.execute(slice, executor);
                } catch (Throwable e) {
                    logger.error("Task execution error", e);
                }
            });
        } finally {
            executor.shutdownNow();
        }
    }

    public static <T> void executeWithAwait(List<T> rows, ExecutorService executor, Consumer<T> consumer, Logger logger) {
        List<Future<?>> futures = new ArrayList<>();
        // SubmitAllTasks
        for (T data : rows) {
            futures.add(executor.submit(() -> {
                try {
                    consumer.accept(data);
                } catch (Throwable e) {
                    logger.error("Task execution error", e);
                }
            }));
        }
        try {
            // OwnershipTasksFinish
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            // ThreadsInterrupted, elegant.
            Thread.currentThread().interrupt();
            // CancelNoneFinishYes.Tasks
            for (Future<?> f : futures) {
                if (!f.isDone()) {
                    f.cancel(true);
                }
            }
        }
    }

    public static <T> void executeBatchWithoutAwait(List<T> rows, ExecutorService executor, Consumer<List<T>> consumer, Logger logger) {
        List<Future<?>> futures = new ArrayList<>();
        // SubmitAllTasks
        futures.add(executor.submit(() -> {
            try {
                consumer.accept(rows);
            } catch (Throwable e) {
                logger.error("Task execution error", e);
            }
        }));
        try {
            // OwnershipTasksFinish
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            // ThreadsInterrupted, elegant.
            Thread.currentThread().interrupt();
            // CancelNoneFinishYes.Tasks
            for (Future<?> f : futures) {
                if (!f.isDone()) {
                    f.cancel(true);
                }
            }
        }
    }

    /**
     * Data FactProcessingInterface
     *
     * @param <T> InputType
     * @param <R> OutputType
     */
    @FunctionalInterface
    public interface Processor<T, R> {

        R process(T data) throws Exception;
    }

    @FunctionalInterface
    public interface Function<T> {

        void execute(List<T> slice, ExecutorService executor);
    }

}