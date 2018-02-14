package com.app.xz.mynote.publics.core.utils.handler;

import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */

public final class ExecutorFactory {

    public static final Executor DEFAULT = new Executor() {
        @Override
        public void execute(@NonNull Runnable command) {
            command.run();
        }
    };

    public static final Executor ASYNC = Executors.newCachedThreadPool(new ThreadFactory() {

        private final AtomicInteger count = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "AsyncExecutor#" + count.getAndIncrement());
        }
    });
    public static final Executor MAIN = new HandlerExecutor(HandlerUtils.getUiHandler());

    public static final Executor BACKGROUND = new Executor() {

        @Override
        public void execute(@NonNull Runnable command) {
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                ASYNC.execute(command);
            } else {
                command.run();
            }

        }
    };

}
