package org.sydlabz.lib.core;

/**
 * @author Seyed Sahil
 * @since 1.0
 */
public final class AsyncTaskOutput<T> {
    private T asyncTaskOutput;
    private Exception executionException;

    protected AsyncTaskOutput(final T asyncTaskOutput) {
        this.asyncTaskOutput = asyncTaskOutput;
    }

    protected AsyncTaskOutput(final Exception executionException) {
        this.executionException = executionException;
    }

    public T output() throws Exception {
        if (this.hasError()) {
            throw this.error();
        }
        if (executionException != null) {
            throw executionException;
        }
        return asyncTaskOutput;
    }

    public Exception error() {
        return executionException;
    }

    public boolean hasError() {
        return !this.isValid();
    }

    public boolean isValid() {
        return executionException == null;
    }
}
