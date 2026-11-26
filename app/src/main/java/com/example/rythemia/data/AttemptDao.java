package com.example.rythemia.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AttemptDao {

    @Insert
    long insert(Attempt attempt);

    /** Every run by the user, newest first. */
    @Query("SELECT * FROM attempts WHERE userId = :userId ORDER BY timestamp DESC, id DESC")
    List<Attempt> forUser(long userId);
}
