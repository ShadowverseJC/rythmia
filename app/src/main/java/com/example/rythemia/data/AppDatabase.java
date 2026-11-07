package com.example.rythemia.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * The app's on-device database. Room refuses queries on the main thread, so all calls go
 * through {@link Background}. A schema change needs a version bump and a migration; the
 * exported schemas in app/schemas show what each version looked like.
 */
@Database(entities = {User.class, Attempt.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static final String NAME = "rhythmia.db";
    private static volatile AppDatabase instance;

    public abstract UserDao users();

    public abstract AttemptDao attempts();

    public static AppDatabase get(@NonNull Context context) {
        AppDatabase db = instance;
        if (db == null) {
            synchronized (AppDatabase.class) {
                db = instance;
                if (db == null) {
                    db = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, NAME).build();
                    instance = db;
                }
            }
        }
        return db;
    }
}
