package br.com.backend.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Test utility: wraps an ExecutorService and implements AutoCloseable so it can be used in try-with-resources.
 */
public class AutoCloseableExecutor implements AutoCloseable {
    private final ExecutorService executor;

    public AutoCloseableExecutor(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public ExecutorService executor() {
        return executor;
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}

