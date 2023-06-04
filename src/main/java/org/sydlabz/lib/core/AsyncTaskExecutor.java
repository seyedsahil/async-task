package org.sydlabz.lib.core;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Seyed Sahil
 * @since 1.0
 */
public final class AsyncTaskExecutor {
    public static AsyncTaskExecutorChain run() {
        return new AsyncTaskExecutorChain();
    }

    public static AsyncTaskExecutorChain runAll(AsyncTask<?>... asyncTasks) {
        if (asyncTasks.length == 0) {
            throw new IllegalStateException();
        }
        AsyncTaskExecutorChain asyncTaskExecutorChain = new AsyncTaskExecutorChain();
        Arrays.stream(asyncTasks).forEach(asyncTask -> asyncTaskExecutorChain.task(asyncTask));
        return asyncTaskExecutorChain;
    }

    public static class AsyncTaskExecutorChain {
        private final Map<Object, CompletableFuture<?>> asyncTaskCompletableFutureMap;
        private final Map<Object, Exception> asyncTaskExecutionErrorMap;

        private AsyncTaskExecutorChain() {
            this.asyncTaskCompletableFutureMap = new LinkedHashMap<>();
            this.asyncTaskExecutionErrorMap = new LinkedHashMap<>();
        }

        public <T> AsyncTaskExecutorChain task(final AsyncTask<T> asyncTask) {
            CompletableFuture<T> asyncTaskCompletableFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return asyncTask.execute();
                } catch (Exception exception) {
                    this.asyncTaskExecutionErrorMap.put(asyncTask.getId(), exception);
                    return null;
                }
            });
            this.asyncTaskCompletableFutureMap.put(asyncTask.getId(), asyncTaskCompletableFuture);
            return this;
        }

        public Map<Object, AsyncTaskOutput<?>> andWait() {
            if (this.asyncTaskCompletableFutureMap.size() == 0) {
                throw new IllegalStateException();
            }
            CompletableFuture.allOf(this.asyncTaskCompletableFutureMap.values().toArray(new CompletableFuture<?>[0])).join();
            return this.asyncTaskCompletableFutureMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, asyncTaskCompletableFutureEntry -> {
                        if (this.asyncTaskExecutionErrorMap.containsKey(asyncTaskCompletableFutureEntry.getKey())) {
                            return new AsyncTaskOutput<>(this.asyncTaskExecutionErrorMap.get(asyncTaskCompletableFutureEntry.getKey()));
                        } else {
                            CompletableFuture<?> asyncTaskCompletableFuture = this.asyncTaskCompletableFutureMap.get(asyncTaskCompletableFutureEntry.getKey());
                            return collectTaskOutput(asyncTaskCompletableFuture);
                        }
                    }));
        }

        private AsyncTaskOutput<?> collectTaskOutput(final CompletableFuture<?> asyncTaskCompletableFuture) {
            AsyncTaskOutput<?> asyncTaskOutput;
            try {
                asyncTaskOutput = new AsyncTaskOutput<>(asyncTaskCompletableFuture.get());
            } catch (Exception executionException) {
                asyncTaskOutput = new AsyncTaskOutput<>(executionException);
            }
            return asyncTaskOutput;
        }
    }
}
