package com.example.rythemia.practice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.rythemia.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** The lessons that exist so far. Ids are stored in the database, so never rename them. */
public final class Lessons {

    public static final String QUARTER = "beginner_quarter";
    public static final String EIGHTH = "beginner_eighth";
    public static final String SIXTEENTH = "beginner_sixteenth";

    /** The curriculum's shape: three tracks of three lessons (only Beginner has content). */
    public static final int TRACKS = 3;
    public static final int LESSONS_PER_TRACK = 3;

    public static final class Lesson {
        @NonNull public final String id;
        @StringRes public final int name;
        @StringRes public final int technique;
        @StringRes public final int summary;

        Lesson(@NonNull String id, @StringRes int name, @StringRes int technique,
               @StringRes int summary) {
            this.id = id;
            this.name = name;
            this.technique = technique;
            this.summary = summary;
        }
    }

    /** The Beginner Rhythm track, in order. */
    public static final List<Lesson> BEGINNER = Collections.unmodifiableList(Arrays.asList(
            new Lesson(QUARTER, R.string.lesson_quarter, R.string.technique_downstrokes,
                    R.string.lesson_quarter_summary),
            new Lesson(EIGHTH, R.string.lesson_eighth, R.string.technique_alternate_picking,
                    R.string.lesson_eighth_summary),
            new Lesson(SIXTEENTH, R.string.lesson_sixteenth, R.string.technique_alternate_picking,
                    R.string.lesson_sixteenth_summary)));

    private Lessons() {
    }

    @Nullable
    public static Lesson find(@NonNull String id) {
        for (Lesson lesson : BEGINNER) {
            if (lesson.id.equals(id)) {
                return lesson;
            }
        }
        return null;
    }
}
