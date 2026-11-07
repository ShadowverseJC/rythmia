package com.example.rythemia.account;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.rythemia.data.AppDatabase;
import com.example.rythemia.data.Attempt;
import com.example.rythemia.data.Session;
import com.example.rythemia.data.User;
import com.example.rythemia.practice.Lessons;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/** Registration and login against a real (in-memory) Room database. */
@RunWith(AndroidJUnit4.class)
public class AccountsTest {

    private AppDatabase db;
    private Accounts accounts;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        accounts = new Accounts(db.users(), new PasswordHasher(1_000));
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void registeredUserCanLogInWithTheirPasswordOnly() {
        long id = created(accounts.register("quack_sabbath", "duck@rock.com", "paranoid1"));

        assertEquals(id, accounts.logIn("quack_sabbath", "paranoid1"));
        assertEquals(Session.NO_USER, accounts.logIn("quack_sabbath", "Paranoid1"));
        assertEquals(Session.NO_USER, accounts.logIn("nobody", "paranoid1"));
    }

    @Test
    public void loginUsernameIgnoresCase() {
        long id = created(accounts.register("Quack", "duck@rock.com", "paranoid1"));

        assertEquals(id, accounts.logIn("qUACK", "paranoid1"));
    }

    @Test
    public void usernamesAndEmailsAreUniqueIgnoringCase() {
        created(accounts.register("Quack", "Duck@Rock.com", "paranoid1"));

        assertEquals(Accounts.Registration.USERNAME_TAKEN,
                accounts.register("quack", "other@rock.com", "paranoid1").status);
        assertEquals(Accounts.Registration.EMAIL_TAKEN,
                accounts.register("other", "duck@rock.COM", "paranoid1").status);
    }

    @Test
    public void databaseRejectsDuplicateUsernamesEvenWithoutTheCheck() {
        created(accounts.register("Quack", "duck@rock.com", "paranoid1"));
        User clash = new User();
        clash.username = "QUACK";
        clash.email = "other@rock.com";

        assertThrows(SQLiteConstraintException.class, () -> db.users().insert(clash));
    }

    @Test
    public void onlyASaltedHashOfThePasswordIsStored() {
        long id = created(accounts.register("quack", "duck@rock.com", "paranoid1"));
        User user = db.users().findById(id);

        assertNotNull(user);
        assertEquals(16, user.passwordSalt.length);
        assertEquals(1_000, user.passwordIterations);
        assertFalse(Arrays.equals("paranoid1".getBytes(StandardCharsets.UTF_8),
                user.passwordHash));
        assertFalse(new String(user.passwordHash, StandardCharsets.ISO_8859_1)
                .contains("paranoid1"));
    }

    @Test
    public void attemptsComeBackNewestFirstPerUser() {
        long quack = created(accounts.register("quack", "duck@rock.com", "paranoid1"));
        long other = created(accounts.register("other", "other@rock.com", "paranoid1"));
        db.attempts().insert(attempt(quack, 100, 1_000));
        db.attempts().insert(attempt(quack, 140, 3_000));
        db.attempts().insert(attempt(other, 200, 4_000));
        db.attempts().insert(attempt(quack, 120, 2_000));

        List<Attempt> runs = db.attempts().forUser(quack);

        assertEquals(3, runs.size());
        assertArrayEquals(new int[]{140, 120, 100},
                new int[]{runs.get(0).bpm, runs.get(1).bpm, runs.get(2).bpm});
    }

    private static long created(Accounts.RegisterResult result) {
        assertEquals(Accounts.Registration.CREATED, result.status);
        assertNotEquals(Session.NO_USER, result.userId);
        return result.userId;
    }

    private static Attempt attempt(long userId, int bpm, long timestamp) {
        Attempt attempt = new Attempt();
        attempt.userId = userId;
        attempt.lessonId = Lessons.SIXTEENTH;
        attempt.bpm = bpm;
        attempt.hits = 12;
        attempt.misses = 4;
        attempt.timestamp = timestamp;
        return attempt;
    }
}
