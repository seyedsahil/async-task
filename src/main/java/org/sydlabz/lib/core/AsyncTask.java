package org.sydlabz.lib.core;

/**
 * @author Seyed Sahil
 * @since 1.0
 */
public abstract class AsyncTask<T> {
    private final String id;

    protected AsyncTask(final String id) {
        this.id = id;
    }

    public final String getId() {
        return id;
    }

    public abstract T execute() throws Exception;
}