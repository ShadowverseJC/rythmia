package com.example.rythemia.data;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

    /**
     * @return the new user's id
     * @throws android.database.sqlite.SQLiteConstraintException if the username or email is
     *                                                           already taken
     */
    @Insert
    long insert(User user);

    @Nullable
    @Query("SELECT * FROM users WHERE id = :id")
    User findById(long id);

    /** Matches ignoring letter case. */
    @Nullable
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    /** Matches ignoring letter case. */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    boolean isUsernameTaken(String username);

    /** Matches ignoring letter case. */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    boolean isEmailTaken(String email);
}
