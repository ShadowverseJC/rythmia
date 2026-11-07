package com.example.rythemia.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One finished run of a lesson. Stars, best results, rank and top speed are all worked out from
 * these rows (see {@link com.example.rythemia.practice.Progress}) rather than stored, so they
 * can never disagree with the history. Deleting a user deletes their attempts.
 */
@Entity(tableName = "attempts",
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id",
                childColumns = "userId", onDelete = ForeignKey.CASCADE),
        indices = @Index("userId"))
public class Attempt {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;

    /** A stable id from {@link com.example.rythemia.practice.Lessons}, not the display name. */
    @NonNull
    public String lessonId = "";

    public int bpm;

    public int hits;

    public int misses;

    /** When the run finished, in epoch milliseconds. */
    public long timestamp;
}
