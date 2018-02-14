package com.app.xz.mynote.publics.core.utils.handler;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 *
 */

public class HandlerExecutor implements Executor {

    private final Handler handler;

    public HandlerExecutor(@NonNull Handler handler) {
        this.handler = handler;
    }

    @Override
    public void execute(@NonNull Runnable command) {
        if (handler.getLooper().getThread() == Thread.currentThread()) {
            command.run();
        } else {
            handler.post(command);
        }
    }
}
