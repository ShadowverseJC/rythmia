package com.example.rythemia.account;

import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.example.rythemia.data.Session;
import com.example.rythemia.data.User;
import com.example.rythemia.data.UserDao;

/** Creating accounts and checking logins. Call from a background thread. */
public final class Accounts {

    /** What happened when registering. */
    public enum Registration { CREATED, USERNAME_TAKEN, EMAIL_TAKEN }

    /** The outcome of {@link #register}: on success, the new user's id. */
    public static final class RegisterResult {
        @NonNull public final Registration status;
        public final long userId;

        RegisterResult(@NonNull Registration status, long userId) {
            this.status = status;
            this.userId = userId;
        }
    }

    private final UserDao users;
    private final PasswordHasher hasher;

    public Accounts(@NonNull UserDao users, @NonNull PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    /** Creates an account unless the username or email (ignoring case) is already used. */
    @WorkerThread
    @NonNull
    public RegisterResult register(@NonNull String username, @NonNull String email,
                                   @NonNull String password) {
        RegisterResult taken = checkTaken(username, email);
        if (taken != null) {
            return taken;
        }
        PasswordHasher.Hash hash = hasher.hash(password.toCharArray());
        User user = new User();
        user.username = username;
        user.email = email;
        user.passwordHash = hash.hash;
        user.passwordSalt = hash.salt;
        user.passwordIterations = hash.iterations;
        user.createdAt = System.currentTimeMillis();
        try {
            return new RegisterResult(Registration.CREATED, users.insert(user));
        } catch (SQLiteConstraintException e) {
            // The unique indexes caught a clash the check above missed
            taken = checkTaken(username, email);
            if (taken != null) {
                return taken;
            }
            throw e;
        }
    }

    /**
     * @return the user's id, or {@link Session#NO_USER} if the username doesn't exist or the
     * password is wrong (callers shouldn't say which)
     */
    @WorkerThread
    public long logIn(@NonNull String username, @NonNull String password) {
        User user = users.findByUsername(username);
        if (user == null) {
            return Session.NO_USER;
        }
        PasswordHasher.Hash stored = new PasswordHasher.Hash(
                user.passwordHash, user.passwordSalt, user.passwordIterations);
        return hasher.verify(password.toCharArray(), stored) ? user.id : Session.NO_USER;
    }

    private RegisterResult checkTaken(String username, String email) {
        if (users.isUsernameTaken(username)) {
            return new RegisterResult(Registration.USERNAME_TAKEN, Session.NO_USER);
        }
        if (users.isEmailTaken(email)) {
            return new RegisterResult(Registration.EMAIL_TAKEN, Session.NO_USER);
        }
        return null;
    }
}
