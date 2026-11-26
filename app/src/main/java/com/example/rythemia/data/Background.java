package com.example.rythemia.data;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runs database and password-hashing work off the main thread, one task at a time, and hands
 * the result (or the exception it threw) back on the main thread.
 */
public final class Background {

    private static final String TAG = "Background";
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /** Receives a task's result on the main thread: {@code error} is set if it threw. */
    public interface Callback<T> {
        void onDone(@Nullable T result, @Nullable Exception error);
    }

    private Background() {
    }

    public static <T> void run(@NonNull Callable<T> task, @NonNull Callback<T> callback) {
        WORKER.execute(() -> {
            T result = null;
            Exception error = null;
            try {
                result = task.call();
            } catch (Exception e) {
                Log.e(TAG, "Background task failed", e);
                error = e;
            }
            T finalResult = result;
            Exception finalError = error;
            MAIN.post(() -> callback.onDone(finalResult, finalError));
        });
    }
}
