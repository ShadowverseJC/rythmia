package com.example.rythemia.account;

import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.example.rythemia.R;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Checks sign-up and login fields before anything touches the database. Callers trim the
 * username and email first; passwords are checked exactly as typed, spaces included.
 */
public final class AccountValidator {

    public static final int MIN_PASSWORD_LENGTH = 8;

    /** 3–20 letters, digits or underscores. */
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{3,20}");

    /** A string resource per field describing its problem, or 0 when the field is fine. */
    public static final class Errors {
        @StringRes public final int username;
        @StringRes public final int email;
        @StringRes public final int password;

        Errors(@StringRes int username, @StringRes int email, @StringRes int password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }

        public boolean any() {
            return username != 0 || email != 0 || password != 0;
        }
    }

    private final Predicate<String> isEmail;

    /** Uses Android's {@link Patterns#EMAIL_ADDRESS} to check emails. */
    public AccountValidator() {
        this(email -> Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    AccountValidator(@NonNull Predicate<String> isEmail) {
        this.isEmail = isEmail;
    }

    @NonNull
    public Errors forRegistration(@NonNull String username, @NonNull String email,
                                  @NonNull String password) {
        int usernameError = username.isEmpty() ? R.string.error_username_empty
                : !USERNAME.matcher(username).matches() ? R.string.error_username_format : 0;
        int emailError = email.isEmpty() ? R.string.error_email_empty
                : !isEmail.test(email) ? R.string.error_email_format : 0;
        int passwordError = password.isEmpty() ? R.string.error_password_empty
                : password.length() < MIN_PASSWORD_LENGTH ? R.string.error_password_short : 0;
        return new Errors(usernameError, emailError, passwordError);
    }

    /** Login only needs both fields; whether they match an account is the database's call. */
    @NonNull
    public Errors forLogin(@NonNull String username, @NonNull String password) {
        return new Errors(username.isEmpty() ? R.string.error_username_empty : 0, 0,
                password.isEmpty() ? R.string.error_password_required : 0);
    }
}
