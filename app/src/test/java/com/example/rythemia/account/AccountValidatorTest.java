package com.example.rythemia.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.rythemia.R;

import org.junit.Test;

public class AccountValidatorTest {

    // Patterns.EMAIL_ADDRESS is Android-only, so the JVM tests use a stand-in check
    private final AccountValidator validator =
            new AccountValidator(email -> email.matches("[^@\\s]+@[^@\\s]+\\.[a-z]+"));

    @Test
    public void registration_acceptsValidDetails() {
        AccountValidator.Errors errors =
                validator.forRegistration("quack_sabbath", "duck@rock.com", "paranoid1");

        assertFalse(errors.any());
    }

    @Test
    public void registration_rejectsEmptyFields() {
        AccountValidator.Errors errors = validator.forRegistration("", "", "");

        assertEquals(R.string.error_username_empty, errors.username);
        assertEquals(R.string.error_email_empty, errors.email);
        assertEquals(R.string.error_password_empty, errors.password);
    }

    @Test
    public void registration_limitsUsernameLengthAndCharacters() {
        assertEquals(R.string.error_username_format,
                validator.forRegistration("ab", "a@b.co", "password1").username);
        assertEquals(R.string.error_username_format,
                validator.forRegistration("a234567890123456789012", "a@b.co", "password1").username);
        assertEquals(R.string.error_username_format,
                validator.forRegistration("quack sabbath", "a@b.co", "password1").username);
        assertEquals(R.string.error_username_format,
                validator.forRegistration("quack!", "a@b.co", "password1").username);
        assertEquals(0, validator.forRegistration("Ab_9", "a@b.co", "password1").username);
    }

    @Test
    public void registration_checksEmailFormat() {
        assertEquals(R.string.error_email_format,
                validator.forRegistration("quack", "not-an-email", "password1").email);
    }

    @Test
    public void registration_requiresMinimumPasswordLength() {
        String shortPassword = "1234567";
        String longEnough = "12345678";

        assertEquals(AccountValidator.MIN_PASSWORD_LENGTH, longEnough.length());
        assertEquals(R.string.error_password_short,
                validator.forRegistration("quack", "a@b.co", shortPassword).password);
        assertEquals(0, validator.forRegistration("quack", "a@b.co", longEnough).password);
    }

    @Test
    public void registration_countsSpacesInPasswords() {
        // Passwords are not trimmed, so surrounding spaces are part of them
        assertEquals(0, validator.forRegistration("quack", "a@b.co", "  pass  ").password);
    }

    @Test
    public void login_onlyRequiresBothFields() {
        assertTrue(validator.forLogin("", "").any());
        assertEquals(R.string.error_username_empty, validator.forLogin("", "x").username);
        assertEquals(R.string.error_password_required, validator.forLogin("q", "").password);
        // No format rules on login: a wrong guess just fails to match an account
        assertFalse(validator.forLogin("q", "x").any());
    }
}
