package com.example.rythemia.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.rythemia.practice.Tempo;

/**
 * Small settings kept in SharedPreferences: who is logged in, and each user's last tempo.
 * Only the user's id is stored here, never their password or its hash.
 */
public final class Session {

    public static final long NO_USER = -1;

    private static final String PREFS = "session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_LAST_BPM = "last_bpm_";

    private final SharedPreferences prefs;

    public Session(@NonNull Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** The logged-in user's id, or {@link #NO_USER}. */
    public long userId() {
        return prefs.getLong(KEY_USER_ID, NO_USER);
    }

    public boolean isLoggedIn() {
        return userId() != NO_USER;
    }

    public void logIn(long userId) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply();
    }

    /** Forgets who is logged in; each user's last tempo is kept for their next login. */
    public void logOut() {
        prefs.edit().remove(KEY_USER_ID).apply();
    }

    public int lastBpm(long userId) {
        return Tempo.clamp(prefs.getInt(KEY_LAST_BPM + userId, Tempo.DEFAULT));
    }

    public void setLastBpm(long userId, int bpm) {
        prefs.edit().putInt(KEY_LAST_BPM + userId, Tempo.clamp(bpm)).apply();
    }
}
