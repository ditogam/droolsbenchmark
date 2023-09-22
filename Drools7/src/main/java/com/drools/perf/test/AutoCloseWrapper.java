package com.drools.perf.test;

import java.util.function.Consumer;

public class AutoCloseWrapper<T> implements AutoCloseable {
    private final T closable;
    private final Consumer<T> closeConsumer;

    public AutoCloseWrapper(T closable, Consumer<T> closeConsumer) {
        this.closable = closable;
        this.closeConsumer = closeConsumer;
    }

    public T getClosable() {
        return closable;
    }

    @Override
    public void close() throws Exception {
        closeConsumer.accept(closable);
    }
}
