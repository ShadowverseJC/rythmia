package com.example.rythemia.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * An account on this device. Usernames and emails are unique ignoring letter case (the NOCASE
 * columns make both the unique indexes and lookups case-insensitive). Only a salted PBKDF2
 * hash of the password is stored; see {@link com.example.rythemia.account.PasswordHasher}.
 */
@Entity(tableName = "users", indices = {
        @Index(value = "username", unique = true),
        @Index(value = "email", unique = true)
})
public class User {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    public String username = "";

    @NonNull
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    public String email = "";

    @NonNull
    public byte[] passwordHash = new byte[0];

    @NonNull
    public byte[] passwordSalt = new byte[0];

    /** PBKDF2 iterations used for this hash, so the count can be raised for new accounts. */
    public int passwordIterations;

    /** When the account was created, in epoch milliseconds. */
    public long createdAt;
}
