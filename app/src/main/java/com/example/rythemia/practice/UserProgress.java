package com.example.rythemia.practice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.example.rythemia.data.AppDatabase;
import com.example.rythemia.data.User;

/** A user's account details and progress, loaded together for the screens that show both. */
public final class UserProgress {

    @NonNull public final User user;
    @NonNull public final Progress progress;

    private UserProgress(@NonNull User user, @NonNull Progress progress) {
        this.user = user;
        this.progress = progress;
    }

    /** @return null if no such user exists (for example, a stale saved login) */
    @WorkerThread
    @Nullable
    public static UserProgress load(@NonNull AppDatabase db, long userId) {
        User user = db.users().findById(userId);
        if (user == null) {
            return null;
        }
        return new UserProgress(user, new Progress(db.attempts().forUser(userId)));
    }
}
