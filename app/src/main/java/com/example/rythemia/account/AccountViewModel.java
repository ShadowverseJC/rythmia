package com.example.rythemia.account;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rythemia.data.AppDatabase;
import com.example.rythemia.data.Background;
import com.example.rythemia.data.Session;

/**
 * Runs login and sign-up in the background for the login and register screens. Living in a
 * ViewModel, the work and its result survive the screen rotating mid-request. On success the
 * user is recorded as logged in before {@link Status#LOGGED_IN} is posted.
 */
public class AccountViewModel extends AndroidViewModel {

    /** The latest request's state; null means nothing is pending or waiting to be shown. */
    public enum Status { WORKING, LOGGED_IN, WRONG_CREDENTIALS, USERNAME_TAKEN, EMAIL_TAKEN, ERROR }

    private final MutableLiveData<Status> status = new MutableLiveData<>();
    private final Accounts accounts;
    private final Session session;

    public AccountViewModel(@NonNull Application application) {
        super(application);
        accounts = new Accounts(AppDatabase.get(application).users(), new PasswordHasher());
        session = new Session(application);
    }

    public LiveData<Status> status() {
        return status;
    }

    /** Expects a trimmed username; the password is used exactly as typed. */
    public void logIn(@NonNull String username, @NonNull String password) {
        if (status.getValue() == Status.WORKING) {
            return;
        }
        status.setValue(Status.WORKING);
        Background.run(() -> accounts.logIn(username, password), (userId, error) -> {
            if (error != null || userId == null) {
                status.setValue(Status.ERROR);
            } else if (userId == Session.NO_USER) {
                status.setValue(Status.WRONG_CREDENTIALS);
            } else {
                session.logIn(userId);
                status.setValue(Status.LOGGED_IN);
            }
        });
    }

    /** Expects a trimmed username and email; the password is used exactly as typed. */
    public void register(@NonNull String username, @NonNull String email,
                         @NonNull String password) {
        if (status.getValue() == Status.WORKING) {
            return;
        }
        status.setValue(Status.WORKING);
        Background.run(() -> accounts.register(username, email, password), (result, error) -> {
            if (error != null || result == null) {
                status.setValue(Status.ERROR);
                return;
            }
            switch (result.status) {
                case USERNAME_TAKEN:
                    status.setValue(Status.USERNAME_TAKEN);
                    break;
                case EMAIL_TAKEN:
                    status.setValue(Status.EMAIL_TAKEN);
                    break;
                default:
                    session.logIn(result.userId);
                    status.setValue(Status.LOGGED_IN);
            }
        });
    }

    /** Call once a result has been shown, so it isn't shown again after a rotation. */
    public void resultShown() {
        status.setValue(null);
    }
}
